package com.monds.scheduler.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class JobDetails {

    @JsonProperty("job_name")
    private String jobName;

    @JsonProperty("job_class_name")
    private String jobClassName;

    @JsonProperty("trigger_state")
    private String state;

    @JsonProperty("prev_fire_time")
    private String prevFireTime;

    @JsonProperty("next_fire_time")
    private String nextFireTime;
}
