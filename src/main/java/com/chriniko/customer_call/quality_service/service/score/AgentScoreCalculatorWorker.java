package com.chriniko.customer_call.quality_service.service.score;

import com.chriniko.customer_call.quality_service.domain.CustomerCall;
import com.chriniko.customer_call.quality_service.domain.CustomerCallsByDate;
import com.chriniko.customer_call.quality_service.dto.AgentScorePerDay;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import lombok.Setter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;

@Component
@Scope("prototype")
public class AgentScoreCalculatorWorker implements Callable<AgentScorePerDay>, DataSerializable, AgentScoreFormula {

    @Setter
    private CustomerCallsByDate customerCallsByDate;

    @Override
    public AgentScorePerDay call() {
        Objects.requireNonNull(customerCallsByDate, "you should provide customerCallsByDate before submitting task");

        LocalDate date = customerCallsByDate.getDate();
        List<CustomerCall> calls = customerCallsByDate.getCustomerCalls();

        double agentScore = getAgentScore(calls);

        return new AgentScorePerDay(date.toString(), String.valueOf(agentScore));
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeObject(customerCallsByDate);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        customerCallsByDate = in.readObject();
    }
}
