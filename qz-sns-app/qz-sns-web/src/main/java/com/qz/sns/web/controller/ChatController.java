package com.qz.sns.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qz.sns.model.entity.*;
import com.qz.sns.sv.result.Result;
import com.qz.sns.sv.result.ResultUtils;
import com.qz.sns.sv.service.*;
import com.qz.sns.sv.session.SessionContext;
import com.qz.sns.sv.session.UserSession;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 聊天管理 前端控制器
 * </p>
 *
 * @author 李彬
 * @since 2025-09-27
 */
@RestController
@RequestMapping("/api/chat")
@Api(tags = "聊天管理")
public class ChatController {

    @Autowired
    private IGroupService groupService;
    
    @Autowired
    private IGroupMemberService groupMemberService;
    
    @Autowired
    private IGroupMessageService groupMessageService;
    
    @Autowired
    private IPrivateMessageService privateMessageService;
    
    @Autowired
    private IFamilyRelationService familyRelationService;
    
    @Autowired
    private IUserService userService;
    
    @Autowired
    private IFriendService friendService;

    @ApiOperation("获取聊天列表（包括群聊和私聊）")
    @GetMapping("/list")
    public Result<Map<String, Object>> getChatList() {
        System.out.println("开始获取聊天列表...");
        
        try {
            UserSession userSession = SessionContext.getSession();
            if (userSession == null) {
                System.out.println("用户未登录");
                return ResultUtils.error(401, "用户未登录");
            }
            
            Long userId = userSession.getUserId();
            System.out.println("当前用户ID: " + userId);
            
            List<Map<String, Object>> chatList = new ArrayList<>();
            
            // 1. 获取家庭群聊
            LambdaQueryWrapper<GroupMember> memberWrapper = new LambdaQueryWrapper<>();
            memberWrapper.eq(GroupMember::getUserId, userId)
                        .eq(GroupMember::getQuit, false);
            List<GroupMember> memberList = groupMemberService.list(memberWrapper);
            System.out.println("用户加入的群组数量: " + memberList.size());
            
            for (GroupMember member : memberList) {
                Group group = groupService.getById(member.getGroupId());
                if (group == null) continue;
                
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
                
                Map<String, Object> groupChat = new HashMap<>();
                groupChat.put("id", group.getId());
                groupChat.put("type", "group");
                groupChat.put("name", group.getName());
                groupChat.put("avatar", group.getHeadImage());
                groupChat.put("memberCount", memberCount);
                groupChat.put("lastMessage", lastMessage != null ? 
                    (lastMessage.getSendNickName() + ": " + lastMessage.getContent()) : "");
                groupChat.put("lastTime", lastMessage != null ? 
                    lastMessage.getSendTime() : group.getCreatedTime());
                groupChat.put("unread", 0); // TODO: 实现未读消息统计
                
                chatList.add(groupChat);
            }
            
            // 2. 获取家庭成员私聊
            LambdaQueryWrapper<FamilyRelation> relationWrapper = new LambdaQueryWrapper<>();
            relationWrapper.eq(FamilyRelation::getUserId, userId);
            List<FamilyRelation> relations = familyRelationService.list(relationWrapper);
            System.out.println("家庭成员数量: " + relations.size());
            
            for (FamilyRelation relation : relations) {
                User relativeUser = userService.getById(relation.getRelativeId());
                if (relativeUser == null) continue;

                LambdaQueryWrapper<Friend> meFriendWrapper = new LambdaQueryWrapper<>();
                meFriendWrapper.eq(Friend::getUserId, userId).eq(Friend::getFriendId, relation.getRelativeId());
                Friend meFriend = friendService.getOne(meFriendWrapper);
                
                // 获取最后一条私聊消息
                LambdaQueryWrapper<PrivateMessage> privateMessageWrapper = new LambdaQueryWrapper<>();
                privateMessageWrapper.and(wrapper -> 
                    wrapper.and(w -> w.eq(PrivateMessage::getSendId, userId)
                                   .eq(PrivateMessage::getRecvId, relation.getRelativeId()))
                           .or(w -> w.eq(PrivateMessage::getSendId, relation.getRelativeId())
                                   .eq(PrivateMessage::getRecvId, userId))
                );
                if (meFriend != null && meFriend.getClearTime() != null) {
                    privateMessageWrapper.gt(PrivateMessage::getSendTime, meFriend.getClearTime());
                }
                privateMessageWrapper.orderByDesc(PrivateMessage::getSendTime)
                 .last("LIMIT 1");
                PrivateMessage lastPrivateMessage = privateMessageService.getOne(privateMessageWrapper);
                
                // 统计未读消息数量
                LambdaQueryWrapper<PrivateMessage> unreadWrapper = new LambdaQueryWrapper<>();
                unreadWrapper.eq(PrivateMessage::getSendId, relation.getRelativeId())
                           .eq(PrivateMessage::getRecvId, userId)
                           .eq(PrivateMessage::getStatus, false); // 0:未读
                long unreadCount = privateMessageService.count(unreadWrapper);
                
                Map<String, Object> privateChat = new HashMap<>();
                privateChat.put("id", relativeUser.getId());
                privateChat.put("type", "private");
                String displayName = relativeUser.getNickname();
                if (meFriend != null) {
                    if (StringUtils.hasText(meFriend.getRemark())) {
                        displayName = meFriend.getRemark();
                    } else if (StringUtils.hasText(meFriend.getFriendNickName())) {
                        displayName = meFriend.getFriendNickName();
                    }
                }
                privateChat.put("name", displayName);
                privateChat.put("avatar", relativeUser.getAvatar());
                privateChat.put("lastMessage", lastPrivateMessage != null ? 
                    lastPrivateMessage.getContent() : "");
                privateChat.put("lastTime", lastPrivateMessage != null ? 
                    lastPrivateMessage.getSendTime() : relativeUser.getCreatedTime());
                privateChat.put("unread", unreadCount);
                privateChat.put("online", true); // TODO: 实现在线状态
                privateChat.put("pinned", meFriend != null && Boolean.TRUE.equals(meFriend.getPinned()));
                
                chatList.add(privateChat);
            }
            
            LambdaQueryWrapper<Friend> friendWrapper = new LambdaQueryWrapper<>();
            friendWrapper.eq(Friend::getUserId, userId);
            List<Friend> friends = friendService.list(friendWrapper);
            System.out.println("好友数量: " + friends.size());
            for (Friend f : friends) {
                Long otherId = f.getFriendId();
                LambdaQueryWrapper<Friend> reciprocalWrapper = new LambdaQueryWrapper<>();
                reciprocalWrapper.eq(Friend::getUserId, otherId)
                                 .eq(Friend::getFriendId, userId);
                Friend reciprocal = friendService.getOne(reciprocalWrapper);
                if (reciprocal == null) {
                    continue;
                }
                
                User friendUser = userService.getById(otherId);
                if (friendUser == null) {
                    continue;
                }
                
                boolean exists = chatList.stream().anyMatch(item ->
                    "private".equals(item.get("type")) && friendUser.getId().equals(item.get("id"))
                );
                if (exists) {
                    continue;
                }
                
                Friend meFriend = f;
                
                LambdaQueryWrapper<PrivateMessage> pmWrapper = new LambdaQueryWrapper<>();
                pmWrapper.and(wrapper -> 
                    wrapper.and(w -> w.eq(PrivateMessage::getSendId, userId)
                                   .eq(PrivateMessage::getRecvId, friendUser.getId()))
                           .or(w -> w.eq(PrivateMessage::getSendId, friendUser.getId())
                                   .eq(PrivateMessage::getRecvId, userId))
                );
                if (meFriend != null && meFriend.getClearTime() != null) {
                    pmWrapper.gt(PrivateMessage::getSendTime, meFriend.getClearTime());
                }
                pmWrapper.orderByDesc(PrivateMessage::getSendTime)
                 .last("LIMIT 1");
                PrivateMessage lastPM = privateMessageService.getOne(pmWrapper);
                
                LambdaQueryWrapper<PrivateMessage> unreadPmWrapper = new LambdaQueryWrapper<>();
                unreadPmWrapper.eq(PrivateMessage::getSendId, friendUser.getId())
                               .eq(PrivateMessage::getRecvId, userId)
                               .eq(PrivateMessage::getStatus, false);
                long unreadCount = privateMessageService.count(unreadPmWrapper);
                
                Map<String, Object> privateChat = new HashMap<>();
                privateChat.put("id", friendUser.getId());
                privateChat.put("type", "private");
                String displayName = friendUser.getNickname();
                if (meFriend != null) {
                    if (StringUtils.hasText(meFriend.getRemark())) {
                        displayName = meFriend.getRemark();
                    } else if (StringUtils.hasText(meFriend.getFriendNickName())) {
                        displayName = meFriend.getFriendNickName();
                    }
                }
                privateChat.put("name", displayName);
                privateChat.put("avatar", friendUser.getAvatar());
                privateChat.put("lastMessage", lastPM != null ? lastPM.getContent() : "");
                privateChat.put("lastTime", lastPM != null ? lastPM.getSendTime() : friendUser.getCreatedTime());
                privateChat.put("unread", unreadCount);
                privateChat.put("online", true);
                privateChat.put("pinned", meFriend != null && Boolean.TRUE.equals(meFriend.getPinned()));
                
                chatList.add(privateChat);
            }
            
            // 3. 添加AI助手聊天
            Map<String, Object> aiChat = new HashMap<>();
            aiChat.put("id", -1);
            aiChat.put("type", "ai");
            aiChat.put("name", "AI阅读助手");
            aiChat.put("avatar", "/static/images/avatars/AI机器人.svg");
            aiChat.put("lastMessage", "根据您的阅读习惯，为您推荐...");
            aiChat.put("lastTime", LocalDateTime.now());
            aiChat.put("unread", 0);
            aiChat.put("online", true);
            
            chatList.add(aiChat);
            
            // 按最后消息时间排序
            chatList.sort((a, b) -> {
                LocalDateTime timeA = (LocalDateTime) a.get("lastTime");
                LocalDateTime timeB = (LocalDateTime) b.get("lastTime");
                return timeB.compareTo(timeA);
            });
            
            Map<String, Object> result = new HashMap<>();
            result.put("chatList", chatList);
            result.put("total", chatList.size());
            
            System.out.println("返回聊天列表，数量: " + chatList.size());
            return ResultUtils.success(result);
            
        } catch (Exception e) {
            System.err.println("获取聊天列表异常: " + e.getMessage());
            e.printStackTrace();
            return ResultUtils.error(500, "获取聊天列表失败");
        }
    }
} 
