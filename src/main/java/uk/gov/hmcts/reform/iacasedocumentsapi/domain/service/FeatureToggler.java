package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

public interface FeatureToggler {

    boolean isFlagKnown(String key);

    boolean getValue(String key, Boolean defaultValue);
}
