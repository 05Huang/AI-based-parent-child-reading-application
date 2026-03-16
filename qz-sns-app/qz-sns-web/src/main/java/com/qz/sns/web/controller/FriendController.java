package com.qz.sns.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qz.sns.model.entity.Friend;
import com.qz.sns.model.entity.User;
import com.qz.sns.sv.result.Result;
import com.qz.sns.sv.result.ResultUtils;
import com.qz.sns.sv.service.IFriendService;
import com.qz.sns.sv.service.IUserService;
import com.qz.sns.sv.session.SessionContext;
import com.qz.sns.sv.session.UserSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/friend")
@RequiredArgsConstructor
public class FriendController {

    private final IFriendService friendService;
    private final IUserService userService;

    @PostMapping("/apply")
    public Result<Map<String, Object>> apply(@RequestBody Map<String, Object> params) {
        UserSession userSession = SessionContext.getSession();
        if (userSession == null) {
            return ResultUtils.error(401, "用户未登录");
        }

        Object friendIdObj = params.get("friendId");
        if (friendIdObj == null) {
            return ResultUtils.error(400, "缺少好友ID");
        }

        Long userId = userSession.getUserId();
        Long friendId = Long.valueOf(friendIdObj.toString());
        if (userId.equals(friendId)) {
            return ResultUtils.error(400, "不能添加自己为好友");
        }

        LambdaQueryWrapper<Friend> existsWrapper = new LambdaQueryWrapper<>();
        existsWrapper.eq(Friend::getUserId, userId).eq(Friend::getFriendId, friendId);
        Friend exists = friendService.getOne(existsWrapper);
        if (exists != null) {
            return ResultUtils.error(400, "已是好友");
        }

        User friendUser = userService.getById(friendId);
        if (friendUser == null) {
            return ResultUtils.error(404, "目标用户不存在");
        }

        Friend friend = new Friend();
        friend.setUserId(userId);
        friend.setFriendId(friendId);
        friend.setFriendNickName(friendUser.getNickname() != null ? friendUser.getNickname() : friendUser.getUsername());
        friend.setFriendHeadImage(friendUser.getAvatar() != null ? friendUser.getAvatar() : friendUser.getAvatarThumb());
        friend.setCreatedTime(LocalDateTime.now());
        friendService.save(friend);

        Map<String, Object> result = new HashMap<>();
        result.put("id", friend.getId());
        result.put("userId", userId);
        result.put("friendId", friendId);
        return ResultUtils.success(result, "好友申请已提交");
    }

    @GetMapping("/requests")
    public Result<List<Map<String, Object>>> getRequests() {
        UserSession userSession = SessionContext.getSession();
        if (userSession == null) {
            return ResultUtils.error(401, "用户未登录");
        }

        Long userId = userSession.getUserId();
        LambdaQueryWrapper<Friend> incomingWrapper = new LambdaQueryWrapper<>();
        incomingWrapper.eq(Friend::getFriendId, userId);
        List<Friend> incoming = friendService.list(incomingWrapper);

        List<Map<String, Object>> list = new ArrayList<>();
        for (Friend f : incoming) {
            User applicant = userService.getById(f.getUserId());
            int status = 0;
            LambdaQueryWrapper<Friend> reciprocalWrapper = new LambdaQueryWrapper<>();
            reciprocalWrapper.eq(Friend::getUserId, userId).eq(Friend::getFriendId, f.getUserId());
            Friend reciprocal = friendService.getOne(reciprocalWrapper);
            if (reciprocal != null) {
                status = 1;
            }

            Map<String, Object> item = new HashMap<>();
            item.put("id", f.getId());
            item.put("userId", f.getUserId());
            item.put("nickname", applicant != null ? applicant.getNickname() : null);
            item.put("username", applicant != null ? applicant.getUsername() : null);
            item.put("avatar", applicant != null ? (applicant.getAvatar() != null ? applicant.getAvatar() : applicant.getAvatarThumb()) : null);
            item.put("message", "申请添加你为好友");
            item.put("createdTime", f.getCreatedTime());
            item.put("status", status);
            list.add(item);
        }
        return ResultUtils.success(list);
    }

    @PostMapping("/handle")
    public Result<Map<String, Object>> handle(@RequestBody Map<String, Object> params) {
        UserSession userSession = SessionContext.getSession();
        if (userSession == null) {
            return ResultUtils.error(401, "用户未登录");
        }
        Object requestIdObj = params.get("requestId");
        Object statusObj = params.get("status");
        if (requestIdObj == null || statusObj == null) {
            return ResultUtils.error(400, "缺少参数");
        }
        Long requestId = Long.valueOf(requestIdObj.toString());
        Integer status = Integer.valueOf(statusObj.toString());

        Friend request = friendService.getById(requestId);
        if (request == null) {
            return ResultUtils.error(404, "申请不存在");
        }
        Long userId = userSession.getUserId();
        if (!userId.equals(request.getFriendId())) {
            return ResultUtils.error(403, "无权限处理该申请");
        }

        Map<String, Object> result = new HashMap<>();
        if (status == 1) {
            LambdaQueryWrapper<Friend> reciprocalWrapper = new LambdaQueryWrapper<>();
            reciprocalWrapper.eq(Friend::getUserId, userId).eq(Friend::getFriendId, request.getUserId());
            Friend reciprocal = friendService.getOne(reciprocalWrapper);
            if (reciprocal == null) {
                User applicant = userService.getById(request.getUserId());
                Friend newFriend = new Friend();
                newFriend.setUserId(userId);
                newFriend.setFriendId(request.getUserId());
                newFriend.setFriendNickName(applicant != null && applicant.getNickname() != null ? applicant.getNickname() : (applicant != null ? applicant.getUsername() : null));
                newFriend.setFriendHeadImage(applicant != null ? (applicant.getAvatar() != null ? applicant.getAvatar() : applicant.getAvatarThumb()) : null);
                newFriend.setCreatedTime(LocalDateTime.now());
                friendService.save(newFriend);
                result.put("acceptedId", newFriend.getId());
            }
            return ResultUtils.success(result, "已同意");
        } else if (status == 2) {
            friendService.removeById(requestId);
            return ResultUtils.success(result, "已拒绝");
        } else {
            return ResultUtils.error(400, "状态值无效");
        }
    }

    @GetMapping("/{friendId}/me")
    public Result<Map<String, Object>> getMyFriendInfo(@PathVariable Long friendId) {
        UserSession session = SessionContext.getSession();
        if (session == null) {
            return ResultUtils.error(401, "用户未登录");
        }
        LambdaQueryWrapper<Friend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Friend::getUserId, session.getUserId()).eq(Friend::getFriendId, friendId);
        Friend me = friendService.getOne(wrapper);
        if (me == null) {
            return ResultUtils.error(404, "好友关系不存在");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("id", me.getId());
        data.put("userId", me.getUserId());
        data.put("friendId", me.getFriendId());
        data.put("friendNickName", me.getFriendNickName());
        data.put("friendHeadImage", me.getFriendHeadImage());
        data.put("pinned", me.getPinned() != null ? me.getPinned() : false);
        data.put("remark", me.getRemark() != null ? me.getRemark() : "");
        data.put("clearTime", me.getClearTime());
        data.put("createdTime", me.getCreatedTime());
        return ResultUtils.success(data);
    }

    @GetMapping("/{friendId}/status")
    public Result<Map<String, Object>> getFollowStatus(@PathVariable Long friendId) {
        UserSession session = SessionContext.getSession();
        if (session == null) {
            return ResultUtils.error(401, "用户未登录");
        }
        if (friendId == null || session.getUserId().equals(friendId)) {
            return ResultUtils.success(Map.of("followed", false));
        }
        LambdaQueryWrapper<Friend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Friend::getUserId, session.getUserId()).eq(Friend::getFriendId, friendId);
        Friend me = friendService.getOne(wrapper);
        return ResultUtils.success(Map.of("followed", me != null));
    }

    @DeleteMapping("/{friendId}/unfollow")
    public Result<Map<String, Object>> unfollow(@PathVariable Long friendId) {
        try {
            UserSession session = SessionContext.getSession();
            if (session == null) {
                return ResultUtils.error(401, "用户未登录");
            }
            if (friendId == null || session.getUserId().equals(friendId)) {
                return ResultUtils.success(Map.of("deleted", false));
            }
            LambdaQueryWrapper<Friend> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Friend::getUserId, session.getUserId()).eq(Friend::getFriendId, friendId);
            boolean removed = friendService.remove(wrapper);
            return ResultUtils.success(Map.of("deleted", removed));
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtils.error(500, "取消关注失败");
        }
    }

    @PostMapping("/{friendId}/pin")
    public Result<Map<String, Object>> pinFriend(@PathVariable Long friendId, @RequestBody Map<String, Object> body) {
        try {
            UserSession session = SessionContext.getSession();
            if (session == null) {
                return ResultUtils.error(401, "用户未登录");
            }
            boolean pinned = body.get("pinned") != null && Boolean.parseBoolean(body.get("pinned").toString());
            LambdaQueryWrapper<Friend> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Friend::getUserId, session.getUserId()).eq(Friend::getFriendId, friendId);
            Friend me = friendService.getOne(wrapper);
            if (me == null) {
                return ResultUtils.error(404, "好友关系不存在");
            }
            me.setPinned(pinned);
            boolean ok = friendService.updateById(me);
            if (!ok) {
                return ResultUtils.error(500, "操作失败");
            }
            return ResultUtils.success(Map.of("pinned", pinned));
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtils.error(500, "操作失败");
        }
    }

    @PutMapping("/{friendId}/remark")
    public Result<Map<String, Object>> updateFriendRemark(@PathVariable Long friendId, @RequestBody Map<String, Object> body) {
        try {
            UserSession session = SessionContext.getSession();
            if (session == null) {
                return ResultUtils.error(401, "用户未登录");
            }
            String remark = body.get("remark") != null ? body.get("remark").toString() : "";
            LambdaQueryWrapper<Friend> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Friend::getUserId, session.getUserId()).eq(Friend::getFriendId, friendId);
            Friend me = friendService.getOne(wrapper);
            if (me == null) {
                return ResultUtils.error(404, "好友关系不存在");
            }
            me.setRemark(remark);
            boolean ok = friendService.updateById(me);
            if (!ok) {
                return ResultUtils.error(500, "保存失败");
            }
            return ResultUtils.success(Map.of("remark", remark));
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtils.error(500, "保存失败");
        }
    }

    @PostMapping("/{friendId}/clear")
    public Result<Map<String, Object>> clearPrivateMessages(@PathVariable Long friendId) {
        try {
            UserSession session = SessionContext.getSession();
            if (session == null) {
                return ResultUtils.error(401, "用户未登录");
            }
            LambdaQueryWrapper<Friend> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Friend::getUserId, session.getUserId()).eq(Friend::getFriendId, friendId);
            Friend me = friendService.getOne(wrapper);
            if (me == null) {
                return ResultUtils.error(404, "好友关系不存在");
            }
            me.setClearTime(LocalDateTime.now());
            boolean ok = friendService.updateById(me);
            if (!ok) {
                return ResultUtils.error(500, "清除失败");
            }
            return ResultUtils.success(Map.of("cleared", true));
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtils.error(500, "清除失败");
        }
    }

    @DeleteMapping("/{friendId}")
    public Result<Map<String, Object>> deleteFriend(@PathVariable Long friendId) {
        try {
            UserSession session = SessionContext.getSession();
            if (session == null) {
                return ResultUtils.error(401, "用户未登录");
            }
            Long userId = session.getUserId();
            LambdaQueryWrapper<Friend> w1 = new LambdaQueryWrapper<>();
            w1.eq(Friend::getUserId, userId).eq(Friend::getFriendId, friendId);
            LambdaQueryWrapper<Friend> w2 = new LambdaQueryWrapper<>();
            w2.eq(Friend::getUserId, friendId).eq(Friend::getFriendId, userId);
            friendService.remove(w1);
            friendService.remove(w2);
            return ResultUtils.success(Map.of("deleted", true), "已删除好友");
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtils.error(500, "删除失败");
        }
    }
}
