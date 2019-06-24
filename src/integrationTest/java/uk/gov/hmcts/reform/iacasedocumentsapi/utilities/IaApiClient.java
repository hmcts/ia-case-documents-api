package uk.gov.hmcts.reform.iacasedocumentsapi.utilities;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;

@SuppressWarnings("unchecked")
public class IaApiClient {

    private static final String ABOUT_TO_SUBMIT_PATH = "/asylum/ccdAboutToSubmit";
    private static final String SUBMITTED_PATH = "/asylum/ccdAboutToStart";

    private ObjectMapper objectMapper;
    private MockMvc mockMvc;

    public IaApiClient(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new Jdk8Module());
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public Map<String, Object> aboutToSubmit(Callback<AsylumCase> callback) throws Exception {

        String responseString = runClientRequest(
                ABOUT_TO_SUBMIT_PATH,
                MediaType.APPLICATION_JSON_UTF8,
                objectMapper.writeValueAsString(callback),
                200);

        Map<String, Object> callbackResponse = objectMapper.readValue(responseString, Map.class);

        return (Map<String, Object>) callbackResponse.get("data");
    }

    private String runClientRequest(final String path,
                                    final MediaType mediaType,
                                    final String content,
                                    final int expectedHttpStatus) throws Exception {
        return mockMvc.perform(post(path)
                .header("Authorization", "Bearer some-token")
                .contentType(mediaType).content(content))
                .andExpect(status().is(expectedHttpStatus))
                .andReturn().getResponse().getContentAsString();
    }
}
