package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.service.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import feign.Request;
import feign.Response;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class FeignClientErrorDecoderTest {

    private FeignClientErrorDecoder feignClientErrorDecoder;

    @BeforeEach
    void setUp() {
        feignClientErrorDecoder = new FeignClientErrorDecoder();
    }

    @ParameterizedTest
    @ValueSource(ints = {400, 404})
    void should_handle_400_error(int statusCode) {

        Map<String, Collection<String>> headers = new HashMap<>();
        Request request =
            Request.create(Request.HttpMethod.PUT, "http://localhost:8096",
                           headers, Request.Body.empty(), null);
        Response response = Response.builder()
            .request(request)
            .status(statusCode)
            .reason("Not found")
            .body("Some body", StandardCharsets.UTF_8)
            .build();

        Throwable throwable = feignClientErrorDecoder.decode("someMethod", response);

        assertThat(throwable).isInstanceOf(ResponseStatusException.class);

        if (statusCode == 400) {
            assertThat(throwable.getMessage())
                .contains("400 BAD_REQUEST \"Error in calling the client method:someMethod\"");
        } else if (statusCode == 404) {
            assertThat(throwable.getMessage())
                .contains("404 NOT_FOUND \"Error in calling the client method:someMethod\"");
        }
    }

    @Test
    void should_handle_500_error() {

        Map<String, Collection<String>> headers = new HashMap<>();
        Request request =
            Request.create(Request.HttpMethod.PUT, "http://localhost:8096",
                           headers, Request.Body.empty(), null);
        Response response = Response.builder()
            .request(request)
            .status(500)
            .reason("Internal server error")
            .body("Some body", StandardCharsets.UTF_8)
            .build();

        Throwable throwable = feignClientErrorDecoder.decode("someMethod", response);

        assertThat(throwable).isInstanceOf(Exception.class);
        assertThat(throwable.getMessage()).contains("Internal server error");
    }
}
