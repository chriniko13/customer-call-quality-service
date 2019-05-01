package com.chriniko.customer_call.quality_service.domain;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor

@Data
@Document("testDocument")
public class CustomerCall implements Serializable {

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
}
