package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.*;

import java.util.HashMap;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.bail.BailDecisionUnsignedRefusalTemplate;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.bail.helper.BailDecisionUnsignedTemplateHelper;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class BailDecisionUnsignedRefusalTemplateTest {
    @Mock private CaseDetails<BailCase> caseDetails;
    @Mock private BailCase bailCase;
    @Mock private BailDecisionUnsignedTemplateHelper bailDecisionUnsignedTemplateHelper;

    private final String templateName = "BAIL_DECISION_UNSIGNED_REFUSAL_TEMPLATE.docx";

    private BailDecisionUnsignedRefusalTemplate bailDecisionUnsignedRefusalTemplate;
    private Map<String, Object> fieldValuesMap = new HashMap<>();

    @BeforeEach
    public void setUp() {
        bailDecisionUnsignedRefusalTemplate =
                new BailDecisionUnsignedRefusalTemplate(templateName, bailDecisionUnsignedTemplateHelper);
    }

    @Test
    void should_return_template_name() {
        assertEquals(templateName, bailDecisionUnsignedRefusalTemplate.getName());
    }

    @Test
    void should_map_case_data_to_template_field_values() {
        String secretaryOfStateRefusalReasons = "Bail is refused because the Financial Conditions Supporter is not deemd suitable.";

        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(bailCase.read(SECRETARY_OF_STATE_REFUSAL_REASONS, String.class)).thenReturn(Optional.of(secretaryOfStateRefusalReasons));
        when(bailDecisionUnsignedTemplateHelper.getCommonMapFieldValues(caseDetails)).thenReturn(fieldValuesMap);
        fieldValuesMap = bailDecisionUnsignedRefusalTemplate.mapFieldValues(caseDetails);
        assertTrue(fieldValuesMap.containsKey("secretaryOfStateRefusalReasons"));
        assertTrue(fieldValuesMap.containsKey("reasonForRefusalDetails"));
        assertTrue(fieldValuesMap.containsKey("tribunalRefusalReason"));
    }

    @Test
    void should_be_tolerant_of_missing_data() {
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(bailCase.read(SECRETARY_OF_STATE_REFUSAL_REASONS, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(REASON_FOR_REFUSAL_DETAILS, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(TRIBUNAL_REFUSAL_REASON, String.class)).thenReturn(Optional.empty());
        when(bailDecisionUnsignedTemplateHelper.getCommonMapFieldValues(caseDetails)).thenReturn(fieldValuesMap);
        fieldValuesMap = bailDecisionUnsignedRefusalTemplate.mapFieldValues(caseDetails);
        assertTrue(fieldValuesMap.containsKey("secretaryOfStateRefusalReasons"));
        assertTrue(fieldValuesMap.containsKey("reasonForRefusalDetails"));
        assertTrue(fieldValuesMap.containsKey("tribunalRefusalReason"));
    }
}
