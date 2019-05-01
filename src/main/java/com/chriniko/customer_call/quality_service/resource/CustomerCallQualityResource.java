package com.chriniko.customer_call.quality_service.resource;

import com.chriniko.customer_call.quality_service.dto.AgentScoreSearchInfo;
import com.chriniko.customer_call.quality_service.dto.AgentScoreSearchInfoResult;
import com.chriniko.customer_call.quality_service.service.CustomerCallQualityService;
import com.chriniko.customer_call.quality_service.validator.AgentScoreSearchInfoValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/search-agent-score")
public class CustomerCallQualityResource {

    @Value("${quality-service.calculation-strategy}")
    private String calculationStrategy;

    private final Map<String, CustomerCallQualityService> customerCallQualityServiceByStrategy;
    private final AgentScoreSearchInfoValidator agentScoreSearchInfoValidator;

    @Autowired
    public CustomerCallQualityResource(Map<String, CustomerCallQualityService> customerCallQualityServiceByStrategy,
                                       AgentScoreSearchInfoValidator agentScoreSearchInfoValidator) {
        this.customerCallQualityServiceByStrategy = customerCallQualityServiceByStrategy;
        this.agentScoreSearchInfoValidator = agentScoreSearchInfoValidator;
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    public @ResponseBody
    HttpEntity<AgentScoreSearchInfoResult> find(@RequestBody @Valid AgentScoreSearchInfo input) {

        agentScoreSearchInfoValidator.validate(input);

        long startTime = System.nanoTime();

        AgentScoreSearchInfoResult result = customerCallQualityServiceByStrategy.get(calculationStrategy).process(input);

        long totalTime = System.nanoTime() - startTime;
        long totalTimeInMS = TimeUnit.MILLISECONDS.convert(totalTime, TimeUnit.NANOSECONDS);

        result.setCalculationStrategy(calculationStrategy);
        result.setTotalTimeInMS(totalTimeInMS);

        return ResponseEntity.ok(result);
    }


}
