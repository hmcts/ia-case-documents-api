package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.bail.legalrepresentative;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.CASE_NOTES;

import java.util.Collections;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.CaseNote;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.bail.editbaildocuments.EditBailDocumentService;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.bail.legalrepresentative.email.LegalRepresentativeBailDocumentsEditedPersonalisation;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LegalRepresentativeDocumentsEditedPersonalisationTest {

    private Long caseId = 12345L;
    private String templateId = "someTemplateId";
    private String bailReferenceNumber = "someReferenceNumber";
    private String legalRepReference = "someLegalRepReference";
    private String homeOfficeReferenceNumber = "someHomeOfficeReferenceNumber";
    private String applicantGivenNames = "someApplicantGivenNames";
    private String applicantFamilyName = "someApplicantFamilyName";
    private String latestModifiedDocuments = "Document1.pdf,\nDocument2.pdf,\nDocument3.pdf";
    private String reasonForChange = "someReasonForChange";
    @Mock
    BailCase bailCase;
    @Mock
    BailCase bailCaseBefore;
    @Mock
    Callback<BailCase> callBack;
    @Mock
    CaseDetails<BailCase> caseDetails;
    @Mock
    CaseDetails<BailCase> caseDetailsBefore;
    @Mock
    EditBailDocumentService editBailDocumentService;
    @Mock
    CaseNote caseNote;
    @Mock
    IdValue<CaseNote> caseNoteIdValue;

    private LegalRepresentativeBailDocumentsEditedPersonalisation legalRepresentativeBailDocumentsEditedPersonalisation;

    @BeforeEach
    public void setup() {
        when(callBack.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(callBack.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(caseDetailsBefore.getCaseData()).thenReturn(bailCaseBefore);

        when(editBailDocumentService.getFormattedDocumentsGivenCaseAndDocNames(eq(bailCaseBefore), eq(bailCase), anyList()))
            .thenReturn(List.of("Document1.pdf", "Document2.pdf", "Document3.pdf"));

        when(caseNoteIdValue.getValue()).thenReturn(caseNote);
        when(caseNote.getCaseNoteDescription()).thenReturn("Document names: [Document1.pdf, Document2.pdf, Document3.pdf]\nReason: " + reasonForChange);
        when(bailCase.read(CASE_NOTES)).thenReturn(Optional.of(List.of(caseNoteIdValue)));

        when(bailCase.read(BAIL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(bailReferenceNumber));
        when(bailCase.read(LEGAL_REP_REFERENCE, String.class)).thenReturn(Optional.of(legalRepReference));
        when(bailCase.read(APPLICANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(applicantGivenNames));
        when(bailCase.read(APPLICANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(applicantFamilyName));
        when(bailCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(bailCase.read(IS_LEGALLY_REPRESENTED_FOR_FLAG, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        legalRepresentativeBailDocumentsEditedPersonalisation =
            new LegalRepresentativeBailDocumentsEditedPersonalisation(templateId, editBailDocumentService);
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, legalRepresentativeBailDocumentsEditedPersonalisation.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_BAIL_EDITED_DOCUMENTS_LEGAL_REPRESENTATIVE",
            legalRepresentativeBailDocumentsEditedPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
            () -> legalRepresentativeBailDocumentsEditedPersonalisation.getPersonalisation((Callback<BailCase>) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("bailCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation =
            legalRepresentativeBailDocumentsEditedPersonalisation.getPersonalisation(callBack);

        assertEquals(bailReferenceNumber, personalisation.get("bailReferenceNumber"));
        assertEquals(legalRepReference, personalisation.get("legalRepReference"));
        assertEquals(applicantGivenNames, personalisation.get("applicantGivenNames"));
        assertEquals(applicantFamilyName, personalisation.get("applicantFamilyName"));
        assertEquals(homeOfficeReferenceNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(latestModifiedDocuments, personalisation.get("latestModifiedDocuments"));
        assertEquals(reasonForChange, personalisation.get("reasonForChange"));
    }

    @Test
    public void should_return_personalisation_when_no_LR_all_information_given() {

        when(bailCase.read(IS_LEGALLY_REPRESENTED_FOR_FLAG, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        Map<String, String> personalisation =
            legalRepresentativeBailDocumentsEditedPersonalisation.getPersonalisation(callBack);

        assertEquals(bailReferenceNumber, personalisation.get("bailReferenceNumber"));
        assertEquals(applicantGivenNames, personalisation.get("applicantGivenNames"));
        assertEquals(applicantFamilyName, personalisation.get("applicantFamilyName"));
        assertEquals(homeOfficeReferenceNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(latestModifiedDocuments, personalisation.get("latestModifiedDocuments"));
        assertEquals(reasonForChange, personalisation.get("reasonForChange"));
    }

    @Test
    public void should_return_personalisation_when_all_mandatory_information_given() {

        when(bailCase.read(BAIL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(APPLICANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(APPLICANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(LEGAL_REP_REFERENCE, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(editBailDocumentService.getFormattedDocumentsGivenCaseAndDocNames(eq(bailCaseBefore), eq(bailCase), anyList()))
            .thenReturn(Collections.emptyList());
        when(bailCase.read(CASE_NOTES)).thenReturn(Optional.empty());

        Map<String, String> personalisation =
            legalRepresentativeBailDocumentsEditedPersonalisation.getPersonalisation(callBack);

        assertEquals("", personalisation.get("bailReferenceNumber"));
        assertEquals("", personalisation.get("legalRepReference"));
        assertEquals("", personalisation.get("applicantGivenNames"));
        assertEquals("", personalisation.get("applicantFamilyName"));
        assertEquals("", personalisation.get("homeOfficeReferenceNumber"));
        assertEquals("", personalisation.get("latestModifiedDocuments"));
        assertEquals("", personalisation.get("reasonForChange"));
    }

}
