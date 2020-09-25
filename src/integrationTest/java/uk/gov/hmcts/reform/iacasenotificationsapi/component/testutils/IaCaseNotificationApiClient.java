package uk.gov.hmcts.reform.iacasenotificationsapi.component.testutils;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.iacasenotificationsapi.component.testutils.fixtures.CallbackForTest;
import uk.gov.hmcts.reform.iacasenotificationsapi.component.testutils.fixtures.PreSubmitCallbackResponseForTest;

@Slf4j
public class IaCaseNotificationApiClient {

    private static final String SERVICE_JWT_TOKEN = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
    private static final String USER_JWT_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJkY28xbjRlNm43Mmo3Z3NyYTQxamhkdmQ0MCIsInN1YiI6IjIzIiwiaWF0IjoxNTYyODQ2NTYwLCJleHAiOjE1NjI4NTA1MDYsImRhdGEiOiJjYXNld29ya2VyLWlhLGNhc2V3b3JrZXItaWEtY2FzZW9mZmljZXIsY2FzZXdvcmtlcixjYXNld29ya2VyLWlhLWxvYTEsY2FzZXdvcmtlci1pYS1jYXNlb2ZmaWNlci1sb2ExLGNhc2V3b3JrZXItbG9hMSIsInR5cGUiOiJBQ0NFU1MiLCJpZCI6IjIzIiwiZm9yZW5hbWUiOiJDYXNlIiwic3VybmFtZSI6Ik9mZmljZXIiLCJkZWZhdWx0LXNlcnZpY2UiOiJDQ0QiLCJsb2EiOjEsImRlZmF1bHQtdXJsIjoiaHR0cHM6Ly9sb2NhbGhvc3Q6OTAwMC9wb2MvY2NkIiwiZ3JvdXAiOiJjYXNld29ya2VyIn0.W_drH_9R5wchdR6ctMaCpgQNkfgSz5XtezODtsspG34";

    private final String aboutToSubmitUrl;
    private final ObjectMapper objectMapper;
    private final MockMvc mockMvc;

    public IaCaseNotificationApiClient(ObjectMapper objectMapper, MockMvc mockMvc) {
        this.objectMapper = objectMapper;
        this.mockMvc = mockMvc;
        this.aboutToSubmitUrl = "/asylum/ccdAboutToSubmit";
    }

    public PreSubmitCallbackResponseForTest aboutToSubmit(CallbackForTest.CallbackForTestBuilder callback) {

        try {
            MvcResult response = mockMvc
                .perform(
                    post(aboutToSubmitUrl)
                        .header("Authorization", USER_JWT_TOKEN)
                        .header("ServiceAuthorization", SERVICE_JWT_TOKEN)
                        .content(objectMapper.writeValueAsString(callback.build()))
                        .contentType(APPLICATION_JSON_VALUE)
                )
                .andReturn();

            return objectMapper.readValue(
                response.getResponse().getContentAsString(),
                PreSubmitCallbackResponseForTest.class
            );
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            // test will fail
            throw new RuntimeException(e);
        }
    }
}