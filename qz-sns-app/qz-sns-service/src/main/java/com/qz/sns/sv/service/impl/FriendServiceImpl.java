package com.qz.sns.sv.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qz.sns.model.entity.Friend;
import com.qz.sns.sv.mapper.FriendMapper;
import com.qz.sns.sv.service.IFriendService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 好友 服务实现类
 * </p>
 *
 * @author 李彬
 * @since 2025-02-18
 */
@Service
public class FriendServiceImpl extends ServiceImpl<FriendMapper, Friend> implements IFriendService {

}
