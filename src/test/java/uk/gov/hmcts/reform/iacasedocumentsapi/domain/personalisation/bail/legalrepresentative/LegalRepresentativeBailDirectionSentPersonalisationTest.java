package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.bail.legalrepresentative;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.*;

import java.util.List;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailDirection;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.bail.legalrepresentative.email.LegalRepresentativeBailDirectionSentPersonalisation;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class LegalRepresentativeBailDirectionSentPersonalisationTest {

    private Long caseId = 12345L;
    private String templateIdForDirectRecipient = "someTemplateIdForDirectRecipient";
    private String templateIdForOtherParties = "someTemplateIdForOtherParties";
    private String bailReferenceNumber = "someReferenceNumber";
    private String legalRepReference = "someLegalRepReference";
    private String homeOfficeReferenceNumber = "someHomeOfficeReferenceNumber";
    private String applicantGivenNames = "someApplicantGivenNames";
    private String applicantFamilyName = "someApplicantFamilyName";
    private String sendDirectionDescription = "someDescriptionOfTheDirectionSent";
    private String dateOfCompliance = "2022-05-24";
    private String dateTimeOldestDirectionCreated = "2022-05-24T15:00:00.000000000";
    private String dateTimeLatestDirectionCreated = "2022-05-24T16:00:00.000000000";
    @Mock BailCase bailCase;
    @Mock IdValue<BailDirection> oldestDirectionIdValue;
    @Mock BailDirection oldestDirection;
    @Mock IdValue<BailDirection> newestDirectionIdValue;
    @Mock BailDirection newestDirection;

    private LegalRepresentativeBailDirectionSentPersonalisation legalRepresentativeBailDirectionSentPersonalisation;

    @BeforeEach
    public void setup() {
        when(bailCase.read(BAIL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(bailReferenceNumber));
        when(bailCase.read(LEGAL_REP_REFERENCE, String.class)).thenReturn(Optional.of(legalRepReference));
        when(bailCase.read(APPLICANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(applicantGivenNames));
        when(bailCase.read(APPLICANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(applicantFamilyName));
        when(bailCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(bailCase.read(IS_LEGALLY_REPRESENTED_FOR_FLAG, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        when(oldestDirectionIdValue.getValue()).thenReturn(oldestDirection);
        when(oldestDirection.getDateTimeDirectionCreated()).thenReturn(dateTimeOldestDirectionCreated);
        when(newestDirectionIdValue.getValue()).thenReturn(newestDirection);
        when(newestDirection.getDateTimeDirectionCreated()).thenReturn(dateTimeLatestDirectionCreated);
        when(bailCase.read(DIRECTIONS)).thenReturn(Optional.of(List.of(oldestDirectionIdValue, newestDirectionIdValue)));

        when(newestDirection.getSendDirectionDescription()).thenReturn(sendDirectionDescription);
        when(newestDirection.getDateOfCompliance()).thenReturn(dateOfCompliance);
        when(newestDirection.getSendDirectionList()).thenReturn("Legal Representative");

        legalRepresentativeBailDirectionSentPersonalisation =
            new LegalRepresentativeBailDirectionSentPersonalisation(templateIdForDirectRecipient, templateIdForOtherParties);
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateIdForDirectRecipient, legalRepresentativeBailDirectionSentPersonalisation.getTemplateId(bailCase));
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_BAIL_SENT_DIRECTION_LEGAL_REPRESENTATIVE",
            legalRepresentativeBailDirectionSentPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
            () -> legalRepresentativeBailDirectionSentPersonalisation.getPersonalisation((BailCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("bailCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given_as_direct_recipient() {

        Map<String, String> personalisation =
            legalRepresentativeBailDirectionSentPersonalisation.getPersonalisation(bailCase);

        assertEquals(bailReferenceNumber, personalisation.get("bailReferenceNumber"));
        assertEquals(applicantGivenNames, personalisation.get("applicantGivenNames"));
        assertEquals(applicantFamilyName, personalisation.get("applicantFamilyName"));
        assertEquals(homeOfficeReferenceNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals("\nLegal representative reference: " + legalRepReference,
            personalisation.get("legalRepReference"));
        assertEquals(sendDirectionDescription, personalisation.get("sendDirectionDescription"));
        assertEquals("24 May 2022", personalisation.get("dateOfCompliance"));
    }

    @Test
    public void should_return_personalisation_when_all_information_given_as_other_party() {
        when(newestDirection.getSendDirectionList()).thenReturn("Applicant");

        Map<String, String> personalisation =
            legalRepresentativeBailDirectionSentPersonalisation.getPersonalisation(bailCase);

        assertEquals(bailReferenceNumber, personalisation.get("bailReferenceNumber"));
        assertEquals(applicantGivenNames, personalisation.get("applicantGivenNames"));
        assertEquals(applicantFamilyName, personalisation.get("applicantFamilyName"));
        assertEquals(homeOfficeReferenceNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals("\nLegal representative reference: " + legalRepReference,
            personalisation.get("legalRepReference"));
        assertEquals(sendDirectionDescription, personalisation.get("sendDirectionDescription"));
        assertEquals("24 May 2022", personalisation.get("dateOfCompliance"));
        assertEquals("Applicant", personalisation.get("party"));
    }

    @Test
    public void should_return_personalisation_when_all_mandatory_information_given() {

        when(bailCase.read(BAIL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(LEGAL_REP_REFERENCE, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(APPLICANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(APPLICANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(DIRECTIONS)).thenReturn(Optional.empty());

        Map<String, String> personalisation =
            legalRepresentativeBailDirectionSentPersonalisation.getPersonalisation(bailCase);

        assertEquals("", personalisation.get("bailReferenceNumber"));
        assertEquals("", personalisation.get("applicantGivenNames"));
        assertEquals("", personalisation.get("applicantFamilyName"));
        assertEquals("", personalisation.get("homeOfficeReferenceNumber"));
        assertEquals("", personalisation.get("legalRepReference"));
        assertEquals("", personalisation.get("sendDirectionDescription"));
        assertEquals("", personalisation.get("dateOfCompliance"));
        assertEquals("", personalisation.get("party"));
    }
}
