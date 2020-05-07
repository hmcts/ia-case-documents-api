package uk.gov.hmcts.reform.iacasepaymentsapi;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = MOCK)
@ActiveProfiles("integration")
public class BadRequestsTest {

    private static final String ABOUT_TO_SUBMIT_PATH = "/asylum/ccdAboutToSubmit";

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    public void setUp() {

        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void shouldRequestUnsupportedMediaTypeToServerAndReceiveHttp415() throws Exception {

        runClientRequest(
            ABOUT_TO_SUBMIT_PATH,
            MediaType.APPLICATION_XML,
            "<xml></xml>",
            HttpStatus.UNSUPPORTED_MEDIA_TYPE.value()
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
