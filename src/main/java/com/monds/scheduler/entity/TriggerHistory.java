package com.monds.scheduler.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@IdClass(TriggerHistoryPK.class)
@Table(name = "qrtz_trigger_histories")
public class TriggerHistory {

    @Id
    @Column(name = "sched_name")
    private String schedName;

    @Id
    @Column(name = "entry_id")
    private String entryId;

    @Column(name = "job_name")
    private String jobName;

    @Column(name = "job_group")
    private String jobGroup;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "start_time")
    private LocalDateTime startTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "end_time")
    private LocalDateTime endTime;
}
