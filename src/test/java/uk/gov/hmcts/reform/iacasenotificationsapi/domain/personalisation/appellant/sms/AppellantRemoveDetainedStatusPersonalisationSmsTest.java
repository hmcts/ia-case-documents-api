package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.EMAIL;

import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.*;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AppellantRemoveDetainedStatusPersonalisationSmsTest {

    @Mock
    AsylumCase asylumCase;

    private Long caseId = 12345L;
    private String smsTemplateId = "someSmsTemplateId";
    private String mockedAppealReferenceNumber = "someReferenceNumber";
    private String mockedAppellantMobilePhone = "07123456789";

    private AppellantRemoveDetainedStatusPersonalisationSms appellantRemoveDetainedStatusPersonalisationSms;

    @BeforeEach
    public void setup() {
        appellantRemoveDetainedStatusPersonalisationSms = new AppellantRemoveDetainedStatusPersonalisationSms(
                smsTemplateId
        );
    }


    @Test
    public void should_return_template_id() {
        assertEquals(smsTemplateId,
                appellantRemoveDetainedStatusPersonalisationSms.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_REMOVE_DETENTION_STATUS_APPELLANT_SMS",
                appellantRemoveDetainedStatusPersonalisationSms.getReferenceId(caseId));
    }

    @Test
    public void should_return_correct_recipient_mobile_number() {
        List<String> mockedContactPreferences = new ArrayList<>(Arrays.asList("wantsSms"));

        when(asylumCase.read(CONTACT_PREFERENCE_UN_REP))
                .thenReturn(Optional.of(mockedContactPreferences));

        when(asylumCase.read(MOBILE_NUMBER, String.class))
                .thenReturn(Optional.of(mockedAppellantMobilePhone));

        assertTrue(appellantRemoveDetainedStatusPersonalisationSms.getRecipientsList(asylumCase)
                .contains(mockedAppellantMobilePhone));
    }

    @Test
    public void should_return_empty_recipient_set_when_sms_contact_preference_not_chosen() {
        List<String> mockedContactPreferences = new ArrayList<>(Arrays.asList());

        when(asylumCase.read(CONTACT_PREFERENCE_UN_REP))
                .thenReturn(Optional.ofNullable(mockedContactPreferences));

        assertTrue(appellantRemoveDetainedStatusPersonalisationSms.getRecipientsList(asylumCase)
                .isEmpty());
        verify(asylumCase, times(0)).read(EMAIL);

    }

    @Test
    public void should_return_personalisation_when_all_information_given() {
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.of(mockedAppealReferenceNumber));
        Map<String, String> personalisation =
                appellantRemoveDetainedStatusPersonalisationSms.getPersonalisation(asylumCase);

        assertEquals(mockedAppealReferenceNumber, personalisation.get("appealReferenceNumber"));
    }

    @Test
    public void should_return_personalisation_when_appeal_ref_missing() {
        Map<String, String> personalisation =
                appellantRemoveDetainedStatusPersonalisationSms.getPersonalisation(asylumCase);

        assertEquals("", personalisation.get("appealReferenceNumber"));
    }
}
