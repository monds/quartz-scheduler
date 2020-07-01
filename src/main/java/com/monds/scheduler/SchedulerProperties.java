package com.monds.scheduler;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "scheduler")
public class SchedulerProperties {

    private List<String> jobBasePackages;

    private Map<String, Map<String, String>> jobProperties;
}
