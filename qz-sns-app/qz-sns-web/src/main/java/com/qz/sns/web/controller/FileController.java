package com.qz.sns.web.controller;

import com.qz.sns.model.vo.UploadImageVO;
import com.qz.sns.sv.result.Result;
import com.qz.sns.sv.result.ResultUtils;
import com.qz.sns.sv.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    /**
     * 上传文件到MinIO
     */
    @PostMapping("/upload")
    public Result<UploadImageVO> uploadFile(@RequestParam("file") MultipartFile file, 
                                          @RequestParam(value = "type", defaultValue = "image") String type,
                                          @RequestParam(value = "path", required = false) String path) {
        try {
            log.info("开始上传文件，类型：{}，路径：{}", type, path);
            UploadImageVO result = fileService.uploadImage(file, path);
            return ResultUtils.success(result);
        } catch (Exception e) {
            log.error("文件上传失败：{}", e.getMessage(), e);
            return ResultUtils.error(500, "文件上传失败：" + e.getMessage());
        }
    }
}