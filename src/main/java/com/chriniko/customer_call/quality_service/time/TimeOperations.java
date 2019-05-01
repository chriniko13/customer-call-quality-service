package com.chriniko.customer_call.quality_service.time;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Component
public class TimeOperations {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public long toEpochMilli(String date) {
        LocalDateTime localDateTime = LocalDateTime.parse(date, FORMATTER);
        Instant instant = localDateTime.toInstant(ZoneOffset.UTC);
        return instant.toEpochMilli();
    }

    public String toDateTime(long epochMilli) {
        Instant instant = Instant.ofEpochMilli(epochMilli);
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
        return localDateTime.format(FORMATTER);
    }

}
