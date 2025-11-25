package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure;

import com.launchdarkly.sdk.LDContext;
import com.launchdarkly.sdk.LDUser;
import com.launchdarkly.sdk.server.interfaces.LDClientInterface;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.FeatureToggler;

@Service
public class LaunchDarklyFeatureToggler implements FeatureToggler {

    private LDClientInterface ldClient;
    private UserDetails userDetails;

    public LaunchDarklyFeatureToggler(LDClientInterface ldClient,
                                      UserDetails userDetails) {
        this.ldClient = ldClient;
        this.userDetails = userDetails;
    }

    public boolean getValue(String key, Boolean defaultValue) {
        LDUser user = new LDUser.Builder(userDetails.getId())
            .firstName(userDetails.getForename())
            .lastName(userDetails.getSurname())
            .email(userDetails.getEmailAddress())
            .build();
        return ldClient.boolVariation(key,
            LDContext.fromUser(user),
            defaultValue
        );
    }

}
