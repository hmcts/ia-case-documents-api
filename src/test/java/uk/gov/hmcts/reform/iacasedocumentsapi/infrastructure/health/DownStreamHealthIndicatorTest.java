package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.health;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.config.HealthCheckConfiguration;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class DownStreamHealthIndicatorTest {

    @Mock RestTemplate restTemplate;
    @Mock HealthCheckConfiguration healthCheckConfiguration;

    private DownStreamHealthIndicator downStreamHealthIndicator;

    @Test
    public void testGetContributor() {
        Map<String, Map<String, String>> services = new HashMap<String, Map<String, String>>();
        services.put("service1", ImmutableMap.of("uri", "http://service1uri", "response", "\"status\":\"UP\""));
        services.put("service2", ImmutableMap.of("uri", "http://service2uri", "response", "\"status\":\"UP\""));

        when(healthCheckConfiguration.getServices()).thenReturn(services);

        downStreamHealthIndicator = new DownStreamHealthIndicator(restTemplate, healthCheckConfiguration);

        assertNotNull(downStreamHealthIndicator.getContributor("service2"));
        assertEquals(ServiceHealthIndicator.class, downStreamHealthIndicator.getContributor("service2").getClass());
        assertTrue(downStreamHealthIndicator.iterator().hasNext());
    }

    @Test
    public void should_throw_exception_when_services_list_is_null_or_empty() {
        when(healthCheckConfiguration.getServices()).thenReturn(null);

        assertThatThrownBy(() -> new DownStreamHealthIndicator(restTemplate, healthCheckConfiguration))
            .hasMessage("HealthCheckConfiguration cannot be null or empty")
            .isExactlyInstanceOf(NullPointerException.class);
    }


}
