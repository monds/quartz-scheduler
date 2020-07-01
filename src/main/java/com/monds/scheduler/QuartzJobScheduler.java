package com.monds.scheduler;

import com.monds.scheduler.annotation.CronJob;
import com.monds.scheduler.annotation.SimpleJob;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.quartz.QuartzProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Slf4j
@Configuration
@ComponentScan("com.monds.scheduler")
public class QuartzJobScheduler {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private QuartzProperties quartzProperties;

    @Autowired(required = false)
    private QuartzTriggerListener quartzTriggerListener;

    @Autowired
    private SchedulerProperties schedulerProperties;

    @PostConstruct
    public void init() {
        log.info("Hello world from Spring...");
    }

    @Bean
    public SpringBeanJobFactory springBeanJobFactory() {
        AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
        log.debug("Configuring Job factory");

        jobFactory.setApplicationContext(applicationContext);
        return jobFactory;
    }

    @Bean
    public SchedulerFactoryBean scheduler() throws ClassNotFoundException {

        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();

        log.debug("Setting the Scheduler up");
        schedulerFactoryBean.setJobFactory(springBeanJobFactory());

        Properties properties = new Properties();
        properties.putAll(quartzProperties.getProperties());

        properties.putIfAbsent("org.quartz.scheduler.instanceId", "AUTO");
        properties.putIfAbsent("org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore");

        schedulerFactoryBean.setQuartzProperties(properties);

        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);

        provider.addIncludeFilter(new AnnotationTypeFilter(SimpleJob.class));
        provider.addIncludeFilter(new AnnotationTypeFilter(CronJob.class));

        List<String> jobBasePackages = schedulerProperties.getJobBasePackages();

        Map<String, Map<String, String>> jobPropertiesMap = schedulerProperties.getJobProperties();

        for (String jobBasePackage : jobBasePackages) {
            log.info("scan job package: {}", jobBasePackage);

            List<JobDetail> jobDetails = new ArrayList<>();
            List<Trigger> triggers = new ArrayList<>();

            for (BeanDefinition beanDef : provider.findCandidateComponents(jobBasePackage)) {
                Class<?> beanClass = Class.forName(beanDef.getBeanClassName());
                log.info("Job: " + beanClass.getName());

                if (beanClass.isAnnotationPresent(SimpleJob.class)) {
                    SimpleJob simpleJob = beanClass.getAnnotation(SimpleJob.class);

                    JobDetail jobDetail = JobBuilder.newJob((Class<? extends Job>) beanClass)
                        .storeDurably(true)
                        .withIdentity(simpleJob.name())
                        .requestRecovery(true)
                        .build();

                    Trigger trigger = TriggerBuilder.newTrigger()
                        .withSchedule(
                            SimpleScheduleBuilder.simpleSchedule()
                                .withIntervalInSeconds(simpleJob.frequencyInSec())
                                .repeatForever()
                        )
                        .forJob(jobDetail.getKey())
                        .withIdentity(simpleJob.name() + "_trigger")
                        .build();

                    jobDetails.add(jobDetail);
                    triggers.add(trigger);

                } else if (beanClass.isAnnotationPresent(CronJob.class)) {
                    CronJob cronJob = beanClass.getAnnotation(CronJob.class);

                    JobDetail jobDetail = JobBuilder.newJob((Class<? extends Job>) beanClass)
                        .storeDurably(true)
                        .withIdentity(cronJob.name())
                        .requestRecovery(true)
                        .build();

                    String cronExpression = cronJob.cronExpression();
                    if (jobPropertiesMap != null) {
                        Map<String, String> jobProperties = jobPropertiesMap.get(cronJob.name());
                        if (jobProperties.containsKey("cronExpression")) {
                            cronExpression = jobProperties.get("cronExpression");
                        }
                    }

                    Trigger trigger = TriggerBuilder.newTrigger()
                        .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                        .forJob(jobDetail.getKey())
                        .withIdentity(cronJob.name() + "_trigger")
                        .build();

                    jobDetails.add(jobDetail);
                    triggers.add(trigger);
                }

                schedulerFactoryBean.setJobDetails(jobDetails.toArray(new JobDetail[0]));
                schedulerFactoryBean.setTriggers(triggers.toArray(new Trigger[0]));

            }
        }



        // Comment the following line to use the default Quartz job store.
//        schedulerFactoryBean.setDataSource(quartzDataSource);
        schedulerFactoryBean.setAutoStartup(false);
        if (quartzTriggerListener != null) {
            schedulerFactoryBean.setGlobalTriggerListeners(quartzTriggerListener);
        }

        return schedulerFactoryBean;
    }


//    @Bean
//    @QuartzDataSource
//    @ConditionalOnProperty(name = "org.quartz.jobStore.class", havingValue = "org.quartz.impl.jdbcjobstore.JobStoreTX")
//    @ConfigurationProperties(prefix = "spring.datasource")
//    public DataSource quartzDataSource() {
//        return DataSourceBuilder.create().build();
//    }

}
