package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.bail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.BAIL_REFERENCE_NUMBER;

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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.bail.helper.BailEndApplicationTemplateHelper;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class BailEndApplicationTemplateTest {

    @Mock private CaseDetails<BailCase> caseDetails;
    @Mock private BailCase bailCase;
    @Mock private BailEndApplicationTemplateHelper bailEndApplicationTemplateHelper;

    private final String templateName = "BAIL_END_APPLICATION_DOCUMENT.docx";

    private BailEndApplicationTemplate bailEndApplicationTemplate;
    private Map<String, Object> fieldValuesMap = new HashMap<>();

    @BeforeEach
    public void setUp() {
        bailEndApplicationTemplate =
            new BailEndApplicationTemplate(templateName, bailEndApplicationTemplateHelper);
    }

    @Test
    void should_return_template_name() {
        assertEquals(templateName, bailEndApplicationTemplate.getName());
    }

    @Test
    void should_map_case_data_to_template_field_values() {

        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(bailEndApplicationTemplateHelper.getCommonMapFieldValues(caseDetails)).thenReturn(fieldValuesMap);
        fieldValuesMap = bailEndApplicationTemplate.mapFieldValues(caseDetails);
    }

    @Test
    void should_be_tolerant_of_missing_data() {

        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(bailCase.read(BAIL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(bailEndApplicationTemplateHelper.getCommonMapFieldValues(caseDetails)).thenReturn(fieldValuesMap);
        fieldValuesMap = bailEndApplicationTemplate.mapFieldValues(caseDetails);
    }

}