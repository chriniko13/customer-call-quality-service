package com.chriniko.customer_call.quality_service.service;

import com.chriniko.customer_call.quality_service.domain.CustomerCall;
import com.chriniko.customer_call.quality_service.dto.AgentScorePerDay;
import com.chriniko.customer_call.quality_service.dto.AgentScoreSearchInfo;
import com.chriniko.customer_call.quality_service.dto.AgentScoreSearchInfoResult;
import com.chriniko.customer_call.quality_service.repository.CustomerCallRepository;
import com.chriniko.customer_call.quality_service.service.score.AgentScoreCalculator;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.joor.Reflect;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;

@RunWith(MockitoJUnitRunner.class)
public class CustomerCallQualityDbServiceTest {

    private CustomerCallQualityDbService customerCallQualityDbService;

    @Mock
    private CustomerCallRepository customerCallRepository;

    @Mock
    private AgentScoreCalculator agentScoreCalculator;

    @Mock
    private HazelcastInstance hazelcastInstance;

    @Mock
    private IMap<Object, Object> cachedResults;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        customerCallQualityDbService = new CustomerCallQualityDbService(customerCallRepository, agentScoreCalculator, hazelcastInstance);

        Reflect.on(customerCallQualityDbService).set("cacheResults", true);
    }

    @Test
    public void process_works_as_expected() {

        // given
        AgentScoreSearchInfo agentScoreSearchInfo = new AgentScoreSearchInfo();
        agentScoreSearchInfo.setAgent("Natasha");
        agentScoreSearchInfo.setStartDate("2017-04-01");
        agentScoreSearchInfo.setEndDate("2017-04-10");


        Mockito.when(hazelcastInstance.getMap(anyString())).thenReturn(cachedResults);
        Mockito.when(cachedResults.get(agentScoreSearchInfo)).thenReturn(null);

        List<CustomerCall> customerCalls = Collections.singletonList(
                new CustomerCall("id", 123L, "2017-04-21 8:41", String.valueOf(Instant.now().toEpochMilli()), "agent", "1.0", "very polite", "3", LocalDate.now())
        );

        Mockito.when(customerCallRepository.find(anyString(), anyString(), anyString()))
                .thenReturn(customerCalls);

        List<AgentScorePerDay> agentScorePerDays = Collections.singletonList(
                new AgentScorePerDay("2017-04-01", "-5.21")
        );

        Mockito.when(agentScoreCalculator.process(anyList()))
                .thenReturn(agentScorePerDays);


        // when
        AgentScoreSearchInfoResult result = customerCallQualityDbService.process(agentScoreSearchInfo);

        // then
        Assert.assertNotNull(result);

        List<AgentScorePerDay> records = result.getResults();
        assertEquals(1, records.size());

        assertEquals("2017-04-01", records.get(0).getDate());
        assertEquals("-5.21", records.get(0).getAgentScore());

    }

}