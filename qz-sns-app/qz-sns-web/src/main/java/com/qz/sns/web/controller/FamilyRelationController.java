package com.qz.sns.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qz.sns.model.entity.FamilyRelation;
import com.qz.sns.model.entity.User;
import com.qz.sns.sv.result.Result;
import com.qz.sns.sv.result.ResultUtils;
import com.qz.sns.sv.service.IFamilyRelationService;
import com.qz.sns.sv.service.IUserService;
import com.qz.sns.sv.session.SessionContext;
import com.qz.sns.sv.session.UserSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 家庭关系管理 前端控制器
 * </p>
 *
 * @author 李彬
 * @since 2025-02-18
 */
@Slf4j
@RestController
@RequestMapping("/api/family")
@RequiredArgsConstructor
public class FamilyRelationController {

    private final IFamilyRelationService familyRelationService;
    private final IUserService userService;

    /**
     * 绑定孩子 - 根据用户名绑定
     */
    @PostMapping("/bind-child")
    public Result<String> bindChild(@RequestParam @NotNull String childUsername, 
                                   @RequestParam @NotNull String relationType) {
        log.info("开始绑定家庭成员操作，成员用户名：{}，关系类型：{}", childUsername, relationType);
        
        try {
            // 获取当前登录用户
            UserSession currentUser = SessionContext.getSession();
            if (currentUser == null) {
                log.error("用户未登录");
                return ResultUtils.error(401, "用户未登录");
            }
            
            Long parentId = currentUser.getUserId();
            log.info("当前家长用户ID：{}", parentId);

            // 验证当前用户是否为家长
            User parent = userService.getUserById(parentId);
            if (parent == null || parent.getRole() != 1) {
                log.error("当前用户不是家长角色，用户ID：{}，角色：{}", parentId, parent != null ? parent.getRole() : "null");
                return ResultUtils.error(403, "只有家长可以绑定孩子");
            }

            // 根据用户名查找要绑定的用户
            User relativeUser = userService.getUserByUsername(childUsername);
            if (relativeUser == null) {
                log.error("未找到用户名为{}的用户", childUsername);
                return ResultUtils.error(404, "未找到该用户");
            }

            // 验证关系类型是否有效
            if (!isValidRelationType(relationType)) {
                log.error("无效的关系类型：{}", relationType);
                return ResultUtils.error(400, "关系类型无效");
            }

            // 检查是否已经绑定过（检查双向关系）
            QueryWrapper<FamilyRelation> checkWrapper = new QueryWrapper<>();
            checkWrapper.and(wrapper -> 
                wrapper.and(subWrapper -> 
                    subWrapper.eq("user_id", parentId).eq("relative_id", relativeUser.getId())
                ).or(subWrapper -> 
                    subWrapper.eq("user_id", relativeUser.getId()).eq("relative_id", parentId)
                )
            );
            List<FamilyRelation> existingRelations = familyRelationService.list(checkWrapper);
            
            if (!existingRelations.isEmpty()) {
                log.error("已经绑定过该用户，当前用户ID：{}，目标用户ID：{}", parentId, relativeUser.getId());
                return ResultUtils.error(400, "已经绑定过该用户");
            }

            // 创建单向家庭关系记录
            // user_id = 发起绑定的人（当前用户）
            // relative_id = 被绑定的人（目标用户）
            LocalDateTime now = LocalDateTime.now();
            
            // 获取反向关系类型
            String reverseRelationType = getReverseRelationType(relationType);
            
            log.info("确定关系类型 - 发起者{}->被绑定者{}：{}，反向关系：{}", 
                parentId, relativeUser.getId(), relationType, reverseRelationType);
            
            // 创建单条关系记录，同时存储双向关系类型
            FamilyRelation relation = new FamilyRelation();
            relation.setUserId(parentId); // 发起者
            relation.setRelativeId(relativeUser.getId()); // 被绑定者
            relation.setRelationType(relationType + "," + reverseRelationType); // 存储双向关系，用逗号分隔
            relation.setCreatedTime(now);

            // 保存关系记录
            boolean saved = familyRelationService.save(relation);
            
            if (saved) {
                log.info("绑定家庭成员成功，发起者ID：{}，被绑定者ID：{}，关系类型：{} <-> {}", 
                    parentId, relativeUser.getId(), relationType, reverseRelationType);
                return ResultUtils.success("绑定成功");
            } else {
                log.error("绑定家庭成员失败，保存数据库记录失败");
                return ResultUtils.error(500, "绑定失败");
            }

        } catch (Exception e) {
            log.error("绑定孩子操作异常：{}", e.getMessage(), e);
            return ResultUtils.error(500, "绑定失败：" + e.getMessage());
        }
    }

    /**
     * 获取当前用户的家庭成员列表
     */
    @GetMapping("/members")
    public Result<Map<String, Object>> getFamilyMembers() {
        log.info("开始获取家庭成员列表");
        
        try {
            // 获取当前登录用户
            UserSession currentUser = SessionContext.getSession();
            if (currentUser == null) {
                log.error("用户未登录");
                return ResultUtils.error(401, "用户未登录");
            }
            
            Long userId = currentUser.getUserId();
            log.info("当前用户ID：{}", userId);

            // 获取用户信息
            User user = userService.getUserById(userId);
            if (user == null) {
                log.error("用户不存在，用户ID：{}", userId);
                return ResultUtils.error(404, "用户不存在");
            }

            Map<String, Object> result = new HashMap<>();
            result.put("currentUser", user);

            // 获取所有家庭关系（使用OR条件查询双向关系）
            QueryWrapper<FamilyRelation> queryWrapper = new QueryWrapper<>();
            queryWrapper.and(wrapper -> 
                wrapper.eq("user_id", userId).or().eq("relative_id", userId)
            );
            List<FamilyRelation> relations = familyRelationService.list(queryWrapper);
            
            log.info("用户ID：{}的家庭关系记录数：{}", userId, relations.size());
            
            // 统计关注数和粉丝数
            // 关注数：我主动绑定的人（user_id = 我）
            QueryWrapper<FamilyRelation> followingWrapper = new QueryWrapper<>();
            followingWrapper.eq("user_id", userId);
            long followingCount = familyRelationService.count(followingWrapper);
            
            // 粉丝数：绑定我的人（relative_id = 我）
            QueryWrapper<FamilyRelation> followersWrapper = new QueryWrapper<>();
            followersWrapper.eq("relative_id", userId);
            long followersCount = familyRelationService.count(followersWrapper);
            
            log.info("用户ID：{}的关注数：{}，粉丝数：{}", userId, followingCount, followersCount);
            
            // 转换为家庭成员信息列表
            List<Map<String, Object>> allMembers = relations.stream()
                .map(relation -> {
                    // 确定对方用户ID：如果当前用户是user_id，则对方是relative_id，反之亦然
                    Long relativeUserId = relation.getUserId().equals(userId) 
                        ? relation.getRelativeId() 
                        : relation.getUserId();
                    
                    User relative = userService.getUserById(relativeUserId);
                    if (relative == null) {
                        log.warn("未找到家庭成员用户，ID：{}", relativeUserId);
                        return null;
                    }
                    
                    // 解析关系类型：格式为 "关系1,关系2"
                    String relationTypeStr = relation.getRelationType();
                    String[] relationTypes = relationTypeStr.split(",");
                    
                    // 确定当前用户的关系类型
                    String currentUserRelationType;
                    if (relation.getUserId().equals(userId)) {
                        // 当前用户是user_id，使用第一个关系类型
                        currentUserRelationType = relationTypes.length > 0 ? relationTypes[0] : relationTypeStr;
                    } else {
                        // 当前用户是relative_id，使用第二个关系类型
                        currentUserRelationType = relationTypes.length > 1 ? relationTypes[1] : relationTypeStr;
                    }
                    
                    Map<String, Object> memberInfo = new HashMap<>();
                    memberInfo.put("id", relative.getId());
                    memberInfo.put("nickname", relative.getNickname());
                    memberInfo.put("username", relative.getUsername());
                    memberInfo.put("avatar", relative.getAvatar());
                    memberInfo.put("avatarThumb", relative.getAvatarThumb());
                    memberInfo.put("bindTime", relation.getCreatedTime());
                    memberInfo.put("relationType", currentUserRelationType);
                    memberInfo.put("sex", relative.getSex());
                    memberInfo.put("role", relative.getRole());
                    
                    log.info("家庭成员 - 昵称：{}，用户名：{}，关系类型：{}", 
                        relative.getNickname(), relative.getUsername(), currentUserRelationType);
                    
                    return memberInfo;
                })
                .filter(info -> info != null)
                .collect(Collectors.toList());
            
            // 按关系类型分类家庭成员
            List<Map<String, Object>> children = allMembers.stream()
                .filter(m -> {
                    String relationType = (String) m.get("relationType");
                    return relationType != null && (
                        relationType.contains("子") && (relationType.startsWith("父") || relationType.startsWith("母")) ||
                        relationType.contains("女") && (relationType.startsWith("父") || relationType.startsWith("母"))
                    );
                })
                .collect(Collectors.toList());
            
            List<Map<String, Object>> parents = allMembers.stream()
                .filter(m -> {
                    String relationType = (String) m.get("relationType");
                    return relationType != null && (
                        relationType.startsWith("子") && (relationType.contains("父") || relationType.contains("母")) ||
                        relationType.startsWith("女") && (relationType.contains("父") || relationType.contains("母"))
                    );
                })
                .collect(Collectors.toList());
            
            Map<String, Object> spouse = allMembers.stream()
                .filter(m -> "夫妻".equals(m.get("relationType")))
                .findFirst()
                .orElse(null);
            
            List<Map<String, Object>> siblings = allMembers.stream()
                .filter(m -> {
                    String relationType = (String) m.get("relationType");
                    return relationType != null && (
                        relationType.contains("兄") || relationType.contains("弟") ||
                        relationType.contains("姐") || relationType.contains("妹")
                    ) && !relationType.contains("父") && !relationType.contains("母");
                })
                .collect(Collectors.toList());
            
            List<Map<String, Object>> grandparents = allMembers.stream()
                .filter(m -> {
                    String relationType = (String) m.get("relationType");
                    return relationType != null && (
                        relationType.startsWith("孙") || relationType.startsWith("女")
                    ) && (relationType.contains("祖") || relationType.contains("奶") || relationType.contains("外"));
                })
                .collect(Collectors.toList());
            
            List<Map<String, Object>> grandchildren = allMembers.stream()
                .filter(m -> {
                    String relationType = (String) m.get("relationType");
                    return relationType != null && (
                        relationType.startsWith("祖") || relationType.startsWith("奶") || relationType.startsWith("外")
                    ) && (relationType.contains("孙") || relationType.contains("女"));
                })
                .collect(Collectors.toList());
            
            List<Map<String, Object>> others = allMembers.stream()
                .filter(m -> {
                    String relationType = (String) m.get("relationType");
                    return relationType != null && (
                        relationType.contains("叔") || relationType.contains("婶") ||
                        relationType.contains("侄") || relationType.contains("舅") ||
                        relationType.contains("甥") || relationType.equals("其他亲属")
                    );
                })
                .collect(Collectors.toList());
            
            // 添加所有分类到结果中
            result.put("children", children);
            result.put("parents", parents);
            if (spouse != null) {
                result.put("spouse", spouse);
            }
            result.put("siblings", siblings);
            result.put("grandparents", grandparents);
            result.put("grandchildren", grandchildren);
            result.put("others", others);
            
            // 添加统计数据
            Map<String, Object> stats = new HashMap<>();
            stats.put("followingCount", followingCount); // 关注数（我主动绑定的人）
            stats.put("followersCount", followersCount); // 粉丝数（绑定我的人）
            result.put("stats", stats);
            
            log.info("获取家庭成员成功 - 子女：{}，父母：{}，配偶：{}，兄弟姐妹：{}，祖辈：{}，孙辈：{}，其他：{}，关注数：{}，粉丝数：{}", 
                children.size(), parents.size(), spouse != null ? 1 : 0, 
                siblings.size(), grandparents.size(), grandchildren.size(), others.size(),
                followingCount, followersCount);

            return ResultUtils.success(result);

        } catch (Exception e) {
            log.error("获取家庭成员列表异常：{}", e.getMessage(), e);
            return ResultUtils.error(500, "获取家庭成员失败：" + e.getMessage());
        }
    }

    /**
     * 解除绑定关系
     */
    @DeleteMapping("/unbind/{relativeId}")
    public Result<String> unbindRelative(@PathVariable @NotNull Long relativeId) {
        log.info("开始解除绑定操作，相关用户ID：{}", relativeId);
        
        try {
            // 获取当前登录用户
            UserSession currentUser = SessionContext.getSession();
            if (currentUser == null) {
                log.error("用户未登录");
                return ResultUtils.error(401, "用户未登录");
            }
            
            Long userId = currentUser.getUserId();
            log.info("当前用户ID：{}", userId);

            // 查找家庭关系记录（单向存储，只有一条记录）
            QueryWrapper<FamilyRelation> queryWrapper = new QueryWrapper<>();
            queryWrapper.and(wrapper -> 
                wrapper.and(subWrapper -> 
                    subWrapper.eq("user_id", userId).eq("relative_id", relativeId)
                ).or(subWrapper -> 
                    subWrapper.eq("user_id", relativeId).eq("relative_id", userId)
                )
            );
            
            FamilyRelation relation = familyRelationService.getOne(queryWrapper);
            if (relation == null) {
                log.error("未找到绑定关系，用户ID：{}，相关用户ID：{}", userId, relativeId);
                return ResultUtils.error(404, "未找到绑定关系");
            }

            // 删除关系记录
            boolean removed = familyRelationService.removeById(relation.getId());
            
            if (removed) {
                log.info("解除绑定成功，关系ID：{}，用户ID：{} <-> {}", 
                    relation.getId(), userId, relativeId);
                return ResultUtils.success("解除绑定成功");
            } else {
                log.error("解除绑定失败，删除关系记录失败，关系ID：{}", relation.getId());
                return ResultUtils.error(500, "解除绑定失败");
            }

        } catch (Exception e) {
            log.error("解除绑定操作异常：{}", e.getMessage(), e);
            return ResultUtils.error(500, "解除绑定失败：" + e.getMessage());
        }
    }

    /**
     * 验证关系类型是否有效
     * @param relationType 关系类型
     * @return 是否有效
     */
    private boolean isValidRelationType(String relationType) {
        return getValidRelationTypes().contains(relationType);
    }

    /**
     * 获取所有有效的关系类型
     * @return 有效关系类型列表
     */
    private java.util.Set<String> getValidRelationTypes() {
        return java.util.Set.of(
            // 父母子女关系
            "父子", "父女", "母子", "母女",
            "子父", "子母", "女父", "女母",
            
            // 夫妻关系
            "夫妻",
            
            // 兄弟姐妹关系
            "兄弟", "兄妹", "姐弟", "姐妹",
            "兄兄", "弟兄", "妹兄", "妹妹",
            
            // 祖孙关系
            "祖孙", "祖女", "奶孙", "奶女",
            "外祖孙", "外祖女", "外奶孙", "外奶女",
            "孙祖", "孙奶", "女祖", "女奶",
            "孙外祖", "孙外奶", "女外祖", "女外奶",
            
            // 叔侄关系
            "叔侄", "叔女", "婶侄", "婶女",
            "侄叔", "侄婶", "女叔", "女婶",
            
            // 舅甥关系
            "舅甥", "舅女", "舅妈甥", "舅妈女",
            "甥舅", "甥舅妈", "女舅", "女舅妈",
            
            // 其他关系
            "其他亲属"
        );
    }

    /**
     * 获取反向关系类型
     * @param relationType 原关系类型
     * @return 反向关系类型
     */
    private String getReverseRelationType(String relationType) {
        java.util.Map<String, String> reverseMap = java.util.Map.ofEntries(
            // 父母子女关系
            java.util.Map.entry("父子", "子父"),
            java.util.Map.entry("父女", "女父"),
            java.util.Map.entry("母子", "子母"),
            java.util.Map.entry("母女", "女母"),
            java.util.Map.entry("子父", "父子"),
            java.util.Map.entry("子母", "母子"),
            java.util.Map.entry("女父", "父女"),
            java.util.Map.entry("女母", "母女"),
            
            // 夫妻关系
            java.util.Map.entry("夫妻", "夫妻"),
            
            // 兄弟姐妹关系
            java.util.Map.entry("兄弟", "兄弟"),
            java.util.Map.entry("兄妹", "妹兄"),
            java.util.Map.entry("姐弟", "弟兄"),
            java.util.Map.entry("姐妹", "姐妹"),
            java.util.Map.entry("妹兄", "兄妹"),
            java.util.Map.entry("弟兄", "姐弟"),
            
            // 祖孙关系
            java.util.Map.entry("祖孙", "孙祖"),
            java.util.Map.entry("祖女", "女祖"),
            java.util.Map.entry("奶孙", "孙奶"),
            java.util.Map.entry("奶女", "女奶"),
            java.util.Map.entry("孙祖", "祖孙"),
            java.util.Map.entry("孙奶", "奶孙"),
            java.util.Map.entry("女祖", "祖女"),
            java.util.Map.entry("女奶", "奶女"),
            
            // 外祖孙关系
            java.util.Map.entry("外祖孙", "孙外祖"),
            java.util.Map.entry("外祖女", "女外祖"),
            java.util.Map.entry("外奶孙", "孙外奶"),
            java.util.Map.entry("外奶女", "女外奶"),
            java.util.Map.entry("孙外祖", "外祖孙"),
            java.util.Map.entry("孙外奶", "外奶孙"),
            java.util.Map.entry("女外祖", "外祖女"),
            java.util.Map.entry("女外奶", "外奶女"),
            
            // 叔侄关系
            java.util.Map.entry("叔侄", "侄叔"),
            java.util.Map.entry("叔女", "女叔"),
            java.util.Map.entry("婶侄", "侄婶"),
            java.util.Map.entry("婶女", "女婶"),
            java.util.Map.entry("侄叔", "叔侄"),
            java.util.Map.entry("侄婶", "婶侄"),
            java.util.Map.entry("女叔", "叔女"),
            java.util.Map.entry("女婶", "婶女"),
            
            // 舅甥关系
            java.util.Map.entry("舅甥", "甥舅"),
            java.util.Map.entry("舅女", "女舅"),
            java.util.Map.entry("舅妈甥", "甥舅妈"),
            java.util.Map.entry("舅妈女", "女舅妈"),
            java.util.Map.entry("甥舅", "舅甥"),
            java.util.Map.entry("甥舅妈", "舅妈甥"),
            java.util.Map.entry("女舅", "舅女"),
            java.util.Map.entry("女舅妈", "舅妈女"),
            
            // 其他关系
            java.util.Map.entry("其他亲属", "其他亲属")
        );
        
        return reverseMap.getOrDefault(relationType, "其他亲属");
    }
} 