package com.chriniko.customer_call.quality_service.service;

import com.chriniko.customer_call.quality_service.dto.AgentScoreSearchInfo;
import com.chriniko.customer_call.quality_service.dto.AgentScoreSearchInfoResult;

public interface CustomerCallQualityService {

    AgentScoreSearchInfoResult process(AgentScoreSearchInfo input);

}

