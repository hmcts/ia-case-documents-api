package uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.iacasedocumentsapi.Application;

@SpringBootTest(classes = {
    TestConfiguration.class,
    Application.class
})
@TestPropertySource(properties = {
    "S2S_URL=http://127.0.0.1:8992/serviceAuth",
    "IDAM_URL=http://127.0.0.1:8992/userAuth",
    "OPEN_ID_IDAM_URL=http://127.0.0.1:8992/userAuth",
    "case_document_am.url=http://127.0.0.1:8992",
    "docmosis.endpoint=http://127.0.0.1:8992",
    "docmosis.render.uri=/docmosis",
    "ccdGatewayUrl=http://127.0.0.1:8992",
    "ROLE_ASSIGNMENT_URL=http://127.0.0.1:8992/amRoleAssignment",
    "emBundler.url=http://127.0.0.1:8992"})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("integration")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class SpringBootIntegrationTest {

    protected GivensBuilder given;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected IaCaseDocumentsApiClient iaCaseDocumentsApiClient;

    @Autowired
    private WebApplicationContext wac;

    protected static WireMockServer server;

    @BeforeEach
    public void setUpApiClient() {
        iaCaseDocumentsApiClient = new IaCaseDocumentsApiClient(objectMapper, mockMvc);
    }

    @BeforeAll
    public void spinUp() {
        server = new WireMockServer(WireMockConfiguration.options()
            .notifier(new Slf4jNotifier(true))
            .port(8992));
        server.start();
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
