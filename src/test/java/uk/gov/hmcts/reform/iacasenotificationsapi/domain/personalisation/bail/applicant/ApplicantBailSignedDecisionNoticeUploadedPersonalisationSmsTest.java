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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.applicant.sms.ApplicantBailSignedDecisionNoticeUploadedPersonalisationSms;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ApplicantBailSignedDecisionNoticeUploadedPersonalisationSmsTest {

    private final String smsTemplateId = "someTemplateId";
    private String mobileNumber = "111 111 111";
    private final String bailReferenceNumber = "someReferenceNumber";
    private final String yes = "yes";
    private final String no = "no";

    @Mock
    BailCase bailCase;

    private ApplicantBailSignedDecisionNoticeUploadedPersonalisationSms applicantBailSignedDecisionNoticeUploadedPersonalisationSms;

    @BeforeEach
    public void setup() {

        when(bailCase.read(APPLICANT_MOBILE_NUMBER_1, String.class)).thenReturn(Optional.of(mobileNumber));
        when(bailCase.read(BAIL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(bailReferenceNumber));

        applicantBailSignedDecisionNoticeUploadedPersonalisationSms =
            new ApplicantBailSignedDecisionNoticeUploadedPersonalisationSms(smsTemplateId);
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(smsTemplateId, applicantBailSignedDecisionNoticeUploadedPersonalisationSms.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_BAIL_SIGNED_DECISION_NOTICE_UPLOADED_APPLICANT_SMS",
            applicantBailSignedDecisionNoticeUploadedPersonalisationSms.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_mobile_number() {
        assertTrue(
            applicantBailSignedDecisionNoticeUploadedPersonalisationSms.getRecipientsList(bailCase).contains(mobileNumber));

        when(bailCase.read(APPLICANT_MOBILE_NUMBER_1, String.class)).thenReturn(Optional.empty());

        assertTrue(applicantBailSignedDecisionNoticeUploadedPersonalisationSms.getRecipientsList(bailCase).isEmpty());
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
            () -> applicantBailSignedDecisionNoticeUploadedPersonalisationSms.getPersonalisation((BailCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("bailCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given_and_bail_granted() {
        when(bailCase.read(RECORD_DECISION_TYPE, String.class)).thenReturn(Optional.of("granted"));

        Map<String, String> personalisation =
            applicantBailSignedDecisionNoticeUploadedPersonalisationSms.getPersonalisation(bailCase);

        assertEquals(bailReferenceNumber, personalisation.get("bailReferenceNumber"));
        assertEquals(yes, personalisation.get("granted"));
        assertEquals(no, personalisation.get("refused"));
    }

    @Test
    public void should_return_personalisation_when_all_information_given_and_bail_conditionally_granted() {
        when(bailCase.read(RECORD_DECISION_TYPE, String.class)).thenReturn(Optional.of("conditionalGrant"));

        Map<String, String> personalisation =
            applicantBailSignedDecisionNoticeUploadedPersonalisationSms.getPersonalisation(bailCase);

        assertEquals(bailReferenceNumber, personalisation.get("bailReferenceNumber"));
        assertEquals(yes, personalisation.get("granted"));
        assertEquals(no, personalisation.get("refused"));
    }

    @Test
    public void should_return_personalisation_when_all_information_given_and_bail_refused() {
        when(bailCase.read(RECORD_DECISION_TYPE, String.class)).thenReturn(Optional.of("refused"));

        Map<String, String> personalisation =
            applicantBailSignedDecisionNoticeUploadedPersonalisationSms.getPersonalisation(bailCase);

        assertEquals(bailReferenceNumber, personalisation.get("bailReferenceNumber"));
        assertEquals(no, personalisation.get("granted"));
        assertEquals(yes, personalisation.get("refused"));
    }

    @Test
    public void should_return_personalisation_when_only_mandatory_information_given() {
        when(bailCase.read(BAIL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(RECORD_DECISION_TYPE, String.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation =
            applicantBailSignedDecisionNoticeUploadedPersonalisationSms.getPersonalisation(bailCase);

        assertEquals("", personalisation.get("bailReferenceNumber"));
        assertEquals("", personalisation.get("granted"));
        assertEquals("", personalisation.get("refused"));
    }
}
