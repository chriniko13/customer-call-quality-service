package com.chriniko.customer_call.quality_service.service;

import com.chriniko.customer_call.quality_service.configuration.HazelcastConfiguration;
import com.chriniko.customer_call.quality_service.domain.CustomerCall;
import com.chriniko.customer_call.quality_service.domain.CustomerCallsByDate;
import com.chriniko.customer_call.quality_service.dto.AgentScorePerDay;
import com.chriniko.customer_call.quality_service.dto.AgentScoreSearchInfo;
import com.chriniko.customer_call.quality_service.dto.AgentScoreSearchInfoResult;
import com.chriniko.customer_call.quality_service.repository.CustomerCallRepository;
import com.chriniko.customer_call.quality_service.service.score.AgentScoreCalculator;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MultiMap;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2

@Service("quality-db-service")
public class CustomerCallQualityDbService implements CustomerCallQualityService {

    private final CustomerCallRepository customerCallRepository;
    private final AgentScoreCalculator agentScoreCalculator;
    private final HazelcastInstance hazelcastInstance;

    @Value("${quality-db-service.cache-enabled}")
    private boolean cacheResults;

    @Autowired
    public CustomerCallQualityDbService(CustomerCallRepository customerCallRepository,
                                        AgentScoreCalculator agentScoreCalculator,
                                        HazelcastInstance hazelcastInstance) {
        this.customerCallRepository = customerCallRepository;
        this.agentScoreCalculator = agentScoreCalculator;
        this.hazelcastInstance = hazelcastInstance;
    }

    @Override
    public AgentScoreSearchInfoResult process(AgentScoreSearchInfo agentScoreSearchInfo) {

        if (cacheResults) {

            IMap<AgentScoreSearchInfo, List<AgentScorePerDay>> cachedResults
                    = hazelcastInstance.getMap(HazelcastConfiguration.QUALITY_DB_SERVICE_RESULTS);

            List<AgentScorePerDay> cachedResult = cachedResults.get(agentScoreSearchInfo);
            if (cachedResult != null) {
                return new AgentScoreSearchInfoResult(cachedResult);
            }

            List<AgentScorePerDay> agentScorePerDays = getAgentScorePerDays(agentScoreSearchInfo);

            cachedResults.put(agentScoreSearchInfo, agentScorePerDays);

            return new AgentScoreSearchInfoResult(agentScorePerDays);

        } else {

            return new AgentScoreSearchInfoResult(getAgentScorePerDays(agentScoreSearchInfo));
        }
    }

    private List<AgentScorePerDay> getAgentScorePerDays(AgentScoreSearchInfo agentScoreSearchInfo) {

        @NotBlank String agent = agentScoreSearchInfo.getAgent();
        @NotBlank String startDate = agentScoreSearchInfo.getStartDate();
        @NotBlank String endDate = agentScoreSearchInfo.getEndDate();

        List<CustomerCall> customerCalls = customerCallRepository.find(agent, startDate, endDate);

        Map<LocalDate, List<CustomerCall>> customerCallsByDate = customerCalls
                .stream()
                .map(CustomerCall::calculateJustDate)
                .collect(Collectors.groupingBy(CustomerCall::getJustDate));

        List<CustomerCallsByDate> customerCallsByDates = customerCallsByDate.entrySet()
                .stream()
                .map(r -> new CustomerCallsByDate(r.getKey(), r.getValue()))
                .collect(Collectors.toList());

        return agentScoreCalculator.process(customerCallsByDates);
    }

}

