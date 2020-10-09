package uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils;

import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.StaticPortWiremockFactory.WIREMOCK_PORT;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.applicationinsights.web.internal.WebRequestTrackingFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import ru.lanwen.wiremock.ext.WiremockResolver;
import uk.gov.hmcts.reform.iacasedocumentsapi.Application;

@ActiveProfiles("integration")
@ExtendWith({
        WiremockResolver.class
})
@TestPropertySource(properties = {
    "S2S_URL=http://127.0.0.1:" + WIREMOCK_PORT + "/serviceAuth",
    "IDAM_URL=http://127.0.0.1:" + WIREMOCK_PORT + "/userAuth",
    "OPEN_ID_IDAM_URL=http://127.0.0.1:" + WIREMOCK_PORT + "/userAuth",
    "docmosis.endpoint=http://127.0.0.1:" + WIREMOCK_PORT,
    "docmosis.render.uri=/docmosis",
    "ccdGatewayUrl=http://127.0.0.1:" + WIREMOCK_PORT,
    "emBundler.url=http://127.0.0.1:" + WIREMOCK_PORT})
@AutoConfigureMockMvc(addFilters = false)
@SpringBootTest(classes = {TestConfiguration.class, Application.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class SpringBootIntegrationTest {

    protected GivensBuilder given;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected IaCaseDocumentsApiClient iaCaseDocumentsApiClient;

    @Autowired
    private WebApplicationContext wac;

    @BeforeEach
    void setUp() {
        WebRequestTrackingFilter filter;
        filter = new WebRequestTrackingFilter();
        filter.init(new MockFilterConfig());
        mockMvc = webAppContextSetup(wac).addFilters(filter).build();
    }

    @BeforeEach
    public void setUpApiClient() {
        iaCaseDocumentsApiClient = new IaCaseDocumentsApiClient(objectMapper, mockMvc);
    }
}
