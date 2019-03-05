package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.health;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.actuate.health.Health;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class DocmosisHealthIndicatorTest {

    private static final String DOCMOSIS_ENDPOINT = "http://docmosis";
    private static final String DOCMOSIS_STATUS_URI = "/rs/render";

    @Mock private RestTemplate restTemplate;
    @Mock private ResponseEntity responseEntity;

    private DocmosisHealthIndicator docmosisHealthIndicator;

    @Before
    public void setUp() {

        docmosisHealthIndicator =
            new DocmosisHealthIndicator(
                DOCMOSIS_ENDPOINT,
                DOCMOSIS_STATUS_URI,
                restTemplate
            );

        doReturn(responseEntity)
            .when(restTemplate)
            .exchange(
                eq(DOCMOSIS_ENDPOINT + DOCMOSIS_STATUS_URI),
                eq(HttpMethod.GET),
                eq(null),
                any(ParameterizedTypeReference.class)
            );
    }

    @Test
    public void should_call_docmosis_and_report_when_up() {

        Map<String, Object> exampleReadyResponse =
            ImmutableMap
                .of(
                    "ready", "true"
                );

        when(responseEntity.getBody()).thenReturn(exampleReadyResponse);

        assertEquals(Health.up().build(), docmosisHealthIndicator.health());
    }

    @Test
    public void should_call_docmosis_and_report_when_down() {

        Map<String, Object> exampleNotReadyResponse =
            ImmutableMap
                .of(
                    "ready", "false"
                );

        when(responseEntity.getBody()).thenReturn(exampleNotReadyResponse);

        assertEquals(Health.down().build(), docmosisHealthIndicator.health());
    }

    @Test
    public void should_report_as_down_if_ready_indicator_not_in_payload() {

        Map<String, Object> exampleUnexpectedResponse =
            ImmutableMap
                .of(
                    "foo", "bar"
                );

        when(responseEntity.getBody()).thenReturn(exampleUnexpectedResponse);

        assertEquals(Health.down().build(), docmosisHealthIndicator.health());
    }

    @Test
    public void should_report_as_down_if_no_data_returned() {

        when(responseEntity.getBody()).thenReturn(null);

        assertEquals(Health.down().build(), docmosisHealthIndicator.health());
    }

    @Test
    public void should_report_as_down_if_http_call_fails() {

        RestClientException underlyingException = mock(RestClientException.class);

        when(restTemplate
            .exchange(
                eq(DOCMOSIS_ENDPOINT + DOCMOSIS_STATUS_URI),
                eq(HttpMethod.GET),
                eq(null),
                any(ParameterizedTypeReference.class)
            )).thenThrow(underlyingException);

        assertEquals(Health.down(underlyingException).build(), docmosisHealthIndicator.health());
    }
}
