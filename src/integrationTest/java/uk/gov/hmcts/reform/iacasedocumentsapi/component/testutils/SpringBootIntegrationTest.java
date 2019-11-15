package uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.matching.MultipartValuePattern.MatchingType;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.iacasedocumentsapi.utilities.AsylumCaseFixtures.someUploadResponse;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.iacasedocumentsapi.Application;
import uk.gov.hmcts.reform.iacasedocumentsapi.utilities.AsylumCaseFixtures;
import uk.gov.hmcts.reform.iacasedocumentsapi.utilities.DocmosisStub;
import uk.gov.hmcts.reform.iacasedocumentsapi.utilities.IaApiClient;

@TestPropertySource(properties = {
        "auth.idam.client.baseUrl=http://127.0.0.1:8990",
        "docmosis.endpoint=http://127.0.0.1:8990",
        "docmosis.render.uri=/docmosis",
        "idam.s2s-auth.url=http://127.0.0.1:8990",
        "ccdGatewayUrl=http://127.0.0.1:8990",
        "emBundler.url=http://127.0.0.1:8990"})
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = MOCK)
@ActiveProfiles("integration")
public abstract class SpringBootIntegrationTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8990);

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    protected IaApiClient iaApiClient;
    protected DocmosisStub docmosisStub = new DocmosisStub();

    @Before
    public void setUpTestInfrastructure() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
        this.iaApiClient = new IaApiClient(mockMvc);
        this.docmosisStub.withDefaults();
    }

    @Before
    public void setupIdamStubs() {

        stubFor(get(urlEqualTo("/details"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(AsylumCaseFixtures.someUserDetails())));
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

        stubFor(post(urlEqualTo("/lease"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJpYWMiLCJleHAiOjE1NzE4NTQyMTl9.Hbxx-w6_kRm1FZcxAJsMMENjFhNLmFP8BBgjCDCpaDgIFhgQcBv5Yh8MMRkk6x2iFwB9JVh0mGQ17YaM4wcjYg")));
    }

}
