package com.qz.sns.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qz.sns.model.entity.AiBotConfig;
import com.qz.sns.model.entity.AiChatRecord;
import com.qz.sns.model.entity.GroupMessage;
import com.qz.sns.model.entity.User;
import com.qz.sns.sv.mapper.AiBotConfigMapper;
import com.qz.sns.sv.result.Result;
import com.qz.sns.sv.result.ResultUtils;
import com.qz.sns.sv.service.IAiChatRecordService;
import com.qz.sns.sv.service.IGroupMessageService;
import com.qz.sns.sv.service.IUserService;
import com.qz.sns.sv.session.SessionContext;
import com.qz.sns.sv.session.UserSession;
import com.qz.sns.sv.util.DeepSeekClient;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/ai")
@Api(tags = "AI阅读助手")
public class AiChatRecordController {

    private static final Long PERSONAL_GROUP_ID = 0L;

    @Autowired
    private IAiChatRecordService aiChatRecordService;

    @Autowired
    private IGroupMessageService groupMessageService;

    @Autowired
    private IUserService userService;

    @Autowired
    private AiBotConfigMapper aiBotConfigMapper;

    @Autowired
    private DeepSeekClient deepSeekClient;

    @ApiOperation("群聊中AI阅读助手回复")
    @PostMapping("/group-chat")
    public Result<Map<String, Object>> groupChat(@RequestBody Map<String, Object> body) {
        try {
            UserSession session = SessionContext.getSession();
            if (session == null) {
                return ResultUtils.error(401, "未登录");
            }
            Object groupIdObj = body.get("groupId");
            Object messageIdObj = body.get("messageId");
            if (groupIdObj == null || messageIdObj == null) {
                return ResultUtils.error(400, "缺少参数");
            }
            Long groupId = Long.valueOf(groupIdObj.toString());
            Long messageId = Long.valueOf(messageIdObj.toString());
            GroupMessage userMessage = groupMessageService.getById(messageId);
            if (userMessage == null || !groupId.equals(userMessage.getGroupId())) {
                return ResultUtils.error(404, "消息不存在");
            }
            if (!session.getUserId().equals(userMessage.getSendId())) {
                return ResultUtils.error(403, "无权限调用");
            }
            String content = userMessage.getContent();
            if (!StringUtils.hasText(content)) {
                return ResultUtils.error(400, "消息内容为空");
            }
            String trigger = "@阅读助手";
            int idx = content.indexOf(trigger);
            if (idx < 0) {
                return ResultUtils.error(400, "未检测到@阅读助手");
            }
            String question = content.substring(idx + trigger.length()).trim();
            if (question.isEmpty()) {
                question = content.replace(trigger, "").trim();
            }
            if (question.isEmpty()) {
                return ResultUtils.error(400, "提问内容为空");
            }
            AiBotConfig config = aiBotConfigMapper.selectOne(new LambdaQueryWrapper<AiBotConfig>()
                    .eq(AiBotConfig::getEnabled, true)
                    .last("limit 1"));
            Long botUserId = config != null && config.getBotUserId() != null ? config.getBotUserId() : -1L;
            String botName = config != null && StringUtils.hasText(config.getBotName()) ? config.getBotName() : "AI阅读助手";
            String botAvatar = "/static/images/avatars/AI机器人.svg";
            Integer maxTokens = config != null ? config.getMaxTokens() : null;
            Double temperature = config != null && config.getTemperature() != null
                    ? config.getTemperature().doubleValue()
                    : null;
            String systemPrompt;
            if (config != null && StringUtils.hasText(config.getSystemPrompt())) {
                systemPrompt = config.getSystemPrompt();
            } else {
                systemPrompt = "你是一个温暖友善的家庭AI阅读助手，擅长帮助家庭成员解答阅读相关问题、讲解故事、解释难词，并给出适合孩子的阅读建议。回答要简洁、友好、有温度，适合亲子共读。";
            }
            DeepSeekClient.ChatResult aiResult = deepSeekClient.chat(systemPrompt, question, maxTokens, temperature);
            String reply = aiResult.getReply();
            if (!StringUtils.hasText(reply)) {
                return ResultUtils.error(500, "AI回复为空");
            }
            AiChatRecord record = new AiChatRecord();
            record.setGroupId(groupId);
            record.setMessageId(messageId);
            record.setUserId(session.getUserId());
            record.setUserQuestion(question);
            record.setAiReply(reply);
            record.setTokensUsed(aiResult.getTokensUsed());
            record.setCreatedTime(LocalDateTime.now());
            aiChatRecordService.save(record);
            User sender = userService.getById(session.getUserId());
            String senderName = sender != null && StringUtils.hasText(sender.getNickname())
                    ? sender.getNickname()
                    : userMessage.getSendNickName();
            String replyContent = senderName != null && !senderName.isEmpty()
                    ? ("@" + senderName + " " + reply)
                    : reply;
            GroupMessage aiMessage = new GroupMessage();
            aiMessage.setGroupId(groupId);
            aiMessage.setSendId(botUserId);
            aiMessage.setSendNickName(botName);
            aiMessage.setContent(replyContent);
            aiMessage.setType(false);
            aiMessage.setStatus(false);
            aiMessage.setSendTime(LocalDateTime.now());
            groupMessageService.save(aiMessage);
            Map<String, Object> result = new HashMap<>();
            result.put("id", aiMessage.getId());
            result.put("content", aiMessage.getContent());
            result.put("type", aiMessage.getType());
            result.put("sendTime", aiMessage.getSendTime());
            result.put("senderId", aiMessage.getSendId());
            result.put("senderName", aiMessage.getSendNickName());
            result.put("avatar", botAvatar);
            result.put("isSelf", false);
            return ResultUtils.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtils.error(500, "AI助手服务异常");
        }
    }

    @ApiOperation("保存个人AI阅读助手对话记录")
    @PostMapping("/personal/record")
    public Result<Map<String, Object>> savePersonalRecord(@RequestBody Map<String, Object> body) {
        try {
            UserSession session = SessionContext.getSession();
            if (session == null) {
                return ResultUtils.error(401, "未登录");
            }
            Object questionObj = body.get("userQuestion");
            Object replyObj = body.get("aiReply");
            if (questionObj == null || replyObj == null) {
                return ResultUtils.error(400, "缺少参数");
            }
            String question = String.valueOf(questionObj).trim();
            String reply = String.valueOf(replyObj).trim();
            if (!StringUtils.hasText(question) || !StringUtils.hasText(reply)) {
                return ResultUtils.error(400, "内容为空");
            }
            Long sessionId = null;
            Object sessionIdObj = body.get("sessionId");
            if (sessionIdObj != null && StringUtils.hasText(sessionIdObj.toString())) {
                try {
                    sessionId = Long.valueOf(sessionIdObj.toString());
                } catch (Exception ignore) {
                }
            }
            if (sessionId == null) {
                sessionId = System.currentTimeMillis();
            }
            Integer tokensUsed = null;
            Object tokensObj = body.get("tokensUsed");
            if (tokensObj != null && StringUtils.hasText(tokensObj.toString())) {
                try {
                    tokensUsed = Integer.valueOf(tokensObj.toString());
                } catch (Exception ignore) {
                }
            }
            AiChatRecord record = new AiChatRecord();
            record.setGroupId(PERSONAL_GROUP_ID);
            record.setMessageId(sessionId);
            record.setUserId(session.getUserId());
            record.setUserQuestion(question);
            record.setAiReply(reply);
            record.setTokensUsed(tokensUsed);
            record.setCreatedTime(LocalDateTime.now());
            aiChatRecordService.save(record);
            Map<String, Object> result = new HashMap<>();
            result.put("id", record.getId());
            result.put("sessionId", sessionId);
            result.put("createdTime", record.getCreatedTime());
            return ResultUtils.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtils.error(500, "保存失败");
        }
    }

    @ApiOperation("获取个人AI阅读助手历史会话列表")
    @GetMapping("/personal/sessions")
    public Result<List<Map<String, Object>>> getPersonalSessions() {
        try {
            UserSession session = SessionContext.getSession();
            if (session == null) {
                return ResultUtils.error(401, "未登录");
            }
            QueryWrapper<AiChatRecord> qw = new QueryWrapper<>();
            qw.select("message_id as sessionId", "max(created_time) as lastTime", "max(id) as lastId");
            qw.eq("user_id", session.getUserId());
            qw.and(w -> w.eq("group_id", PERSONAL_GROUP_ID).or().isNull("group_id"));
            qw.isNotNull("message_id");
            qw.groupBy("message_id");
            qw.orderByDesc("lastTime");
            List<Map<String, Object>> raw = aiChatRecordService.listMaps(qw);
            List<Map<String, Object>> list = new ArrayList<>();
            for (Map<String, Object> r : raw) {
                Object sessionIdObj = r.get("sessionId");
                if (sessionIdObj == null) continue;
                Long sessionId;
                try {
                    sessionId = Long.valueOf(sessionIdObj.toString());
                } catch (Exception e) {
                    continue;
                }
                LambdaQueryWrapper<AiChatRecord> lastWrapper = new LambdaQueryWrapper<>();
                lastWrapper.eq(AiChatRecord::getUserId, session.getUserId())
                        .and(w -> w.eq(AiChatRecord::getGroupId, PERSONAL_GROUP_ID).or().isNull(AiChatRecord::getGroupId))
                        .eq(AiChatRecord::getMessageId, sessionId)
                        .orderByDesc(AiChatRecord::getCreatedTime)
                        .last("limit 1");
                AiChatRecord last = aiChatRecordService.getOne(lastWrapper);
                if (last == null) continue;
                Map<String, Object> item = new HashMap<>();
                item.put("sessionId", sessionId);
                item.put("lastTime", last.getCreatedTime());
                item.put("lastQuestion", last.getUserQuestion());
                item.put("lastReply", last.getAiReply());
                String title = last.getUserQuestion();
                if (title != null) {
                    title = title.trim();
                    if (title.length() > 20) {
                        title = title.substring(0, 20) + "...";
                    }
                }
                item.put("title", title);
                list.add(item);
            }
            return ResultUtils.success(list);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtils.error(500, "获取失败");
        }
    }

    @ApiOperation("获取个人AI阅读助手某会话的对话记录")
    @GetMapping("/personal/session/{sessionId}")
    public Result<List<Map<String, Object>>> getPersonalSession(@PathVariable Long sessionId) {
        try {
            UserSession session = SessionContext.getSession();
            if (session == null) {
                return ResultUtils.error(401, "未登录");
            }
            if (sessionId == null) {
                return ResultUtils.error(400, "参数错误");
            }
            LambdaQueryWrapper<AiChatRecord> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AiChatRecord::getUserId, session.getUserId());
            wrapper.and(w -> w.eq(AiChatRecord::getGroupId, PERSONAL_GROUP_ID).or().isNull(AiChatRecord::getGroupId));
            wrapper.eq(AiChatRecord::getMessageId, sessionId);
            wrapper.orderByAsc(AiChatRecord::getCreatedTime);
            List<AiChatRecord> records = aiChatRecordService.list(wrapper);
            List<Map<String, Object>> list = new ArrayList<>();
            for (AiChatRecord r : records) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", r.getId());
                item.put("sessionId", r.getMessageId());
                item.put("userQuestion", r.getUserQuestion());
                item.put("aiReply", r.getAiReply());
                item.put("tokensUsed", r.getTokensUsed());
                item.put("createdTime", r.getCreatedTime());
                list.add(item);
            }
            return ResultUtils.success(list);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtils.error(500, "获取失败");
        }
    }

    @ApiOperation("清空个人AI阅读助手全部历史记录")
    @DeleteMapping("/personal/clear")
    public Result<Map<String, Object>> clearPersonalHistory() {
        try {
            UserSession session = SessionContext.getSession();
            if (session == null) {
                return ResultUtils.error(401, "未登录");
            }
            LambdaQueryWrapper<AiChatRecord> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AiChatRecord::getUserId, session.getUserId());
            wrapper.and(w -> w.eq(AiChatRecord::getGroupId, PERSONAL_GROUP_ID).or().isNull(AiChatRecord::getGroupId));
            boolean ok = aiChatRecordService.remove(wrapper);
            return ResultUtils.success(Map.of("cleared", ok));
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtils.error(500, "清空失败");
        }
    }
}
