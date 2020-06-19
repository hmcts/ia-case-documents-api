package uk.gov.hmcts.reform.iacasepaymentsapi.component;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertNotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.iacasepaymentsapi.Application;
import uk.gov.hmcts.reform.iacasepaymentsapi.component.testutils.IaCasePaymentsApiClient;
import uk.gov.hmcts.reform.iacasepaymentsapi.component.testutils.TestConfiguration;

@ActiveProfiles("integration")
@RunWith(SpringRunner.class)
@TestPropertySource(properties = {
    "IDAM_URL=http://127.0.0.1:8990/idam",
    "S2S_URL=http://127.0.0.1:8990/s2s",
    "IA_IDAM_CLIENT_ID=ia",
    "IA_IDAM_SECRET=something",
    "FEES_REGISTER_API_URL=http://localhost:8990",
    "PAYMENT_API_URL=http://localhost:8990"
})
@SpringBootTest(classes = {
    TestConfiguration.class,
    Application.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
public abstract class SpringBootIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    protected IaCasePaymentsApiClient iaCasePaymentsApiClient;

    private String feeRegisterApiUri = "/fees-register/fees/lookup";

    private String paymentApiUri = "/credit-account-payments";

    @Value("classpath:fees-register-api-response.json")
    private Resource resourceFile;

    @Value("classpath:fees-without-register-api-response.json")
    private Resource resourceFileWithoutHearing;

    @Value("classpath:payment-api-response.json")
    private Resource paymentResponseResourceFile;

    @Value("classpath:credit-account-payment-request_with_hearing.json")
    private Resource creditAccountPaymentHearingRequestResourceFile;

    @Value("classpath:credit-account-payment-request_without_hearing.json")
    private Resource creditAccountPaymentWithoutHearingRequestResourceFile;

    @LocalServerPort
    protected int port;

    private ObjectMapper objectMapper = new ObjectMapper();

    private WireMockServer wireMockServer;

    @BeforeEach
    public void setUp() {
        wireMockServer = new WireMockServer(wireMockConfig().port(8990));
        wireMockServer.start();
        configureFor("127.0.0.1", 8990);
    }

    @BeforeEach
    public void setUpApiClient() {
        iaCasePaymentsApiClient = new IaCasePaymentsApiClient(mockMvc, port);
    }

    @BeforeEach
    public void setupServiceAuthStub() {

        stubFor(get(urlEqualTo("/s2s/details"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("ia")));

        stubFor(post(urlEqualTo("/s2s/lease"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwiaWF0IjoxNTE2MjM5MDIyfQ."
                          + "L8i6g3PfcHlioHCCPURC9pmXT7gdJpx3kOoyAfNUwCc")));
    }

    @BeforeEach
    public void setUpFeeRegisterOralFeeStub() throws IOException {

        String feeResponseJson =
            new String(Files.readAllBytes(Paths.get(resourceFile.getURI())));

        String feeWithoutResponseJson =
            new String(Files.readAllBytes(Paths.get(resourceFileWithoutHearing.getURI())));

        assertNotNull(feeResponseJson);
        assertNotNull(feeWithoutResponseJson);

        Map<String, StringValuePattern> queryParamsFee = getFeeHearingRequestParams();
        Map<String, StringValuePattern> queryParamsWithoutFee = getWithoutFeeHearingRequestParams();

        stubFor(get(urlPathEqualTo(feeRegisterApiUri))
            .withQueryParams(queryParamsFee)
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-type", "Application/json")
                    .withBody(feeResponseJson)));

        stubFor(get(urlPathEqualTo(feeRegisterApiUri))
            .withQueryParams(queryParamsWithoutFee)
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-type", "Application/json")
                    .withBody(feeWithoutResponseJson)));
    }

    @BeforeEach
    public void setUpPaymentApiStub() throws IOException {

        String paymentResponseJson =
            new String(Files.readAllBytes(Paths.get(paymentResponseResourceFile.getURI())));

        String creditAccountPaymentHearingJson =
            new String(Files.readAllBytes(Paths.get(creditAccountPaymentHearingRequestResourceFile.getURI())));

        String creditAccountPaymentWithoutHearingJson =
            new String(Files.readAllBytes(Paths.get(creditAccountPaymentWithoutHearingRequestResourceFile.getURI())));

        assertNotNull(paymentResponseJson);

        stubFor(post(urlPathEqualTo(paymentApiUri))
            .withRequestBody(equalToJson(creditAccountPaymentHearingJson))
            .willReturn(aResponse()
                .withStatus(201)
                .withHeader("Content-type", "Application/json")
                .withBody(paymentResponseJson)));

        stubFor(post(urlPathEqualTo(paymentApiUri))
                    .withRequestBody(equalToJson(creditAccountPaymentWithoutHearingJson))
                    .willReturn(aResponse()
                                    .withStatus(201)
                                    .withHeader("Content-type", "Application/json")
                                    .withBody(paymentResponseJson)));
    }

    private Map<String, StringValuePattern> getFeeHearingRequestParams() {

        Map<String, StringValuePattern> queryParams = new HashMap<>();
        queryParams.put("channel", equalTo("default"));
        queryParams.put("event", equalTo("issue"));
        queryParams.put("jurisdiction1", equalTo("tribunal"));
        queryParams.put("jurisdiction2", containing("immigration and asylum chamber"));
        queryParams.put("keyword", equalTo("ABC"));
        queryParams.put("service", equalTo("other"));

        return queryParams;
    }

    private Map<String, StringValuePattern> getWithoutFeeHearingRequestParams() {

        Map<String, StringValuePattern> queryParams = new HashMap<>();
        queryParams.put("channel", equalTo("default"));
        queryParams.put("event", equalTo("issue"));
        queryParams.put("jurisdiction1", equalTo("tribunal"));
        queryParams.put("jurisdiction2", containing("immigration and asylum chamber"));
        queryParams.put("keyword", equalTo("DEF"));
        queryParams.put("service", equalTo("other"));
        return queryParams;
    }

    @AfterEach
    public void tearDown() {
        wireMockServer.stop();
    }
}
