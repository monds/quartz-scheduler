package com.monds.scheduler;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class JobProperties {

    private String name;
    private String triggerType = "cron";
    private String cronExpression;
}
