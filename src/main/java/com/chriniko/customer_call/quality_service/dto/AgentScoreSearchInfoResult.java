package com.chriniko.customer_call.quality_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentScoreSearchInfoResult {

    private String calculationStrategy;
    private long totalTimeInMS;
    private List<AgentScorePerDay> results;

    public AgentScoreSearchInfoResult(List<AgentScorePerDay> results) {
        this.results = results;
    }
}