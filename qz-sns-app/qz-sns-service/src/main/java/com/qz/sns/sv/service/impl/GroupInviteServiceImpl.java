package com.qz.sns.sv.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qz.sns.model.entity.GroupInvite;
import com.qz.sns.sv.mapper.GroupInviteMapper;
import com.qz.sns.sv.service.IGroupInviteService;
import org.springframework.stereotype.Service;

@Service
public class GroupInviteServiceImpl extends ServiceImpl<GroupInviteMapper, GroupInvite> implements IGroupInviteService {
}
