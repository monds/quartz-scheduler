package com.monds.scheduler.repository;

import com.monds.scheduler.entity.TriggerHistory;
import com.monds.scheduler.entity.TriggerHistoryPK;
import org.springframework.data.repository.RepositoryDefinition;

@RepositoryDefinition(domainClass = TriggerHistory.class, idClass = TriggerHistoryPK.class)
public interface TriggerHistoryRepository {

    TriggerHistory save(TriggerHistory triggerHistory);
}
