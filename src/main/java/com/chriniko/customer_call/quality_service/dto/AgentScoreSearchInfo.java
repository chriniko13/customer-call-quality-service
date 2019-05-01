package com.chriniko.customer_call.quality_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentScoreSearchInfo implements Serializable {

    @NotBlank
    private String agent;

    @NotBlank
    @Pattern(regexp = "\\d{4}-\\d{1,2}-\\d{1,2}")
    @JsonProperty("start_date")
    private String startDate;

    @NotBlank
    @Pattern(regexp = "\\d{4}-\\d{1,2}-\\d{1,2}")
    @JsonProperty("end_date")
    private String endDate;

}
