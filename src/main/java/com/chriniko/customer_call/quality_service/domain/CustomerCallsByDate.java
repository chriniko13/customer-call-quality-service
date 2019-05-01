package com.chriniko.customer_call.quality_service.domain;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerCallsByDate implements DataSerializable {

    private LocalDate date;
    private List<CustomerCall> customerCalls;

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeUTF(date.toString());
        out.writeObject(customerCalls);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        date = LocalDate.parse(in.readUTF());
        customerCalls = in.readObject();
    }
}
