package uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.infrastructure.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.testutils.SpringBootIntegrationTest;

class GetWelcomeIntegrationTest extends SpringBootIntegrationTest {

    @Test
    void welcomeRootEndpoint() throws Exception {
        MvcResult response = mockMvc
            .perform(get("/"))
            .andExpect(status().isOk())
            .andReturn();

        assertEquals("Welcome to Case Payment Service", response.getResponse().getContentAsString());
    }
}
