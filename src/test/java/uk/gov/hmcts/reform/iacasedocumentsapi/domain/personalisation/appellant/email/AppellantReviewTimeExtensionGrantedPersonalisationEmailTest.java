package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.email;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.TimeExtensionStatus.SUBMITTED;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State.AWAITING_REASONS_FOR_APPEAL;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.email.AppellantReviewTimeExtensionGrantedPersonalisationEmail;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.TimeExtension;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.TimeExtensionDecision;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.TimeExtensionStatus;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.TimeExtensionFinder;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AppellantReviewTimeExtensionGrantedPersonalisationEmailTest {

    @Mock
    Callback<AsylumCase> callback;
    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;
    @Mock
    TimeExtensionFinder timeExtensionFinder;

    private Long caseId = 12345L;
    private String smsTemplateId = "someEmailTemplateId";
    private String iaAipFrontendUrl = "http://localhost";

    private String mockedAppealReferenceNumber = "someReferenceNumber";
    private String mockedAppealHomeOfficeReferenceNumber = "someHomeOfficeReferenceNumber";
    private String mockedAppellantGivenNames = "someAppellantGivenNames";
    private String mockedAppellantFamilyName = "someAppellantFamilyName";
    private String mockedAppellantEmailAddress = "appelant@example.net";

    private String timeExtensionRequestDate = "2020-03-01";
    private String timeExtensionNewDate = "2020-04-01";
    private String expectedTimeExtensionNewDate = "1 Apr 2020";
    private String timeExtensionReason = "the reason";


    private IdValue<TimeExtension> mockedTimeExtension;

    private AppellantReviewTimeExtensionGrantedPersonalisationEmail
        appellantReviewTimeExtensionGrantedPersonalisationEmail;

    @BeforeEach
    public void setup() {

        mockedTimeExtension = new IdValue<>("someId", new TimeExtension(
            timeExtensionRequestDate,
            timeExtensionReason,
            AWAITING_REASONS_FOR_APPEAL,
            SUBMITTED,
            null,
            TimeExtensionDecision.GRANTED,
            null,
            timeExtensionNewDate)
        );

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(mockedAppealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(mockedAppealHomeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(mockedAppellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(mockedAppellantFamilyName));

        appellantReviewTimeExtensionGrantedPersonalisationEmail =
            new AppellantReviewTimeExtensionGrantedPersonalisationEmail(
                smsTemplateId,
                iaAipFrontendUrl,
                recipientsFinder,
                timeExtensionFinder
            );
    }

    @Test
    public void should_return_given_template_id() {
        Assert.assertEquals(smsTemplateId, appellantReviewTimeExtensionGrantedPersonalisationEmail.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        Assert.assertEquals(caseId + "_REVIEW_TIME_EXTENSION_GRANTED_APPELLANT_AIP_EMAIL",
            appellantReviewTimeExtensionGrantedPersonalisationEmail.getReferenceId(caseId));
    }

    @Test
    public void should_throw_exception_on_recipients_when_case_is_null() {

        when(recipientsFinder.findAll(null, NotificationType.EMAIL))
            .thenThrow(new NullPointerException("asylumCase must not be null"));

        assertThatThrownBy(() -> appellantReviewTimeExtensionGrantedPersonalisationEmail.getRecipientsList(null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_return_given_mobile_mobile_list_from_subscribers_in_asylum_case() {

        when(recipientsFinder.findAll(asylumCase, NotificationType.EMAIL))
            .thenReturn(Collections.singleton(mockedAppellantEmailAddress));

        assertTrue(appellantReviewTimeExtensionGrantedPersonalisationEmail.getRecipientsList(asylumCase)
            .contains(mockedAppellantEmailAddress));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> appellantReviewTimeExtensionGrantedPersonalisationEmail
            .getPersonalisation((Callback<AsylumCase>) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        String awaitingReasonsForAppealNextActionText = "why you think the Home Office decision is wrong";

        when(callback.getCaseDetails())
            .thenReturn(new CaseDetails<>(1L, "IA", AWAITING_REASONS_FOR_APPEAL, asylumCase, LocalDateTime.now()));
        when(timeExtensionFinder
            .findCurrentTimeExtension(AWAITING_REASONS_FOR_APPEAL, TimeExtensionStatus.GRANTED, asylumCase))
            .thenReturn(mockedTimeExtension);
        when(timeExtensionFinder.findNextActionText(AWAITING_REASONS_FOR_APPEAL))
            .thenReturn(awaitingReasonsForAppealNextActionText);

        Map<String, String> personalisation =
            appellantReviewTimeExtensionGrantedPersonalisationEmail.getPersonalisation(callback);
        assertEquals(mockedAppealReferenceNumber, personalisation.get("Appeal Ref Number"));
        assertEquals(mockedAppealHomeOfficeReferenceNumber, personalisation.get("HO Ref Number"));
        assertEquals(mockedAppellantGivenNames, personalisation.get("Given names"));
        assertEquals(mockedAppellantFamilyName, personalisation.get("Family name"));
        assertEquals(awaitingReasonsForAppealNextActionText, personalisation.get("Next action text"));
        assertEquals(expectedTimeExtensionNewDate, personalisation.get("due date"));
        assertEquals(iaAipFrontendUrl, personalisation.get("Hyperlink to service"));

    }

    @Test
    public void should_return_personalisation_when_only_mandatory_information_given() {

        String awaitingReasonsForAppealNextActionText = "why you think the Home Office decision is wrong";

        when(callback.getCaseDetails())
            .thenReturn(new CaseDetails<>(1L, "IA", AWAITING_REASONS_FOR_APPEAL, asylumCase, LocalDateTime.now()));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(timeExtensionFinder
            .findCurrentTimeExtension(AWAITING_REASONS_FOR_APPEAL, TimeExtensionStatus.GRANTED, asylumCase))
            .thenReturn(mockedTimeExtension);
        when(timeExtensionFinder.findNextActionText(AWAITING_REASONS_FOR_APPEAL))
            .thenReturn(awaitingReasonsForAppealNextActionText);

        Map<String, String> personalisation =
            appellantReviewTimeExtensionGrantedPersonalisationEmail.getPersonalisation(callback);
        assertEquals("", personalisation.get("Appeal Ref Number"));
        assertEquals("", personalisation.get("HO Ref Number"));
        assertEquals("", personalisation.get("Given names"));
        assertEquals("", personalisation.get("Family name"));
        assertEquals(awaitingReasonsForAppealNextActionText, personalisation.get("Next action text"));
        assertEquals(expectedTimeExtensionNewDate, personalisation.get("due date"));
        assertEquals(iaAipFrontendUrl, personalisation.get("Hyperlink to service"));
    }
}
