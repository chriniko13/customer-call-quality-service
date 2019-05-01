package com.chriniko.customer_call.quality_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentScorePerDay implements DataSerializable {

    private String date;

    @JsonProperty("agent_score")
    private String agentScore;

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeUTF(date);
        out.writeUTF(agentScore);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        date = in.readUTF();
        agentScore = in.readUTF();
    }
}
