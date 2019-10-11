package uk.gov.hmcts.reform.iacasedocumentsapi.utilities;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;

@SuppressWarnings("unchecked")
public class IaApiClient {

    private static final String ABOUT_TO_SUBMIT_PATH = "/asylum/ccdAboutToSubmit";
    private static final String ABOUT_TO_START_PATH = "/asylum/ccdAboutToStart";

    private ObjectMapper objectMapper;
    private MockMvc mockMvc;

    public IaApiClient(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new Jdk8Module());
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public MvcResult aboutToSubmit(Callback<AsylumCase> callback,
                                   HttpStatus expectedHttpStatus) throws Exception {

        return runRequestAndGetMvcResult(
            ABOUT_TO_SUBMIT_PATH,
            MediaType.APPLICATION_JSON_UTF8,
            objectMapper.writeValueAsString(callback),
            expectedHttpStatus.value());

    }

    public PreSubmitCallbackResponse<AsylumCase> aboutToSubmitWithMappedResponse(Callback<AsylumCase> callback,
                                                                                 HttpStatus expectedHttpStatus) throws Exception {

        MvcResult mvcResult = runRequestAndGetMvcResult(
            ABOUT_TO_SUBMIT_PATH,
            MediaType.APPLICATION_JSON_UTF8,
            objectMapper.writeValueAsString(callback),
            expectedHttpStatus.value());

        return objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
            new TypeReference<PreSubmitCallbackResponse<AsylumCase>>() {
            });

    }

    private MvcResult runRequestAndGetMvcResult(final String path,
                                                final MediaType mediaType,
                                                final String content,
                                                final int expectedHttpStatus) throws Exception {
        return mockMvc.perform(post(path)
            .header("Authorization", "Bearer some-token")
            .contentType(mediaType).content(content))
            .andExpect(status().is(expectedHttpStatus))
            .andReturn();
    }
}
