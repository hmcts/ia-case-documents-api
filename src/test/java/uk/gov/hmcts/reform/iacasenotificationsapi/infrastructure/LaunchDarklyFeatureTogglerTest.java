package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.launchdarkly.sdk.LDContext;
import com.launchdarkly.sdk.server.interfaces.LDClientInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.UserDetails;

class LaunchDarklyFeatureTogglerTest {

    private LDClientInterface ldClient;
    private UserDetails userDetails;
    private LaunchDarklyFeatureToggler toggler;

    @BeforeEach
    void setUp() {
        ldClient = mock(LDClientInterface.class);
        userDetails = mock(UserDetails.class);

        when(userDetails.getId()).thenReturn("user123");
        when(userDetails.getForename()).thenReturn("John");
        when(userDetails.getSurname()).thenReturn("Doe");
        when(userDetails.getEmailAddress()).thenReturn("john.doe@example.com");

        toggler = new LaunchDarklyFeatureToggler(ldClient, userDetails);
    }

    @Test
    void shouldReturnTrueWhenFeatureIsEnabled() {
        String key = "feature-key";
        Boolean defaultValue = false;

        when(ldClient.boolVariation(eq(key), any(LDContext.class), eq(defaultValue))).thenReturn(true);

        ArgumentCaptor<LDContext> capturedContext = ArgumentCaptor.forClass(LDContext.class);
        boolean result = toggler.getValue(key, defaultValue);

        assertTrue(result);
        verify(ldClient).boolVariation(eq(key), capturedContext.capture(), eq(defaultValue));
        LDContext ccValue = capturedContext.getValue();
        assertEquals("user", ccValue.getValue("kind").stringValue());
        assertEquals(userDetails.getId(), ccValue.getValue("key").stringValue());
        assertEquals(userDetails.getForename(), ccValue.getValue("firstName").stringValue());
        assertEquals(userDetails.getSurname(), ccValue.getValue("lastName").stringValue());
        assertEquals(userDetails.getEmailAddress(), ccValue.getValue("email").stringValue());
    }

    @Test
    void shouldReturnFalseWhenFeatureIsDisabled() {
        String key = "feature-key";
        Boolean defaultValue = true;

        when(ldClient.boolVariation(eq(key), any(LDContext.class), eq(defaultValue))).thenReturn(false);

        ArgumentCaptor<LDContext> capturedContext = ArgumentCaptor.forClass(LDContext.class);
        boolean result = toggler.getValue(key, defaultValue);

        assertFalse(result);
        verify(ldClient).boolVariation(eq(key), capturedContext.capture(), eq(defaultValue));
        LDContext ccValue = capturedContext.getValue();
        assertEquals("user", ccValue.getValue("kind").stringValue());
        assertEquals(userDetails.getId(), ccValue.getValue("key").stringValue());
        assertEquals(userDetails.getForename(), ccValue.getValue("firstName").stringValue());
        assertEquals(userDetails.getSurname(), ccValue.getValue("lastName").stringValue());
        assertEquals(userDetails.getEmailAddress(), ccValue.getValue("email").stringValue());
    }

    @Test
    void shouldReturnDefaultValueWhenClientReturnsNull() {
        String key = "feature-key";
        Boolean defaultValue = true;

        when(ldClient.boolVariation(eq(key), any(LDContext.class), eq(defaultValue))).thenReturn(defaultValue);

        ArgumentCaptor<LDContext> capturedContext = ArgumentCaptor.forClass(LDContext.class);
        boolean result = toggler.getValue(key, defaultValue);

        assertEquals(defaultValue, result);
        verify(ldClient).boolVariation(eq(key), capturedContext.capture(), eq(defaultValue));
        LDContext ccValue = capturedContext.getValue();
        assertEquals("user", ccValue.getValue("kind").stringValue());
        assertEquals(userDetails.getId(), ccValue.getValue("key").stringValue());
        assertEquals(userDetails.getForename(), ccValue.getValue("firstName").stringValue());
        assertEquals(userDetails.getSurname(), ccValue.getValue("lastName").stringValue());
        assertEquals(userDetails.getEmailAddress(), ccValue.getValue("email").stringValue());
    }
}