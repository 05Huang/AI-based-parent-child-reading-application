package com.qz.sns.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qz.sns.model.entity.Group;
import com.qz.sns.model.entity.GroupMember;
import com.qz.sns.model.entity.GroupMessage;
import com.qz.sns.model.entity.User;
import com.qz.sns.sv.result.Result;
import com.qz.sns.sv.result.ResultUtils;
import com.qz.sns.sv.service.IGroupMemberService;
import com.qz.sns.sv.service.IGroupMessageService;
import com.qz.sns.sv.service.IGroupService;
import com.qz.sns.sv.service.IUserService;
import com.qz.sns.sv.session.SessionContext;
import com.qz.sns.sv.session.UserSession;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 群消息 前端控制器
 * </p>
 *
 * @author 李彬
 * @since 2025-02-18
 */
@RestController
@RequestMapping("/api/groupMessage")
@Api(tags = "群消息管理")
public class GroupMessageController {

    @Autowired
    private IGroupMessageService groupMessageService;
    
    @Autowired
    private IGroupService groupService;
    
    @Autowired
    private IGroupMemberService groupMemberService;
    
    @Autowired
    private IUserService userService;

    @ApiOperation("获取用户的群聊列表")
    @GetMapping("/groups")
    public Result<List<Map<String, Object>>> getUserGroups() {
        System.out.println("开始获取用户群聊列表...");
        
        try {
            UserSession userSession = SessionContext.getSession();
            if (userSession == null) {
                System.out.println("用户未登录");
                return ResultUtils.error(401, "用户未登录");
            }
            
            Long userId = userSession.getUserId();
            System.out.println("当前用户ID: " + userId);
            
            // 查询用户加入的所有群组
            LambdaQueryWrapper<GroupMember> memberWrapper = new LambdaQueryWrapper<>();
            memberWrapper.eq(GroupMember::getUserId, userId)
                        .eq(GroupMember::getQuit, false);
            List<GroupMember> memberList = groupMemberService.list(memberWrapper);
            System.out.println("用户加入的群组数量: " + memberList.size());
            
            // 获取群组详细信息
            List<Map<String, Object>> groupList = memberList.stream().map(member -> {
                Group group = groupService.getById(member.getGroupId());
                if (group == null) {
                    return null;
                }
                
                // 获取群组最后一条消息
                LambdaQueryWrapper<GroupMessage> messageWrapper = new LambdaQueryWrapper<>();
                messageWrapper.eq(GroupMessage::getGroupId, group.getId())
                            .orderByDesc(GroupMessage::getSendTime)
                            .last("LIMIT 1");
                GroupMessage lastMessage = groupMessageService.getOne(messageWrapper);
                
                // 获取群成员数量
                LambdaQueryWrapper<GroupMember> countWrapper = new LambdaQueryWrapper<>();
                countWrapper.eq(GroupMember::getGroupId, group.getId())
                           .eq(GroupMember::getQuit, false);
                long memberCount = groupMemberService.count(countWrapper);
                
                Map<String, Object> groupInfo = new HashMap<>();
                groupInfo.put("id", group.getId());
                groupInfo.put("name", group.getName());
                groupInfo.put("avatar", group.getHeadImage());
                groupInfo.put("memberCount", memberCount);
                groupInfo.put("lastMessage", lastMessage != null ? lastMessage.getContent() : "");
                groupInfo.put("lastTime", lastMessage != null ? lastMessage.getSendTime() : group.getCreatedTime());
                groupInfo.put("unread", 0); // TODO: 实现未读消息统计
                
                return groupInfo;
            }).filter(group -> group != null).collect(Collectors.toList());
            
            System.out.println("返回群组列表，数量: " + groupList.size());
            return ResultUtils.success(groupList);
            
        } catch (Exception e) {
            System.err.println("获取用户群聊列表异常: " + e.getMessage());
            e.printStackTrace();
            return ResultUtils.error(500, "获取群聊列表失败");
        }
    }

    @ApiOperation("获取群消息列表")
    @GetMapping("/{groupId}")
    public Result<Map<String, Object>> getGroupMessages(
            @ApiParam("群组ID") @PathVariable Long groupId,
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam("页大小") @RequestParam(defaultValue = "20") Integer size) {
        
        System.out.println("开始获取群消息列表，群组ID: " + groupId + ", 页码: " + page + ", 页大小: " + size);
        
        try {
            UserSession userSession = SessionContext.getSession();
            if (userSession == null) {
                System.out.println("用户未登录");
                return ResultUtils.error(401, "用户未登录");
            }
            
            // 验证用户是否在该群组中
            LambdaQueryWrapper<GroupMember> memberWrapper = new LambdaQueryWrapper<>();
            memberWrapper.eq(GroupMember::getGroupId, groupId)
                        .eq(GroupMember::getUserId, userSession.getUserId())
                        .eq(GroupMember::getQuit, false);
            GroupMember member = groupMemberService.getOne(memberWrapper);
            if (member == null) {
                System.out.println("用户不在该群组中");
                return ResultUtils.error(403, "您不在该群组中");
            }
            
            // 获取群组信息
            Group group = groupService.getById(groupId);
            if (group == null) {
                System.out.println("群组不存在");
                return ResultUtils.error(404, "群组不存在");
            }
            
            // 获取群成员列表
            LambdaQueryWrapper<GroupMember> allMemberWrapper = new LambdaQueryWrapper<>();
            allMemberWrapper.eq(GroupMember::getGroupId, groupId)
                           .eq(GroupMember::getQuit, false);
            List<GroupMember> members = groupMemberService.list(allMemberWrapper);
            
            // 分页查询消息
            Page<GroupMessage> messagePage = new Page<>(page, size);
            LambdaQueryWrapper<GroupMessage> messageWrapper = new LambdaQueryWrapper<>();
            messageWrapper.eq(GroupMessage::getGroupId, groupId)
                         .orderByDesc(GroupMessage::getSendTime);
            IPage<GroupMessage> messagePageResult = groupMessageService.page(messagePage, messageWrapper);
            
            // 构造返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("groupInfo", Map.of(
                "id", group.getId(),
                "name", group.getName(),
                "avatar", group.getHeadImage(),
                "memberCount", members.size()
            ));
            result.put("members", members.stream().map(m -> Map.of(
                "id", m.getUserId(),
                "name", m.getUserNickName(),
                "avatar", m.getHeadImage(),
                "role", "成员" // TODO: 可以根据需要添加角色判断
            )).collect(Collectors.toList()));
            result.put("messages", messagePageResult.getRecords().stream().map(msg -> {
                Map<String, Object> msgInfo = new HashMap<>();
                msgInfo.put("id", msg.getId());
                msgInfo.put("content", msg.getContent());
                msgInfo.put("type", msg.getType());
                msgInfo.put("sendTime", msg.getSendTime());
                msgInfo.put("senderId", msg.getSendId());
                msgInfo.put("senderName", msg.getSendNickName());
                msgInfo.put("isSelf", msg.getSendId().equals(userSession.getUserId()));
                
                // 获取发送者头像
                User sender = userService.getById(msg.getSendId());
                msgInfo.put("avatar", sender != null ? sender.getAvatar() : "");
                
                return msgInfo;
            }).collect(Collectors.toList()));
            result.put("total", messagePageResult.getTotal());
            result.put("hasMore", messagePageResult.getCurrent() < messagePageResult.getPages());
            
            System.out.println("返回群消息列表，消息数量: " + messagePageResult.getRecords().size());
            return ResultUtils.success(result);
            
        } catch (Exception e) {
            System.err.println("获取群消息列表异常: " + e.getMessage());
            e.printStackTrace();
            return ResultUtils.error(500, "获取消息列表失败");
        }
    }

    @ApiOperation("发送群消息")
    @PostMapping("/send")
    public Result<Map<String, Object>> sendGroupMessage(@RequestBody Map<String, Object> params) {
        System.out.println("开始发送群消息，参数: " + params);
        
        try {
            UserSession userSession = SessionContext.getSession();
            if (userSession == null) {
                System.out.println("用户未登录");
                return ResultUtils.error(401, "用户未登录");
            }
            
            Long groupId = Long.valueOf(params.get("groupId").toString());
            String content = params.get("content").toString();
            Integer type = Integer.valueOf(params.getOrDefault("type", 0).toString());
            
            // 验证用户是否在该群组中
            LambdaQueryWrapper<GroupMember> memberWrapper = new LambdaQueryWrapper<>();
            memberWrapper.eq(GroupMember::getGroupId, groupId)
                        .eq(GroupMember::getUserId, userSession.getUserId())
                        .eq(GroupMember::getQuit, false);
            GroupMember member = groupMemberService.getOne(memberWrapper);
            if (member == null) {
                System.out.println("用户不在该群组中");
                return ResultUtils.error(403, "您不在该群组中");
            }
            
            // 获取用户信息
            User user = userService.getById(userSession.getUserId());
            if (user == null) {
                System.out.println("用户信息不存在");
                return ResultUtils.error(404, "用户信息不存在");
            }
            
            // 创建群消息
            GroupMessage groupMessage = new GroupMessage();
            groupMessage.setGroupId(groupId);
            groupMessage.setSendId(userSession.getUserId());
            groupMessage.setSendNickName(user.getNickname());
            groupMessage.setContent(content);
            groupMessage.setType(type == 0 ? false : true); // 0:文字, 其他:非文字
            groupMessage.setStatus(false); // 0:正常
            groupMessage.setSendTime(LocalDateTime.now());
            
            boolean saved = groupMessageService.save(groupMessage);
            if (!saved) {
                System.out.println("保存群消息失败");
                return ResultUtils.error(500, "发送消息失败");
            }
            
            // 构造返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("id", groupMessage.getId());
            result.put("content", groupMessage.getContent());
            result.put("type", groupMessage.getType());
            result.put("sendTime", groupMessage.getSendTime());
            result.put("senderId", groupMessage.getSendId());
            result.put("senderName", groupMessage.getSendNickName());
            result.put("avatar", user.getAvatar());
            result.put("isSelf", true);
            
            System.out.println("群消息发送成功，消息ID: " + groupMessage.getId());
            return ResultUtils.success(result);
            
        } catch (Exception e) {
            System.err.println("发送群消息异常: " + e.getMessage());
            e.printStackTrace();
            return ResultUtils.error(500, "发送消息失败");
        }
    }
}
