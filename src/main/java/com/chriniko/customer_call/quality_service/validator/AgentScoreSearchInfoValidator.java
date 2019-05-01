package com.chriniko.customer_call.quality_service.validator;

import com.chriniko.customer_call.quality_service.dto.AgentScoreSearchInfo;
import com.chriniko.customer_call.quality_service.error.ValidationException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;

@Component
public class AgentScoreSearchInfoValidator {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public void validate(AgentScoreSearchInfo input) {

        Arrays.asList(input.getStartDate(), input.getEndDate())
                .forEach(d -> {
                    try {
                        FORMATTER.parse(d);
                    } catch (DateTimeParseException e) {
                        throw new ValidationException("not valid date provided, message: " + e.getMessage());
                    }
                });


        LocalDate startDate = LocalDate.parse(input.getStartDate(), FORMATTER);
        LocalDate endDate = LocalDate.parse(input.getEndDate(), FORMATTER);

        if (startDate.isAfter(endDate)) {
            throw new ValidationException("provided startDate is after provided endDate");
        }

    }

}
