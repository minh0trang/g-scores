package com.example.backend.importer;

public enum SeedState {
    NOT_STARTED,
    SEEDING,
    READY_EXISTING_DATA,
    READY_IMPORTED,
    NOT_READY_EMPTY_DB,
    NOT_READY_UNVERIFIED_DATA,
    DATA_INCONSISTENT,
    FAILED
}

