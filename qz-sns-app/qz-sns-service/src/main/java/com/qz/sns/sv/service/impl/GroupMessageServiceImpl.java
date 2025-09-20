package com.qz.sns.sv.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qz.sns.model.entity.GroupMessage;
import com.qz.sns.sv.mapper.GroupMessageMapper;
import com.qz.sns.sv.service.IGroupMessageService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 群消息 服务实现类
 * </p>
 *
 * @author 李彬
 * @since 2025-02-18
 */
@Service
public class GroupMessageServiceImpl extends ServiceImpl<GroupMessageMapper, GroupMessage> implements IGroupMessageService {

}
