package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.bail.legalrepresentative;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.APPLICANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.APPLICANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.BAIL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.IS_LEGALLY_REPRESENTED_FOR_FLAG;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.LEGAL_REP_REFERENCE;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.bail.legalrepresentative.email.LegalRepresentativeForceCaseToHearingPersonalisation;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LegalRepresentativeForceCaseToHearingPersonalisationTest {

    private final Long caseId = 12345L;
    private final String templateId = "someTemplateIdWithLegalRep";
    private final String bailReferenceNumber = "someReferenceNumber";
    private final String legalRepReference = "someLegalRepReference";
    private final String homeOfficeReferenceNumber = "someHomeOfficeReferenceNumber";
    private final String applicantGivenNames = "someApplicantGivenNames";
    private final String applicantFamilyName = "someApplicantFamilyName";
    @Mock BailCase bailCase;
    private LegalRepresentativeForceCaseToHearingPersonalisation legalRepForceCaseToHearingPersonalisationTest;

    @BeforeEach
    public void setup() {

        when(bailCase.read(BAIL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(bailReferenceNumber));
        when(bailCase.read(LEGAL_REP_REFERENCE, String.class)).thenReturn(Optional.of(legalRepReference));
        when(bailCase.read(APPLICANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(applicantGivenNames));
        when(bailCase.read(APPLICANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(applicantFamilyName));
        when(bailCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(bailCase.read(IS_LEGALLY_REPRESENTED_FOR_FLAG, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        legalRepForceCaseToHearingPersonalisationTest =
                new LegalRepresentativeForceCaseToHearingPersonalisation(templateId);
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(templateId, legalRepForceCaseToHearingPersonalisationTest.getTemplateId(bailCase));
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_LEGAL_REP_FORCE_CASE_TO_HEARING",
                legalRepForceCaseToHearingPersonalisationTest.getReferenceId(caseId));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
                () -> legalRepForceCaseToHearingPersonalisationTest.getPersonalisation((BailCase) null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("bailCase must not be null");
    }

    @Test
    void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation =
                legalRepForceCaseToHearingPersonalisationTest.getPersonalisation(bailCase);

        assertEquals(bailReferenceNumber, personalisation.get("bailReferenceNumber"));
        assertEquals(legalRepReference, personalisation.get("legalRepReference"));
        assertEquals(applicantGivenNames, personalisation.get("applicantGivenNames"));
        assertEquals(applicantFamilyName, personalisation.get("applicantFamilyName"));
        assertEquals(homeOfficeReferenceNumber, personalisation.get("homeOfficeReferenceNumber"));
    }

    @Test
    void should_return_personalisation_when_all_mandatory_information_given() {

        when(bailCase.read(BAIL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(APPLICANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(APPLICANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(LEGAL_REP_REFERENCE, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation =
                legalRepForceCaseToHearingPersonalisationTest.getPersonalisation(bailCase);

        assertEquals("", personalisation.get("bailReferenceNumber"));
        assertEquals("", personalisation.get("legalRepReference"));
        assertEquals("", personalisation.get("applicantGivenNames"));
        assertEquals("", personalisation.get("applicantFamilyName"));
        assertEquals("", personalisation.get("homeOfficeReferenceNumber"));
    }

    @Test
    void should_throw_exception_when_legal_rep_email_is_missing() {
        when(bailCase.read(BailCaseFieldDefinition.LEGAL_REP_EMAIL, String.class)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            legalRepForceCaseToHearingPersonalisationTest.getRecipientsList(bailCase);
        });

        String expectedMessage = "legalRepresentativeEmailAddress is not present";
        String actualMessage = exception.getMessage();

        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }

}

