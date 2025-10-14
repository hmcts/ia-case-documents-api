package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.bail.hearingcentre;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.APPLICANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.APPLICANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.BAIL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.IS_LEGALLY_REPRESENTED_FOR_FLAG;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.LEGAL_REP_REFERENCE;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.bail.hearingcentre.email.HearingCentreSubmitApplicationPersonalisation;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class HearingCentreSubmitApplicationPersonalisationTest {

    private final String templateId = "someTemplateId";
    private final String templateIdWithoutLR = "someTemplateIdWithoutLR";
    private final String homeOfficeReferenceNumber = "horeferencenumber";
    private final String bailReferenceNumber = "someReferenceNumber";
    private final String legalRepReferenceNumber = "someLegalRepRefNumber";
    private final String applicantGivenNames = "someAppellantGivenNames";
    private final String applicantFamilyName = "someAppellantFamilyName";
    @Mock BailCase bailCase;
    @Mock private EmailAddressFinder emailAddressFinder;
    private HearingCentreSubmitApplicationPersonalisation hearingCentreSubmitApplicationPersonalisation;

    @BeforeEach
    public void setup() {

        when(bailCase.read(BAIL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(bailReferenceNumber));
        when(bailCase.read(LEGAL_REP_REFERENCE, String.class)).thenReturn(Optional.of(legalRepReferenceNumber));
        when(bailCase.read(APPLICANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(applicantGivenNames));
        when(bailCase.read(APPLICANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(applicantFamilyName));
        when(bailCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(bailCase.read(IS_LEGALLY_REPRESENTED_FOR_FLAG, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        hearingCentreSubmitApplicationPersonalisation =
            new HearingCentreSubmitApplicationPersonalisation(templateId, templateIdWithoutLR, emailAddressFinder);
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, hearingCentreSubmitApplicationPersonalisation.getTemplateId(bailCase));
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_BAIL_APPLICATION_SUBMITTED_HEARING_CENTRE",
            hearingCentreSubmitApplicationPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_for_hearing_centre() {
        String hearingCentreEmail = "someHearingCentre@Email.com";
        when(emailAddressFinder.getBailHearingCentreEmailAddress(bailCase)).thenReturn(hearingCentreEmail);
        assertTrue(hearingCentreSubmitApplicationPersonalisation.getRecipientsList(bailCase)
            .contains(hearingCentreEmail));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
            () -> hearingCentreSubmitApplicationPersonalisation.getPersonalisation((BailCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("bailCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation =
            hearingCentreSubmitApplicationPersonalisation.getPersonalisation(bailCase);

        assertEquals(bailReferenceNumber, personalisation.get("bailReferenceNumber"));
        assertEquals(legalRepReferenceNumber, personalisation.get("legalRepReference"));
        assertEquals(applicantGivenNames, personalisation.get("applicantGivenNames"));
        assertEquals(applicantFamilyName, personalisation.get("applicantFamilyName"));
        assertEquals(homeOfficeReferenceNumber, personalisation.get("homeOfficeReferenceNumber"));
    }

    @Test
    public void should_return_personalisation_when_no_LR_all_information_given() {

        when(bailCase.read(IS_LEGALLY_REPRESENTED_FOR_FLAG, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        Map<String, String> personalisation =
            hearingCentreSubmitApplicationPersonalisation.getPersonalisation(bailCase);

        assertEquals(templateIdWithoutLR, hearingCentreSubmitApplicationPersonalisation.getTemplateId(bailCase));
        assertEquals(bailReferenceNumber, personalisation.get("bailReferenceNumber"));
        assertNull(personalisation.get("legalRepReference"));
        assertEquals(applicantGivenNames, personalisation.get("applicantGivenNames"));
        assertEquals(applicantFamilyName, personalisation.get("applicantFamilyName"));
        assertEquals(homeOfficeReferenceNumber, personalisation.get("homeOfficeReferenceNumber"));
    }

    @Test
    public void should_return_personalisation_when_all_mandatory_information_given() {

        when(bailCase.read(BAIL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(APPLICANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(APPLICANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(LEGAL_REP_REFERENCE, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation =
            hearingCentreSubmitApplicationPersonalisation.getPersonalisation(bailCase);

        assertEquals("", personalisation.get("bailReferenceNumber"));
        assertEquals("", personalisation.get("legalRepReference"));
        assertEquals("", personalisation.get("applicantGivenNames"));
        assertEquals("", personalisation.get("applicantFamilyName"));
        assertEquals("", personalisation.get("homeOfficeReferenceNumber"));
    }
}
