package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.bail.applicant;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.APPLICANT_DATE_OF_BIRTH;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.APPLICANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.APPLICANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.APPLICANT_MOBILE_NUMBER_1;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.BAIL_REFERENCE_NUMBER;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.bail.applicant.sms.ApplicantBailNocChangedLrPersonalisationSms;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ApplicantBailNocPersonalisationSmsTest {

    private final String smsTemplateId = "someTemplateId";
    private final String mobileNumber = "111 111 111";
    private final String bailReferenceNumber = "someReferenceNumber";
    private final String applicantDoB = "2000-05-13";
    private final String applicantGivenNames = "someApplicantGivenNames";
    private final String applicantFamilyName = "someApplicantFamilyName";

    @Mock
    BailCase bailCase;

    private ApplicantBailNocChangedLrPersonalisationSms applicantBailNocPersonalisationSms;

    @BeforeEach
    public void setup() {

        when(bailCase.read(APPLICANT_MOBILE_NUMBER_1, String.class)).thenReturn(Optional.of(mobileNumber));
        when(bailCase.read(BAIL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(bailReferenceNumber));
        when(bailCase.read(BailCaseFieldDefinition.APPLICANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(applicantGivenNames));
        when(bailCase.read(BailCaseFieldDefinition.APPLICANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(applicantFamilyName));
        when(bailCase.read(BailCaseFieldDefinition.APPLICANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.of(applicantDoB));

        applicantBailNocPersonalisationSms =
            new ApplicantBailNocChangedLrPersonalisationSms(smsTemplateId);
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(smsTemplateId, applicantBailNocPersonalisationSms.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_BAIL_NOC_CHANGED_LR_APPLICANT_SMS",
            applicantBailNocPersonalisationSms.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_mobile_number() {
        assertTrue(
            applicantBailNocPersonalisationSms.getRecipientsList(bailCase).contains(mobileNumber));

        when(bailCase.read(APPLICANT_MOBILE_NUMBER_1, String.class)).thenReturn(Optional.empty());

        assertTrue(applicantBailNocPersonalisationSms.getRecipientsList(bailCase).isEmpty());
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
            () -> applicantBailNocPersonalisationSms.getPersonalisation((BailCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("bailCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation =
            applicantBailNocPersonalisationSms.getPersonalisation(bailCase);

        assertEquals(bailReferenceNumber, personalisation.get("bailReferenceNumber"));
        assertEquals(applicantGivenNames, personalisation.get("applicantGivenNames"));
        assertEquals(applicantFamilyName, personalisation.get("applicantFamilyName"));
        assertEquals(applicantDoB, personalisation.get("applicantDateOfBirth"));

    }

    @Test
    public void should_return_personalisation_when_only_mandatory_information_given() {

        when(bailCase.read(BAIL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(APPLICANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(APPLICANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(APPLICANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation =
            applicantBailNocPersonalisationSms.getPersonalisation(bailCase);

        assertEquals("", personalisation.get("bailReferenceNumber"));
        assertEquals("", personalisation.get("applicantGivenNames"));
        assertEquals("", personalisation.get("applicantFamilyName"));
        assertEquals("", personalisation.get("applicantDateOfBirth"));
    }
}
