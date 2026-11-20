package com.example.backend.importer;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("dataSeederHealthIndicator")
@RequiredArgsConstructor
public class DataSeederHealthIndicator implements HealthIndicator {

    private final DataSeedState dataSeedState;

    @Override
    public Health health() {
        DataSeedState.Snapshot snap = dataSeedState.snapshot();
        SeedState state = snap.state();

        return switch (state) {
            case READY_EXISTING_DATA, READY_IMPORTED -> Health.up()
                    .withDetail("state", state.name())
                    .withDetail("epoch", snap.epoch())
                    .withDetail("detail", snap.detail())
                    .build();
            case NOT_STARTED, SEEDING, NOT_READY_EMPTY_DB, NOT_READY_UNVERIFIED_DATA -> Health.outOfService()
                    .withDetail("state", state.name())
                    .withDetail("epoch", snap.epoch())
                    .withDetail("detail", snap.detail())
                    .build();
            case DATA_INCONSISTENT, FAILED -> Health.down()
                    .withDetail("state", state.name())
                    .withDetail("epoch", snap.epoch())
                    .withDetail("detail", snap.detail())
                    .build();
        };
    }
}

