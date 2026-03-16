package com.qz.sns.web.controller;

import com.qz.sns.model.entity.Complaint;
import com.qz.sns.sv.result.Result;
import com.qz.sns.sv.result.ResultUtils;
import com.qz.sns.sv.service.IComplaintService;
import com.qz.sns.sv.service.IGroupService;
import com.qz.sns.sv.service.IUserService;
import com.qz.sns.sv.session.SessionContext;
import com.qz.sns.sv.session.UserSession;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

@RestController
@RequestMapping("/api/complaint")
@Api(tags = "投诉管理")
public class ComplaintController {

    @Autowired
    private IComplaintService complaintService;
    @Autowired
    private IUserService userService;
    @Autowired
    private IGroupService groupService;

    @ApiOperation("提交投诉")
    @PostMapping
    public Result<Map<String, Object>> submitComplaint(@RequestBody Map<String, Object> body) {
        try {
            UserSession session = SessionContext.getSession();
            if (session == null) {
                return ResultUtils.error(401, "未登录");
            }
            String content = body.get("content") != null ? body.get("content").toString() : null;
            if (content == null || content.trim().length() < 10) {
                return ResultUtils.error(400, "投诉内容过短");
            }
            Complaint c = new Complaint();
            c.setComplainantUserId(session.getUserId());
            if (body.get("groupId") != null) {
                c.setGroupId(Long.valueOf(body.get("groupId").toString()));
            }
            c.setTargetType(body.get("targetType") != null ? Integer.valueOf(body.get("targetType").toString()) : 1);
            if (body.get("targetId") != null) {
                c.setTargetId(Long.valueOf(body.get("targetId").toString()));
            }
            c.setContent(content.trim());
            c.setStatus(0);
            c.setCreatedTime(LocalDateTime.now());
            boolean ok = complaintService.save(c);
            if (!ok) {
                return ResultUtils.error(500, "提交失败");
            }
            return ResultUtils.success(Map.of("id", c.getId()), "已提交");
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtils.error(500, "提交失败");
        }
    }

    @ApiOperation("获取我的投诉记录")
    @GetMapping("/my")
    public Result<Map<String, Object>> getMyComplaints(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        try {
            UserSession session = SessionContext.getSession();
            if (session == null) {
                return ResultUtils.error(401, "未登录");
            }
            Page<Complaint> p = new Page<>(page, size);
            LambdaQueryWrapper<Complaint> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Complaint::getComplainantUserId, session.getUserId())
                   .orderByDesc(Complaint::getCreatedTime);
            IPage<Complaint> resultPage = complaintService.page(p, wrapper);
            List<Map<String, Object>> list = resultPage.getRecords().stream().map(c -> {
                Map<String, Object> m = new HashMap<>();
                m.put("id", c.getId());
                m.put("content", c.getContent());
                m.put("status", c.getStatus());
                m.put("createdTime", c.getCreatedTime());
                m.put("targetType", c.getTargetType());
                m.put("targetId", c.getTargetId());
                if (c.getGroupId() != null) {
                    var g = groupService.getById(c.getGroupId());
                    m.put("groupName", g != null ? g.getName() : null);
                }
                return m;
            }).collect(Collectors.toList());
            Map<String, Object> data = new HashMap<>();
            data.put("list", list);
            data.put("total", resultPage.getTotal());
            data.put("page", resultPage.getCurrent());
            data.put("size", resultPage.getSize());
            return ResultUtils.success(data);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtils.error(500, "获取失败");
        }
    }

    @ApiOperation("获取投诉列表（管理员）")
    @GetMapping("/list")
    public Result<Map<String, Object>> listComplaints(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer targetType,
            @RequestParam(required = false) Long groupId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        try {
            UserSession session = SessionContext.getSession();
            if (session == null) {
                return ResultUtils.error(401, "未登录");
            }
            var user = userService.getById(session.getUserId());
            if (user == null || user.getUserType() == null || user.getUserType() != 2) {
                return ResultUtils.error(403, "只有管理员可查看");
            }
            Page<Complaint> p = new Page<>(page, size);
            LambdaQueryWrapper<Complaint> wrapper = new LambdaQueryWrapper<>();
            if (status != null) {
                wrapper.eq(Complaint::getStatus, status);
            }
            if (targetType != null) {
                wrapper.eq(Complaint::getTargetType, targetType);
            }
            if (groupId != null) {
                wrapper.eq(Complaint::getGroupId, groupId);
            }
            wrapper.orderByDesc(Complaint::getCreatedTime);
            IPage<Complaint> resultPage = complaintService.page(p, wrapper);
            List<Map<String, Object>> list = resultPage.getRecords().stream().map(c -> {
                Map<String, Object> m = new HashMap<>();
                m.put("id", c.getId());
                m.put("complainantUserId", c.getComplainantUserId());
                var u = userService.getById(c.getComplainantUserId());
                m.put("complainantName", u != null ? u.getNickname() : null);
                m.put("content", c.getContent());
                m.put("status", c.getStatus());
                m.put("createdTime", c.getCreatedTime());
                m.put("handledBy", c.getHandledBy());
                m.put("handledTime", c.getHandledTime());
                m.put("result", c.getResult());
                m.put("targetType", c.getTargetType());
                m.put("targetId", c.getTargetId());
                if (c.getGroupId() != null) {
                    var g = groupService.getById(c.getGroupId());
                    m.put("groupId", c.getGroupId());
                    m.put("groupName", g != null ? g.getName() : null);
                }
                return m;
            }).collect(Collectors.toList());
            Map<String, Object> data = new HashMap<>();
            data.put("list", list);
            data.put("total", resultPage.getTotal());
            data.put("page", resultPage.getCurrent());
            data.put("size", resultPage.getSize());
            return ResultUtils.success(data);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtils.error(500, "获取失败");
        }
    }

    @ApiOperation("处理投诉（管理员）")
    @PutMapping("/{id}/handle")
    public Result<Map<String, Object>> handleComplaint(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            UserSession session = SessionContext.getSession();
            if (session == null) {
                return ResultUtils.error(401, "未登录");
            }
            var user = userService.getById(session.getUserId());
            if (user == null || user.getUserType() == null || user.getUserType() != 2) {
                return ResultUtils.error(403, "只有管理员可处理");
            }
            Complaint c = complaintService.getById(id);
            if (c == null) {
                return ResultUtils.error(404, "投诉不存在");
            }
            String result = body.get("result") != null ? body.get("result").toString() : "";
            Integer newStatus = body.get("status") != null ? Integer.valueOf(body.get("status").toString()) : 1;
            c.setResult(result);
            c.setStatus(newStatus);
            c.setHandledBy(session.getUserId());
            c.setHandledTime(LocalDateTime.now());
            boolean ok = complaintService.updateById(c);
            if (!ok) {
                return ResultUtils.error(500, "处理失败");
            }
            Map<String, Object> data = new HashMap<>();
            data.put("id", c.getId());
            data.put("status", c.getStatus());
            data.put("result", c.getResult());
            data.put("handledBy", c.getHandledBy());
            data.put("handledTime", c.getHandledTime());
            return ResultUtils.success(data, "已处理");
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtils.error(500, "处理失败");
        }
    }
}
