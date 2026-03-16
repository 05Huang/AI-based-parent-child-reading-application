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
import com.qz.sns.web.ws.ChatWebSocketHandler;
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

    @Autowired
    private ChatWebSocketHandler chatWebSocketHandler;

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
            
            // 获取家庭成员（通过家庭关系表，检查双向关系）
            LambdaQueryWrapper<FamilyRelation> relationWrapper = new LambdaQueryWrapper<>();
            relationWrapper.and(wrapper -> 
                wrapper.eq(FamilyRelation::getUserId, userId)
                       .or()
                       .eq(FamilyRelation::getRelativeId, userId)
            );
            List<FamilyRelation> relations = familyRelationService.list(relationWrapper);
            System.out.println("家庭成员数量: " + relations.size());
            
            List<Map<String, Object>> contactList = relations.stream().map(relation -> {
                // 确定对方用户ID：如果当前用户是user_id，则对方是relative_id，反之亦然
                Long relativeUserId = relation.getUserId().equals(userId) 
                    ? relation.getRelativeId() 
                    : relation.getUserId();
                
                User relativeUser = userService.getById(relativeUserId);
                if (relativeUser == null) {
                    return null;
                }
                
                // 获取最后一条私聊消息
                LambdaQueryWrapper<PrivateMessage> messageWrapper = new LambdaQueryWrapper<>();
                messageWrapper.and(wrapper -> 
                    wrapper.and(w -> w.eq(PrivateMessage::getSendId, userId)
                                   .eq(PrivateMessage::getRecvId, relativeUserId))
                           .or(w -> w.eq(PrivateMessage::getSendId, relativeUserId)
                                   .eq(PrivateMessage::getRecvId, userId))
                ).orderByDesc(PrivateMessage::getSendTime)
                 .last("LIMIT 1");
                PrivateMessage lastMessage = privateMessageService.getOne(messageWrapper);
                
                // 统计未读消息数量
                LambdaQueryWrapper<PrivateMessage> unreadWrapper = new LambdaQueryWrapper<>();
                unreadWrapper.eq(PrivateMessage::getSendId, relativeUserId)
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
            
            LambdaQueryWrapper<Friend> friendWrapper = new LambdaQueryWrapper<>();
            friendWrapper.eq(Friend::getUserId, userId);
            List<Friend> friends = friendService.list(friendWrapper);
            for (Friend f : friends) {
                LambdaQueryWrapper<Friend> reciprocalWrapper = new LambdaQueryWrapper<>();
                reciprocalWrapper.eq(Friend::getUserId, f.getFriendId()).eq(Friend::getFriendId, userId);
                Friend reciprocal = friendService.getOne(reciprocalWrapper);
                if (reciprocal == null) {
                    continue;
                }
                User relativeUser = userService.getById(f.getFriendId());
                if (relativeUser == null) {
                    continue;
                }
                boolean exists = contactList.stream().anyMatch(c -> c.get("id").equals(relativeUser.getId()));
                if (exists) {
                    continue;
                }
                LambdaQueryWrapper<PrivateMessage> messageWrapper = new LambdaQueryWrapper<>();
                messageWrapper.and(wrapper -> 
                    wrapper.and(w -> w.eq(PrivateMessage::getSendId, userId)
                                   .eq(PrivateMessage::getRecvId, relativeUser.getId()))
                           .or(w -> w.eq(PrivateMessage::getSendId, relativeUser.getId())
                                   .eq(PrivateMessage::getRecvId, userId))
                ).orderByDesc(PrivateMessage::getSendTime)
                 .last("LIMIT 1");
                PrivateMessage lastMessage = privateMessageService.getOne(messageWrapper);
                LambdaQueryWrapper<PrivateMessage> unreadWrapper = new LambdaQueryWrapper<>();
                unreadWrapper.eq(PrivateMessage::getSendId, relativeUser.getId())
                           .eq(PrivateMessage::getRecvId, userId)
                           .eq(PrivateMessage::getStatus, false);
                long unreadCount = privateMessageService.count(unreadWrapper);
                Map<String, Object> contactInfo = new HashMap<>();
                contactInfo.put("id", relativeUser.getId());
                contactInfo.put("name", relativeUser.getNickname());
                contactInfo.put("avatar", relativeUser.getAvatar());
                contactInfo.put("lastMessage", lastMessage != null ? lastMessage.getContent() : "");
                contactInfo.put("lastTime", lastMessage != null ? lastMessage.getSendTime() : relativeUser.getCreatedTime());
                contactInfo.put("unread", unreadCount);
                contactInfo.put("online", true);
                contactList.add(contactInfo);
            }
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
            
            LambdaQueryWrapper<FamilyRelation> relationWrapper = new LambdaQueryWrapper<>();
            relationWrapper.and(wrapper -> 
                wrapper.and(subWrapper -> 
                    subWrapper.eq(FamilyRelation::getUserId, userId)
                             .eq(FamilyRelation::getRelativeId, contactId)
                ).or(subWrapper -> 
                    subWrapper.eq(FamilyRelation::getUserId, contactId)
                             .eq(FamilyRelation::getRelativeId, userId)
                )
            );
            FamilyRelation relation = familyRelationService.getOne(relationWrapper);
            boolean isFamily = relation != null;
            LambdaQueryWrapper<Friend> f1 = new LambdaQueryWrapper<>();
            f1.eq(Friend::getUserId, userId).eq(Friend::getFriendId, contactId);
            LambdaQueryWrapper<Friend> f2 = new LambdaQueryWrapper<>();
            f2.eq(Friend::getUserId, contactId).eq(Friend::getFriendId, userId);
            Friend rf1 = friendService.getOne(f1);
            Friend rf2 = friendService.getOne(f2);
            boolean isFriend = rf1 != null && rf2 != null;
            if (!isFamily && !isFriend) {
                System.out.println("无联系关系，用户ID: " + userId + ", 联系人ID: " + contactId);
                return ResultUtils.error(403, "您与该用户无联系关系");
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
            LambdaQueryWrapper<Friend> meFriendWrapper = new LambdaQueryWrapper<>();
            meFriendWrapper.eq(Friend::getUserId, userId).eq(Friend::getFriendId, contactId);
            Friend meFriend = friendService.getOne(meFriendWrapper);
            if (meFriend != null && meFriend.getClearTime() != null) {
                messageWrapper.gt(PrivateMessage::getSendTime, meFriend.getClearTime());
            }
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
                "name", (meFriend != null && meFriend.getRemark() != null && !meFriend.getRemark().trim().isEmpty())
                        ? meFriend.getRemark()
                        : (meFriend != null && meFriend.getFriendNickName() != null && !meFriend.getFriendNickName().trim().isEmpty()
                            ? meFriend.getFriendNickName()
                            : contact.getNickname()),
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
            Object recvIdObj = params.get("recvId");
            Object contactIdObj = params.get("contactId");
            Long contactId = recvIdObj != null
                    ? Long.valueOf(recvIdObj.toString())
                    : (contactIdObj != null ? Long.valueOf(contactIdObj.toString()) : null);
            if (contactId == null) {
                System.out.println("缺少接收用户ID参数: recvId 或 contactId");
                return ResultUtils.error(400, "缺少接收用户ID");
            }
            Object contentObj = params.get("content");
            String content = contentObj == null ? null : contentObj.toString();
            if (content == null || content.trim().isEmpty()) {
                System.out.println("消息内容为空");
                return ResultUtils.error(400, "消息内容不能为空");
            }
            Integer type = Integer.valueOf(params.getOrDefault("type", 0).toString());
            
            LambdaQueryWrapper<FamilyRelation> relationWrapper = new LambdaQueryWrapper<>();
            relationWrapper.and(wrapper -> 
                wrapper.and(subWrapper -> 
                    subWrapper.eq(FamilyRelation::getUserId, userId)
                             .eq(FamilyRelation::getRelativeId, contactId)
                ).or(subWrapper -> 
                    subWrapper.eq(FamilyRelation::getUserId, contactId)
                             .eq(FamilyRelation::getRelativeId, userId)
                )
            );
            FamilyRelation relation = familyRelationService.getOne(relationWrapper);
            boolean isFamily = relation != null;
            LambdaQueryWrapper<Friend> f1 = new LambdaQueryWrapper<>();
            f1.eq(Friend::getUserId, userId).eq(Friend::getFriendId, contactId);
            LambdaQueryWrapper<Friend> f2 = new LambdaQueryWrapper<>();
            f2.eq(Friend::getUserId, contactId).eq(Friend::getFriendId, userId);
            Friend rf1 = friendService.getOne(f1);
            Friend rf2 = friendService.getOne(f2);
            boolean isFriend = rf1 != null && rf2 != null;
            if (!isFamily && !isFriend) {
                System.out.println("无联系关系，用户ID: " + userId + ", 联系人ID: " + contactId);
                return ResultUtils.error(403, "您与该用户无联系关系");
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

            try {
                Map<String, Object> payload = new HashMap<>();
                payload.put("type", "private");
                payload.put("chatId", userId);
                payload.put("message", result);
                chatWebSocketHandler.sendToUser(contactId, payload);
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            System.out.println("私聊消息发送成功，联系人ID: " + contactId + ", 消息ID: " + privateMessage.getId());
            return ResultUtils.success(result);
            
        } catch (Exception e) {
            System.err.println("发送私聊消息异常: " + e.getMessage());
            e.printStackTrace();
            return ResultUtils.error(500, "发送消息失败");
        }
    }
}
