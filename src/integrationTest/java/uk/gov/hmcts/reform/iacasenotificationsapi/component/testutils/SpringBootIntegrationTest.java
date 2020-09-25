package uk.gov.hmcts.reform.iacasenotificationsapi.component.testutils;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.iacasenotificationsapi.Application;

@ActiveProfiles("integration")
@RunWith(SpringRunner.class)
@TestPropertySource(properties = {
    "S2S_URL=http://127.0.0.1:8990/serviceAuth",
    "IDAM_URL=http://127.0.0.1:8990/userAuth",
    "OPEN_ID_IDAM_URL=http://127.0.0.1:8990/userAuth",
    "IA_CASE_DOCUMENTS_API_URL=http://localhost:8990/ia-case-documents-api",
    "govnotify.baseUrl=http://localhost:8990",
    "govnotify.key=test_key-7f72d0fb-2bc4-421b-bceb-1bf5bf350ff9-3df5a74b-f25b-4052-b00f-3f71d33cd0eb"
})
@AutoConfigureMockMvc(addFilters = false)
@SpringBootTest(classes = { TestConfiguration.class, Application.class }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class SpringBootIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected GivensBuilder given;
    protected IaCaseNotificationApiClient iaCaseNotificationApiClient;
    protected GovNotifyApiVerifications then;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(
        wireMockConfig()
            .port(8990));

    @Before
    public void setUpGivens() {
        given = new GivensBuilder();
    }

    @Before
    public void setUpVerifications() {
        then = new GovNotifyApiVerifications();
    }

    @Before
    public void setUpApiClient() {
        iaCaseNotificationApiClient = new IaCaseNotificationApiClient(objectMapper, mockMvc);
    }

    @Before
    public void setupServiceAuthStub() {

        stubFor(post(urlEqualTo("/serviceAuth/lease"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwiaWF0IjoxNTE2MjM5MDIyfQ.L8i6g3PfcHlioHCCPURC9pmXT7gdJpx3kOoyAfNUwCc")));
    }
}
