package com.healthcare.ai.dto;

public enum SymptomDuration {
    LESS_THAN_ONE_DAY("less than one day"),
    ONE_TO_THREE_DAYS("one to three days"),
    MORE_THAN_THREE_DAYS("more than three days");

    private final String label;

    SymptomDuration(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
