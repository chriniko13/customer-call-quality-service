package com.chriniko.customer_call.quality_service.configuration;

import com.hazelcast.config.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.management.ManagementFactory;

@Configuration
public class HazelcastConfiguration {

    public static final String CUSTOMER_CALLS_BY_AGENT = "customer-calls-by-agent";

    public static final String DISTRIBUTED_COMPUTATION_EXECUTOR = "distributed-computation-executor";
    public static final String AGENT_SCORE_CALCULATOR_WORKER_COUNTDOWN_LATCH = "agent-score-calculator-worker-countdown-latch";

    public static final String QUALITY_DB_SERVICE_RESULTS = "quality-db-service-results";

    public static final String DEFAULT_JOB_TRACKER = "default-job-tracker";

    public static final String CUSTOMER_CALLS_BY_DOC_ID = "customer-calls-by-document-id";

    private static final String INSTANCE_NAME = "customer-call-quality-service@"
            + ManagementFactory.getRuntimeMXBean().getName()
            + " --- hazelcast-instance";

    @Bean
    public Config hazelCastConfig() {
        Config config = new Config();

        config.setInstanceName(INSTANCE_NAME)
                .addJobTrackerConfig(
                        new JobTrackerConfig()
                                .setName(DEFAULT_JOB_TRACKER)
                )
                .addMapConfig(
                        new MapConfig()
                                .setName(CUSTOMER_CALLS_BY_DOC_ID)
                                .setMaxSizeConfig(new MaxSizeConfig(15_000, MaxSizeConfig.MaxSizePolicy.FREE_HEAP_SIZE))
                                .setEvictionPolicy(EvictionPolicy.NONE)
                                .setTimeToLiveSeconds(-1)
                )
                .addMapConfig(
                        new MapConfig()
                                .setName(QUALITY_DB_SERVICE_RESULTS)
                                .setMaxSizeConfig(new MaxSizeConfig(200, MaxSizeConfig.MaxSizePolicy.FREE_HEAP_SIZE))
                                .setEvictionPolicy(EvictionPolicy.LFU)
                                .setTimeToLiveSeconds(60)
                )
                .addMapConfig(
                        new MapConfig()
                                .setName(CUSTOMER_CALLS_BY_AGENT)
                                .setMaxSizeConfig(new MaxSizeConfig(200, MaxSizeConfig.MaxSizePolicy.FREE_HEAP_SIZE))
                                .setEvictionPolicy(EvictionPolicy.NONE)
                                .setTimeToLiveSeconds(-1)
                )
                .addCountDownLatchConfig(
                        new CountDownLatchConfig()
                                .setName(AGENT_SCORE_CALCULATOR_WORKER_COUNTDOWN_LATCH)
                )
                .addExecutorConfig(
                        new ExecutorConfig()
                                .setName(DISTRIBUTED_COMPUTATION_EXECUTOR)
                                .setQueueCapacity(300)
                                .setPoolSize(30)
                                .setStatisticsEnabled(true)
                );

        return config;
    }

}
