package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.clients;

import static feign.Request.create;
import static feign.Response.Body;
import static feign.Response.builder;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import feign.Request.HttpMethod;
import feign.RequestTemplate;
import feign.Response;
import feign.Util;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.web.server.ResponseStatusException;

public class FeignErrorDecoderTest {

    @Mock private Response response;

    @Mock private RequestTemplate requestTemplate;

    private FeignErrorDecoder feignErrorDecoder;

    @BeforeEach
    public void setUp() {

        feignErrorDecoder = new FeignErrorDecoder();
    }

    @Test
    public void should_decode_for_500() {

        response = builder()
            .status(500)
            .reason("Internal server error")
            .request(create(HttpMethod.GET, "/api", Collections.emptyMap(), null, Util.UTF_8, requestTemplate))
            .body("Internal server error", Util.UTF_8)
            .build();

        assertThat(feignErrorDecoder.decode("someMethod", response), instanceOf(Exception.class));
    }

    @Test
    public void should_decode_for_404() {

        response = builder()
            .status(404)
            .reason("Not found")
            .request(create(HttpMethod.GET, "/api", Collections.emptyMap(), null, Util.UTF_8, requestTemplate))
            .body("No data found", Util.UTF_8)
            .build();

        assertThat(feignErrorDecoder.decode("someMethod", response), instanceOf(ResponseStatusException.class));
    }

    @Test
    public void should_decode_for_400() {

        response = builder()
            .status(400)
            .reason("Bad request")
            .request(create(HttpMethod.GET, "/api", Collections.emptyMap(), null, Util.UTF_8, requestTemplate))
            .body("Bad request data".getBytes())
            .build();

        assertThat(feignErrorDecoder.decode("someMethod", response), instanceOf(ResponseStatusException.class));
    }

    @Test
    public void handle_sneaky_exception() throws IOException {

        Body body = mock(Body.class);
        when(body.asReader(Charset.forName("UTF-8")))
            .thenThrow(new IOException("Error in reading response body"));

        response = builder()
            .status(400)
            .reason("Bad request")
            .request(create(HttpMethod.GET, "/api", Collections.emptyMap(), null, Util.UTF_8, requestTemplate))
            .body(body)
            .build();

        feignErrorDecoder.decode("someMethod", response);
        assertThat(feignErrorDecoder.decode("someMethod", response), instanceOf(ResponseStatusException.class));
    }
}
