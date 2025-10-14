package uk.gov.hmcts.reform.iacasedocumentsapi.component;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.fixtures.AsylumCaseCollectionForTest.someListOf;
import static uk.gov.hmcts.reform.iacasenotificationsapi.component.testutils.fixtures.AsylumCaseForTest.anAsylumCase;
import static uk.gov.hmcts.reform.iacasenotificationsapi.component.testutils.fixtures.CallbackForTest.CallbackForTestBuilder.callback;
import static uk.gov.hmcts.reform.iacasenotificationsapi.component.testutils.fixtures.CaseDetailsForTest.CaseDetailsForTestBuilder.someCaseDetailsWith;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.DIRECTIONS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REPRESENTATIVE_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.NOTIFICATIONS_SENT;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event.SEND_DIRECTION;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State.APPEAL_SUBMITTED;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.WithNotificationEmailStub;
import uk.gov.hmcts.reform.iacasenotificationsapi.component.testutils.fixtures.CallbackForTest;
import uk.gov.hmcts.reform.iacasenotificationsapi.component.testutils.fixtures.PreSubmitCallbackResponseForTest;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;

@Slf4j
@SuppressWarnings("unchecked")
class SendsDirectionTest extends SpringBootIntegrationTest implements WithServiceAuthStub,
    WithNotificationEmailStub {

    private static final String someNotificationId = UUID.randomUUID().toString();
    private static final String UUID_PATTERN =
        "[0-9a-fA-F]{8}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{12}";
    @MockBean
    private GovNotifyNotificationSender notificationSender;

    @Test
    @WithMockUser(authorities = {"caseworker-ia", "tribunal-caseworker"})
    void sends_notification() {

        addServiceAuthStub(server);
        addNotificationEmailStub(server);

        when(notificationSender.sendEmail(anyString(), anyString(), anyMap(), anyString(), any(Callback.class)))
            .thenReturn(someNotificationId);

        PreSubmitCallbackResponseForTest response = aboutToSubmit(callback()
            .event(SEND_DIRECTION)
            .caseDetails(someCaseDetailsWith()
                .state(APPEAL_SUBMITTED)
                .caseData(anAsylumCase()
                    .with(HEARING_CENTRE, HearingCentre.MANCHESTER)
                    .with(APPEAL_REFERENCE_NUMBER, "some-appeal-reference-number")
                    .with(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, "legalrep@domain.com")
                    .with(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, APPEAL_SUBMITTED)
                    .with(DIRECTIONS, someListOf(Direction.class)
                        .with(new Direction(
                            "exp",
                            Parties.RESPONDENT,
                            "1980-04-12",
                            "1980-04-12",
                            DirectionTag.NONE,
                            Collections.emptyList(),
                            Collections.emptyList(),
                            UUID.randomUUID().toString(),
                            "someDirectionType")
                        )))));

        Optional<List<IdValue<String>>> notificationsSent =
            response
                .getData()
                .read(NOTIFICATIONS_SENT);

        assertTrue(notificationsSent.isPresent());
        List<IdValue<String>> notifications = notificationsSent.get();

        assertThat(notifications.size()).isEqualTo(2);
        assertThat(notifications.get(0).getId()).contains("_RESPONDENT_NON_STANDARD_DIRECTION");
        assertThat(notifications.get(0).getValue()).matches(UUID_PATTERN);
    }

    private PreSubmitCallbackResponseForTest aboutToSubmit(CallbackForTest.CallbackForTestBuilder callback) {

        try {
            MvcResult response = mockMvc
                .perform(
                    post("/asylum/ccdAboutToSubmit")
                        .content(objectMapper.writeValueAsString(callback.build()))
                        .contentType(APPLICATION_JSON_VALUE)
                )
                .andReturn();

            return objectMapper.readValue(
                response.getResponse().getContentAsString(),
                PreSubmitCallbackResponseForTest.class
            );
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            // test will fail
            throw new RuntimeException(e);
        }
    }
}
