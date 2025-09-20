package com.qz.sns.sv.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qz.sns.model.entity.PrivateMessage;
import com.qz.sns.sv.mapper.PrivateMessageMapper;
import com.qz.sns.sv.service.IPrivateMessageService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 私聊消息 服务实现类
 * </p>
 *
 * @author 李彬
 * @since 2025-02-18
 */
@Service
public class PrivateMessageServiceImpl extends ServiceImpl<PrivateMessageMapper, PrivateMessage> implements IPrivateMessageService {

}
