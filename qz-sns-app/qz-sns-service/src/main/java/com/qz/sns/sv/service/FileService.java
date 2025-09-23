package com.qz.sns.sv.service;

import com.qz.sns.model.vo.UploadImageVO;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    /**
     * 上传图片到MinIO
     * @param file 图片文件
     * @return 上传结果，包含图片URL
     */
    UploadImageVO uploadImage(MultipartFile file) throws Exception;

    /**
     * 上传图片到MinIO的指定路径
     * @param file 图片文件
     * @param path 自定义路径
     * @return 上传结果，包含图片URL
     */
    UploadImageVO uploadImage(MultipartFile file, String path) throws Exception;

    /**
     * 上传图片到MinIO的指定路径，并使用指定的文件名
     * @param file 图片文件
     * @param path 自定义路径
     * @param fileName 自定义文件名
     * @return 上传结果，包含图片URL
     */
    UploadImageVO uploadImage(MultipartFile file, String path, String fileName) throws Exception;

    /**
     * 删除MinIO中的文件
     * @param url 文件URL
     * @return 是否删除成功
     */
    boolean deleteFile(String url);
}
