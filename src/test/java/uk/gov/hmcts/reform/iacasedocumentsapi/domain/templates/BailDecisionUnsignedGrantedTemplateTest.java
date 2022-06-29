package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.*;

import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.bail.BailDecisionUnsignedGrantedTemplate;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.bail.helper.BailDecisionUnsignedTemplateHelper;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class BailDecisionUnsignedGrantedTemplateTest {
    @Mock private CaseDetails<BailCase> caseDetails;
    @Mock private BailCase bailCase;
    @Mock private BailDecisionUnsignedTemplateHelper bailDecisionUnsignedTemplateHelper;

    private final String templateName = "BAIL_DECISION_UNSIGNED_REFUSAL_TEMPLATE.docx";

    private String financialAmountSupporterUndertakes1 = "1500";
    private String supporterGivenNames = "Supporter1";
    private String supporterFamilyNames = "Family";
    private String supporterAddressLine1 = "123 Test Street";
    private String supporterAddressLine2 = "Test Area";
    private String supporterAddressLine3 = "Test District";
    private String supporterAddressPostTown = "Test Town";
    private String supporterAddressCounty = "South";
    private String supporterAddressPostCode = "AB1 2CD";
    private String supporterAddressCountry = "UK";
    private String conditionsForBailResidence = "conditions residence";
    private String conditionsForBailAppearance = "conditions apperance";
    private String conditionsForBailActivities = "conditions activites";
    private String conditionsForBailElectronicMonitoring = "conditions electornic monitoring";
    private String conditionsForBailOther = "conditions other";
    private String bailTransferDirections = "bail transfer directions";

    private List<String> conditionsList =
            Arrays.asList("bailAppearance", "bailActivities", "bailResidence", "bailElectronicMonitoring", "bailOther");

    private AddressUk supporterAddressUk = new AddressUk(
            supporterAddressLine1,
            supporterAddressLine2,
            supporterAddressLine3,
            supporterAddressPostTown,
            supporterAddressCounty,
            supporterAddressPostCode,
            supporterAddressCountry
    );

    private BailDecisionUnsignedGrantedTemplate bailDecisionUnsignedGrantedTemplate;
    private Map<String, Object> fieldValuesMap = new HashMap<>();

    @BeforeEach
    public void setUp() {
        bailDecisionUnsignedGrantedTemplate =
                new BailDecisionUnsignedGrantedTemplate(templateName, bailDecisionUnsignedTemplateHelper);
    }

    @Test
    void should_map_case_data_to_template_field_values() {
        dataSetUp();
        when(bailDecisionUnsignedTemplateHelper.getCommonMapFieldValues(caseDetails)).thenReturn(fieldValuesMap);
        fieldValuesMap = bailDecisionUnsignedGrantedTemplate.mapFieldValues(caseDetails);
        checkCommonFields();
    }

    @Test
    void should_not_set_condition_data_if_not_present() {
        dataSetUp();
        List<String> testConditionsList =
                Arrays.asList("bailResidence", "bailElectronicMonitoring");

        when(bailCase.read(CONDITIONS_FOR_BAIL)).thenReturn(Optional.of(testConditionsList));

        fieldValuesMap = bailDecisionUnsignedGrantedTemplate.mapFieldValues(caseDetails);
        assertFalse(fieldValuesMap.containsKey("conditionsForBailAppearance"));
        assertFalse(fieldValuesMap.containsKey("conditionsForBailActivities"));
        assertFalse(fieldValuesMap.containsKey("conditionsForBailOther"));
    }

    @Test
    void should_not_set_supporter_2_data_if_not_present() {
        dataSetUp();
        when(bailCase.read(JUDGE_HAS_AGREED_TO_SUPPORTER2, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        fieldValuesMap = bailDecisionUnsignedGrantedTemplate.mapFieldValues(caseDetails);
        assertFalse(fieldValuesMap.containsKey("supporter2GivenNames"));
        assertFalse(fieldValuesMap.containsKey("supporter2FamilyNames"));
        assertFalse(fieldValuesMap.containsKey("supporter2AddressDetails"));
        assertFalse(fieldValuesMap.containsKey("financialAmountSupporter2Undertakes1"));
    }

    @Test
    void should_not_set_bail_transfer_direction_data_if_not_present() {
        dataSetUp();
        when(bailCase.read(BAIL_TRANSFER_YES_OR_NO, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        fieldValuesMap = bailDecisionUnsignedGrantedTemplate.mapFieldValues(caseDetails);
        assertFalse(fieldValuesMap.containsKey("bailTransferDirections"));
    }

    @Test
    void should_set_supporter_1_2_3_4_data_if_present() {
        dataSetUp();
        supporter2DataSetUp();
        supporter3DataSetUp();
        supporter4DataSetUp();
        fieldValuesMap = bailDecisionUnsignedGrantedTemplate.mapFieldValues(caseDetails);
        checkCommonFields();
        assertSupporter2Fields();
        assertSupporter3Fields();
        assertSupporter4Fields();
    }

    //Helper method for common assertions
    private void checkCommonFields() {
        assertTrue(fieldValuesMap.containsKey("conditionsForBailResidence"));
        assertTrue(fieldValuesMap.containsKey("conditionsForBailAppearance"));
        assertTrue(fieldValuesMap.containsKey("conditionsForBailActivities"));
        assertTrue(fieldValuesMap.containsKey("conditionsForBailElectronicMonitoring"));
        assertTrue(fieldValuesMap.containsKey("conditionsForBailOther"));
        assertTrue(fieldValuesMap.containsKey("supporterGivenNames"));
        assertTrue(fieldValuesMap.containsKey("supporterFamilyNames"));
        assertTrue(fieldValuesMap.containsKey("supporterAddressDetails"));
        assertTrue(fieldValuesMap.containsKey("financialAmountSupporterUndertakes1"));
        assertTrue(fieldValuesMap.containsKey("bailTransferYesOrNo"));
        assertTrue(fieldValuesMap.containsKey("bailTransferDirections"));
    }

    private void assertSupporter2Fields() {
        assertTrue(fieldValuesMap.containsKey("judgeHasAgreedToSupporter2"));
        assertTrue(fieldValuesMap.containsKey("supporter2GivenNames"));
        assertTrue(fieldValuesMap.containsKey("supporter2FamilyNames"));
        assertTrue(fieldValuesMap.containsKey("supporter2AddressDetails"));
        assertTrue(fieldValuesMap.containsKey("financialAmountSupporter2Undertakes1"));
    }

    private void assertSupporter3Fields() {
        assertTrue(fieldValuesMap.containsKey("judgeHasAgreedToSupporter3"));
        assertTrue(fieldValuesMap.containsKey("supporter3GivenNames"));
        assertTrue(fieldValuesMap.containsKey("supporter3FamilyNames"));
        assertTrue(fieldValuesMap.containsKey("supporter3AddressDetails"));
        assertTrue(fieldValuesMap.containsKey("financialAmountSupporter3Undertakes1"));
    }

    private void assertSupporter4Fields() {
        assertTrue(fieldValuesMap.containsKey("judgeHasAgreedToSupporter4"));
        assertTrue(fieldValuesMap.containsKey("supporter4GivenNames"));
        assertTrue(fieldValuesMap.containsKey("supporter4FamilyNames"));
        assertTrue(fieldValuesMap.containsKey("supporter4AddressDetails"));
        assertTrue(fieldValuesMap.containsKey("financialAmountSupporter4Undertakes1"));
    }

    // Helper method to set the common data
    void dataSetUp() {
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(bailCase.read(CONDITIONS_FOR_BAIL)).thenReturn(Optional.of(conditionsList));
        when(bailCase.read(CONDITIONS_FOR_BAIL_RESIDENCE, String.class)).thenReturn(Optional.of(conditionsForBailResidence));
        when(bailCase.read(CONDITIONS_FOR_BAIL_APPEARANCE, String.class)).thenReturn(Optional.of(conditionsForBailAppearance));
        when(bailCase.read(CONDITIONS_FOR_BAIL_ACTIVITIES, String.class)).thenReturn(Optional.of(conditionsForBailActivities));
        when(bailCase.read(CONDITIONS_FOR_BAIL_ELECTRONIC_MONITORING, String.class)).thenReturn(Optional.of(conditionsForBailElectronicMonitoring));
        when(bailCase.read(CONDITIONS_FOR_BAIL_OTHER, String.class)).thenReturn(Optional.of(conditionsForBailOther));
        when(bailCase.read(RECORD_FINANCIAL_CONDITION_YES_OR_NO, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(JUDGE_HAS_AGREED_TO_SUPPORTER1, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(SUPPORTER_GIVEN_NAMES, String.class)).thenReturn(Optional.of(supporterGivenNames));
        when(bailCase.read(SUPPORTER_GIVEN_NAMES, String.class)).thenReturn(Optional.of(supporterGivenNames));
        when(bailCase.read(SUPPORTER_FAMILY_NAMES, String.class)).thenReturn(Optional.of(supporterFamilyNames));
        when(bailCase.read(SUPPORTER_ADDRESS_DETAILS, AddressUk.class)).thenReturn(Optional.of(supporterAddressUk));
        when(bailCase.read(FINANCIAL_AMOUNT_SUPPORTER_UNDERTAKES_1, String.class)).thenReturn(Optional.of(financialAmountSupporterUndertakes1));

        when(bailCase.read(BAIL_TRANSFER_YES_OR_NO, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(BAIL_TRANSFER_DIRECTIONS, String.class)).thenReturn(Optional.of(bailTransferDirections));
    }

    private void supporter2DataSetUp() {
        when(bailCase.read(JUDGE_HAS_AGREED_TO_SUPPORTER2, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(SUPPORTER_2_GIVEN_NAMES, String.class)).thenReturn(Optional.of(supporterGivenNames));
        when(bailCase.read(SUPPORTER_2_FAMILY_NAMES, String.class)).thenReturn(Optional.of(supporterFamilyNames));
        when(bailCase.read(SUPPORTER_2_ADDRESS_DETAILS, AddressUk.class)).thenReturn(Optional.of(supporterAddressUk));
        when(bailCase.read(FINANCIAL_AMOUNT_SUPPORTER_2_UNDERTAKES_1, String.class)).thenReturn(Optional.of(financialAmountSupporterUndertakes1));
    }

    private void supporter3DataSetUp() {
        when(bailCase.read(JUDGE_HAS_AGREED_TO_SUPPORTER3, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(SUPPORTER_3_GIVEN_NAMES, String.class)).thenReturn(Optional.of(supporterGivenNames));
        when(bailCase.read(SUPPORTER_3_FAMILY_NAMES, String.class)).thenReturn(Optional.of(supporterFamilyNames));
        when(bailCase.read(SUPPORTER_3_ADDRESS_DETAILS, AddressUk.class)).thenReturn(Optional.of(supporterAddressUk));
        when(bailCase.read(FINANCIAL_AMOUNT_SUPPORTER_3_UNDERTAKES_1, String.class)).thenReturn(Optional.of(financialAmountSupporterUndertakes1));
    }

    private void supporter4DataSetUp() {
        when(bailCase.read(JUDGE_HAS_AGREED_TO_SUPPORTER4, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(SUPPORTER_4_GIVEN_NAMES, String.class)).thenReturn(Optional.of(supporterGivenNames));
        when(bailCase.read(SUPPORTER_4_FAMILY_NAMES, String.class)).thenReturn(Optional.of(supporterFamilyNames));
        when(bailCase.read(SUPPORTER_4_ADDRESS_DETAILS, AddressUk.class)).thenReturn(Optional.of(supporterAddressUk));
        when(bailCase.read(FINANCIAL_AMOUNT_SUPPORTER_4_UNDERTAKES_1, String.class)).thenReturn(Optional.of(financialAmountSupporterUndertakes1));
    }
}

