package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import com.launchdarkly.sdk.LDUser;
import com.launchdarkly.sdk.server.interfaces.LDClientInterface;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.security.idam.IdamUserDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.security.idam.IdentityManagerResponseException;

@ExtendWith(MockitoExtension.class)
public class LaunchDarklyFeatureTogglerTest {

    @Mock
    private LDClientInterface ldClient;

    @Mock
    private UserDetailsProvider userDetailsProvider;

    @InjectMocks
    private LaunchDarklyFeatureToggler launchDarklyFeatureToggler;

    private UserDetails userDetails = new IdamUserDetails(
        "accessToken",
        "id",
        Lists.newArrayList("role1", "role2"),
        "emailAddress",
        "forname",
        "surname"
    );

    @Test
    public void should_return_default_value_when_key_does_not_exist() {
        String notExistingKey = "not-existing-key";
        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);
        when(ldClient.boolVariation(
            notExistingKey,
            new LDUser.Builder(userDetails.getId())
                .firstName(userDetails.getForename())
                .lastName(userDetails.getSurname())
                .email(userDetails.getEmailAddress())
                .build(),
            false)
        ).thenReturn(false);

        assertFalse(launchDarklyFeatureToggler.getValue(notExistingKey, false));
    }

    @Test
    public void should_return_value_when_key_exists() {
        String existingKey = "existing-key";
        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);
        when(ldClient.boolVariation(
            existingKey,
            new LDUser.Builder(userDetails.getId())
                .firstName(userDetails.getForename())
                .lastName(userDetails.getSurname())
                .email(userDetails.getEmailAddress())
                .build(),
            false)
        ).thenReturn(true);

        assertTrue(launchDarklyFeatureToggler.getValue(existingKey, false));
    }

    @Test
    public void throw_exception_when_user_details_provider_unavailable() {
        when(userDetailsProvider.getUserDetails()).thenThrow(IdentityManagerResponseException.class);

        assertThatThrownBy(() -> launchDarklyFeatureToggler.getValue("existing-key", true))
            .isExactlyInstanceOf(IdentityManagerResponseException.class);
    }
}
