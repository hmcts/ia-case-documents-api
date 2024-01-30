package uk.gov.hmcts.reform.iacasepaymentsapi;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.iacasepaymentsapi.testutils.SpringBootIntegrationTest;

class BadRequestsTest extends SpringBootIntegrationTest {

    private static final String ABOUT_TO_SUBMIT_PATH = "/asylum/ccdAboutToSubmit";

    @Test
    void shouldRequestUnsupportedMediaTypeToServerAndReceiveHttp415() throws Exception {
        mockMvc.perform(post(ABOUT_TO_SUBMIT_PATH)
                .contentType(MediaType.APPLICATION_XML).content("<xml></xml>"))
                .andExpect(status().is(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value())).andReturn();
    }
}
