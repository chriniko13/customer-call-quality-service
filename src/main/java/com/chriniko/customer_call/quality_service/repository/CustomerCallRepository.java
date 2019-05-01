package com.chriniko.customer_call.quality_service.repository;

import com.chriniko.customer_call.quality_service.domain.CustomerCall;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CustomerCallRepository extends
        MongoRepository<CustomerCall, String>,
        CustomerCallRepositoryCustom /* Note: https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.custom-implementations */ {


    List<CustomerCall> findByAgentEquals(String agent);

}
