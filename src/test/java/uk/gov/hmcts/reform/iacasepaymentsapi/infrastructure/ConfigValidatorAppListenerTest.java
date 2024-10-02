package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.ConfigValidatorAppListener.CLUSTER_NAME;

@ExtendWith(MockitoExtension.class)
class ConfigValidatorAppListenerTest {

    @Mock
    private Environment env;
    private ConfigValidatorAppListener configValidatorAppListener;
    private static final String PREVIEW_REFERENCE = "cft-preview-01-aks";
    private static final String SECRET = "secret";

    @BeforeEach
    public void setup() {
        configValidatorAppListener = new ConfigValidatorAppListener(env);
        configValidatorAppListener.setEnvironment(env);
    }

    @ParameterizedTest
    @MethodSource("provideIaConfigValidatorSecretAndClusterNameForException")
    void throwsExceptionWhenIaConfigValidatorSecretIsInvalid(String secret, String clusterName) {
        configValidatorAppListener.setIaConfigValidatorSecret(secret);
        when(env.getProperty(CLUSTER_NAME)).thenReturn(clusterName);
        assertThrows(IllegalArgumentException.class, configValidatorAppListener::breakOnMissingIaConfigValidatorSecret);
        verify(env).getProperty(CLUSTER_NAME);
    }

    private static Stream<Arguments> provideIaConfigValidatorSecretAndClusterNameForException() {
        return Stream.of(
            Arguments.of(null, PREVIEW_REFERENCE),
            Arguments.of("", PREVIEW_REFERENCE)
        );
    }

    @ParameterizedTest
    @MethodSource("provideIaConfigValidatorSecretAndClusterName")
    void whenIaConfigValidatorSecretsCorrectlySet_ThenRunsSuccessfully(String secret, String clusterName) {
        configValidatorAppListener.setIaConfigValidatorSecret(secret);
        when(env.getProperty(CLUSTER_NAME)).thenReturn(clusterName);
        configValidatorAppListener.breakOnMissingIaConfigValidatorSecret();
        verify(env).getProperty(CLUSTER_NAME);
    }

    @Test
    @SuppressWarnings("java:S2699")
    void throwsExceptionWhenIaConfigValidatorSecretIsNullSimulateLocal() {
        configValidatorAppListener.setIaConfigValidatorSecret(null);
        when(env.getProperty(CLUSTER_NAME)).thenReturn(null);
        configValidatorAppListener.breakOnMissingIaConfigValidatorSecret();
        verify(env).getProperty(CLUSTER_NAME);
    }

    @Test
    @SuppressWarnings("java:S2699")
    void throwsExceptionWhenIaConfigValidatorSecretIsEmptySimulateLocal() {
        configValidatorAppListener.setIaConfigValidatorSecret("");
        when(env.getProperty(CLUSTER_NAME)).thenReturn(null);
        configValidatorAppListener.breakOnMissingIaConfigValidatorSecret();
        verify(env).getProperty(CLUSTER_NAME);
    }

    private static Stream<Arguments> provideIaConfigValidatorSecretAndClusterName() {
        return Stream.of(
            Arguments.of(SECRET, null),
            Arguments.of(SECRET, PREVIEW_REFERENCE)
        );
    }
}
