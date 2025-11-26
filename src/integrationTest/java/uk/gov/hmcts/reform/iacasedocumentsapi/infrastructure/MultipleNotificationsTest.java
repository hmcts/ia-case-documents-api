package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure;

import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.fixtures.AsylumCaseForTest.anAsylumCase;
import static uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.fixtures.CallbackForTest.CallbackForTestBuilder.callback;
import static uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.fixtures.CaseDetailsForTest.CaseDetailsForTestBuilder.someCaseDetailsWith;
import static uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.fixtures.UserDetailsForTest.UserDetailsForTestBuilder.userWith;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.DIRECTIONS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LEGAL_REPRESENTATIVE_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.NOTIFICATIONS_SENT;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.OUT_OF_TIME_DECISION_TYPE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.SpringBootIntegrationTest;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.WithDocumentUploadStub;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.WithIdamStub;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.WithRoleAssignmentStub;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.WithServiceAuthStub;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.WithDocmosisStub;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.fixtures.AsylumCaseForTest;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.fixtures.PreSubmitCallbackResponseForTest;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.GovNotifyNotificationSender;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.security.CcdEventAuthorizor;

@Slf4j
class MultipleNotificationsTest extends SpringBootIntegrationTest implements WithServiceAuthStub, WithDocmosisStub,
    WithIdamStub, WithRoleAssignmentStub, WithDocumentUploadStub {

    private static final String ABOUT_TO_SUBMIT_PATH = "/asylum/ccdAboutToSubmit";

    @MockBean
    private CcdEventAuthorizor ccdEventAuthorizor;

    @MockBean
    private GovNotifyNotificationSender notificationSender;

    private final List<Map.Entry<Event, String>> eventAndNotificationSuffixPair =
        List.of(
            new HashMap.SimpleImmutableEntry<>(Event.SUBMIT_APPEAL, "_APPEAL_SUBMITTED_CASE_OFFICER"),
            new HashMap.SimpleImmutableEntry<>(Event.UPLOAD_RESPONDENT_EVIDENCE, "_BUILD_CASE_DIRECTION"),
            new HashMap.SimpleImmutableEntry<>(
                Event.REQUEST_HEARING_REQUIREMENTS,
                "_LEGAL_REPRESENTATIVE_HEARING_REQUIREMENTS_DIRECTION"
            ),
            new HashMap.SimpleImmutableEntry<>(Event.ADD_APPEAL_RESPONSE, "_LEGAL_REPRESENTATIVE_REVIEW_DIRECTION"),
            new HashMap.SimpleImmutableEntry<>(Event.SEND_DIRECTION, "_RESPONDENT_NON_STANDARD_DIRECTION"),
            new HashMap.SimpleImmutableEntry<>(Event.REQUEST_RESPONDENT_REVIEW, "_RESPONDENT_REVIEW_DIRECTION")
        );

    @Test
    @WithMockUser(authorities = {"caseworker-ia", "tribunal-caseworker"})
    void should_send_multiple_notifications_of_same_type_with_unique_reference_numbers() throws JsonProcessingException {

        addServiceAuthStub(server);
        addDocmosisStub(server);
        addRoleAssignmentActorStub(server);
        addIdamTokenStub(server);
        addDocumentUploadStub(server);
        someLoggedIn(
            userWith()
                .roles(newHashSet("caseworker-ia", "tribunal-caseworker"))
                .forename("Case")
                .surname("Officer"), server
        );
        String notificationId = "test-notification-id";

        eventAndNotificationSuffixPair
            .forEach(eventPair -> {
                         try {
                             runTestScenario(notificationId, eventPair);
                         } catch (Exception e) {
                             e.printStackTrace();
                             assert false;
                         }
                     }
            );
    }

    private void runTestScenario(String notificationId, Map.Entry<Event, String> eventWithSuffixPair) {

        log.info(
            "Scenario eventId: {} expecting suffix: {}", eventWithSuffixPair.getKey(),
            eventWithSuffixPair.getValue()
        );

        long caseDetailsId = 1L;

        String existingReference =
            caseDetailsId
                + eventWithSuffixPair.getValue();

        List<IdValue<String>> existingNotifications =
            List.of(new IdValue<>(existingReference + "_existing", notificationId));

        when(notificationSender.sendEmail(
            anyString(),
            anyString(),
            anyMap(),
            anyString(),
            any(Callback.class)
        )).thenReturn(notificationId);

        Direction direction = createExistingDirection(eventWithSuffixPair.getKey());

        PreSubmitCallbackResponseForTest callbackResponse = iaCaseDocumentsApiClient.aboutToSubmit(
            callback()
                .event(eventWithSuffixPair.getKey())
                .caseDetails(someCaseDetailsWith()
                                 .id(caseDetailsId)
                                 .jurisdiction("IA")
                                 .state(State.APPEAL_SUBMITTED)
                                 .caseData(anAsylumCase()
                                               .with(
                                                   DIRECTIONS,
                                                   Collections.singletonList(new IdValue<>("1", direction))
                                               )
                                               .with(NOTIFICATIONS_SENT, existingNotifications)
                                               .with(APPEAL_REFERENCE_NUMBER, "PA/12345/2025")
                                               .with(APPELLANT_FAMILY_NAME, "some-name")
                                               .with(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, "someone@somewhere.com")
                                               .with(HEARING_CENTRE, HearingCentre.MANCHESTER)
                                               .with(
                                                   CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL,
                                                   State.APPEAL_SUBMITTED
                                               )
                                               .with(OUT_OF_TIME_DECISION_TYPE, OutOfTimeDecisionType.APPROVED))
                                 .createdDate(LocalDateTime.now())));

        AsylumCase asylumCaseResponse = callbackResponse.getAsylumCase();

        assertThat(asylumCaseResponse).isNotNull();
        assertThat(asylumCaseResponse.read(NOTIFICATIONS_SENT).isPresent()).isTrue();

        Optional<List<IdValue<String>>> maybeNotificationsSent = asylumCaseResponse.read(NOTIFICATIONS_SENT);

        List<IdValue<String>> allNotifications =
            maybeNotificationsSent.orElseThrow(IllegalStateException::new);

        assertTrue(allNotifications.size() > 1);
        assertNotEquals(allNotifications.get(0).getId(), allNotifications.get(1).getId());
    }

    private Direction createExistingDirection(Event event) {

        String dateFormat = "yyyy-MM-dd";
        Parties party = Parties.RESPONDENT;
        DirectionTag directionTag = DirectionTag.NONE;

        switch (event) {
            case SUBMIT_APPEAL:
                break;
            case UPLOAD_RESPONDENT_EVIDENCE:
                directionTag = DirectionTag.BUILD_CASE;
                break;
            case REQUEST_HEARING_REQUIREMENTS:
                directionTag = DirectionTag.LEGAL_REPRESENTATIVE_HEARING_REQUIREMENTS;
                break;
            case ADD_APPEAL_RESPONSE:
                directionTag = DirectionTag.LEGAL_REPRESENTATIVE_REVIEW;
                break;
            case REQUEST_RESPONDENT_EVIDENCE:
                directionTag = DirectionTag.RESPONDENT_EVIDENCE;
                break;
            case SEND_DIRECTION:
                break;
            case REQUEST_RESPONDENT_REVIEW:
                directionTag = DirectionTag.RESPONDENT_REVIEW;
                break;
            default:
                throw new IllegalStateException("unexpected event");
        }

        return new Direction(
            "some-explanation",
            party,
            LocalDate.now().plusDays(7L).format(DateTimeFormatter.ofPattern(dateFormat)),
            LocalDate.now().format(DateTimeFormatter.ofPattern(dateFormat)),
            directionTag,
            Collections.emptyList(),
            Collections.emptyList(),
            UUID.randomUUID().toString(),
            "someDirectionType"
        );
    }
}
