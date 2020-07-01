package com.monds.scheduler;

import com.monds.scheduler.controller.ResponseMessage;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.MalformedURLException;
import java.net.URL;

@Slf4j
@Component
public class SchedulerStarter implements ApplicationRunner {

    @Autowired
    private SchedulerFactoryBean schedulerFactoryBean;

    @Value("${scheduler.ha.enable:false}")
    private boolean haEnable;

    @Value("${scheduler.ha.second.host:127.0.0.1}")
    private String secondHost;

    @Value("${scheduler.ha.second.port:18080}")
    private int secondPort;

    @Value("${scheduler.ha.check.interval:10000}")
    private long checkInterval;

    private boolean started;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        if (!haEnable) {
            schedulerFactoryBean.getScheduler().start();
            started = true;
            return;
        }

        if (started) {
            log.info("Scheduler is already started.");
            return;
        }

        while (true) {

            RestTemplate restTemplate = new RestTemplate();

            URL secondUrl;
            try {
                secondUrl = new URL("http", secondHost, secondPort, "/api/scheduler");
            } catch (MalformedURLException e) {
                log.error("Second URL creation failed", e);
                break;
            }

//            log.info(">>>> URL Check: {}", secondUrl.toString());

            try {

                ResponseEntity<ResponseMessage> responseEntity = restTemplate.getForEntity(secondUrl.toString(), ResponseMessage.class);

                if (responseEntity.getBody() != null) {

                    String message = responseEntity.getBody().getMessage();

                    if (message.equals("NOT_STARTED")) {

                        startScheduler();

                        break; // Stop Checking!

                    } else if (message.equals("STARTED")) {

                        log.info("Other Scheduler is running.");
                    }

                }

            } catch (RestClientException ex) {

                log.warn("Second scheduler not found: {}", ex.getMessage());

                startScheduler();

                break; // Stop Checking!
            }

            try {
                Thread.sleep(checkInterval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }

    private void startScheduler() throws SchedulerException {
        schedulerFactoryBean.getScheduler().start();
        log.info("Scheduler Start.");
        started = true; // rerun 방지
    }
}
