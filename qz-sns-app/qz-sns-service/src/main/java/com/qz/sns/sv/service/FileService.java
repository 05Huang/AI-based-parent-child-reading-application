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
     * 删除MinIO中的文件
     * @param url 文件URL
     * @return 是否删除成功
     */
    boolean deleteFile(String url);
}
