package com.qz.sns.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qz.sns.model.entity.Group;
import com.qz.sns.model.entity.GroupMember;
import com.qz.sns.model.entity.GroupMessage;
import com.qz.sns.model.entity.GroupInvite;
import com.qz.sns.model.entity.User;
import com.qz.sns.sv.result.Result;
import com.qz.sns.sv.result.ResultUtils;
import com.qz.sns.sv.service.IGroupMemberService;
import com.qz.sns.sv.service.IGroupMessageService;
import com.qz.sns.sv.service.IGroupService;
import com.qz.sns.sv.service.IGroupInviteService;
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
    
    @Autowired
    private IGroupInviteService groupInviteService;

    @Autowired
    private ChatWebSocketHandler chatWebSocketHandler;

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
                String displayName = (member.getRemarkGroupName() != null && !member.getRemarkGroupName().trim().isEmpty())
                        ? member.getRemarkGroupName()
                        : group.getName();
                groupInfo.put("name", displayName);
                groupInfo.put("rawName", group.getName());
                groupInfo.put("avatar", group.getHeadImage());
                groupInfo.put("memberCount", memberCount);
                groupInfo.put("lastMessage", lastMessage != null ? lastMessage.getContent() : "");
                groupInfo.put("lastTime", lastMessage != null ? lastMessage.getSendTime() : group.getCreatedTime());
                groupInfo.put("unread", 0); // TODO: 实现未读消息统计
                groupInfo.put("pinned", Boolean.TRUE.equals(member.getPinned()));
                groupInfo.put("pinnedTime", member.getPinnedTime());
                
                return groupInfo;
            }).filter(group -> group != null).collect(Collectors.toList());
            
            // 置顶排序优先，其次按最后消息时间
            groupList = groupList.stream()
                    .sorted((a, b) -> {
                        boolean ap = Boolean.TRUE.equals(a.get("pinned"));
                        boolean bp = Boolean.TRUE.equals(b.get("pinned"));
                        if (ap != bp) return bp ? 1 : -1;
                        LocalDateTime at = (LocalDateTime) a.get("lastTime");
                        LocalDateTime bt = (LocalDateTime) b.get("lastTime");
                        if (at == null && bt == null) return 0;
                        if (at == null) return 1;
                        if (bt == null) return -1;
                        return bt.compareTo(at);
                    })
                    .collect(Collectors.toList());
            
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
            
            // 检查用户是否是管理员，如果是管理员则允许查看所有群组的消息
            User user = userService.getById(userSession.getUserId());
            boolean isAdmin = user != null && user.getUserType() != null && user.getUserType() == 2;
            
            if (!isAdmin) {
                // 非管理员需要验证用户是否在该群组中
                LambdaQueryWrapper<GroupMember> memberWrapper = new LambdaQueryWrapper<>();
                memberWrapper.eq(GroupMember::getGroupId, groupId)
                            .eq(GroupMember::getUserId, userSession.getUserId())
                            .eq(GroupMember::getQuit, false);
                GroupMember member = groupMemberService.getOne(memberWrapper);
                if (member == null) {
                    System.out.println("用户不在该群组中");
                    return ResultUtils.error(403, "您不在该群组中");
                }
            } else {
                System.out.println("管理员用户，允许查看所有群组的消息");
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
            Map<Long, GroupMember> memberMap = new java.util.HashMap<>();
            for (GroupMember m : members) {
                if (m != null && m.getUserId() != null) {
                    memberMap.put(m.getUserId(), m);
                }
            }
            
            // 分页查询消息
            Page<GroupMessage> messagePage = new Page<>(page, size);
            LambdaQueryWrapper<GroupMessage> messageWrapper = new LambdaQueryWrapper<>();
            messageWrapper.eq(GroupMessage::getGroupId, groupId)
                         .orderByDesc(GroupMessage::getSendTime);
            // 清除过滤：仅显示 clearTime 之后的消息（成员维度）
            LambdaQueryWrapper<GroupMember> meWrapper = new LambdaQueryWrapper<>();
            meWrapper.eq(GroupMember::getGroupId, groupId)
                    .eq(GroupMember::getUserId, userSession.getUserId())
                    .eq(GroupMember::getQuit, false);
            GroupMember me = groupMemberService.getOne(meWrapper);
            if (me != null && me.getClearTime() != null) {
                messageWrapper.gt(GroupMessage::getSendTime, me.getClearTime());
            }
            IPage<GroupMessage> messagePageResult = groupMessageService.page(messagePage, messageWrapper);
            
            // 构造返回结果
            Map<String, Object> result = new HashMap<>();
            Map<String, Object> groupInfoMap = new HashMap<>();
            groupInfoMap.put("id", group.getId());
            String displayName = (me != null && me.getRemarkGroupName() != null && !me.getRemarkGroupName().trim().isEmpty())
                    ? me.getRemarkGroupName()
                    : group.getName();
            groupInfoMap.put("name", displayName);
            groupInfoMap.put("rawName", group.getName());
            groupInfoMap.put("avatar", group.getHeadImage());
            groupInfoMap.put("memberCount", members.size());
            groupInfoMap.put("ownerId", group.getOwnerId());
            result.put("groupInfo", groupInfoMap);
            final Long ownerId = group.getOwnerId();
            List<Map<String, Object>> memberList = members.stream().map(m -> {
                Map<String, Object> mi = new HashMap<>();
                mi.put("id", m.getUserId());
                mi.put("name", m.getUserNickName());
                mi.put("avatar", m.getHeadImage());
                mi.put("role", (ownerId != null && ownerId.equals(m.getUserId())) ? "群主" : "成员");
                return mi;
            }).collect(Collectors.toList());
            if (ownerId != null && (memberList.stream().noneMatch(mi -> ownerId.equals((Long) mi.get("id"))))) {
                User owner = userService.getById(ownerId);
                Map<String, Object> ownerInfo = new HashMap<>();
                ownerInfo.put("id", ownerId);
                ownerInfo.put("name", owner != null ? owner.getNickname() : null);
                ownerInfo.put("avatar", owner != null ? owner.getAvatar() : null);
                ownerInfo.put("role", "群主");
                ownerInfo.put("joinTime", group.getCreatedTime());
                memberList.add(0, ownerInfo);
            }
            result.put("members", memberList);
            result.put("messages", messagePageResult.getRecords().stream().map(msg -> {
                Map<String, Object> msgInfo = new HashMap<>();
                msgInfo.put("id", msg.getId());
                msgInfo.put("content", msg.getContent());
                msgInfo.put("type", msg.getType());
                msgInfo.put("sendTime", msg.getSendTime());
                msgInfo.put("senderId", msg.getSendId());
                GroupMember senderMember = memberMap.get(msg.getSendId());
                String dynamicName = senderMember != null && senderMember.getUserNickName() != null && !senderMember.getUserNickName().trim().isEmpty()
                        ? senderMember.getUserNickName()
                        : msg.getSendNickName();
                msgInfo.put("senderName", dynamicName);
                msgInfo.put("isSelf", msg.getSendId().equals(userSession.getUserId()));
                
                User sender = userService.getById(msg.getSendId());
                String avatar = null;
                if (senderMember != null && senderMember.getHeadImage() != null && !senderMember.getHeadImage().trim().isEmpty()) {
                    avatar = senderMember.getHeadImage();
                } else {
                    avatar = sender != null ? sender.getAvatar() : "";
                }
                if ((avatar == null || avatar.trim().isEmpty()) && msg.getSendId() != null && msg.getSendId() < 0) {
                    avatar = "/static/images/avatars/AI机器人.svg";
                }
                if ((avatar == null || avatar.trim().isEmpty()) && "AI阅读助手".equals(dynamicName)) {
                    avatar = "/static/images/avatars/AI机器人.svg";
                }
                msgInfo.put("avatar", avatar);
                
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

    @ApiOperation("置顶/取消置顶群聊（按用户维度）")
    @PostMapping("/{groupId}/pin")
    public Result<Map<String, Object>> pinGroup(@PathVariable Long groupId, @RequestBody Map<String, Object> body) {
        try {
            UserSession session = SessionContext.getSession();
            if (session == null) return ResultUtils.error(401, "未登录");
            boolean pinned = body.get("pinned") != null && Boolean.parseBoolean(body.get("pinned").toString());
            LambdaQueryWrapper<GroupMember> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(GroupMember::getGroupId, groupId).eq(GroupMember::getUserId, session.getUserId()).eq(GroupMember::getQuit, false);
            GroupMember me = groupMemberService.getOne(wrapper);
            if (me == null) return ResultUtils.error(403, "您不在该群组中");
            me.setPinned(pinned);
            me.setPinnedTime(pinned ? LocalDateTime.now() : null);
            boolean ok = groupMemberService.updateById(me);
            if (!ok) return ResultUtils.error(500, "操作失败");
            return ResultUtils.success(Map.of("pinned", pinned));
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtils.error(500, "操作失败");
        }
    }

    @ApiOperation("清除本人群消息（设置清除时间阈值）")
    @PostMapping("/{groupId}/clear")
    public Result<Map<String, Object>> clearGroupMessages(@PathVariable Long groupId) {
        try {
            UserSession session = SessionContext.getSession();
            if (session == null) return ResultUtils.error(401, "未登录");
            LambdaQueryWrapper<GroupMember> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(GroupMember::getGroupId, groupId).eq(GroupMember::getUserId, session.getUserId()).eq(GroupMember::getQuit, false);
            GroupMember me = groupMemberService.getOne(wrapper);
            if (me == null) return ResultUtils.error(403, "您不在该群组中");
            me.setClearTime(LocalDateTime.now());
            boolean ok = groupMemberService.updateById(me);
            if (!ok) return ResultUtils.error(500, "清除失败");
            return ResultUtils.success(Map.of("cleared", true));
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtils.error(500, "清除失败");
        }
    }

    @ApiOperation("更新群名（仅群主）")
    @PutMapping("/{groupId}/name")
    public Result<Map<String, Object>> updateGroupName(@PathVariable Long groupId, @RequestBody Map<String, Object> body) {
        try {
            UserSession session = SessionContext.getSession();
            if (session == null) return ResultUtils.error(401, "未登录");
            String name = body.get("name") != null ? body.get("name").toString().trim() : null;
            if (name == null || name.isEmpty()) return ResultUtils.error(400, "群名不能为空");
            Group g = groupService.getById(groupId);
            if (g == null) return ResultUtils.error(404, "群组不存在");
            if (!session.getUserId().equals(g.getOwnerId())) return ResultUtils.error(403, "只有群主可修改");
            g.setName(name);
            boolean ok = groupService.updateById(g);
            if (!ok) return ResultUtils.error(500, "更新失败");
            return ResultUtils.success(Map.of("name", name));
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtils.error(500, "更新失败");
        }
    }

    @ApiOperation("更新群头像（仅群主）")
    @PutMapping("/{groupId}/avatar")
    public Result<Map<String, Object>> updateGroupAvatar(@PathVariable Long groupId, @RequestBody Map<String, Object> body) {
        try {
            UserSession session = SessionContext.getSession();
            if (session == null) return ResultUtils.error(401, "未登录");
            String avatarUrl = body.get("avatarUrl") != null ? body.get("avatarUrl").toString() : null;
            if (avatarUrl == null || avatarUrl.trim().isEmpty()) return ResultUtils.error(400, "头像地址不能为空");
            Group g = groupService.getById(groupId);
            if (g == null) return ResultUtils.error(404, "群组不存在");
            if (!session.getUserId().equals(g.getOwnerId())) return ResultUtils.error(403, "只有群主可修改");
            g.setHeadImage(avatarUrl);
            boolean ok = groupService.updateById(g);
            if (!ok) return ResultUtils.error(500, "更新失败");
            return ResultUtils.success(Map.of("avatar", avatarUrl));
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtils.error(500, "更新失败");
        }
    }

    @ApiOperation("更新我在群中的昵称")
    @PutMapping("/{groupId}/nickname")
    public Result<Map<String, Object>> updateMyNickname(@PathVariable Long groupId, @RequestBody Map<String, Object> body) {
        try {
            UserSession session = SessionContext.getSession();
            if (session == null) return ResultUtils.error(401, "未登录");
            String nickname = body.get("nickname") != null ? body.get("nickname").toString().trim() : null;
            if (nickname == null || nickname.isEmpty()) return ResultUtils.error(400, "昵称不能为空");
            LambdaQueryWrapper<GroupMember> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(GroupMember::getGroupId, groupId).eq(GroupMember::getUserId, session.getUserId()).eq(GroupMember::getQuit, false);
            GroupMember me = groupMemberService.getOne(wrapper);
            if (me == null) return ResultUtils.error(403, "您不在该群组中");
            me.setUserNickName(nickname);
            boolean ok = groupMemberService.updateById(me);
            if (!ok) return ResultUtils.error(500, "保存失败");
            return ResultUtils.success(Map.of("nickname", nickname));
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtils.error(500, "保存失败");
        }
    }

    @ApiOperation("更新我看到的群名备注")
    @PutMapping("/{groupId}/remark-group")
    public Result<Map<String, Object>> updateMyGroupRemark(@PathVariable Long groupId, @RequestBody Map<String, Object> body) {
        try {
            UserSession session = SessionContext.getSession();
            if (session == null) return ResultUtils.error(401, "未登录");
            String remark = body.get("remark") != null ? body.get("remark").toString() : "";
            LambdaQueryWrapper<GroupMember> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(GroupMember::getGroupId, groupId).eq(GroupMember::getUserId, session.getUserId()).eq(GroupMember::getQuit, false);
            GroupMember me = groupMemberService.getOne(wrapper);
            if (me == null) return ResultUtils.error(403, "您不在该群组中");
            me.setRemarkGroupName(remark);
            boolean ok = groupMemberService.updateById(me);
            if (!ok) return ResultUtils.error(500, "保存失败");
            return ResultUtils.success(Map.of("remark", remark));
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtils.error(500, "保存失败");
        }
    }

    @ApiOperation("退出群聊（成员）")
    @PostMapping("/{groupId}/quit")
    public Result<Map<String, Object>> quitGroup(@PathVariable Long groupId) {
        try {
            UserSession session = SessionContext.getSession();
            if (session == null) return ResultUtils.error(401, "未登录");
            Group g = groupService.getById(groupId);
            if (g == null) return ResultUtils.error(404, "群组不存在");
            if (session.getUserId().equals(g.getOwnerId())) return ResultUtils.error(403, "群主不能退出，请解散群聊");
            LambdaQueryWrapper<GroupMember> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(GroupMember::getGroupId, groupId).eq(GroupMember::getUserId, session.getUserId()).eq(GroupMember::getQuit, false);
            GroupMember me = groupMemberService.getOne(wrapper);
            if (me == null) return ResultUtils.error(403, "您不在该群组中");
            me.setQuit(true);
            me.setQuitTime(LocalDateTime.now());
            boolean ok = groupMemberService.updateById(me);
            if (!ok) return ResultUtils.error(500, "退出失败");
            return ResultUtils.success(Map.of("quit", true));
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtils.error(500, "退出失败");
        }
    }

    @ApiOperation("解散群聊（群主）")
    @PostMapping("/{groupId}/dissolve")
    public Result<Map<String, Object>> dissolveGroup(@PathVariable Long groupId) {
        try {
            UserSession session = SessionContext.getSession();
            if (session == null) return ResultUtils.error(401, "未登录");
            Group g = groupService.getById(groupId);
            if (g == null) return ResultUtils.error(404, "群组不存在");
            if (!session.getUserId().equals(g.getOwnerId())) return ResultUtils.error(403, "只有群主可操作");
            g.setDissolve(true);
            boolean ok = groupService.updateById(g);
            if (!ok) return ResultUtils.error(500, "解散失败");
            LambdaQueryWrapper<GroupMember> all = new LambdaQueryWrapper<>();
            all.eq(GroupMember::getGroupId, groupId).eq(GroupMember::getQuit, false);
            List<GroupMember> members = groupMemberService.list(all);
            LocalDateTime now = LocalDateTime.now();
            for (GroupMember m : members) {
                m.setQuit(true);
                m.setQuitTime(now);
                groupMemberService.updateById(m);
            }
            return ResultUtils.success(Map.of("dissolved", true));
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtils.error(500, "解散失败");
        }
    }
    @ApiOperation("邀请加入群聊")
    @PostMapping("/{groupId}/invite")
    public Result<Map<String, Object>> invite(@PathVariable Long groupId, @RequestBody Map<String, Object> body) {
        try {
            System.out.println("开始发送群聊邀请，群组ID: " + groupId + "，参数: " + body);
            UserSession userSession = SessionContext.getSession();
            if (userSession == null) {
                return ResultUtils.error(401, "用户未登录");
            }
            Group group = groupService.getById(groupId);
            if (group == null) {
                return ResultUtils.error(404, "群组不存在");
            }
            Object idsObj = body.get("inviteeIds");
            List<Long> inviteeIds;
            if (idsObj instanceof List) {
                inviteeIds = ((List<?>) idsObj).stream().map(o -> Long.valueOf(o.toString())).collect(Collectors.toList());
            } else {
                inviteeIds = new java.util.ArrayList<>();
                Object idObj = body.get("inviteeId");
                if (idObj != null) {
                    inviteeIds.add(Long.valueOf(idObj.toString()));
                }
            }
            System.out.println("准备邀请成员数量: " + inviteeIds.size());
            int created = 0;
            for (Long inviteeId : inviteeIds) {
                LambdaQueryWrapper<GroupMember> memberWrapper = new LambdaQueryWrapper<>();
                memberWrapper.eq(GroupMember::getGroupId, groupId).eq(GroupMember::getUserId, inviteeId).eq(GroupMember::getQuit, false);
                GroupMember exists = groupMemberService.getOne(memberWrapper);
                if (exists != null) {
                    System.out.println("用户已是群成员，跳过: " + inviteeId);
                    continue;
                }
                LambdaQueryWrapper<GroupInvite> dupWrapper = new LambdaQueryWrapper<>();
                dupWrapper.eq(GroupInvite::getGroupId, groupId)
                        .eq(GroupInvite::getInviteeId, inviteeId)
                        .eq(GroupInvite::getStatus, 0);
                GroupInvite dup = groupInviteService.getOne(dupWrapper);
                if (dup != null) {
                    System.out.println("已存在待处理邀请，跳过: " + inviteeId);
                    continue;
                }
                GroupInvite invite = new GroupInvite();
                invite.setGroupId(groupId);
                invite.setInviterId(userSession.getUserId());
                invite.setInviteeId(inviteeId);
                invite.setMessage("邀请你加入群聊：" + group.getName());
                invite.setStatus(0);
                invite.setCreatedTime(java.time.LocalDateTime.now());
                boolean ok = groupInviteService.save(invite);
                if (ok) created++;
            }
            System.out.println("本次邀请创建条数: " + created);
            Map<String, Object> result = new HashMap<>();
            result.put("groupId", groupId);
            result.put("created", created);
            return ResultUtils.success(result, "邀请已发送");
        } catch (Exception e) {
            System.out.println("群聊邀请发生异常: " + e.getMessage());
            e.printStackTrace();
            return ResultUtils.error(500, "邀请失败");
        }
    }

    @ApiOperation("获取我的群聊邀请")
    @GetMapping("/invites")
    public Result<List<Map<String, Object>>> getInvites() {
        try {
            UserSession userSession = SessionContext.getSession();
            if (userSession == null) {
                return ResultUtils.error(401, "用户未登录");
            }
            LambdaQueryWrapper<GroupInvite> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(GroupInvite::getInviteeId, userSession.getUserId()).eq(GroupInvite::getStatus, 0);
            List<GroupInvite> invites = groupInviteService.list(wrapper);
            List<Map<String, Object>> list = invites.stream().map(inv -> {
                Group g = groupService.getById(inv.getGroupId());
                Map<String, Object> map = new HashMap<>();
                map.put("id", inv.getId());
                map.put("groupId", inv.getGroupId());
                map.put("groupName", g != null ? g.getName() : null);
                map.put("groupAvatar", g != null ? g.getHeadImage() : null);
                map.put("message", inv.getMessage());
                map.put("createdTime", inv.getCreatedTime());
                map.put("type", 2);
                return map;
            }).collect(Collectors.toList());
            return ResultUtils.success(list);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtils.error(500, "获取邀请失败");
        }
    }

    @ApiOperation("处理群聊邀请")
    @PostMapping("/invites/{inviteId}/handle")
    public Result<Map<String, Object>> handleInvite(@PathVariable Long inviteId, @RequestBody Map<String, Object> body) {
        try {
            UserSession userSession = SessionContext.getSession();
            if (userSession == null) {
                return ResultUtils.error(401, "用户未登录");
            }
            Object statusObj = body.get("status");
            if (statusObj == null) {
                return ResultUtils.error(400, "缺少参数");
            }
            Integer status = Integer.valueOf(statusObj.toString());
            GroupInvite invite = groupInviteService.getById(inviteId);
            if (invite == null) {
                return ResultUtils.error(404, "邀请不存在");
            }
            if (!userSession.getUserId().equals(invite.getInviteeId())) {
                return ResultUtils.error(403, "无权限处理该邀请");
            }
            Map<String, Object> result = new HashMap<>();
            if (status == 1) {
                LambdaQueryWrapper<GroupMember> memberWrapper = new LambdaQueryWrapper<>();
                memberWrapper.eq(GroupMember::getGroupId, invite.getGroupId())
                        .eq(GroupMember::getUserId, userSession.getUserId())
                        .eq(GroupMember::getQuit, false);
                GroupMember exists = groupMemberService.getOne(memberWrapper);
                if (exists == null) {
                    Group g = groupService.getById(invite.getGroupId());
                    User u = userService.getById(userSession.getUserId());
                    GroupMember gm = new GroupMember();
                    gm.setGroupId(invite.getGroupId());
                    gm.setUserId(userSession.getUserId());
                    gm.setUserNickName(u != null ? u.getNickname() : null);
                    gm.setHeadImage(u != null ? u.getAvatar() : null);
                    gm.setRemarkGroupName(g != null ? g.getName() : null);
                    gm.setQuit(false);
                    gm.setCreatedTime(java.time.LocalDateTime.now());
                    groupMemberService.save(gm);
                    result.put("joined", true);
                }
                invite.setStatus(1);
                groupInviteService.updateById(invite);
                return ResultUtils.success(result, "已加入群聊");
            } else if (status == 2) {
                invite.setStatus(2);
                groupInviteService.updateById(invite);
                return ResultUtils.success(result, "已拒绝邀请");
            } else {
                return ResultUtils.error(400, "状态值无效");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtils.error(500, "处理邀请失败");
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
            String sendName = null;
            try {
                sendName = member.getUserNickName();
            } catch (Exception ignored) {}
            if (sendName == null || sendName.trim().isEmpty()) {
                sendName = user.getNickname();
            }
            groupMessage.setSendNickName(sendName);
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

            try {
                LambdaQueryWrapper<GroupMember> membersWrapper = new LambdaQueryWrapper<>();
                membersWrapper.eq(GroupMember::getGroupId, groupId)
                        .eq(GroupMember::getQuit, false);
                List<GroupMember> members = groupMemberService.list(membersWrapper);
                List<Long> targetUserIds = members.stream()
                        .map(GroupMember::getUserId)
                        .filter(id -> id != null && !id.equals(userSession.getUserId()))
                        .collect(Collectors.toList());
                if (!targetUserIds.isEmpty()) {
                    Map<String, Object> payload = new HashMap<>();
                    payload.put("type", "group");
                    payload.put("chatId", groupId);
                    payload.put("message", result);
                    chatWebSocketHandler.sendToUsers(targetUserIds, payload);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            System.out.println("群消息发送成功，消息ID: " + groupMessage.getId());
            return ResultUtils.success(result);
            
        } catch (Exception e) {
            System.err.println("发送群消息异常: " + e.getMessage());
            e.printStackTrace();
            return ResultUtils.error(500, "发送消息失败");
        }
    }

    @ApiOperation("获取所有群聊列表（管理后台使用）")
    @GetMapping("/admin/all-groups")
    public Result<List<Map<String, Object>>> getAllGroups() {
        System.out.println("开始获取所有群聊列表...");
        
        try {
            UserSession userSession = SessionContext.getSession();
            if (userSession == null) {
                System.out.println("用户未登录");
                return ResultUtils.error(401, "用户未登录");
            }
            
            // 检查用户是否是管理员
            User user = userService.getById(userSession.getUserId());
            if (user == null || user.getUserType() == null || user.getUserType() != 2) {
                System.out.println("用户不是管理员");
                return ResultUtils.error(403, "只有管理员可以查看所有群组");
            }
            
            // 查询所有群组
            List<Group> allGroups = groupService.list();
            System.out.println("数据库中的群组总数: " + allGroups.size());
            
            // 构建返回数据
            List<Map<String, Object>> groupList = allGroups.stream().map(group -> {
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
                groupInfo.put("createdTime", group.getCreatedTime());
                groupInfo.put("unread", 0);
                
                return groupInfo;
            }).collect(Collectors.toList());
            
            System.out.println("返回群组列表，数量: " + groupList.size());
            return ResultUtils.success(groupList);
            
        } catch (Exception e) {
            System.err.println("获取所有群聊列表异常: " + e.getMessage());
            e.printStackTrace();
            return ResultUtils.error(500, "获取群聊列表失败");
        }
    }

    @ApiOperation("获取群组成员列表")
    @GetMapping("/{groupId}/members")
    public Result<List<Map<String, Object>>> getGroupMembers(
            @ApiParam("群组ID") @PathVariable Long groupId) {
        System.out.println("开始获取群组成员列表，群组ID: " + groupId);
        
        try {
            UserSession userSession = SessionContext.getSession();
            if (userSession == null) {
                System.out.println("用户未登录");
                return ResultUtils.error(401, "用户未登录");
            }
            
            // 检查用户是否是管理员，如果是管理员则允许查看所有群组的成员
            User user = userService.getById(userSession.getUserId());
            boolean isAdmin = user != null && user.getUserType() != null && user.getUserType() == 2;
            
            if (!isAdmin) {
                // 非管理员需要验证用户是否是该群组的成员
                LambdaQueryWrapper<GroupMember> memberWrapper = new LambdaQueryWrapper<>();
                memberWrapper.eq(GroupMember::getGroupId, groupId)
                            .eq(GroupMember::getUserId, userSession.getUserId())
                            .eq(GroupMember::getQuit, false);
                GroupMember member = groupMemberService.getOne(memberWrapper);
                if (member == null) {
                    System.out.println("用户不是该群组的成员");
                    return ResultUtils.error(403, "您不是该群组的成员");
                }
            } else {
                System.out.println("管理员用户，允许查看所有群组的成员");
            }

            // 获取群组成员列表
            LambdaQueryWrapper<GroupMember> membersWrapper = new LambdaQueryWrapper<>();
            membersWrapper.eq(GroupMember::getGroupId, groupId)
                          .eq(GroupMember::getQuit, false);
            List<GroupMember> members = groupMemberService.list(membersWrapper);

            // 构造返回结果
            Group group = groupService.getById(groupId);
            final Long ownerId = group != null ? group.getOwnerId() : null;
            List<Map<String, Object>> memberList = members.stream().map(m -> {
                Map<String, Object> memberInfo = new HashMap<>();
                memberInfo.put("id", m.getUserId());
                memberInfo.put("name", m.getUserNickName());
                memberInfo.put("avatar", m.getHeadImage());
                memberInfo.put("role", (ownerId != null && ownerId.equals(m.getUserId())) ? "群主" : "成员");
                memberInfo.put("joinTime", m.getCreatedTime());
                memberInfo.put("remarkNickName", m.getRemarkNickName());
                memberInfo.put("remarkGroupName", m.getRemarkGroupName());
                memberInfo.put("pinned", Boolean.TRUE.equals(m.getPinned()));
                memberInfo.put("pinnedTime", m.getPinnedTime());
                return memberInfo;
            }).collect(Collectors.toList());

            if (ownerId != null && (memberList.stream().noneMatch(mi -> ownerId.equals((Long) mi.get("id"))))) {
                User owner = userService.getById(ownerId);
                Map<String, Object> ownerInfo = new HashMap<>();
                ownerInfo.put("id", ownerId);
                ownerInfo.put("name", owner != null ? owner.getNickname() : null);
                ownerInfo.put("avatar", owner != null ? owner.getAvatar() : null);
                ownerInfo.put("role", "群主");
                ownerInfo.put("joinTime", group != null ? group.getCreatedTime() : null);
                memberList.add(0, ownerInfo);
            }

            System.out.println("返回群组成员列表，数量: " + memberList.size());
            return ResultUtils.success(memberList);

        } catch (Exception e) {
            System.err.println("获取群组成员列表异常: " + e.getMessage());
            e.printStackTrace();
            return ResultUtils.error(500, "获取群组成员列表失败");
        }
    }

    @ApiOperation("获取我在该群的成员信息")
    @GetMapping("/{groupId}/member/me")
    public Result<Map<String, Object>> getMyMemberInfo(@PathVariable Long groupId) {
        try {
            UserSession session = SessionContext.getSession();
            if (session == null) {
                return ResultUtils.error(401, "未登录");
            }
            LambdaQueryWrapper<GroupMember> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(GroupMember::getGroupId, groupId)
                   .eq(GroupMember::getUserId, session.getUserId())
                   .eq(GroupMember::getQuit, false);
            GroupMember me = groupMemberService.getOne(wrapper);
            if (me == null) {
                return ResultUtils.error(403, "您不在该群组中");
            }
            Map<String, Object> m = new HashMap<>();
            m.put("id", me.getUserId());
            m.put("userId", me.getUserId());
            m.put("userNickName", me.getUserNickName());
            m.put("remarkNickName", me.getRemarkNickName());
            m.put("remarkGroupName", me.getRemarkGroupName());
            m.put("pinned", Boolean.TRUE.equals(me.getPinned()));
            m.put("pinnedTime", me.getPinnedTime());
            m.put("clearTime", me.getClearTime());
            m.put("headImage", me.getHeadImage());
            return ResultUtils.success(m);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtils.error(500, "获取失败");
        }
    }

    @ApiOperation("创建群聊")
    @PostMapping("/groups")
    public Result<Map<String, Object>> createGroup(@RequestBody Map<String, Object> body) {
        try {
            UserSession userSession = SessionContext.getSession();
            if (userSession == null) {
                return ResultUtils.error(401, "用户未登录");
            }

            String groupName = body.get("name") != null ? body.get("name").toString()
                    : (body.get("groupName") != null ? body.get("groupName").toString() : null);
            if (groupName == null || groupName.trim().isEmpty()) {
                return ResultUtils.error(400, "群聊名称不能为空");
            }

            Long ownerId = body.get("ownerId") != null ? Long.valueOf(body.get("ownerId").toString()) : userSession.getUserId();

            Group group = new Group();
            group.setName(groupName);
            group.setOwnerId(ownerId);
            String avatar = body.get("avatar") != null ? body.get("avatar").toString()
                    : (body.get("headImage") != null ? body.get("headImage").toString() : null);
            if (avatar == null || avatar.trim().isEmpty()) {
                String seed = URLEncoder.encode(groupName, StandardCharsets.UTF_8);
                avatar = "https://api.dicebear.com/7.x/bottts/svg?seed=" + seed;
            }
            group.setHeadImage(avatar);
            group.setCreatedTime(LocalDateTime.now());
            group.setDissolve(false);
            group.setIsBanned(false);
            boolean created = groupService.save(group);
            if (!created) {
                return ResultUtils.error(500, "创建群聊失败");
            }

            List<?> mids = body.get("memberIds") instanceof List ? (List<?>) body.get("memberIds") : null;
            List<?> inviteeList = body.get("inviteeIds") instanceof List ? (List<?>) body.get("inviteeIds") : null;
            Map<Long, Boolean> uniq = new HashMap<>();
            if (mids != null) {
                for (Object o : mids) {
                    try {
                        Long uid = Long.valueOf(o.toString());
                        uniq.put(uid, true);
                    } catch (Exception ignored) {
                    }
                }
            }
            uniq.put(ownerId, true);

            int count = 0;
            for (Long uid : uniq.keySet()) {
                User u = userService.getById(uid);
                GroupMember gm = new GroupMember();
                gm.setGroupId(group.getId());
                gm.setUserId(uid);
                gm.setUserNickName(u != null ? u.getNickname() : null);
                gm.setHeadImage(u != null ? u.getAvatar() : null);
                gm.setRemarkGroupName(groupName);
                gm.setQuit(false);
                gm.setCreatedTime(LocalDateTime.now());
                boolean saved = groupMemberService.save(gm);
                if (saved) count++;
            }

            int inviteCreated = 0;
            if (inviteeList != null) {
                for (Object o : inviteeList) {
                    try {
                        Long inviteeId = Long.valueOf(o.toString());
                        // 已是成员直接跳过
                        LambdaQueryWrapper<GroupMember> memberWrapper = new LambdaQueryWrapper<>();
                        memberWrapper.eq(GroupMember::getGroupId, group.getId())
                                .eq(GroupMember::getUserId, inviteeId)
                                .eq(GroupMember::getQuit, false);
                        GroupMember exists = groupMemberService.getOne(memberWrapper);
                        if (exists != null) {
                            continue;
                        }
                        // 存在待处理邀请直接跳过
                        LambdaQueryWrapper<com.qz.sns.model.entity.GroupInvite> dupWrapper = new LambdaQueryWrapper<>();
                        dupWrapper.eq(com.qz.sns.model.entity.GroupInvite::getGroupId, group.getId())
                                .eq(com.qz.sns.model.entity.GroupInvite::getInviteeId, inviteeId)
                                .eq(com.qz.sns.model.entity.GroupInvite::getStatus, 0);
                        com.qz.sns.model.entity.GroupInvite dup = groupInviteService.getOne(dupWrapper);
                        if (dup != null) {
                            continue;
                        }
                        com.qz.sns.model.entity.GroupInvite invite = new com.qz.sns.model.entity.GroupInvite();
                        invite.setGroupId(group.getId());
                        invite.setInviterId(ownerId);
                        invite.setInviteeId(inviteeId);
                        invite.setMessage("邀请你加入群聊：" + groupName);
                        invite.setStatus(0);
                        invite.setCreatedTime(LocalDateTime.now());
                        boolean ok = groupInviteService.save(invite);
                        if (ok) inviteCreated++;
                    } catch (Exception ignored) {
                    }
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("id", group.getId());
            result.put("name", group.getName());
            result.put("avatar", group.getHeadImage());
            result.put("memberCount", count);
            result.put("createdTime", group.getCreatedTime());
            result.put("unread", 0);
            result.put("inviteCreated", inviteCreated);
            return ResultUtils.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtils.error(500, "创建群聊失败");
        }
    }

    @ApiOperation("加入群聊")
    @PostMapping("/{groupId}/join")
    public Result<Map<String, Object>> joinGroup(@PathVariable Long groupId, @RequestBody Map<String, Object> params) {
        try {
            UserSession userSession = SessionContext.getSession();
            if (userSession == null) {
                return ResultUtils.error(401, "用户未登录");
            }
            Long userId = params.get("userId") != null ? Long.valueOf(params.get("userId").toString()) : userSession.getUserId();

            Group group = groupService.getById(groupId);
            if (group == null) {
                return ResultUtils.error(404, "群组不存在");
            }

            LambdaQueryWrapper<GroupMember> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(GroupMember::getGroupId, groupId)
                    .eq(GroupMember::getUserId, userId)
                    .eq(GroupMember::getQuit, false);
            GroupMember exists = groupMemberService.getOne(wrapper);
            if (exists != null) {
                Map<String, Object> result = new HashMap<>();
                result.put("groupId", groupId);
                result.put("userId", userId);
                return ResultUtils.success(result);
            }

            User user = userService.getById(userId);
            GroupMember gm = new GroupMember();
            gm.setGroupId(groupId);
            gm.setUserId(userId);
            gm.setUserNickName(user != null ? user.getNickname() : null);
            gm.setHeadImage(user != null ? user.getAvatar() : null);
            gm.setRemarkGroupName(group.getName());
            gm.setQuit(false);
            gm.setCreatedTime(LocalDateTime.now());
            boolean saved = groupMemberService.save(gm);
            if (!saved) {
                return ResultUtils.error(500, "加入群聊失败");
            }

            Map<String, Object> result = new HashMap<>();
            result.put("groupId", groupId);
            result.put("userId", userId);
            return ResultUtils.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtils.error(500, "加入群聊失败");
        }
    }
}
