package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.sms;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.sms.AppellantRequestHearingRequirementsPersonalisationSms;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DirectionFinder;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AppellantRequestHearingRequirementsPersonalisationSmsTest {
    private final Long caseId = 12345L;
    private final String smsTemplateId = "someSmsTemplateId";
    private final String iaAipFrontendUrl = "http://localhost";
    private final String mockedAppealReferenceNumber = "someReferenceNumber";
    private final String mockedAppellantMobilePhone = "07123456789";
    private final String directionDueDate = "2019-08-27";
    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;
    @Mock
    DirectionFinder directionFinder;
    @Mock
    Direction direction;
    private AppellantRequestHearingRequirementsPersonalisationSms appellantRequestHearingRequirementsPersonalisationSms;

    @BeforeEach
    public void setup() {

        when((direction.getDateDue())).thenReturn(directionDueDate);
        when(directionFinder.findFirst(asylumCase, DirectionTag.LEGAL_REPRESENTATIVE_HEARING_REQUIREMENTS))
            .thenReturn(Optional.of(direction));

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(mockedAppealReferenceNumber));

        appellantRequestHearingRequirementsPersonalisationSms = new AppellantRequestHearingRequirementsPersonalisationSms(
            smsTemplateId,
            iaAipFrontendUrl,
            recipientsFinder,
            directionFinder);
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(smsTemplateId, appellantRequestHearingRequirementsPersonalisationSms.getTemplateId());
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_REQUEST_HEARING_REQUIREMENTS_DIRECTION_AIP_SMS",
            appellantRequestHearingRequirementsPersonalisationSms.getReferenceId(caseId));
    }

    @Test
    void should_throw_exception_on_recipients_when_case_is_null() {

        when(recipientsFinder.findAll(null, NotificationType.SMS))
            .thenThrow(new NullPointerException("asylumCase must not be null"));

        assertThatThrownBy(() -> appellantRequestHearingRequirementsPersonalisationSms.getRecipientsList(null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    void should_throw_exception_on_personalisation_when_direction_is_empty() {

        when(directionFinder.findFirst(asylumCase, DirectionTag.LEGAL_REPRESENTATIVE_HEARING_REQUIREMENTS)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appellantRequestHearingRequirementsPersonalisationSms.getPersonalisation(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Appellant request hearing requirements direction is not present");
    }


    @Test
    void should_return_given_mobile_mobile_list_from_subscribers_in_asylum_case() {

        when(recipientsFinder.findAll(asylumCase, NotificationType.SMS))
            .thenReturn(Collections.singleton(mockedAppellantMobilePhone));

        assertTrue(appellantRequestHearingRequirementsPersonalisationSms.getRecipientsList(asylumCase)
            .contains(mockedAppellantMobilePhone));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
            () -> appellantRequestHearingRequirementsPersonalisationSms.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    void should_return_personalisation_when_all_information_given() {
        Map<String, String> personalisation =
            appellantRequestHearingRequirementsPersonalisationSms.getPersonalisation(asylumCase);
        assertEquals(mockedAppealReferenceNumber, personalisation.get("Appeal Ref Number"));
        assertEquals(iaAipFrontendUrl, personalisation.get("Hyperlink to service"));
        assertEquals("27 Aug 2019", personalisation.get("dueDate"));

    }

    @Test
    void should_return_personalisation_when_only_mandatory_information_given() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation =
            appellantRequestHearingRequirementsPersonalisationSms.getPersonalisation(asylumCase);

        assertEquals("", personalisation.get("Appeal Ref Number"));
        assertEquals(iaAipFrontendUrl, personalisation.get("Hyperlink to service"));
        assertEquals("27 Aug 2019", personalisation.get("dueDate"));
    }
}
