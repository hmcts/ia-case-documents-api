package uk.gov.hmcts.reform.iacasenotificationsapi.component.testutils;

import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.iacasenotificationsapi.component.testutils.StaticPortWiremockFactory.WIREMOCK_PORT;

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
import uk.gov.hmcts.reform.iacasenotificationsapi.Application;

@SpringBootTest(classes = {
    TestConfiguration.class,
    Application.class
})
@TestPropertySource(properties = {
    "CCD_URL=http://127.0.0.1:" + WIREMOCK_PORT + "/ccd",
    "IDAM_URL=http://127.0.0.1:" + WIREMOCK_PORT + "/userAuth",
    "OPEN_ID_IDAM_URL=http://127.0.0.1:" + WIREMOCK_PORT + "/userAuth",
    "S2S_URL=http://127.0.0.1:" + WIREMOCK_PORT + "/serviceAuth",
    "IA_CASE_DOCUMENTS_API_URL=http://localhost:" + WIREMOCK_PORT + "/ia-case-documents-api",
    "govnotify.baseUrl=http://localhost:" + WIREMOCK_PORT,
    "govnotify.key=test_key-7f72d0fb-2bc4-421b-bceb-1bf5bf350ff9-3df5a74b-f25b-4052-b00f-3f71d33cd0eb"
})
@ExtendWith({
    WiremockResolver.class
})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("integration")
public class SpringBootIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    private WebApplicationContext wac;

    @BeforeEach
    void setUp() {
        WebRequestTrackingFilter filter;
        filter = new WebRequestTrackingFilter();
        filter.init(new MockFilterConfig());
        mockMvc = webAppContextSetup(wac).addFilters(filter).build();
    }

}

