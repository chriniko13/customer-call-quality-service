package com.chriniko.customer_call.quality_service.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
public class CustomerCallsByDate implements Serializable {

    private LocalDate date;
    private List<CustomerCall> customerCalls;

}
