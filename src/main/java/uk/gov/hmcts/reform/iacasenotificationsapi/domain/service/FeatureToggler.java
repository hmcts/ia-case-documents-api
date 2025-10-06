package uk.gov.hmcts.reform.iacasenotificationsapi.domain.service;

public interface FeatureToggler {

    boolean getValue(String key, Boolean defaultValue);

}
