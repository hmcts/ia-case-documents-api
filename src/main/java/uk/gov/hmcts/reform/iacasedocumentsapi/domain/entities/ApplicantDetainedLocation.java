package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum ApplicantDetainedLocation {

    IMIGRATION_REMOVAL_CENTER("immigrationRemovalCentre", "Immigration removal centre"),
    PRISON("prison", "Prison");

    @JsonValue
    private final String code;
    private final String location;

    ApplicantDetainedLocation(String code, String location) {
        this.code = code;
        this.location = location;
    }

    public static final Map<String, ApplicantDetainedLocation> locationMapping = new HashMap<>();

    static {
        for (ApplicantDetainedLocation loc : ApplicantDetainedLocation.values()) {
            locationMapping.put(loc.getCode(), loc);
        }
    }

    public static Optional<ApplicantDetainedLocation> from(String code) {
        return Optional.of(locationMapping.get(code));
    }

    public String getLocation() {
        return location;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return getLocation();
    }

}
