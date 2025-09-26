package com.qz.sns.sv.service.impl;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qz.sns.common.constant.UserConstant;
import com.qz.sns.model.dto.*;
import com.qz.sns.model.entity.Content;
import com.qz.sns.model.entity.ContentImage;
import com.qz.sns.model.entity.User;
import com.qz.sns.model.vo.ContentResponse;
import com.qz.sns.sv.exception.GlobalException;
import com.qz.sns.sv.mapper.CommentMapper;
import com.qz.sns.sv.mapper.ContentImageMapper;
import com.qz.sns.sv.mapper.ContentMapper;
import com.qz.sns.sv.mapper.UserMapper;
import com.qz.sns.sv.service.IContentService;
import com.qz.sns.sv.session.SessionContext;
import com.qz.sns.sv.session.UserSession;
import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.jsoup.Jsoup;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * <p>
 * 内容表 服务实现类
 * </p>
 *
 * @author 李彬
 * @since 2025-02-18
 */
@Slf4j
@Service
public class ContentServiceImpl extends ServiceImpl<ContentMapper, Content> implements IContentService {

    @Autowired
    private ContentMapper contentMapper;

    @Autowired
    private ContentImageMapper contentImageMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private ViewHistoryServiceImpl  viewHistoryService;

    private FileService fileService;

    @Override
    @Transactional
    public ContentResponse createContent(ContentDTO request, Long creatorId) {
        // 验证和处理内容中的段落标记
        String processedContent = processContentParagraphs(request.getContent());

        // 创建内容对象
        Content content = new Content();
        content.setTitle(request.getTitle());
        content.setContent(processedContent);
        content.setType(request.getType());
        content.setCoverUrl(request.getCoverUrl());
        content.setMediaUrl(request.getMediaUrl());
        content.setCategoryId(request.getCategoryId());
        content.setTags(request.getTags());
        content.setContentLength(request.getContentLength()); // 设置内容长度
        content.setCreatorId(creatorId);
        content.setStatus(1); // 默认状态为正常
        content.setViewCount(0);
        content.setLikeCount(0);
        content.setCommentCount(0);
        content.setShareCount(0);
        content.setCreatedTime(LocalDateTime.now());
        content.setUpdatedTime(LocalDateTime.now());

        // 保存内容
        contentMapper.insert(content);

        // 处理内容图片关联
        List<ContentImage> imageList = saveContentImages(content.getId(), request.getImageUrls());
        
        // 尝试调用Python服务，但不影响主要功能
        try {
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("content_id", content.getId());
            jsonRequest.put("type", request.getType());
            jsonRequest.put("title", request.getTitle());
            jsonRequest.put("content", request.getContent());
            jsonRequest.put("tags", request.getTags());
            String jsonResponse = sendPostRequest("http://localhost:5000/extract_and_save_features", jsonRequest.toJSONString());
            log.info("Python服务响应: {}", jsonResponse);
        } catch (Exception e) {
            // 仅记录错误，不影响主要功能
            log.warn("调用Python服务失败，这不会影响内容的保存: {}", e.getMessage());
        }

        // 返回响应
        return buildContentResponse(content, imageList);
    }
    private String sendPostRequest(String urlString, String jsonInputString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; utf-8");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);

        // 发送请求
        try (DataOutputStream out = new DataOutputStream(conn.getOutputStream())) {
            out.write(jsonInputString.getBytes("UTF-8")); // 确保使用 UTF-8 编码
            out.flush();
        }

        int responseCode = conn.getResponseCode();
        if (responseCode >= 400) {
            try (BufferedReader errorReader = new BufferedReader(
                    new InputStreamReader(conn.getErrorStream(), "UTF-8"))) {
                String line;
                StringBuilder errorResponse = new StringBuilder();
                while ((line = errorReader.readLine()) != null) {
                    errorResponse.append(line);
                }
                throw new RuntimeException("Server returned error: " + responseCode + ", body: " + errorResponse.toString());
            }
        }

        // 读取响应
        StringBuilder response = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
        }

        return response.toString();
    }

    @Override
    @Transactional
    public ContentResponse updateContent(Long id, ContentDTO request, Long userId) {
        log.info("开始更新内容，ID: {}, 用户ID: {}, 请求数据: {}", id, userId, request);
        
        // 查找内容
        Content content = contentMapper.selectById(id);
        if (content == null) {
            throw new ResourceNotFoundException("内容不存在: " + id);
        }

        // 只对非测试用户验证所有权
        if (userId != UserConstant.TEST_USER_ID && !content.getCreatorId().equals(userId)) {
            throw new GlobalException("无权更新此内容");
        }

        // 验证和处理内容中的段落标记
        String processedContent = processContentParagraphs(request.getContent());

        // 更新内容
        content.setTitle(request.getTitle());
        content.setContent(processedContent);
        content.setType(request.getType());
        content.setCoverUrl(request.getCoverUrl());
        content.setMediaUrl(request.getMediaUrl());
        content.setCategoryId(request.getCategoryId());
        content.setTags(request.getTags());
        content.setContentLength(request.getContentLength()); // 更新内容长度
        content.setUpdatedTime(LocalDateTime.now());

        log.info("准备更新内容，更新后的数据: {}", content);
        
        // 保存内容
        int updateResult = contentMapper.updateById(content);
        log.info("内容更新结果: {}", updateResult);

        // 更新内容图片关联
        contentImageMapper.deleteByContentId(id);
        List<ContentImage> imageList = saveContentImages(content.getId(), request.getImageUrls());
        
        // 尝试调用Python服务，但不影响主要功能
        try {
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("content_id", content.getId());
            jsonRequest.put("type", request.getType());
            jsonRequest.put("title", request.getTitle());
            jsonRequest.put("content", request.getContent());
            jsonRequest.put("tags", request.getTags());

            String jsonResponse = sendPostRequest("http://localhost:5000/extract_and_save_features", jsonRequest.toJSONString());
            log.info("Python服务响应: {}", jsonResponse);
        } catch (Exception e) {
            // 仅记录错误，不影响主要功能
            log.warn("调用Python服务失败，这不会影响内容的保存: {}", e.getMessage());
        }
        
        // 返回响应
        ContentResponse response = buildContentResponse(content, imageList);
        log.info("更新内容完成，返回数据: {}", response);
        return response;
    }

    @Override
    public ContentRequest getContentDetail(Long id) {
        // 1. 获取内容基本信息
        Content content = contentMapper.selectById(id);
        if (content == null) {
            return null;
        }

        // 2. 获取创建者信息
        User creator = userMapper.selectById(content.getCreatorId());

        // 3. 获取内容图片
        List<ContentImage> images = contentImageMapper.selectByContentId(id);

        // 4. 构建返回对象
        ContentRequest detailDTO = new ContentRequest();
        BeanUtils.copyProperties(content, detailDTO);

        // 设置创建者信息
        detailDTO.setCreatorName(creator.getNickname());
        detailDTO.setCreatorAvatar(creator.getAvatar());

        // 设置图片列表
        detailDTO.setImages(images);

        return detailDTO;
    }

    @Override
    @Transactional
    public void incrementViewCount(Long id) {
        log.info("增加文章浏览量，文章ID：{}", id);
        
        // 增加浏览量
        contentMapper.incrementViewCount(id);
        
        // 尝试添加浏览记录（需要获取当前用户ID）
        Long currentUserId = getCurrentUserId();
        if (currentUserId != null) {
            log.info("为用户{}添加浏览记录，文章ID：{}", currentUserId, id);
            try {
                viewHistoryService.addViewHistory(currentUserId, id);
            } catch (Exception e) {
                log.warn("添加浏览记录失败：{}", e.getMessage());
                // 不抛出异常，确保浏览量增加操作成功
            }
        } else {
            log.info("用户未登录，跳过浏览记录添加");
        }
    }

    private Long getCurrentUserId() {
        try {
            // 尝试从SessionContext获取当前用户ID
            UserSession session = SessionContext.getSession();
            if (session != null) {
                return session.getUserId();
            }
        } catch (Exception e) {
            log.debug("获取当前用户ID失败，可能是Session未初始化或已过期：{}", e.getMessage());
        }
        // 如果SessionContext不可用，返回null
        return null;
    }

    /**
     * 验证和处理内容中的段落标记，确保所有段落都有唯一ID
     */
    private String processContentParagraphs(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        // 使用Jsoup解析HTML
        Document doc = Jsoup.parse(content);
        Elements paragraphs = doc.select("p, h1, h2, h3, h4, h5, h6, blockquote, pre");

        // 检查是否所有段落都有ID
        boolean needsProcessing = false;
        for (Element p : paragraphs) {
            if (!p.hasAttr("id") || !p.attr("id").startsWith("para-")) {
                needsProcessing = true;
                break;
            }
        }

        // 如果需要处理，为缺少ID的段落添加ID
        if (needsProcessing) {
            long timestamp = System.currentTimeMillis();
            int index = 0;

            for (Element p : paragraphs) {
                if (!p.hasAttr("id") || !p.attr("id").startsWith("para-")) {
                    p.attr("id", "para-" + timestamp + "-" + (index++));
                }
            }
        }

        // 返回处理后的HTML内容
        return doc.body().html();
    }

    /**
     * 从内容中提取所有图片URL
     */
    private List<String> extractImagesFromContent(String content) {
        List<String> images = new ArrayList<>();
        if (content == null || content.isEmpty()) {
            return images;
        }

        // 使用正则表达式提取图片URL
        Pattern pattern = Pattern.compile("<img[^>]+src=[\"']([^\"']+)[\"'][^>]*>");
        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            images.add(matcher.group(1));
        }

        return images;
    }

    /**
     * 保存内容图片关联
     */
    private List<ContentImage> saveContentImages(Long contentId, List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return new ArrayList<>();
        }

        List<ContentImage> imageList = new ArrayList<>();
        int sortOrder = 0;

        for (String imageUrl : imageUrls) {
            ContentImage image = new ContentImage();
            image.setContentId(contentId);
            image.setImageUrl(imageUrl);
            image.setSortOrder(sortOrder++);
            image.setCreatedTime(LocalDateTime.now());

            contentImageMapper.insert(image);
            imageList.add(image);
        }

        return imageList;
    }

    /**
     * 构建内容响应对象
     */
    private ContentResponse buildContentResponse(Content content, List<ContentImage> images) {
        ContentResponse response = new ContentResponse();
        BeanUtils.copyProperties(content, response);

        // 添加创建者信息
        try {
            User user = userMapper.selectById(content.getCreatorId());
            if (user != null) {
                response.setCreatorName(user.getNickname());
                response.setCreatorAvatar(user.getAvatar());
            }
        } catch (Exception e) {
            log.warn("获取用户信息失败: " + content.getCreatorId(), e);
        }

        // 添加图片信息
        response.setImages(images);

        return response;
    }

    /**
     * 分页查询内容列表
     */
    /**
     * 分页查询内容列表
     * 支持通过status参数查询不同状态的内容
     */
    @Override
    public PageDTO<ContentRequest> getContentPage(ContentQueryDTO queryDTO) {
        log.info("分页查询内容列表，查询条件：{}，状态：{}", queryDTO,
                queryDTO.getStatus() != null ? queryDTO.getStatus() : "默认(正常)");

        // 构建分页对象
        Page<ContentRequest> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());

        // 执行分页查询
        IPage<ContentRequest> contentPage = contentMapper.selectContentPage(page, queryDTO);

        // 构建返回结果
        PageDTO<ContentRequest> result = new PageDTO<>();
        result.setRecords(contentPage.getRecords());
        result.setTotal(contentPage.getTotal());
        result.setCurrent((int) contentPage.getCurrent());
        result.setSize((int) contentPage.getSize());

        String statusText = queryDTO.getStatus() != null ?
                (queryDTO.getStatus() == 1 ? "正常" : "下架") : "默认(正常)";
        log.info("内容列表查询完成，状态：{}，共查询到{}条记录", statusText, result.getTotal());

        return result;
    }

    /**
     * 删除内容（逻辑删除，同时删除相关评论）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteContent(ContentDeleteDTO deleteDTO) {
        log.info("开始删除内容，内容ID：{}，删除原因：{}", deleteDTO.getId(), deleteDTO.getDeleteReason());

        // 验证内容是否存在
        ContentRequest content = getContentDetail(deleteDTO.getId());
        if (content == null) {
            throw new RuntimeException("内容不存在或已被删除");
        }

        try {
            // 1. 逻辑删除内容
            UpdateWrapper<Content> contentUpdateWrapper = new UpdateWrapper<>();
            contentUpdateWrapper.eq("id", deleteDTO.getId())
                    .eq("status", 1)
                    .set("status", 0)
                    .set("updated_time", LocalDateTime.now());

            int contentResult = contentMapper.update(null, contentUpdateWrapper);

            // 2. 逻辑删除相关评论
            int commentResult = commentMapper.deleteCommentsByContentId(deleteDTO.getId());

            log.info("内容删除完成，内容ID：{}，删除内容：{}条，删除评论：{}条",
                    deleteDTO.getId(), contentResult, commentResult);

            return contentResult > 0;

        } catch (Exception e) {
            log.error("删除内容失败，内容ID：{}，错误：{}", deleteDTO.getId(), e.getMessage(), e);
            throw new RuntimeException("删除内容失败：" + e.getMessage());
        }
    }

    /**
     * 批量删除内容（逻辑删除，同时删除相关评论）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchDeleteResult batchDeleteContent(ContentBatchDeleteDTO batchDeleteDTO) {
        log.info("开始批量删除内容，内容数量：{}，删除原因：{}",
                batchDeleteDTO.getIds().length, batchDeleteDTO.getDeleteReason());

        BatchDeleteResult result = new BatchDeleteResult();
        int successCount = 0;
        int failCount = 0;
        StringBuilder errorMessages = new StringBuilder();

        try {
            // 1. 批量逻辑删除内容
            UpdateWrapper<Content> contentUpdateWrapper = new UpdateWrapper<>();
            contentUpdateWrapper.in("id", (Object[]) batchDeleteDTO.getIds())
                    .eq("status", 1)
                    .set("status", 0)
                    .set("updated_time", LocalDateTime.now());

            int contentResult = contentMapper.update(null, contentUpdateWrapper);

            // 2. 批量逻辑删除相关评论
            int commentResult = commentMapper.deleteCommentsByContentIds(batchDeleteDTO.getIds());

            // 统计结果
            successCount = contentResult;
            failCount = batchDeleteDTO.getIds().length - contentResult;

            log.info("批量删除内容完成，成功：{}条，失败：{}条，删除评论：{}条",
                    successCount, failCount, commentResult);

            result.setSuccessCount(successCount);
            result.setFailCount(failCount);
            result.setTotalCount(batchDeleteDTO.getIds().length);
            result.setErrorMessage(failCount > 0 ? "部分内容删除失败" : null);

        } catch (Exception e) {
            log.error("批量删除内容失败，错误：{}", e.getMessage(), e);
            result.setSuccessCount(0);
            result.setFailCount(batchDeleteDTO.getIds().length);
            result.setTotalCount(batchDeleteDTO.getIds().length);
            result.setErrorMessage("批量删除失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 搜索内容
     */
    @Override
    public PageDTO<ContentRequest> searchContent(String keyword, Integer current, Integer size) {
        log.info("搜索内容，关键词：{}，页码：{}，每页大小：{}", keyword, current, size);

        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("搜索关键词不能为空");
        }

        // 构建分页对象
        Page<ContentRequest> page = new Page<>(current, size);

        // 执行搜索
        IPage<ContentRequest> contentPage = contentMapper.searchContent(page, keyword.trim());

        // 构建返回结果
        PageDTO<ContentRequest> result = new PageDTO<>();
        result.setRecords(contentPage.getRecords());
        result.setTotal(contentPage.getTotal());
        result.setCurrent((int) contentPage.getCurrent());
        result.setSize((int) contentPage.getSize());

        log.info("内容搜索完成，关键词：{}，共找到{}条记录", keyword, result.getTotal());
        return result;
    }

    /**
     * 获取热门内容
     */
    @Override
    public List<ContentRequest> getHotContent(Integer limit) {
        log.info("获取热门内容，限制数量：{}", limit);

        if (limit == null || limit <= 0) {
            limit = 10; // 默认10条
        }

        if (limit > 100) {
            limit = 100; // 最多100条
        }

        List<ContentRequest> hotContent = contentMapper.selectHotContent(limit);
        log.info("获取热门内容完成，实际返回{}条记录", hotContent.size());

        return hotContent;
    }

    /**
     * 根据分类获取内容
     */
    @Override
    public List<ContentRequest> getContentByCategory(Long categoryId, Integer limit) {
        log.info("根据分类获取内容，分类ID：{}，限制数量：{}", categoryId, limit);

        if (categoryId == null) {
            throw new IllegalArgumentException("分类ID不能为空");
        }

        if (limit == null || limit <= 0) {
            limit = 20; // 默认20条
        }

        if (limit > 100) {
            limit = 100; // 最多100条
        }

        List<ContentRequest> content = contentMapper.selectContentByCategory(categoryId, limit);
        log.info("根据分类获取内容完成，分类ID：{}，实际返回{}条记录", categoryId, content.size());

        return content;
    }

    /**
     * 根据创建者获取内容
     */
    @Override
    public PageDTO<ContentRequest> getContentByCreator(Long creatorId, Integer current, Integer size) {
        log.info("根据创建者获取内容，创建者ID：{}，页码：{}，每页大小：{}", creatorId, current, size);

        if (creatorId == null) {
            throw new IllegalArgumentException("创建者ID不能为空");
        }

        // 构建分页对象
        Page<ContentRequest> page = new Page<>(current, size);

        // 执行查询
        IPage<ContentRequest> contentPage = contentMapper.selectContentByCreator(page, creatorId);

        // 构建返回结果
        PageDTO<ContentRequest> result = new PageDTO<>();
        result.setRecords(contentPage.getRecords());
        result.setTotal(contentPage.getTotal());
        result.setCurrent((int) contentPage.getCurrent());
        result.setSize((int) contentPage.getSize());

        log.info("根据创建者获取内容完成，创建者ID：{}，共找到{}条记录", creatorId, result.getTotal());
        return result;
    }

    /**
     * 根据标签获取内容
     */
    @Override
    public PageDTO<ContentRequest> getContentByTag(String tag, Integer current, Integer size) {
        log.info("根据标签获取内容，标签：{}，页码：{}，每页大小：{}", tag, current, size);

        if (tag == null || tag.trim().isEmpty()) {
            throw new IllegalArgumentException("标签不能为空");
        }

        // 构建分页对象
        Page<ContentRequest> page = new Page<>(current, size);

        // 执行查询
        IPage<ContentRequest> contentPage = contentMapper.selectContentByTag(page, tag.trim());

        // 构建返回结果
        PageDTO<ContentRequest> result = new PageDTO<>();
        result.setRecords(contentPage.getRecords());
        result.setTotal(contentPage.getTotal());
        result.setCurrent((int) contentPage.getCurrent());
        result.setSize((int) contentPage.getSize());

        log.info("根据标签获取内容完成，标签：{}，共找到{}条记录", tag, result.getTotal());
        return result;
    }

    /**
     * 更新内容状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateContentStatus(Long id, Integer status) {
        log.info("更新内容状态，内容ID：{}，目标状态：{}", id, status);

        if (id == null) {
            throw new IllegalArgumentException("内容ID不能为空");
        }

        if (status == null || (status != 0 && status != 1)) {
            throw new IllegalArgumentException("状态值只能为0或1");
        }

        // 验证内容是否存在（不过滤状态）
        ContentRequest content = contentMapper.selectContentByIdWithoutStatusFilter(id);
        if (content == null) {
            throw new RuntimeException("内容不存在");
        }

        // 检查状态是否需要更新
        if (content.getStatus().equals(status)) {
            log.info("内容状态无需更新，内容ID：{}，当前状态：{}", id, status);
            return true;
        }

        try {
            // 更新内容状态
            UpdateWrapper<Content> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", id)
                    .set("status", status)
                    .set("updated_time", LocalDateTime.now());

            int contentResult = contentMapper.update(null, updateWrapper);

            if (contentResult > 0) {
                // 根据状态同步处理评论
                int commentResult = 0;
                if (status == 1) {
                    // 恢复内容时，恢复相关评论
                    commentResult = commentMapper.restoreCommentsByContentId(id);
                    log.info("内容恢复成功，同时恢复了{}条评论", commentResult);
                } else {
                    // 下架内容时，下架相关评论
                    commentResult = commentMapper.deleteCommentsByContentId(id);
                    log.info("内容下架成功，同时下架了{}条评论", commentResult);
                }

                String statusText = status == 1 ? "上架" : "下架";
                log.info("内容状态更新成功，内容ID：{}，状态：{}，影响评论：{}条",
                        id, statusText, commentResult);

                return true;
            } else {
                log.error("内容状态更新失败，内容ID：{}", id);
                return false;
            }

        } catch (Exception e) {
            log.error("更新内容状态失败，内容ID：{}，错误：{}", id, e.getMessage(), e);
            throw new RuntimeException("更新内容状态失败：" + e.getMessage());
        }
    }
}
