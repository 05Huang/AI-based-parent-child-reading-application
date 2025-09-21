package com.qz.sns.sv.service.impl;

import com.qz.sns.common.utils.BeanUtils;
import com.qz.sns.model.dto.VideoUploadRequest;
import com.qz.sns.model.entity.Content;
import com.qz.sns.sv.config.MinioConfig;
import com.qz.sns.common.constant.Constant;
import com.qz.sns.common.enums.FileType;
import com.qz.sns.common.enums.ResultCode;
import com.qz.sns.model.vo.UploadImageVO;
import com.qz.sns.sv.exception.GlobalException;
import com.qz.sns.sv.mapper.ContentMapper;
import com.qz.sns.sv.session.SessionContext;
import com.qz.sns.sv.util.FileUtil;
import com.qz.sns.sv.util.ImageUtil;
import com.qz.sns.sv.util.MinioUtil;
import io.minio.PutObjectArgs;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.lang3.StringUtils;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;

/**
 * todo 通过校验文件MD5实现重复文件秒传
 * 文件上传服务
 *
 * @author Blue
 * @date 2022/10/28
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {
    private final MinioUtil minioUtil;
    private final ContentMapper contentMapper;
    private final MinioConfig minioConfig;


    @PostConstruct
    public void init() {
        String bucketName = minioConfig.getBucketName();
        log.info("正在初始化MinIO存储桶: {}", bucketName);
        
        try {
            // 检查存储桶是否存在
            if (!minioUtil.bucketExists(bucketName)) {
                log.info("存储桶{}不存在，开始创建", bucketName);
                // 创建存储桶
                minioUtil.makeBucket(bucketName);
                log.info("存储桶{}创建成功", bucketName);
            }
            
            // 无论存储桶是否存在，都设置公开访问策略
            minioUtil.setBucketPublic(bucketName);
            
            log.info("MinIO存储桶{}初始化完成", bucketName);
        } catch (Exception e) {
            log.error("初始化MinIO存储桶{}失败: {}", bucketName, e.getMessage(), e);
            throw new RuntimeException("初始化MinIO存储桶失败", e);
        }
    }


    public String uploadFile(MultipartFile file) {
        Long userId = SessionContext.getSession().getUserId();
        // 大小校验
        if (file.getSize() > Constant.MAX_FILE_SIZE) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "文件大小不能超过20M");
        }
        // 上传
        String fileName = minioUtil.upload(minioConfig.getBucketName(), minioConfig.getFilePath(), file);
        if (StringUtils.isEmpty(fileName)) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "文件上传失败");
        }
        String url = generUrl(FileType.FILE, fileName);
        log.info("文件文件成功，用户id:{},url:{}", userId, url);
        return url;
    }

    public UploadImageVO uploadImage(MultipartFile file) {
        try {
//            Long userId = SessionContext.getSession().getUserId();//获取当前用户id--必须要登录
            // 大小校验
            if (file.getSize() > Constant.MAX_IMAGE_SIZE) {
                throw new GlobalException(ResultCode.PROGRAM_ERROR, "图片大小不能超过20M");
            }
            // 图片格式校验
            if (!FileUtil.isImage(file.getOriginalFilename())) {
                throw new GlobalException(ResultCode.PROGRAM_ERROR, "图片格式不合法");
            }
            // 上传原图
            UploadImageVO vo = new UploadImageVO();
            String fileName = minioUtil.upload(minioConfig.getBucketName(), minioConfig.getImagePath(), file);
            if (StringUtils.isEmpty(fileName)) {
                throw new GlobalException(ResultCode.PROGRAM_ERROR, "图片上传失败");
            }
            vo.setOriginUrl(generUrl(FileType.IMAGE, fileName));
            // 大于30K的文件需上传缩略图
            if (file.getSize() > 30 * 1024) {
                byte[] imageByte = ImageUtil.compressForScale(file.getBytes(), 30);
                fileName = minioUtil.upload(minioConfig.getBucketName(), minioConfig.getImagePath(), Objects.requireNonNull(file.getOriginalFilename()), imageByte, file.getContentType());
                if (StringUtils.isEmpty(fileName)) {
                    throw new GlobalException(ResultCode.PROGRAM_ERROR, "图片上传失败");
                }
            }
            vo.setThumbUrl(generUrl(FileType.IMAGE, fileName));
            log.info("文件图片成功，用户id:{},url:{}",2, vo.getOriginUrl());
            return vo;
        } catch (IOException e) {
            log.error("上传图片失败，{}", e.getMessage(), e);
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "图片上传失败");
        }
    }

    //视频上传接口--
    public String uploadVideo(VideoUploadRequest  request, MultipartFile coverFile, MultipartFile videoFile) {
        try {
            // 1. 上传封面图
            if (coverFile != null && !coverFile.isEmpty()) {
                UploadImageVO uploadImageVO = uploadImage(coverFile);
                request.setCoverUrl(uploadImageVO.getOriginUrl());
            }

            // 2. 上传视频（只有type=2时）
            if (request.getType() == 2 && videoFile != null && !videoFile.isEmpty()) {
                // 大小校验
                if (videoFile.getSize() > Constant.MAX_VIDEO_SIZE) {
                    throw new GlobalException(ResultCode.PROGRAM_ERROR, "视频大小不能超过200M");
                }
                // 上传
                String fileName = minioUtil.upload(minioConfig.getBucketName(), minioConfig.getVideoPath(), videoFile);
                if (StringUtils.isEmpty(fileName)) {
                    throw new GlobalException(ResultCode.PROGRAM_ERROR, "视频上传失败");
                }
                String url = generUrl(FileType.VIDEO  , fileName);
                log.info("视频上传成功，用户id:{},url:{}", request.getId(), url);
                request.setMediaUrl(url);
            }

            // 3. 保存数据库（TODO：补充 Service 调用）
            Content content = BeanUtils.copyProperties(request, Content.class);
            contentMapper.insert(content);

            return "上传成功";
        } catch (Exception e) {
            log.error("上传视频失败，{}", e.getMessage(), e);
            return "上传失败";
        }
    }


    public String generUrl(FileType fileTypeEnum, String fileName) {
        String url = minioConfig.getDomain() + "/" + minioConfig.getBucketName();
        switch (fileTypeEnum) {
            case FILE:
                url += "/" + minioConfig.getFilePath() + "/";
                break;
            case IMAGE:
                url += "/" + minioConfig.getImagePath() + "/";
                break;
            case VIDEO:
                url += "/" + minioConfig.getVideoPath() + "/";
                break;
            default:
                break;
        }
        url += fileName;
        return url;
    }

    //上传图文图片
    public String uploadFileContent(MultipartFile file) {
        Long userId = SessionContext.getSession().getUserId(); // 获取当前用户id--必须要登录
        // 大小校验
        if (file.getSize() > Constant.MAX_IMAGE_SIZE) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "图片大小不能超过20M");
        }
        // 图片格式校验
        if (!FileUtil.isImage(file.getOriginalFilename())) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "图片格式不合法");
        }

        // 调整图片尺寸和裁剪
        BufferedImage resizedImage = resizeImage(file);

        // 上传处理后的图片
        String fileName = minioUtil.upload(minioConfig.getBucketName(), minioConfig.getImagePath(), convertToMultipartFile(resizedImage));
        if (StringUtils.isEmpty(fileName)) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "图片上传失败");
        }

        String url = generUrl(FileType.IMAGE, fileName);
        log.info("文件图片成功，用户id:{},url:{}", userId, url);
        return url;
    }

    // 调整图片尺寸
    private BufferedImage resizeImage(MultipartFile file) {
        try {
            // 使用 Thumbnailator 进行高质量缩放
            return Thumbnails.of(file.getInputStream())
                    .size(600, 600) // 设置宽高
                    .outputQuality(1.0) // 设置质量为100%
                    .asBufferedImage(); // 返回 BufferedImage
        } catch (IOException e) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "图片处理失败");
        }
    }
    // 将BufferedImage转换为MultipartFile
    private MultipartFile convertToMultipartFile(BufferedImage image) {
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            // 设置JPEG输出的质量
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
            ImageWriter writer = writers.next();
            ImageOutputStream ios = ImageIO.createImageOutputStream(os);
            writer.setOutput(ios);

            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(1.0f); // 设置质量为100%

            writer.write(null, new IIOImage(image, null, null), param);
            writer.dispose();
            return new MockMultipartFile("file", "image.jpg", "image/jpeg", os.toByteArray());
        } catch (IOException e) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "转换图片失败");
        }
    }
    //删除文件
    public boolean deleteFile(String fileUrl) {
        try {
            if (fileUrl == null || fileUrl.isEmpty()) {
                return false;
            }
            String basePath = minioConfig.getImagePath(); // 比如 "images/"
            int baseIndex = fileUrl.indexOf(basePath);
            if (baseIndex == -1) {
                return false;
            }
            String objectKey = fileUrl.substring(baseIndex); // 得到 images/xxx.jpg
            System.out.println("删除对象key: " + objectKey);
            return minioUtil.remove(minioConfig.getBucketName(), objectKey);
        } catch (Exception e) {
            log.error("删除文件失败，{}", e.getMessage(), e);
            return false;
        }
    }
}
