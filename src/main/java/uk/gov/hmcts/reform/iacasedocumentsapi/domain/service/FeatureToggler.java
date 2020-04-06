package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

public interface FeatureToggler {

    boolean getValue(String key, Boolean defaultValue);

}
