package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.launchdarkly.sdk.LDContext;
import com.launchdarkly.sdk.server.interfaces.LDClientInterface;
import java.util.List;
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

    private final UserDetails userDetails = new IdamUserDetails(
        "accessToken",
        "id",
        List.of("role1", "role2"),
        "emailAddress",
        "forname",
        "surname"
    );

    @Test
    public void should_return_default_value_when_key_does_not_exist() {
        String notExistingKey = "not-existing-key";
        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);
        when(ldClient.boolVariation(
                 eq(notExistingKey),
                 any(LDContext.class),
                 eq(false)
             )
        ).thenReturn(false);

        assertFalse(launchDarklyFeatureToggler.getValue(notExistingKey, false));
    }

    @Test
    public void should_return_value_when_key_exists() {
        String existingKey = "existing-key";
        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);
        when(ldClient.boolVariation(
                 eq(existingKey),
                 any(LDContext.class),
                 eq(false)
             )
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
