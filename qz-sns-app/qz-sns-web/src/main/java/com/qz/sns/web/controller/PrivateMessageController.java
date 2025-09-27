package com.qz.sns.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qz.sns.model.entity.FamilyRelation;
import com.qz.sns.model.entity.Friend;
import com.qz.sns.model.entity.PrivateMessage;
import com.qz.sns.model.entity.User;
import com.qz.sns.sv.result.Result;
import com.qz.sns.sv.result.ResultUtils;
import com.qz.sns.sv.service.IFamilyRelationService;
import com.qz.sns.sv.service.IFriendService;
import com.qz.sns.sv.service.IPrivateMessageService;
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
 * 私聊消息 前端控制器
 * </p>
 *
 * @author 李彬
 * @since 2025-02-18
 */
@RestController
@RequestMapping("/api/privateMessage")
@Api(tags = "私聊消息管理")
public class PrivateMessageController {

    @Autowired
    private IPrivateMessageService privateMessageService;
    
    @Autowired
    private IFriendService friendService;
    
    @Autowired
    private IFamilyRelationService familyRelationService;
    
    @Autowired
    private IUserService userService;

    @ApiOperation("获取私聊联系人列表")
    @GetMapping("/contacts")
    public Result<List<Map<String, Object>>> getContacts() {
        System.out.println("开始获取私聊联系人列表...");
        
        try {
            UserSession userSession = SessionContext.getSession();
            if (userSession == null) {
                System.out.println("用户未登录");
                return ResultUtils.error(401, "用户未登录");
            }
            
            Long userId = userSession.getUserId();
            System.out.println("当前用户ID: " + userId);
            
            // 获取家庭成员（通过家庭关系表）
            LambdaQueryWrapper<FamilyRelation> relationWrapper = new LambdaQueryWrapper<>();
            relationWrapper.eq(FamilyRelation::getUserId, userId);
            List<FamilyRelation> relations = familyRelationService.list(relationWrapper);
            System.out.println("家庭成员数量: " + relations.size());
            
            List<Map<String, Object>> contactList = relations.stream().map(relation -> {
                User relativeUser = userService.getById(relation.getRelativeId());
                if (relativeUser == null) {
                    return null;
                }
                
                // 获取最后一条私聊消息
                LambdaQueryWrapper<PrivateMessage> messageWrapper = new LambdaQueryWrapper<>();
                messageWrapper.and(wrapper -> 
                    wrapper.and(w -> w.eq(PrivateMessage::getSendId, userId)
                                   .eq(PrivateMessage::getRecvId, relation.getRelativeId()))
                           .or(w -> w.eq(PrivateMessage::getSendId, relation.getRelativeId())
                                   .eq(PrivateMessage::getRecvId, userId))
                ).orderByDesc(PrivateMessage::getSendTime)
                 .last("LIMIT 1");
                PrivateMessage lastMessage = privateMessageService.getOne(messageWrapper);
                
                // 统计未读消息数量
                LambdaQueryWrapper<PrivateMessage> unreadWrapper = new LambdaQueryWrapper<>();
                unreadWrapper.eq(PrivateMessage::getSendId, relation.getRelativeId())
                           .eq(PrivateMessage::getRecvId, userId)
                           .eq(PrivateMessage::getStatus, false); // 0:未读
                long unreadCount = privateMessageService.count(unreadWrapper);
                
                Map<String, Object> contactInfo = new HashMap<>();
                contactInfo.put("id", relativeUser.getId());
                contactInfo.put("name", relativeUser.getNickname());
                contactInfo.put("avatar", relativeUser.getAvatar());
                contactInfo.put("lastMessage", lastMessage != null ? lastMessage.getContent() : "");
                contactInfo.put("lastTime", lastMessage != null ? lastMessage.getSendTime() : relativeUser.getCreatedTime());
                contactInfo.put("unread", unreadCount);
                contactInfo.put("online", true); // TODO: 实现在线状态
                contactInfo.put("relationType", relation.getRelationType());
                
                return contactInfo;
            }).filter(contact -> contact != null).collect(Collectors.toList());
            
            System.out.println("返回联系人列表，数量: " + contactList.size());
            return ResultUtils.success(contactList);
            
        } catch (Exception e) {
            System.err.println("获取私聊联系人列表异常: " + e.getMessage());
            e.printStackTrace();
            return ResultUtils.error(500, "获取联系人列表失败");
        }
    }

    @ApiOperation("获取私聊消息列表")
    @GetMapping("/{contactId}")
    public Result<Map<String, Object>> getPrivateMessages(
            @ApiParam("联系人ID") @PathVariable Long contactId,
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam("页大小") @RequestParam(defaultValue = "20") Integer size) {
        
        System.out.println("开始获取私聊消息列表，联系人ID: " + contactId + ", 页码: " + page + ", 页大小: " + size);
        
        try {
            UserSession userSession = SessionContext.getSession();
            if (userSession == null) {
                System.out.println("用户未登录");
                return ResultUtils.error(401, "用户未登录");
            }
            
            Long userId = userSession.getUserId();
            
            // 获取联系人信息
            User contact = userService.getById(contactId);
            if (contact == null) {
                System.out.println("联系人不存在");
                return ResultUtils.error(404, "联系人不存在");
            }
            
            // 验证是否为家庭成员
            LambdaQueryWrapper<FamilyRelation> relationWrapper = new LambdaQueryWrapper<>();
            relationWrapper.eq(FamilyRelation::getUserId, userId)
                          .eq(FamilyRelation::getRelativeId, contactId);
            FamilyRelation relation = familyRelationService.getOne(relationWrapper);
            if (relation == null) {
                System.out.println("不是家庭成员");
                return ResultUtils.error(403, "您与该用户不是家庭成员");
            }
            
            // 分页查询消息
            Page<PrivateMessage> messagePage = new Page<>(page, size);
            LambdaQueryWrapper<PrivateMessage> messageWrapper = new LambdaQueryWrapper<>();
            messageWrapper.and(wrapper -> 
                wrapper.and(w -> w.eq(PrivateMessage::getSendId, userId)
                               .eq(PrivateMessage::getRecvId, contactId))
                       .or(w -> w.eq(PrivateMessage::getSendId, contactId)
                               .eq(PrivateMessage::getRecvId, userId))
            ).orderByDesc(PrivateMessage::getSendTime);
            IPage<PrivateMessage> messagePageResult = privateMessageService.page(messagePage, messageWrapper);
            
            // 标记消息为已读
            LambdaQueryWrapper<PrivateMessage> updateWrapper = new LambdaQueryWrapper<>();
            updateWrapper.eq(PrivateMessage::getSendId, contactId)
                        .eq(PrivateMessage::getRecvId, userId)
                        .eq(PrivateMessage::getStatus, false);
            List<PrivateMessage> unreadMessages = privateMessageService.list(updateWrapper);
            unreadMessages.forEach(msg -> {
                msg.setStatus(true); // 设置为已读
                privateMessageService.updateById(msg);
            });
            
            // 构造返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("contactInfo", Map.of(
                "id", contact.getId(),
                "name", contact.getNickname(),
                "avatar", contact.getAvatar(),
                "online", true // TODO: 实现在线状态
            ));
            result.put("messages", messagePageResult.getRecords().stream().map(msg -> {
                Map<String, Object> msgInfo = new HashMap<>();
                msgInfo.put("id", msg.getId());
                msgInfo.put("content", msg.getContent());
                msgInfo.put("type", msg.getType());
                msgInfo.put("sendTime", msg.getSendTime());
                msgInfo.put("senderId", msg.getSendId());
                msgInfo.put("isSelf", msg.getSendId().equals(userId));
                
                return msgInfo;
            }).collect(Collectors.toList()));
            result.put("total", messagePageResult.getTotal());
            result.put("hasMore", messagePageResult.getCurrent() < messagePageResult.getPages());
            
            System.out.println("返回私聊消息列表，消息数量: " + messagePageResult.getRecords().size());
            return ResultUtils.success(result);
            
        } catch (Exception e) {
            System.err.println("获取私聊消息列表异常: " + e.getMessage());
            e.printStackTrace();
            return ResultUtils.error(500, "获取消息列表失败");
        }
    }

    @ApiOperation("发送私聊消息")
    @PostMapping("/send")
    public Result<Map<String, Object>> sendPrivateMessage(@RequestBody Map<String, Object> params) {
        System.out.println("开始发送私聊消息，参数: " + params);
        
        try {
            UserSession userSession = SessionContext.getSession();
            if (userSession == null) {
                System.out.println("用户未登录");
                return ResultUtils.error(401, "用户未登录");
            }
            
            Long userId = userSession.getUserId();
            Long contactId = Long.valueOf(params.get("contactId").toString());
            String content = params.get("content").toString();
            Integer type = Integer.valueOf(params.getOrDefault("type", 0).toString());
            
            // 验证是否为家庭成员
            LambdaQueryWrapper<FamilyRelation> relationWrapper = new LambdaQueryWrapper<>();
            relationWrapper.eq(FamilyRelation::getUserId, userId)
                          .eq(FamilyRelation::getRelativeId, contactId);
            FamilyRelation relation = familyRelationService.getOne(relationWrapper);
            if (relation == null) {
                System.out.println("不是家庭成员");
                return ResultUtils.error(403, "您与该用户不是家庭成员");
            }
            
            // 创建私聊消息
            PrivateMessage privateMessage = new PrivateMessage();
            privateMessage.setSendId(userId);
            privateMessage.setRecvId(contactId);
            privateMessage.setContent(content);
            privateMessage.setType(type == 0 ? false : true); // 0:文字, 其他:非文字
            privateMessage.setStatus(false); // 0:未读
            privateMessage.setSendTime(LocalDateTime.now());
            
            boolean saved = privateMessageService.save(privateMessage);
            if (!saved) {
                System.out.println("保存私聊消息失败");
                return ResultUtils.error(500, "发送消息失败");
            }
            
            // 构造返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("id", privateMessage.getId());
            result.put("content", privateMessage.getContent());
            result.put("type", privateMessage.getType());
            result.put("sendTime", privateMessage.getSendTime());
            result.put("senderId", privateMessage.getSendId());
            result.put("isSelf", true);
            
            System.out.println("私聊消息发送成功，消息ID: " + privateMessage.getId());
            return ResultUtils.success(result);
            
        } catch (Exception e) {
            System.err.println("发送私聊消息异常: " + e.getMessage());
            e.printStackTrace();
            return ResultUtils.error(500, "发送消息失败");
        }
    }
}
