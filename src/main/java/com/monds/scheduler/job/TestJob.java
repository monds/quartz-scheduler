package com.monds.scheduler.job;

import com.monds.scheduler.annotation.CronJob;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.Map;

@Slf4j
@CronJob(name = "TestJob", cronExpression = "0 0/5 * * * ?")
public class TestJob extends QuartzJobBean {
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {

        for (Map.Entry<String, Object> entry : context.getMergedJobDataMap().entrySet()) {
            log.info(">>>> key: {}, value: {}", entry.getKey(), entry.getValue());
        }

        if (context.getJobDetail().getJobDataMap().containsKey("date")) {
            log.info(">>>> date: {}", context.getJobDetail().getJobDataMap().get("date"));
        }

        log.info("############# JOB TEST #############");

        try {
            Thread.sleep(1000L * 5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
