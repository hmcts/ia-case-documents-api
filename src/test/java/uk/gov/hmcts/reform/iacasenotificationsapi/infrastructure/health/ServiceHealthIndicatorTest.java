package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.health;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.actuate.health.Health;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class ServiceHealthIndicatorTest {

    @Mock RestTemplate restTemplate;
    @Mock ResponseEntity responseEntity;

    private String uri = "https://status.notifications.service.gov.uk";
    private String matcher = "\"status\":\"UP\"";

    private ServiceHealthIndicator serviceHealthIndicator;

    @Before
    public void setUp() {
        serviceHealthIndicator = new ServiceHealthIndicator(uri, matcher, restTemplate);
    }

    @Test
    public void health_status_should_be_up_when_the_service_is_running() {
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(restTemplate.getForEntity(uri, String.class)).thenReturn(responseEntity);

        assertEquals(Health.up().build(), serviceHealthIndicator.health());
    }

    @Test
    public void health_status_should_show_down_when_the_service_is_not_running() {
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);
        when(restTemplate.getForEntity(uri, String.class)).thenReturn(responseEntity);

        assertEquals(Health.down().build(), serviceHealthIndicator.health());
    }

    @Test
    public void health_should_throw_exception_rest_error() {
        when(restTemplate.getForEntity(uri, String.class)).thenThrow(new RestClientException("Internal server error"));

        assertEquals(Health.down(new RestClientException("Internal server error")).build(), serviceHealthIndicator.health());
    }


}
