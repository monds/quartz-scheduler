package com.monds.scheduler.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode
public class TriggerHistoryPK implements Serializable {
    protected String schedName;
    protected String entryId;

}
