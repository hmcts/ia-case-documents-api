package uk.gov.hmcts.reform.iacasenotificationsapi;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.iacasenotificationsapi.component.testutils.SpringBootIntegrationTest;

public class BadRequestsTest extends SpringBootIntegrationTest {

    private static final String ABOUT_TO_START_PATH = "/asylum/ccdAboutToStart";
    private static final String ABOUT_TO_SUBMIT_PATH = "/asylum/ccdAboutToSubmit";

    @Test
    public void shouldRequestUnsupportedMediaTypeToServerAndReceiveHttp415() throws Exception {

        runClientRequest(
            ABOUT_TO_SUBMIT_PATH,
            MediaType.APPLICATION_XML,
            "<xml></xml>",
            HttpStatus.UNSUPPORTED_MEDIA_TYPE.value()
        );

        runClientRequest(
            ABOUT_TO_START_PATH,
            MediaType.APPLICATION_XML,
            "<xml></xml>",
            HttpStatus.UNSUPPORTED_MEDIA_TYPE.value()
        );

        runClientRequest(
            ABOUT_TO_START_PATH,
            MediaType.TEXT_PLAIN,
            "random text",
            HttpStatus.UNSUPPORTED_MEDIA_TYPE.value()
        );
    }

    @Test
    public void shouldMakeBadRequestsToServerAndReceiveHttp4xx() throws Exception {

        runClientRequest(
            ABOUT_TO_START_PATH,
            MediaType.APPLICATION_JSON,
            "random string",
            HttpStatus.BAD_REQUEST.value()
        );

        runClientRequest(
            ABOUT_TO_SUBMIT_PATH,
            MediaType.APPLICATION_JSON,
            "{  \"foo\": \"bar\"}",
            HttpStatus.BAD_REQUEST.value()
        );
    }

    private void runClientRequest(
        final String path,
        final MediaType mediaType,
        final String content,
        final int expectedHttpStatus
    ) throws Exception {
        mockMvc.perform(post(path)
            .contentType(mediaType).content(content))
            .andExpect(status().is(expectedHttpStatus)).andReturn();
    }
}
