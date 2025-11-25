package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.sms;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppellantNocRequestDecisionPersonalisationSmsTest {

    @Mock
    Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    AsylumCase asylumCase;

    private Long caseId = 12345L;
    private String smsTemplateId = "someSmsTemplateId";

    private long mockedAppealReferenceNumber = 1236;
    private String mockedAppellantMobilePhone = "07123456789";
    private String mockedAppellantGivenNames = "someAppellantGivenNames";
    private String mockedAppellantFamilyName = "someAppellantFamilyName";
    private String dateOfBirth = "2020-03-01";
    private String expectedDateOfBirth = "1 Mar 2020";

    private AppellantNocRequestDecisionPersonalisationSms appellantNocRequestDecisionPersonalisationSms;

    @BeforeEach
    void setup() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getCaseDetails().getId()).thenReturn(mockedAppealReferenceNumber);

        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.of(dateOfBirth));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(mockedAppellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(mockedAppellantFamilyName));

        appellantNocRequestDecisionPersonalisationSms = new AppellantNocRequestDecisionPersonalisationSms(smsTemplateId);
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(smsTemplateId, appellantNocRequestDecisionPersonalisationSms.getTemplateId());
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_NOC_REQUEST_DECISION_APPELLANT_SMS",
            appellantNocRequestDecisionPersonalisationSms.getReferenceId(caseId));
    }

    @Test
    void should_throw_exception_on_recipients_when_case_is_null() {

        assertThatThrownBy(() -> appellantNocRequestDecisionPersonalisationSms.getRecipientsList(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("appellantMobileNumber is not present");
    }

    @Test
    void should_return_given_mobile_mobile_list_from_subscribers_in_asylum_case() {

        when(asylumCase.read(MOBILE_NUMBER,String.class))
            .thenReturn(Optional.of(mockedAppellantMobilePhone));

        assertTrue(
            appellantNocRequestDecisionPersonalisationSms.getRecipientsList(asylumCase).contains(mockedAppellantMobilePhone));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> appellantNocRequestDecisionPersonalisationSms.getPersonalisation((Callback<AsylumCase>) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");
    }

    @Test
    void should_return_personalisation_when_all_information_given() {
        when(caseDetails.getId()).thenReturn(mockedAppealReferenceNumber);

        Map<String, String> personalisation = appellantNocRequestDecisionPersonalisationSms.getPersonalisation(callback);
        assertEquals(String.valueOf(mockedAppealReferenceNumber), personalisation.get("Ref Number"));
        assertEquals(mockedAppellantGivenNames, personalisation.get("Given names"));
        assertEquals(mockedAppellantFamilyName, personalisation.get("Family name"));
        assertEquals(expectedDateOfBirth, personalisation.get("Date Of Birth"));


    }

    @Test
    void should_throw_exception_on_personalisation_when_date_of_birth_is_null() {
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appellantNocRequestDecisionPersonalisationSms.getPersonalisation(callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Appellant's birth of date is not present");
    }

    @Test
    void should_return_personalisation_when_only_mandatory_information_given() {

        when(caseDetails.getId()).thenReturn(mockedAppealReferenceNumber);
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.of(dateOfBirth));


        Map<String, String> personalisation = appellantNocRequestDecisionPersonalisationSms.getPersonalisation(callback);
        assertEquals(String.valueOf(mockedAppealReferenceNumber), personalisation.get("Ref Number"));
        assertEquals("", personalisation.get("Given names"));
        assertEquals("", personalisation.get("Family name"));
        assertEquals(expectedDateOfBirth, personalisation.get("Date Of Birth"));

    }
}
