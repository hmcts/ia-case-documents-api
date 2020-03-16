package uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.matching.MultipartValuePattern.MatchingType;
import static uk.gov.hmcts.reform.iacasedocumentsapi.utilities.AsylumCaseFixtures.someUploadResponse;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.iacasedocumentsapi.Application;
import uk.gov.hmcts.reform.iacasedocumentsapi.utilities.AsylumCaseFixtures;
import uk.gov.hmcts.reform.iacasedocumentsapi.utilities.DocmosisStub;

@ActiveProfiles("integration")
@RunWith(SpringRunner.class)
@TestPropertySource(properties = {
    "S2S_URL=http://127.0.0.1:8990/serviceAuth",
    "IDAM_URL=http://127.0.0.1:8990/userAuth",
    "docmosis.endpoint=http://127.0.0.1:8990",
    "docmosis.render.uri=/docmosis",
    "ccdGatewayUrl=http://127.0.0.1:8990",
    "emBundler.url=http://127.0.0.1:8990"})
@SpringBootTest(classes = {TestConfiguration.class, Application.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class SpringBootIntegrationTest {

    static final String JWT_URL = "http://127.0.0.1:8990/userAuth/o/jwks";

    @Value("classpath:idam-jwks.json")
    private Resource resourceJwksFile;

    @Value("classpath:open-id-configuration.json")
    private Resource resourceOpenIdConfigurationFile;

    protected GivensBuilder given;

    @LocalServerPort
    protected int port;

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
        iaCaseDocumentsApiClient = new IaCaseDocumentsApiClient(port);
    }

    @Before
    public void setUpTestInfrastructure() {
        this.docmosisStub.withDefaults();
    }

    @Before
    public void setupIdamStubs() throws IOException {

        stubFor(get(urlEqualTo("/userAuth/o/.well-known/openid-configuration"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(FileUtils.readFileToString(resourceOpenIdConfigurationFile.getFile()))));

        stubFor(get(urlEqualTo("/userAuth/o/jwks"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(FileUtils.readFileToString(resourceJwksFile.getFile()))));

        stubFor(get(urlEqualTo("/userAuth/o/userinfo"))
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

        stubFor(get(urlEqualTo("/serviceAuth/details"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("ia")));

        stubFor(post(urlEqualTo("/serviceAuth/lease"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwiaWF0IjoxNTE2MjM5MDIyfQ.L8i6g3PfcHlioHCCPURC9pmXT7gdJpx3kOoyAfNUwCc")));
    }

}
