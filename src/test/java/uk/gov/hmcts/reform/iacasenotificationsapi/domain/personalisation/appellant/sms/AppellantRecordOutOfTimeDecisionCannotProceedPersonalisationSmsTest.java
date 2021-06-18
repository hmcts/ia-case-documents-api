package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;



@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class AppellantRecordOutOfTimeDecisionCannotProceedPersonalisationSmsTest {


    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;
    @Mock
    EmailAddressFinder emailAddressFinder;


    private String recordOutOfDecisionCannotProceedTemplateId = "recordOutOfDecisionCannotProceedTemplateId";

    private Long caseId = 12345L;
    private String iaAipFrontendUrl = "http://localhost";
    private String appealReferenceNumber = "someReferenceNumber";
    private String mockedAppellantMobileNumber = "1234445556";

    private AppellantRecordOutOfTimeDecisionCannotProceedPersonalisationSms
            recordOutOfTimeDecisionCannotProceedPersonalisationSms;

    @BeforeEach
    void setUp() {

        recordOutOfTimeDecisionCannotProceedPersonalisationSms =
                new AppellantRecordOutOfTimeDecisionCannotProceedPersonalisationSms(
                        recordOutOfDecisionCannotProceedTemplateId,
                        iaAipFrontendUrl, recipientsFinder, emailAddressFinder);

    }

    @Test
    void should_return_personalisation_when_all_information_given_before_listing() {

        String designatedHearingCentre = "belfast@hearingcentre.gov";
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
                .thenReturn(Optional.empty());
        when(emailAddressFinder.getHearingCentreEmailAddress(asylumCase)).thenReturn(designatedHearingCentre);

        Map<String, String> personalisation =
                recordOutOfTimeDecisionCannotProceedPersonalisationSms.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("Appeal Ref Number"));
        assertEquals(iaAipFrontendUrl, personalisation.get("Hyperlink to service"));
        assertEquals(designatedHearingCentre, personalisation.get("designated hearing centre"));
        verify(emailAddressFinder).getHearingCentreEmailAddress(asylumCase);
        verify(emailAddressFinder, times(0)).getListCaseHearingCentreEmailAddress(asylumCase);

    }

    @Test
    void should_return_personalisation_when_all_information_given_after_listing() {

        String designatedHearingCentre = "belfast@hearingcentre.gov";
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
                .thenReturn(Optional.of(HearingCentre.BELFAST));
        when(emailAddressFinder.getListCaseHearingCentreEmailAddress(asylumCase)).thenReturn(designatedHearingCentre);

        Map<String, String> personalisation =
                recordOutOfTimeDecisionCannotProceedPersonalisationSms.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("Appeal Ref Number"));
        assertEquals(iaAipFrontendUrl, personalisation.get("Hyperlink to service"));
        assertEquals(designatedHearingCentre, personalisation.get("designated hearing centre"));
        verify(emailAddressFinder, times(0)).getHearingCentreEmailAddress(asylumCase);
        verify(emailAddressFinder).getListCaseHearingCentreEmailAddress(asylumCase);
    }


    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_RECORD_OUT_OF_TIME_DECISION_CANNOT_PROCEED_AIP_SMS",
                recordOutOfTimeDecisionCannotProceedPersonalisationSms.getReferenceId(caseId));
    }

    @Test
    void should_return_given_email_address_from_asylum_case() {
        when(recipientsFinder.findAll(asylumCase, NotificationType.SMS))
                .thenReturn(Collections.singleton(mockedAppellantMobileNumber));

        assertTrue(recordOutOfTimeDecisionCannotProceedPersonalisationSms.getRecipientsList(asylumCase)
                .contains(mockedAppellantMobileNumber));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
                () -> recordOutOfTimeDecisionCannotProceedPersonalisationSms.getPersonalisation((AsylumCase) null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("asylumCase must not be null");
    }
}
