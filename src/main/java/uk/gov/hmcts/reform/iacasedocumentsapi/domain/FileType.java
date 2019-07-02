package uk.gov.hmcts.reform.iacasedocumentsapi.domain;

import java.util.stream.Stream;

public enum FileType {

    PDF("pdf");

    private final String value;

    FileType(String pdf) {
        value = pdf;
    }

    public String getValue() {
        return value;
    }

    public static FileType from(String value) {
        return Stream.of(values())
            .filter(v -> v.value.equals(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(value + " not a FileType"));
    }
}
