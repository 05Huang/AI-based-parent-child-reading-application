package com.qz.sns.sv.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qz.sns.model.entity.ContentImage;
import com.qz.sns.sv.mapper.ContentImageMapper;
import com.qz.sns.sv.service.IContentImageService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 内容图片关联表 服务实现类
 * </p>
 *
 * @author 李彬
 * @since 2025-02-18
 */
@Service
public class ContentImageServiceImpl extends ServiceImpl<ContentImageMapper, ContentImage> implements IContentImageService {

}
