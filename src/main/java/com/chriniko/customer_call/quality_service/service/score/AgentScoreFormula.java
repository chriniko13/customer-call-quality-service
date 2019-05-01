package com.chriniko.customer_call.quality_service.service.score;

import com.chriniko.customer_call.quality_service.domain.CustomerCall;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;


public interface AgentScoreFormula {

    default double getAgentScore(List<CustomerCall> calls) {

        double sum = 0;
        long sumDuration = 0;

        for (CustomerCall call : calls) {

            double sentiment = Double.parseDouble(call.getSentiment());
            double intermediate = sentiment * 0.2D;

            switch (call.getPoliteness()) {
                case CustomerCall.POLITE:
                    // nothing
                    break;
                case CustomerCall.VERY_POLITE:
                    intermediate += 0.2;
                    break;
                case CustomerCall.NOT_POLITE:
                    intermediate -= 0.4;
                    break;
                default:
                    throw new IllegalStateException("unknown politeness level");
            }

            sum += intermediate;
            sumDuration += Long.parseLong(call.getDuration());
        }

        double avgDuration = getAvgDuration(calls.size(), sumDuration);
        double agentScore = sum - avgDuration;
        return round(agentScore, 2);
    }

    default double getAvgDuration(int noOfCalls, long sumDuration) {
        return BigDecimal
                .valueOf(sumDuration)
                .divide(BigDecimal.valueOf(noOfCalls), new MathContext(6))
                .doubleValue();
    }

    default double round(double value, int places) {
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

}
