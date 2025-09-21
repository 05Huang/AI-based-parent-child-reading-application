package com.qz.sns.sv.service.impl;

import com.qz.sns.model.vo.UploadImageVO;
import com.qz.sns.sv.config.MinioConfig;
import com.qz.sns.sv.service.FileService;
import io.minio.*;
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

    @Override
    public UploadImageVO uploadImage(MultipartFile file) throws Exception {
        // 生成文件名
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = UUID.randomUUID().toString() + extension;
        
        // 构建存储路径
        String objectName = minioConfig.getImagePath() + "/" + fileName;
        
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
}
