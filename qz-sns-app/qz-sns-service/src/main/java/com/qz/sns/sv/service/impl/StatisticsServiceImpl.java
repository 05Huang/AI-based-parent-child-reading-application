package com.qz.sns.sv.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qz.sns.model.entity.Statistics;
import com.qz.sns.sv.mapper.StatisticsMapper;
import com.qz.sns.sv.service.IStatisticsService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户统计数据表 服务实现类
 * </p>
 *
 * @author 李彬
 * @since 2025-02-18
 */
@Service
public class StatisticsServiceImpl extends ServiceImpl<StatisticsMapper, Statistics> implements IStatisticsService {

}
