package com.chriniko.customer_call.quality_service.time;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TimeOperationsTest {

    // Note: 1491139140000 == 2017-04-02 13:19

    @Test
    public void toEpochMilli() {

        // given
        TimeOperations timeOperations = new TimeOperations();

        // when
        long result = timeOperations.toEpochMilli("2017-04-02 13:19");

        // then
        assertEquals(1491139140000L, result);
    }

    @Test
    public void toDateTime() {

        // given
        TimeOperations timeOperations = new TimeOperations();

        // when
        String result = timeOperations.toDateTime(1491139140000L);

        // then
        assertEquals("2017-04-02 13:19", result);

    }
}