package com.qz.sns.sv.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qz.sns.model.entity.Complaint;
import com.qz.sns.sv.mapper.ComplaintMapper;
import com.qz.sns.sv.service.IComplaintService;
import org.springframework.stereotype.Service;

@Service
public class ComplaintServiceImpl extends ServiceImpl<ComplaintMapper, Complaint> implements IComplaintService {
}

