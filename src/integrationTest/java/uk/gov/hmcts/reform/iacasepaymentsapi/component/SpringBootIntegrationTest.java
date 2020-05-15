package uk.gov.hmcts.reform.iacasepaymentsapi.component;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertNotNull;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.iacasepaymentsapi.Application;
import uk.gov.hmcts.reform.iacasepaymentsapi.component.testutils.IaCasePaymentsApiClient;

@ActiveProfiles("integration")
@RunWith(SpringRunner.class)
@TestPropertySource(properties = {
    "FEES_REGISTER_API_URL=http://localhost:8990"
})
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class SpringBootIntegrationTest {

    protected IaCasePaymentsApiClient iaCasePaymentsApiClient;

    private String feeRegisterApiUri = "/fees-register/fees/lookup";

    @Value("classpath:fees-register-api-response.json")
    private Resource resourceFile;

    @LocalServerPort
    protected int port;

    private WireMockServer wireMockServer;

    @BeforeEach
    public void setUp() {
        wireMockServer = new WireMockServer(wireMockConfig().port(8990));
        wireMockServer.start();
        configureFor("127.0.0.1", 8990);
    }

    @BeforeEach
    public void setUpApiClient() {
        iaCasePaymentsApiClient = new IaCasePaymentsApiClient(port);
    }

    @BeforeEach
    public void setUpFeeRegisterOralFeeStub() throws IOException {

        String feeResponseJson =
            new String(Files.readAllBytes(Paths.get(resourceFile.getURI())));

        assertNotNull(feeResponseJson);

        Map<String, StringValuePattern> queryParams = getOralFeeRequestParams();
        stubFor(get(urlPathEqualTo(feeRegisterApiUri))
            .withQueryParams(queryParams)
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-type", "Application/json")
                    .withBody(feeResponseJson)));
    }

    private Map<String, StringValuePattern> getOralFeeRequestParams() {

        Map<String, StringValuePattern> queryParams = new HashMap<>();
        queryParams.put("channel", equalTo("default"));
        queryParams.put("event", equalTo("issue"));
        queryParams.put("jurisdiction1", equalTo("tribunal"));
        queryParams.put("jurisdiction2", containing("immigration and asylum chamber"));
        queryParams.put("keyword", equalTo("ABC"));
        queryParams.put("service", equalTo("other"));

        return queryParams;
    }

    @AfterEach
    public void tearDown() {
        wireMockServer.stop();
    }

}
