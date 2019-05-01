package com.chriniko.customer_call.quality_service.service;

import com.chriniko.customer_call.quality_service.configuration.HazelcastConfiguration;
import com.chriniko.customer_call.quality_service.domain.CustomerCall;
import com.chriniko.customer_call.quality_service.domain.CustomerCallsByDate;
import com.chriniko.customer_call.quality_service.dto.AgentScorePerDay;
import com.chriniko.customer_call.quality_service.dto.AgentScoreSearchInfo;
import com.chriniko.customer_call.quality_service.dto.AgentScoreSearchInfoResult;
import com.chriniko.customer_call.quality_service.repository.CustomerCallRepository;
import com.chriniko.customer_call.quality_service.service.score.AgentScoreCalculator;
import com.chriniko.customer_call.quality_service.time.TimeOperations;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Log4j2

@Service("quality-heap-service")
public class CustomerCallQualityHeapService implements CustomerCallQualityService {

    private final AgentScoreCalculator agentScoreCalculator;
    private final HazelcastInstance hazelcastInstance;
    private final CustomerCallRepository customerCallRepository;
    private final TimeOperations timeOperations;

    @Autowired
    public CustomerCallQualityHeapService(AgentScoreCalculator agentScoreCalculator,
                                          HazelcastInstance hazelcastInstance,
                                          CustomerCallRepository customerCallRepository,
                                          TimeOperations timeOperations) {
        this.agentScoreCalculator = agentScoreCalculator;
        this.hazelcastInstance = hazelcastInstance;
        this.customerCallRepository = customerCallRepository;
        this.timeOperations = timeOperations;
    }

    @PostConstruct
    void init() {
        index();
    }

    @Override
    public AgentScoreSearchInfoResult process(AgentScoreSearchInfo agentScoreSearchInfo) {
        List<CustomerCallsByDate> customerCallsByDate = getCustomerCalls(agentScoreSearchInfo);

        List<AgentScorePerDay> agentScorePerDays = agentScoreCalculator.process(customerCallsByDate);

        return new AgentScoreSearchInfoResult(agentScorePerDays);
    }

    public void index() {
        List<CustomerCall> customerCalls = customerCallRepository.findAll();
        store(customerCalls);
    }

    private void store(List<CustomerCall> customerCalls) {
        constructCustomerCallsByAgentMap(customerCalls);
        printDataInfo(customerCalls);
    }

    private List<CustomerCallsByDate> find(String agent) {
        IMap<String, List<CustomerCallsByDate>> customerCallsByAgentMap = hazelcastInstance.getMap(HazelcastConfiguration.CUSTOMER_CALLS_BY_AGENT);
        return customerCallsByAgentMap.get(agent);
    }

    private List<CustomerCallsByDate> getCustomerCalls(AgentScoreSearchInfo agentScoreSearchInfo) {

        @NotBlank String agent = agentScoreSearchInfo.getAgent();
        @NotBlank String startDate = agentScoreSearchInfo.getStartDate();
        @NotBlank String endDate = agentScoreSearchInfo.getEndDate();

        List<CustomerCallsByDate> customerCalls = find(agent);

        long startEpochMilli = timeOperations.toEpochMilli(startDate + " 00:00");
        long endEpochMilli = timeOperations.toEpochMilli(endDate + " 23:59");

        return customerCalls
                .stream()
                .filter(c -> {
                    long customerCallEpochMilli = c.getDate()
                            .atStartOfDay()
                            .toInstant(ZoneOffset.UTC)
                            .toEpochMilli();

                    return customerCallEpochMilli >= startEpochMilli && customerCallEpochMilli <= endEpochMilli;
                })
                .collect(Collectors.toList());
    }

    private void constructCustomerCallsByAgentMap(List<CustomerCall> customerCalls) {

        IMap<String, List<CustomerCallsByDate>> customerCallsByAgentMap
                = hazelcastInstance.getMap(HazelcastConfiguration.CUSTOMER_CALLS_BY_AGENT);

        Map<String, List<CustomerCall>> customerCallsByAgent = customerCalls
                .stream()
                .collect(Collectors.groupingBy(CustomerCall::getAgent));

        for (Map.Entry<String, List<CustomerCall>> entry : customerCallsByAgent.entrySet()) {

            String agentName = entry.getKey();
            List<CustomerCall> agentAllCustomerCalls = entry.getValue();

            List<CustomerCallsByDate> customerCallsByDates = getCustomerCallsByDates(agentAllCustomerCalls);

            customerCallsByAgentMap.set(agentName, customerCallsByDates);
        }

        log.debug("customerCallsByAgentMap.size() == {}", customerCallsByAgentMap.size());
        log.debug("customerCallsByAgentMap.keySet() == {}", customerCallsByAgentMap.keySet());
        customerCallsByAgentMap.forEach((agent, customerCallsByDates) -> {

            List<String> customerCallsByDatesInfo = customerCallsByDates
                    .stream()
                    .map(r -> r.getDate().toString() + "#" + r.getCustomerCalls().size())
                    .collect(Collectors.toList());

            long totalRecords = customerCallsByDates
                    .stream()
                    .map(CustomerCallsByDate::getCustomerCalls)
                    .mapToLong(Collection::size)
                    .sum();

            log.debug("agentName: {} --- customerCallsByDates.size(): {} --- totalRecords: {} --- customerCallsByDatesInfo: {}",
                    agent, customerCallsByDates.size(), totalRecords, customerCallsByDatesInfo);
        });
    }

    private List<CustomerCallsByDate> getCustomerCallsByDates(List<CustomerCall> agentAllCustomerCalls) {

        Map<LocalDate, List<CustomerCall>> agentAllCustomerCallsByJustDate = agentAllCustomerCalls
                .stream()
                .map(CustomerCall::calculateJustDate)
                .collect(Collectors.groupingBy(CustomerCall::getJustDate));

        return agentAllCustomerCallsByJustDate
                .entrySet()
                .stream()
                .map(record -> new CustomerCallsByDate(record.getKey(), record.getValue()))
                .collect(Collectors.toList());
    }

    private void printDataInfo(List<CustomerCall> customerCalls) {
        Set<String> politenessLevels = customerCalls.stream().map(CustomerCall::getPoliteness).collect(Collectors.toSet());
        log.debug("politenessLevels: {}", politenessLevels);
    }
}
