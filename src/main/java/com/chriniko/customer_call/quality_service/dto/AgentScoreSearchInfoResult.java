package com.chriniko.customer_call.quality_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentScoreSearchInfoResult {

    private String calculationStrategy;
    private long totalTimeInMS;
    private Collection<AgentScorePerDay> results;

    public AgentScoreSearchInfoResult(Collection<AgentScorePerDay> results) {
        this.results = results;
    }
}