package uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.matching.MultipartValuePattern.MatchingType;
import static uk.gov.hmcts.reform.iacasedocumentsapi.utilities.AsylumCaseFixtures.someUploadResponse;

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
import uk.gov.hmcts.reform.iacasedocumentsapi.Application;
import uk.gov.hmcts.reform.iacasedocumentsapi.utilities.DocmosisStub;

@ActiveProfiles("integration")
@RunWith(SpringRunner.class)
@TestPropertySource(properties = {
    "S2S_URL=http://127.0.0.1:8990/serviceAuth",
    "IDAM_URL=http://127.0.0.1:8990/userAuth",
    "OPEN_ID_IDAM_URL=http://127.0.0.1:8990/userAuth",
    "docmosis.endpoint=http://127.0.0.1:8990",
    "docmosis.render.uri=/docmosis",
    "ccdGatewayUrl=http://127.0.0.1:8990",
    "emBundler.url=http://127.0.0.1:8990"})
@AutoConfigureMockMvc(addFilters = false)
@SpringBootTest(classes = {TestConfiguration.class, Application.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class SpringBootIntegrationTest {

    protected GivensBuilder given;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8990);

    protected IaCaseDocumentsApiClient iaCaseDocumentsApiClient;
    protected DocmosisStub docmosisStub = new DocmosisStub();

    @Before
    public void setUpGivens() {
        given = new GivensBuilder();
    }

    @Before
    public void setUpApiClient() {
        iaCaseDocumentsApiClient = new IaCaseDocumentsApiClient(objectMapper, mockMvc);
    }

    @Before
    public void setUpTestInfrastructure() {
        this.docmosisStub.withDefaults();
    }

    @Before
    public void setupDocumentUploadStub() {

        stubFor(post(urlEqualTo("/documents"))
            .withMultipartRequestBody(aMultipart().matchingType(MatchingType.ALL))
            .willReturn(aResponse()
                .withStatus(201)
                .withBody(someUploadResponse())));
    }

    @Before
    public void setupS2S() {

        stubFor(post(urlEqualTo("/serviceAuth/lease"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")));
    }

}
