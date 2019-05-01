package com.chriniko.customer_call.quality_service.repository;

import com.chriniko.customer_call.quality_service.domain.CustomerCall;
import com.chriniko.customer_call.quality_service.time.TimeOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

public class CustomerCallRepositoryCustomImpl implements CustomerCallRepositoryCustom {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private TimeOperations timeOperations;

    @Override
    public List<CustomerCall> find(String agent, String fromDate, String toDate) {

        long fromEpochMilli = timeOperations.toEpochMilli(fromDate + " 00:00");
        long toEpochMilli = timeOperations.toEpochMilli(toDate + " 23:59");

        Query query = new Query();
        query.addCriteria(
                Criteria.where("agent").is(agent)
                        .and("datetimestamp")
                        .gte(String.valueOf(fromEpochMilli))
                        .lte(String.valueOf(toEpochMilli))
        );

        return mongoTemplate.find(query, CustomerCall.class);
    }
}
