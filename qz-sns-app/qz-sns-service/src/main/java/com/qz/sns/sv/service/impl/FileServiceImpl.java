package com.qz.sns.sv.service.impl;

import com.qz.sns.common.constant.Constant;
import com.qz.sns.model.vo.UploadImageVO;
import com.qz.sns.sv.config.MinioConfig;
import com.qz.sns.sv.service.FileService;
import com.qz.sns.sv.util.MinioUtil;
import io.minio.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;
    private final MinioUtil minioUtil;

    @PostConstruct
    public void init() {
        try {
            String bucketName = minioConfig.getBucketName();
            log.info("正在初始化MinIO存储桶: {}", bucketName);
            if (!minioUtil.bucketExists(bucketName)) {
                log.info("存储桶{}不存在，开始创建", bucketName);
                minioUtil.makeBucket(bucketName);
            }
            // 确保Bucket是公开的，以便前端可以直接访问图片
            minioUtil.setBucketPublic(bucketName);
            log.info("MinIO存储桶{}初始化完成并已设置为公开", bucketName);
        } catch (Exception e) {
            log.error("初始化MinIO存储桶失败", e);
        }
    }

    @Override
    public UploadImageVO uploadImage(MultipartFile file) throws Exception {
        return uploadImage(file, null);
    }

    @Override
    public UploadImageVO uploadImage(MultipartFile file, String customPath) throws Exception {
        return uploadImage(file, customPath, null);
    }

    @Override
    public UploadImageVO uploadImage(MultipartFile file, String customPath, String customFileName) throws Exception {
        // 确定文件名
        String fileName;
        if (customFileName != null && !customFileName.isEmpty()) {
            fileName = customFileName;
        } else if (customPath != null && customPath.contains("video_cover")) {
            // 视频封面固定命名
            fileName = "video_cover.jpg";
        } else {
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            fileName = UUID.randomUUID().toString() + extension;
        }
        
        // 构建存储路径
        String objectName;
        if (customPath != null && !customPath.isEmpty()) {
            // 对于视频封面，customPath格式：video/resource/{contentId}/video_cover
            if (customPath.contains("video/resource")) {
                // 直接使用customPath，不添加imagePath前缀
                objectName = customPath.replace("/video_cover", "") + "/" + fileName;
            } else {
                // 普通图片上传，添加imagePath前缀
                objectName = minioConfig.getImagePath() + "/" + customPath + "/" + fileName;
            }
        } else {
            objectName = minioConfig.getImagePath() + "/" + fileName;
        }
        
        log.info("准备上传图片到MinIO, customPath: {}, objectName: {}, bucket: {}", customPath, objectName, minioConfig.getBucketName());
        
        try (InputStream inputStream = file.getInputStream()) {
            // 上传文件
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
            
            // 构建返回结果
            UploadImageVO result = new UploadImageVO();
            result.setOriginUrl(minioConfig.getDomain() + "/" + minioConfig.getBucketName() + "/" + objectName);
            result.setFileName(fileName);
            result.setFileSize(file.getSize());
            
            log.info("文件上传成功：{}", result.getOriginUrl());
            return result;
        } catch (Exception e) {
            log.error("文件上传失败：{}", e.getMessage(), e);
            throw new Exception("文件上传失败：" + e.getMessage());
        }
    }

    @Override
    public boolean deleteFile(String url) {
        try {
            // 从URL中提取bucket和object名称
            String path = url.substring(minioConfig.getDomain().length() + 1);
            String bucket = path.substring(0, path.indexOf('/'));
            String object = path.substring(path.indexOf('/') + 1);
            
            // 删除文件
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(object)
                    .build());
            
            log.info("文件删除成功：{}", url);
            return true;
        } catch (Exception e) {
            log.error("文件删除失败：{}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public UploadImageVO uploadVideoFile(MultipartFile file, String customPath) throws Exception {
        log.info("开始上传视频文件，文件大小：{}MB", file.getSize() / 1024.0 / 1024.0);
        
        // 大小校验 - 视频文件限制200MB
        if (file.getSize() > Constant.MAX_VIDEO_SIZE) {
            throw new Exception("视频文件大小不能超过200MB");
        }
        
        // 视频格式校验
        String originalFilename = file.getOriginalFilename();
        if (!isVideo(originalFilename)) {
            throw new Exception("视频格式不合法，支持mp4、mov、avi、mkv、wmv格式");
        }
        
        // 固定文件名为video.mp4
        String fileName = "video.mp4";
        
        // 构建存储路径 - 直接使用customPath，不再添加videoPath前缀
        String objectName;
        if (customPath != null && !customPath.isEmpty()) {
            // customPath格式：video/resource/{contentId}
            objectName = customPath + "/" + fileName;
        } else {
            objectName = minioConfig.getVideoPath() + "/" + fileName;
        }
        
        try (InputStream inputStream = file.getInputStream()) {
            // 上传文件
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
            
            // 构建返回结果
            UploadImageVO result = new UploadImageVO();
            result.setOriginUrl(minioConfig.getDomain() + "/" + minioConfig.getBucketName() + "/" + objectName);
            result.setThumbUrl(result.getOriginUrl()); // 视频文件没有缩略图，使用同一个URL
            result.setFileName(fileName);
            result.setFileSize(file.getSize());
            
            log.info("视频文件上传成功：{}", result.getOriginUrl());
            return result;
        } catch (Exception e) {
            log.error("视频文件上传失败：{}", e.getMessage(), e);
            throw new Exception("视频文件上传失败：" + e.getMessage());
        }
    }
    
    /**
     * 判断是否为视频文件
     */
    private boolean isVideo(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }
        String extension = getFileExtension(fileName).toLowerCase();
        return extension.matches("mp4|mov|avi|mkv|wmv|flv|webm|m4v");
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1);
    }
}
