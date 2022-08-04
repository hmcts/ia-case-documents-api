package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import static org.junit.jupiter.api.Assertions.*;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.bail.BailDecisionUnsignedMindedRefusalTemplate;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.bail.helper.BailDecisionUnsignedTemplateHelper;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class BailDecisionUnsignedMindedRefusalTemplateTest {
    @Mock private CaseDetails<BailCase> caseDetails;
    @Mock private BailCase bailCase;
    @Mock private BailDecisionUnsignedTemplateHelper bailDecisionUnsignedTemplateHelper;

    private final String templateName = "BAIL_DECISION_UNSIGNED_MINDED_REFUSAL_TEMPLATE.docx";

    private BailDecisionUnsignedMindedRefusalTemplate bailDecisionUnsignedMindedRefusalTemplate;
    private Map<String, Object> fieldValuesMap = new HashMap<>();

    @BeforeEach
    public void setUp() {
        bailDecisionUnsignedMindedRefusalTemplate =
                new BailDecisionUnsignedMindedRefusalTemplate(templateName, bailDecisionUnsignedTemplateHelper);
    }

    @Test
    void should_map_case_data_to_template_field_values() {
        String secretaryOfStateRefusalReasons = "Bail is refused because the Financial Conditions Supporter is not deemd suitable.";
        String reasonsJudgeIsMindedDetails = "The applicant has agreed to report at Hatton Cross every week and "
                + "has a financial conditions supporter who will pay £1,500 if bail conditions are not met.";

        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(bailCase.read(SECRETARY_OF_STATE_REFUSAL_REASONS, String.class)).thenReturn(Optional.of(reasonsJudgeIsMindedDetails));
        when(bailCase.read(SECRETARY_OF_STATE_REFUSAL_REASONS, String.class)).thenReturn(Optional.of(secretaryOfStateRefusalReasons));
        when(bailDecisionUnsignedTemplateHelper.getCommonMapFieldValues(caseDetails)).thenReturn(fieldValuesMap);
        fieldValuesMap = bailDecisionUnsignedMindedRefusalTemplate.mapFieldValues(caseDetails);
        assertTrue(fieldValuesMap.containsKey("secretaryOfStateRefusalReasons"));
        assertTrue(fieldValuesMap.containsKey("reasonForRefusalDetails"));
        assertTrue(fieldValuesMap.containsKey("tribunalRefusalReason"));
        assertTrue(fieldValuesMap.containsKey("reasonsJudgeIsMindedDetails"));
    }

    @Test
    void should_be_tolerant_of_missing_data() {
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(bailCase.read(SECRETARY_OF_STATE_REFUSAL_REASONS, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(SECRETARY_OF_STATE_REFUSAL_REASONS, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(REASON_FOR_REFUSAL_DETAILS, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(TRIBUNAL_REFUSAL_REASON, String.class)).thenReturn(Optional.empty());
        when(bailDecisionUnsignedTemplateHelper.getCommonMapFieldValues(caseDetails)).thenReturn(fieldValuesMap);
        fieldValuesMap = bailDecisionUnsignedMindedRefusalTemplate.mapFieldValues(caseDetails);
        assertTrue(fieldValuesMap.containsKey("secretaryOfStateRefusalReasons"));
        assertTrue(fieldValuesMap.containsKey("reasonForRefusalDetails"));
        assertTrue(fieldValuesMap.containsKey("tribunalRefusalReason"));
        assertTrue(fieldValuesMap.containsKey("reasonsJudgeIsMindedDetails"));
    }

}

