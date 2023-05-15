package uk.gov.hmcts.reform.iacasenotificationsapi.component.testutils;

import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.microsoft.applicationinsights.web.internal.WebRequestTrackingFilter;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.iacasenotificationsapi.Application;

@SpringBootTest(classes = {
    TestConfiguration.class,
    Application.class
})
@TestPropertySource(properties = {
    "CCD_URL=http://127.0.0.1:8990/ccd",
    "IDAM_URL=http://127.0.0.1:8990/userAuth",
    "OPEN_ID_IDAM_URL=http://127.0.0.1:8990/userAuth",
    "S2S_URL=http://127.0.0.1:8990/serviceAuth",
    "IA_CASE_DOCUMENTS_API_URL=http://localhost:8990/ia-case-documents-api",
    "govnotify.baseUrl=http://localhost:8990",
    "govnotify.key=test_key-7f72d0fb-2bc4-421b-bceb-1bf5bf350ff9-3df5a74b-f25b-4052-b00f-3f71d33cd0eb"
})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("integration")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SpringBootIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    private WebApplicationContext wac;

    protected static WireMockServer server;

    @BeforeAll
    public void spinUp() {
        server = new WireMockServer(WireMockConfiguration.options()
            .notifier(new Slf4jNotifier(true))
            .port(8990));
        server.start();
    }

    @BeforeEach
    void setUp() {
        WebRequestTrackingFilter filter;
        filter = new WebRequestTrackingFilter();
        filter.init(new MockFilterConfig());
        mockMvc = webAppContextSetup(wac).addFilters(filter).build();
    }

    @AfterEach
    public void reset() {
        server.resetAll();
    }

    @AfterAll
    public void shutDown() {
        server.stop();
    }

}

