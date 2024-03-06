package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.helper.UpdatedTribunalDecisionAndReasonsCoverLetterTemplateHelper;

@ExtendWith(MockitoExtension.class)
public class UpdatedTribunalDecisionAndReasonsCoverLetterTemplateTest {

    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private UpdatedTribunalDecisionAndReasonsCoverLetterTemplateHelper templateHelper;

    private String templateName = "some-template-name.docx";
    private String someAppealReferenceNumber = "some-appeal-ref";
    private String someHomeOfficeReferenceNumber = "some-home-office-ref";
    private String someLegalRepReferenceNumber = "some-legal-rep-ref";
    private String someGivenNames = "some-given-name";
    private String someFamilyName = "some-family-name";

    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "customer.services@example.com";

    private UpdatedTribunalDecisionAndReasonsCoverLetterTemplate updatedTribunalDecisionAndReasonsCoverLetterTemplate;

    @BeforeEach
    public void setUp() {

        updatedTribunalDecisionAndReasonsCoverLetterTemplate = new UpdatedTribunalDecisionAndReasonsCoverLetterTemplate(
            templateName,
            templateHelper
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"Allowed", "Dismissed"})
    public void returns_correctly_mapped_template_values(String updatedAppealDecisionn) {

        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(someLegalRepReferenceNumber));
        Map<String, Object> fieldValues = new HashMap<>();
        fieldValues.put("appealReferenceNumber", someAppealReferenceNumber);
        fieldValues.put("homeOfficeReferenceNumber", someHomeOfficeReferenceNumber);
        fieldValues.put("appellantGivenNames", someGivenNames);
        fieldValues.put("appellantFamilyName", someFamilyName);
        fieldValues.put("allowed", updatedAppealDecisionn.equals("Allowed") ? "Yes" : "No");
        fieldValues.put("customerServicesTelephone", customerServicesTelephone);
        fieldValues.put("customerServicesEmail", customerServicesEmail);

        when(templateHelper.getCommonMapFieldValues(caseDetails)).thenReturn(fieldValues);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        Map<String, Object> templateValues = updatedTribunalDecisionAndReasonsCoverLetterTemplate.mapFieldValues(caseDetails);

        assertEquals(templateValues.size(), 9);
        assertEquals(templateValues.get("hmcts"), "[userImage:hmcts.png]");
        assertEquals(templateValues.get("appealReferenceNumber"), someAppealReferenceNumber);
        assertEquals(templateValues.get("homeOfficeReferenceNumber"), someHomeOfficeReferenceNumber);
        assertEquals(templateValues.get("legalRepReferenceNumber"), someLegalRepReferenceNumber);
        assertEquals(templateValues.get("appellantGivenNames"), someGivenNames);
        assertEquals(templateValues.get("appellantFamilyName"), someFamilyName);
        if (updatedAppealDecisionn.equals("Allowed")) {
            assertEquals(templateValues.get("allowed"), "Yes");
        } else {
            assertEquals(templateValues.get("allowed"), "No");
        }
        assertEquals(customerServicesTelephone, fieldValues.get("customerServicesTelephone"));
        assertEquals(customerServicesEmail, fieldValues.get("customerServicesEmail"));
    }

    @Test
    public void should_return_template_name() {

        assertEquals(updatedTribunalDecisionAndReasonsCoverLetterTemplate.getName(), templateName);
    }
}
