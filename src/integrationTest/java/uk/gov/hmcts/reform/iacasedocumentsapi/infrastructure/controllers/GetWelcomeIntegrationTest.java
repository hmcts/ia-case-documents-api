package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.SpringBootIntegrationTest;

class GetWelcomeIntegrationTest extends SpringBootIntegrationTest {

    @Test
    void welcomeRootEndpoint() throws Exception {
        MvcResult response = mockMvc
            .perform(get("/"))
            .andExpect(status().isOk())
            .andReturn();

        assertEquals("{\"message\": \"Welcome to Immigration & Asylum case documents API\"}", response.getResponse().getContentAsString());
    }
}
