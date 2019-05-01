package com.chriniko.customer_call.quality_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.io.IOException;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentScoreSearchInfo implements DataSerializable {

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

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeUTF(agent);
        out.writeUTF(startDate);
        out.writeUTF(endDate);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        agent = in.readUTF();
        startDate = in.readUTF();
        endDate = in.readUTF();
    }
}
