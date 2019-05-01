package com.chriniko.customer_call.quality_service.repository;

import com.chriniko.customer_call.quality_service.domain.CustomerCall;

import java.util.List;

public interface CustomerCallRepositoryCustom {

    List<CustomerCall> find(String agent, String fromDate, String toDate);
}
