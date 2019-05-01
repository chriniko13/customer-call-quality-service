package com.chriniko.customer_call.quality_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentScorePerDay implements Serializable {

    private String date;

    @JsonProperty("agent_score")
    private String agentScore;
}
