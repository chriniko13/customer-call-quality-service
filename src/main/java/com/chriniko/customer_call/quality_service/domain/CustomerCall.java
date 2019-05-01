package com.chriniko.customer_call.quality_service.domain;


import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.IOException;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor

@Data
@Document("testDocument")
public class CustomerCall implements DataSerializable {

    public static final String VERY_POLITE = "very polite";
    public static final String NOT_POLITE = "not polite";
    public static final String POLITE = "polite";

    @Id
    private String id;

    private long documentId;

    private String date; // Note: 2017-04-21 8:41

    @Field("datetimestamp")
    private String dateTimestamp;

    private String agent;

    private String sentiment;

    private String politeness; // Note: [polite, not polite, very polite]

    private String duration;

    @Transient
    private LocalDate justDate; // Note: 2017-04-21

    @Transient
    public CustomerCall calculateJustDate() {
        this.justDate = LocalDate.parse(getDate().split(" ")[0]);
        return this;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeUTF(id);
        out.writeLong(documentId);
        out.writeUTF(date);
        out.writeUTF(dateTimestamp);
        out.writeUTF(agent);
        out.writeUTF(sentiment);
        out.writeUTF(politeness);
        out.writeUTF(duration);
        if (justDate != null) {
            out.writeUTF(justDate.toString());
        }
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        id = in.readUTF();
        documentId = in.readLong();
        date = in.readUTF();
        dateTimestamp = in.readUTF();
        agent = in.readUTF();
        sentiment = in.readUTF();
        politeness = in.readUTF();
        duration = in.readUTF();

        try {
            justDate = LocalDate.parse(in.readUTF());
        } catch (IOException e) {
        }
    }
}
