package com.qz.sns.sv.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qz.sns.model.entity.Group;
import com.qz.sns.sv.mapper.GroupMapper;
import com.qz.sns.sv.service.IGroupService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 群 服务实现类
 * </p>
 *
 * @author 李彬
 * @since 2025-02-18
 */
@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, Group> implements IGroupService {

}
