package com.qz.sns.sv.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qz.sns.model.entity.GroupMember;
import com.qz.sns.sv.mapper.GroupMemberMapper;
import com.qz.sns.sv.service.IGroupMemberService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 群成员 服务实现类
 * </p>
 *
 * @author 李彬
 * @since 2025-02-18
 */
@Service
public class GroupMemberServiceImpl extends ServiceImpl<GroupMemberMapper, GroupMember> implements IGroupMemberService {

}
