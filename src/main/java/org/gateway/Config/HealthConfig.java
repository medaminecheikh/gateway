package org.gateway.Config;

import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.actuate.health.StatusAggregator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
public class HealthConfig {

    @Bean
    public StatusAggregator statusAggregator() {
        return new StatusAggregator() {
            @Override
            public Status getAggregateStatus(Status... statuses) {
                return StatusAggregator.super.getAggregateStatus(statuses);
            }

            @Override
            public Status getAggregateStatus(Set<Status> statuses) {
                if (statuses.contains(Status.DOWN)) {
                    return Status.DOWN;
                }
                return Status.UP;
            }
        };
    }
}
