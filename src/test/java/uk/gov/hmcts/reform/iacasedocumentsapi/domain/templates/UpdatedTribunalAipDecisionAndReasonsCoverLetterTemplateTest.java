package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
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
class UpdatedTribunalAipDecisionAndReasonsCoverLetterTemplateTest {

    private final String templateName = "";
    private final String someAppealReferenceNumber = "appealReferenceNumber";
    private final String someHomeOfficeReferenceNumber = "homeOfficeRef";
    private final String someGivenNames = "some-given-name";
    private final String someFamilyName = "some-family-name";
    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "customer.services@example.com";
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private UpdatedTribunalDecisionAndReasonsCoverLetterTemplateHelper templateHelper;
    private UpdatedTribunalAipDecisionAndReasonsCoverLetterTemplate updatedTribunalAipDecisionAndReasonsCoverLetterTemplate;

    @BeforeEach
    public void setUp() {

        updatedTribunalAipDecisionAndReasonsCoverLetterTemplate = new UpdatedTribunalAipDecisionAndReasonsCoverLetterTemplate(
            templateName,
            templateHelper
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"Allowed", "Dismissed"})
    void returns_correctly_mapped_template_values(String updatedAppealDecision) {

        Map<String, Object> fieldValues = new HashMap<>();
        fieldValues.put("appealReferenceNumber", someAppealReferenceNumber);
        fieldValues.put("homeOfficeReferenceNumber", someHomeOfficeReferenceNumber);
        fieldValues.put("appellantGivenNames", someGivenNames);
        fieldValues.put("appellantFamilyName", someFamilyName);
        fieldValues.put("allowed", updatedAppealDecision.equals("Allowed") ? "Yes" : "No");
        fieldValues.put("customerServicesTelephone", customerServicesTelephone);
        fieldValues.put("customerServicesEmail", customerServicesEmail);

        when(templateHelper.getCommonMapFieldValues(caseDetails)).thenReturn(fieldValues);

        Map<String, Object> templateValues = updatedTribunalAipDecisionAndReasonsCoverLetterTemplate.mapFieldValues(caseDetails);

        assertEquals(7, templateValues.size());
        assertEquals(templateValues.get("appealReferenceNumber"), someAppealReferenceNumber);
        assertEquals(templateValues.get("homeOfficeReferenceNumber"), someHomeOfficeReferenceNumber);
        assertEquals(templateValues.get("appellantGivenNames"), someGivenNames);
        assertEquals(templateValues.get("appellantFamilyName"), someFamilyName);
        if (updatedAppealDecision.equals("Allowed")) {
            assertEquals(templateValues.get("allowed"), "Yes");
        } else {
            assertEquals(templateValues.get("allowed"), "No");
        }
        assertEquals(customerServicesTelephone, fieldValues.get("customerServicesTelephone"));
        assertEquals(customerServicesEmail, fieldValues.get("customerServicesEmail"));
    }

    @Test
    void should_return_template_name() {
        assertEquals(updatedTribunalAipDecisionAndReasonsCoverLetterTemplate.getName(), templateName);
    }

}
