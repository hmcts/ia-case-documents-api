package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.applicant;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.*;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.applicant.sms.ApplicantBailApplicationEndedPersonalisationSms;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ApplicantBailApplicationEndedPersonalisationSmsTest {

    private final String smsTemplateId = "someTemplateId";
    private String mobileNumber = "111 111 111";
    private final String bailReferenceNumber = "someReferenceNumber";
    private final String outcomeOfApplication = "someOutcome";
    private final String endApplicationDate = "2022-05-13";

    @Mock
    BailCase bailCase;

    private ApplicantBailApplicationEndedPersonalisationSms applicantBailApplicationEndedPersonalisationSms;

    @BeforeEach
    public void setup() {

        when(bailCase.read(APPLICANT_MOBILE_NUMBER_1, String.class)).thenReturn(Optional.of(mobileNumber));
        when(bailCase.read(BAIL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(bailReferenceNumber));
        when(bailCase.read(END_APPLICATION_OUTCOME, String.class)).thenReturn(Optional.of(outcomeOfApplication));
        when(bailCase.read(END_APPLICATION_DATE, String.class)).thenReturn(Optional.of(endApplicationDate));

        applicantBailApplicationEndedPersonalisationSms =
            new ApplicantBailApplicationEndedPersonalisationSms(smsTemplateId);
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(smsTemplateId, applicantBailApplicationEndedPersonalisationSms.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_BAIL_APPLICATION_ENDED_APPLICANT_SMS",
            applicantBailApplicationEndedPersonalisationSms.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_mobile_number() {
        assertTrue(
            applicantBailApplicationEndedPersonalisationSms.getRecipientsList(bailCase).contains(mobileNumber));

        when(bailCase.read(APPLICANT_MOBILE_NUMBER_1, String.class)).thenReturn(Optional.empty());

        assertTrue(applicantBailApplicationEndedPersonalisationSms.getRecipientsList(bailCase).isEmpty());
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
            () -> applicantBailApplicationEndedPersonalisationSms.getPersonalisation((BailCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("bailCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation =
            applicantBailApplicationEndedPersonalisationSms.getPersonalisation(bailCase);

        assertEquals(bailReferenceNumber, personalisation.get("bailReferenceNumber"));
        assertEquals(outcomeOfApplication, personalisation.get("endApplicationOutcome"));
        assertEquals(endApplicationDate, personalisation.get("endApplicationDate"));
    }

    @Test
    public void should_return_personalisation_when_only_mandatory_information_given() {

        when(bailCase.read(BAIL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(END_APPLICATION_OUTCOME, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(END_APPLICATION_DATE, String.class)).thenReturn(Optional.empty());


        Map<String, String> personalisation =
            applicantBailApplicationEndedPersonalisationSms.getPersonalisation(bailCase);

        assertEquals("", personalisation.get("bailReferenceNumber"));
        assertEquals("", personalisation.get("endApplicationOutcome"));
        assertEquals("", personalisation.get("endApplicationDate"));
    }
}
