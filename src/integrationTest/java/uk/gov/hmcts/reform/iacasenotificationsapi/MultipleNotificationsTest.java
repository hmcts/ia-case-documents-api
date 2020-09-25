package uk.gov.hmcts.reform.iacasenotificationsapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.iacasenotificationsapi.component.testutils.SpringBootIntegrationTest;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.NotificationSender;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.security.CcdEventAuthorizor;

@Slf4j
public class MultipleNotificationsTest extends SpringBootIntegrationTest {

    @MockBean
    private CcdEventAuthorizor ccdEventAuthorizor;
    @MockBean
    private NotificationSender notificationSender;

    private static final String ABOUT_TO_START_PATH = "/asylum/ccdAboutToStart";
    private static final String ABOUT_TO_SUBMIT_PATH = "/asylum/ccdAboutToSubmit";

    private List<Map.Entry<Event, String>> eventAndNotificationSuffixPair =
        Lists.newArrayList(
            new HashMap.SimpleImmutableEntry<>(Event.SUBMIT_APPEAL, "_APPEAL_SUBMITTED_CASE_OFFICER"),
            new HashMap.SimpleImmutableEntry<>(Event.UPLOAD_RESPONDENT_EVIDENCE, "_BUILD_CASE_DIRECTION"),
            new HashMap.SimpleImmutableEntry<>(Event.REQUEST_HEARING_REQUIREMENTS, "_LEGAL_REPRESENTATIVE_HEARING_REQUIREMENTS_DIRECTION"),
            new HashMap.SimpleImmutableEntry<>(Event.ADD_APPEAL_RESPONSE, "_LEGAL_REPRESENTATIVE_REVIEW_DIRECTION"),
            new HashMap.SimpleImmutableEntry<>(Event.REQUEST_RESPONDENT_EVIDENCE, "_RESPONDENT_EVIDENCE_DIRECTION"),
            new HashMap.SimpleImmutableEntry<>(Event.SEND_DIRECTION, "_RESPONDENT_NON_STANDARD_DIRECTION"),
            new HashMap.SimpleImmutableEntry<>(Event.REQUEST_RESPONDENT_REVIEW, "_RESPONDENT_REVIEW_DIRECTION"));

    @Test
    @WithMockUser(authorities = {"caseworker-ia", "caseworker-ia-caseofficer"})
    public void should_send_multiple_notifications_of_same_type_with_unique_reference_numbers() {

        String notificationId = "test-notification-id";

        eventAndNotificationSuffixPair.forEach(eventPair -> {
                try {
                    runTestScenario(notificationId, eventPair);
                } catch (Exception e) {
                    e.printStackTrace();
                    assert false;
                }
            }
        );
    }

    public void runTestScenario(String notificationId, Map.Entry<Event, String> eventWithSuffixPair) throws Exception {

        log.info("Scenario eventId: {} expecting suffix: {}", eventWithSuffixPair.getKey(), eventWithSuffixPair.getValue());

        long caseDetailsId = 1L;

        String existingReference =
            caseDetailsId
            + eventWithSuffixPair.getValue();

        List<IdValue<String>> existingNotifications =
            Lists.newArrayList(new IdValue<>(existingReference, notificationId));

        when(notificationSender.sendEmail(anyString(), anyString(), anyMap(), anyString())).thenReturn(notificationId);

        Direction direction = createExistingDirection(eventWithSuffixPair.getKey());

        AsylumCase caseData = new AsylumCase();
        caseData.write(DIRECTIONS, Collections.singletonList(new IdValue<>("1", direction)));
        caseData.write(NOTIFICATIONS_SENT, existingNotifications);
        caseData.write(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, "someone@somewhere.com");
        caseData.write(HEARING_CENTRE, HearingCentre.MANCHESTER);
        caseData.write(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.APPEAL_SUBMITTED);

        CaseDetails<AsylumCase> caseDetails = new CaseDetails<>(
            caseDetailsId,
            "IA",
            State.APPEAL_SUBMITTED,
            caseData,
            LocalDateTime.now()
        );

        Callback<AsylumCase> callback = new Callback<>(caseDetails,
            Optional.empty(),
            eventWithSuffixPair.getKey());

        final String json = objectMapper.writeValueAsString(callback);

        final PreSubmitCallbackResponse<AsylumCase> callbackResponse = doPost(
            ABOUT_TO_SUBMIT_PATH,
            MediaType.APPLICATION_JSON,
            json,
            HttpStatus.OK.value()
        );

        AsylumCase asylumCaseResponse = callbackResponse.getData();

        assertThat(asylumCaseResponse).isNotNull();
        assertThat(asylumCaseResponse.read(NOTIFICATIONS_SENT).isPresent()).isTrue();

        Optional<List<IdValue<String>>> maybeNotificationsSent = asylumCaseResponse.read(NOTIFICATIONS_SENT);

        List<IdValue<String>> allNotifications =
            maybeNotificationsSent.orElseThrow(IllegalStateException::new);

        assertThat(allNotifications.size()).isGreaterThan(1);
        assertThat(allNotifications.get(0).getId()).isNotEqualTo(allNotifications.get(1).getId());

    }

    private PreSubmitCallbackResponse<AsylumCase> doPost(
        final String path,
        final MediaType mediaType,
        final String content,
        final int expectedHttpStatus
    ) throws Exception {
        MvcResult mvcResult = mockMvc.perform(post(path)
            .contentType(mediaType).content(content))
            .andExpect(status().is(expectedHttpStatus)).andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();

        return objectMapper.readValue(jsonResponse,
            new TypeReference<PreSubmitCallbackResponse<AsylumCase>>() {
            }
        );

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
                party = Parties.RESPONDENT;
                directionTag = DirectionTag.NONE;
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
            Collections.emptyList());
    }

}
