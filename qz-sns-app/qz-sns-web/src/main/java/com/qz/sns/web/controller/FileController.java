package com.qz.sns.web.controller;


import ch.qos.logback.core.net.SyslogOutputStream;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qz.sns.model.dto.VideoUploadRequest;
import com.qz.sns.model.vo.UploadImageVO;
import com.qz.sns.sv.result.Result;
import com.qz.sns.sv.result.ResultUtils;
import com.qz.sns.sv.service.impl.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@Tag(name = "文件上传")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @Operation(summary = "上传图片", description = "上传图片,上传后返回原图和缩略图的url")
    @PostMapping("/image/upload")
    public Result <UploadImageVO> uploadImage(@RequestParam("file") MultipartFile file) {

        return ResultUtils.success(fileService.uploadImage(file));
    }

    @CrossOrigin
    @Operation(summary = "上传文件", description = "上传文件，上传后返回文件url")
    @PostMapping("/file/upload")
    public Result<String> uploadFile(@RequestParam("file") MultipartFile file) {
        return ResultUtils.success(fileService.uploadFile(file), "");
    }

    @CrossOrigin // 允许跨域请求
    @Operation(summary = "上传视频", description = "上传视频，上传后返回结果")
    @PostMapping(value = "/video/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<String> uploadVideo(
            @RequestPart("data") String jsonData,
            @RequestPart("cover") MultipartFile coverFile,
            @RequestPart("video") MultipartFile videoFile) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        VideoUploadRequest request = objectMapper.readValue(jsonData, VideoUploadRequest.class);
        System.out.println("jsonData = " + jsonData);
        return ResultUtils.success(fileService.uploadVideo(request, coverFile, videoFile), "");
    }

    /**
     * 上传图片
     */
    @PostMapping("/content/image")
    public  Result<Map<String, String>> uploadImageContent(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResultUtils.error(400, "文件不能为空");
            }


            // 上传到MinIO
            String url = fileService.uploadFileContent(file);

            // 返回URL
            Map<String, String> result = new HashMap<>();
            result.put("url", url);

            return ResultUtils.success(result, "上传成功");
        } catch (Exception e) {
            log.error("上传图片失败", e);
            return ResultUtils.error(500, "上传图片失败: " + e.getMessage());
        }
    }


    /**
     * 删除文件
     */
    @DeleteMapping("delete/file")
    public  Result<Boolean> deleteFile(@RequestParam String url) {
        try {
            boolean success = fileService.deleteFile(url);
            if (success) {
                return ResultUtils.success(true, "删除成功");
            } else {
                return ResultUtils.error(400, "删除失败");
            }
        } catch (Exception e) {
            log.error("删除文件失败", e);
            return ResultUtils.error(500, "删除文件失败: " + e.getMessage());
        }
    }
}