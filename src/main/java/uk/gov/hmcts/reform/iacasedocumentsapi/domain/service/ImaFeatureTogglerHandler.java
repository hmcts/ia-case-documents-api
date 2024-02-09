package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import org.springframework.stereotype.Service;

@Service
public class ImaFeatureTogglerHandler {

    private final FeatureToggler featureToggler;

    public ImaFeatureTogglerHandler(FeatureToggler featureToggler) {
        this.featureToggler = featureToggler;
    }

    public boolean isImaEnabled() {
        return featureToggler.getValue("ima-feature-flag", false);
    }
}
