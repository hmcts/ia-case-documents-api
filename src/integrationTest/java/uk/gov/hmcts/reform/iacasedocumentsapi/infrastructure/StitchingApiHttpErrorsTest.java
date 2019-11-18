package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.SpringBootIntegrationTest;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.utilities.AsylumCaseFixtures;
import uk.gov.hmcts.reform.iacasedocumentsapi.utilities.CallbackBuilder;
import uk.gov.hmcts.reform.iacasedocumentsapi.utilities.CaseDetailsBuilder;

@SuppressWarnings("unchecked")
public class StitchingApiHttpErrorsTest extends SpringBootIntegrationTest {

    private static final String STITCH_API_PATH = "/api/stitch-ccd-bundles";

    @Test
    public void should_return_500_with_correct_message_when_api_returns_500() throws Exception {

        stubFor(post(urlPathEqualTo(STITCH_API_PATH))
            .willReturn(aResponse()
                .withStatus(500)
                .withHeader("Content-Type", "application/json")
                .withBody("Error from stitching-api"))
        );

        AsylumCase asylumCase = AsylumCaseFixtures.someAsylumCaseWithDefaults();

        CaseDetails caseDetails =
            CaseDetailsBuilder.caseDetailsBuilder()
                .jurisdiction("IA")
                .state(State.FINAL_BUNDLING)
                .id(1L)
                .caseData(asylumCase)
                .createdDate(LocalDateTime.now())
                .build();

        Callback<AsylumCase> callback = CallbackBuilder.callbackBuilder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(Optional.empty())
            .event(Event.GENERATE_HEARING_BUNDLE)
            .build();

        MvcResult mvcResult = iaApiClient.aboutToSubmit(callback, HttpStatus.INTERNAL_SERVER_ERROR);

        String content = mvcResult.getResponse().getContentAsString();

        assertNotNull(content);
        assertThat(content).startsWith("Couldn't create bundle using API: ");
        assertThat(content).contains(STITCH_API_PATH);

    }

    @Test
    public void should_return_500_with_correct_message_when_api_returns_400() throws Exception {

        stubFor(post(urlPathEqualTo(STITCH_API_PATH))
            .willReturn(aResponse()
                .withStatus(400)
                .withHeader("Content-Type", "application/json")
                .withBody("Error from stitching-api"))
        );

        AsylumCase asylumCase = AsylumCaseFixtures.someAsylumCaseWithDefaults();

        CaseDetails caseDetails =
            CaseDetailsBuilder.caseDetailsBuilder()
                .jurisdiction("IA")
                .state(State.FINAL_BUNDLING)
                .id(1L)
                .caseData(asylumCase)
                .createdDate(LocalDateTime.now())
                .build();

        Callback<AsylumCase> callback = CallbackBuilder.callbackBuilder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(Optional.empty())
            .event(Event.GENERATE_HEARING_BUNDLE)
            .build();

        MvcResult mvcResult = iaApiClient.aboutToSubmit(callback, HttpStatus.INTERNAL_SERVER_ERROR);
        String content = mvcResult.getResponse().getContentAsString();

        assertNotNull(content);
        assertThat(content).startsWith("Couldn't create bundle using API: ");
        assertThat(content).contains(STITCH_API_PATH);

    }


}
