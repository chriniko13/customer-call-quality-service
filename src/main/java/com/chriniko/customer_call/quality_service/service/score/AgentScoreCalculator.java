package com.chriniko.customer_call.quality_service.service.score;

import com.chriniko.customer_call.quality_service.configuration.HazelcastConfiguration;
import com.chriniko.customer_call.quality_service.domain.CustomerCall;
import com.chriniko.customer_call.quality_service.domain.CustomerCallsByDate;
import com.chriniko.customer_call.quality_service.dto.AgentScorePerDay;
import com.chriniko.customer_call.quality_service.error.ProcessingException;
import com.hazelcast.core.ExecutionCallback;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ICountDownLatch;
import com.hazelcast.core.IExecutorService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Log4j2

@Component
public class AgentScoreCalculator implements AgentScoreFormula {

    private static final String ERROR_MESSAGE = "could not calculate agent score [concurrent approach]";

    @Value("${agent-score-calculator.concurrent-approach}")
    private boolean concurrentApproach;

    private final HazelcastInstance hazelcastInstance;
    private final ObjectFactory<AgentScoreCalculatorWorker> agentScoreCalculatorWorkerObjectFactory;

    @Autowired
    public AgentScoreCalculator(HazelcastInstance hazelcastInstance,
                                ObjectFactory<AgentScoreCalculatorWorker> agentScoreCalculatorWorkerObjectFactory) {
        this.hazelcastInstance = hazelcastInstance;
        this.agentScoreCalculatorWorkerObjectFactory = agentScoreCalculatorWorkerObjectFactory;
    }

    public List<AgentScorePerDay> process(List<CustomerCallsByDate> customerCallsByProvidedTimeIntervals) {

        int records = customerCallsByProvidedTimeIntervals.size();

        List<AgentScorePerDay> results = records > 1 && concurrentApproach
                ? concurrentApproach(customerCallsByProvidedTimeIntervals, records)
                : singleThreadApproach(customerCallsByProvidedTimeIntervals);

        results.sort(Comparator.comparing(AgentScorePerDay::getDate));

        return results;
    }

    private List<AgentScorePerDay> concurrentApproach(List<CustomerCallsByDate> customerCallsByProvidedTimeIntervals, int records) {

        List<AgentScorePerDay> results = Collections.synchronizedList(new ArrayList<>(customerCallsByProvidedTimeIntervals.size()));

        ICountDownLatch countDownLatch = hazelcastInstance.getCountDownLatch(HazelcastConfiguration.AGENT_SCORE_CALCULATOR_WORKER_COUNTDOWN_LATCH);
        if (!countDownLatch.trySetCount(records)) {
            throw new ProcessingException(ERROR_MESSAGE);
        }

        IExecutorService executorService = hazelcastInstance.getExecutorService(HazelcastConfiguration.DISTRIBUTED_COMPUTATION_EXECUTOR);

        for (CustomerCallsByDate customerCallsByDate : customerCallsByProvidedTimeIntervals) {

            AgentScoreCalculatorWorker agentScoreCalculatorWorker = agentScoreCalculatorWorkerObjectFactory.getObject();
            agentScoreCalculatorWorker.setCustomerCallsByDate(customerCallsByDate);

            executorService.submit(agentScoreCalculatorWorker, new ExecutionCallback<AgentScorePerDay>() {
                @Override
                public void onResponse(AgentScorePerDay result) {
                    results.add(result);
                    countDownLatch.countDown();
                }

                @Override
                public void onFailure(Throwable t) {
                    log.error("agentScoreCalculatorWorker error occurred during processing, message: " + t.getMessage(), t);
                }
            });
        }

        try {
            boolean countReachedZero = countDownLatch.await(1, TimeUnit.SECONDS);
            if (!countReachedZero) {
                throw new ProcessingException(ERROR_MESSAGE);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("agentScoreCalculatorWorker error occurred during processing, message: " + e.getMessage(), e);
            throw new ProcessingException(ERROR_MESSAGE);
        }

        return results;
    }


    private List<AgentScorePerDay> singleThreadApproach(List<CustomerCallsByDate> customerCallsByProvidedTimeIntervals) {
        List<AgentScorePerDay> results = new ArrayList<>(customerCallsByProvidedTimeIntervals.size());

        for (CustomerCallsByDate customerCall : customerCallsByProvidedTimeIntervals) {

            LocalDate date = customerCall.getDate();
            List<CustomerCall> calls = customerCall.getCustomerCalls();

            double agentScoreRounded = getAgentScore(calls);

            results.add(new AgentScorePerDay(date.toString(), String.valueOf(agentScoreRounded)));
        }

        return results;
    }

}
