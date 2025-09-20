package com.qz.sns.sv.task;


import com.qz.sns.sv.service.impl.IntimacyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class IntimacyCalculationTask {

    @Autowired
    private IntimacyService intimacyService;

    /**
     * 每周一凌晨0点执行
     */
    @Scheduled(cron = "0 0 0 ? * MON")
    public void calculateWeeklyIntimacyRanking() {
        log.info("开始执行每周亲密度排行计算任务");
        long startTime = System.currentTimeMillis();
        
        try {
            intimacyService.calculateAndSaveAllIntimacyScores();
            log.info("每周亲密度排行计算任务完成，耗时：{}ms", System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            log.error("每周亲密度排行计算任务执行失败", e);
            
            // 重试一次
            try {
                log.info("尝试重新执行亲密度排行计算任务");
                intimacyService.calculateAndSaveAllIntimacyScores();
                log.info("亲密度排行计算任务重试成功，耗时：{}ms", System.currentTimeMillis() - startTime);
            } catch (Exception ex) {
                log.error("亲密度排行计算任务重试失败", ex);
                // 可以考虑添加报警通知机制
            }
        }
    }
}