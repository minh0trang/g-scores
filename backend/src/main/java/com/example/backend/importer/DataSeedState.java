package com.example.backend.importer;

import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Component;

@Component
public class DataSeedState {

    public record Snapshot(SeedState state, long epoch, String detail) {
        public boolean isDataReady() {
            return state == SeedState.READY_EXISTING_DATA
                    || state == SeedState.READY_IMPORTED;
        }
    }

    private final AtomicReference<Snapshot> current = new AtomicReference<>(
            new Snapshot(SeedState.NOT_STARTED, 0L, "Dữ liệu chưa bắt đầu khởi tạo."));

    public Snapshot snapshot() {
        return current.get();
    }

    public SeedState getState() {
        return current.get().state();
    }

    public boolean isDataReady() {
        return current.get().isDataReady();
    }

    public Snapshot markSeeding(String detail) {
        return transitionTo(SeedState.SEEDING, detail);
    }

    public Snapshot markReadyExisting(String detail) {
        return transitionTo(SeedState.READY_EXISTING_DATA, detail);
    }

    public Snapshot markReadyImported(String detail) {
        return transitionTo(SeedState.READY_IMPORTED, detail);
    }

    public Snapshot markNotReadyEmpty(String detail) {
        return transitionTo(SeedState.NOT_READY_EMPTY_DB, detail);
    }

    public Snapshot markNotReadyUnverified(String detail) {
        return transitionTo(SeedState.NOT_READY_UNVERIFIED_DATA, detail);
    }

    public Snapshot markDataInconsistent(String detail) {
        return transitionTo(SeedState.DATA_INCONSISTENT, detail);
    }

    public Snapshot markFailed(String detail) {
        return transitionTo(SeedState.FAILED, detail);
    }

    private Snapshot transitionTo(SeedState nextState, String detail) {
        return current.updateAndGet(prev -> new Snapshot(nextState, prev.epoch() + 1, detail));
    }
}

