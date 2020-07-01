package com.monds.scheduler.controller;

import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.Trigger.TriggerState;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@SuppressWarnings("unused")
@Slf4j
@RestController
@RequestMapping("/api")
public class JobController {

    @Autowired
    private SchedulerFactoryBean schedulerFactoryBean;

    @PostMapping(value = "/job/{jobName}/pause")
    public ResponseMessage pauseJob(@PathVariable String jobName) {
        JobKey jobKey = new JobKey(jobName, "DEFAULT");
        try {
            schedulerFactoryBean.getScheduler().pauseJob(jobKey);
        } catch (SchedulerException e) {
            e.printStackTrace();
            return ResponseMessage.buildErrorMessage(e);
        }

        return new ResponseMessage("success", jobName + " paused.");
    }

    @PostMapping(value = "/job/{jobName}/resume")
    public ResponseMessage resumeJob(@PathVariable String jobName) {
        JobKey jobKey = new JobKey(jobName, "DEFAULT");
        try {
            schedulerFactoryBean.getScheduler().resumeJob(jobKey);
        } catch (SchedulerException e) {
            e.printStackTrace();
            return ResponseMessage.buildErrorMessage(e);
        }

        return new ResponseMessage("success", jobName + " resumed.");
    }

    @GetMapping(value = "/job/{jobName}")
    public ResponseMessage getJobState(@PathVariable String jobName) {

        TriggerState triggerState;
        try {
            triggerState = schedulerFactoryBean.getScheduler().getTriggerState(new TriggerKey(jobName + "_trigger"));
        } catch (SchedulerException e) {
            e.printStackTrace();
            return ResponseMessage.buildErrorMessage(e);
        }

        return new ResponseMessage("success", triggerState.name());
    }

    @PostMapping(value = "/job/{jobName}/trigger")
    public ResponseMessage triggerJob(@PathVariable String jobName, @RequestBody Optional<Map<String, Object>> jobDataMap) {

        try {

            List<JobExecutionContext> executingJobs = schedulerFactoryBean.getScheduler().getCurrentlyExecutingJobs();
            for (JobExecutionContext executingJob : executingJobs) {
                if (executingJob.getJobDetail().getKey().getName().equals(jobName)) {
                    return new ResponseMessage("error", jobName + " is already running.");
                }
            }

            JobDataMap dataMap = new JobDataMap(jobDataMap.orElse(Collections.emptyMap()));
            schedulerFactoryBean.getScheduler().triggerJob(new JobKey(jobName), dataMap);


        } catch (SchedulerException e) {
            e.printStackTrace();
            return ResponseMessage.buildErrorMessage(e);
        }

        return new ResponseMessage("success", jobName + " running success.");
    }

    @GetMapping("/scheduler")
    public ResponseMessage getSchedulerStatus() {
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setStatus("success");
        boolean isRunning;
        try {
            isRunning = schedulerFactoryBean.getScheduler().isStarted();
        } catch (SchedulerException e) {
            e.printStackTrace();
            return ResponseMessage.buildErrorMessage(e);
        }
        responseMessage.setMessage(isRunning ? "STARTED" : "NOT_STARTED");
        return responseMessage;
    }

    @GetMapping("/jobs")
    public Object getJobs() {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        try {
            Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.jobGroupEquals("DEFAULT"));
            List<JobDetails> jobDetailsList = new ArrayList<>();
            DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            for (JobKey jobKey : jobKeys) {

                JobDetail jobDetail = scheduler.getJobDetail(jobKey);

                List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
                Trigger trigger = triggers.get(0);

                JobDetails jobDetails = new JobDetails();
                jobDetails.setJobName(jobKey.getName());
                jobDetails.setJobClassName(jobDetail.getJobClass().getName());
                jobDetails.setState(scheduler.getTriggerState(trigger.getKey()).toString());

                Date previousFireTime = trigger.getPreviousFireTime();
                if (previousFireTime != null) {
                    jobDetails.setPrevFireTime(LocalDateTime.ofInstant(previousFireTime.toInstant(), ZoneId.systemDefault()).format(DATE_TIME_FORMAT));
                }
                Date nextFireTime = trigger.getNextFireTime();
                if (nextFireTime != null) {
                    jobDetails.setNextFireTime(LocalDateTime.ofInstant(nextFireTime.toInstant(), ZoneId.systemDefault()).format(DATE_TIME_FORMAT));
                }

                jobDetailsList.add(jobDetails);
            }

            return jobDetailsList;

        } catch (SchedulerException e) {
            e.printStackTrace();
            return ResponseMessage.buildErrorMessage(e);
        }
    }
}
