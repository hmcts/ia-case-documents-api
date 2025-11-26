package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients;

import com.launchdarkly.sdk.LDContext;
import com.launchdarkly.sdk.LDUser;
import com.launchdarkly.sdk.server.interfaces.LDClientInterface;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.FeatureToggler;

@Service
public class LaunchDarklyFeatureToggler implements FeatureToggler {

    private LDClientInterface ldClient;
    private UserDetailsProvider userDetailsProvider;

    public LaunchDarklyFeatureToggler(LDClientInterface ldClient,
                                      UserDetailsProvider userDetailsProvider) {
        this.ldClient = ldClient;
        this.userDetailsProvider = userDetailsProvider;
    }

    public boolean getValue(String key, Boolean defaultValue) {

        UserDetails userDetails = userDetailsProvider.getUserDetails();
        LDUser ldUser = new LDUser.Builder(userDetails.getId())
            .firstName(userDetails.getForename())
            .lastName(userDetails.getSurname())
            .email(userDetails.getEmailAddress())
            .build();
        return ldClient.boolVariation(
            key,
            LDContext.fromUser(ldUser),
            defaultValue
        );
    }

}
