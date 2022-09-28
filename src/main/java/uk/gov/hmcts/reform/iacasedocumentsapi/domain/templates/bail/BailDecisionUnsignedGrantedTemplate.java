package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.bail;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.*;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.DocumentTemplate;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.bail.helper.BailDecisionUnsignedTemplateHelper;

@Component
public class BailDecisionUnsignedGrantedTemplate implements DocumentTemplate<BailCase> {

    private final String templateName;
    private final BailDecisionUnsignedTemplateHelper bailDecisionUnsignedTemplateHelper;

    public BailDecisionUnsignedGrantedTemplate(
        @Value("${decisionUnsignedDocument.grant.templateName}") String templateName,
        BailDecisionUnsignedTemplateHelper bailDecisionUnsignedTemplateHelper) {
        this.templateName = templateName;
        this.bailDecisionUnsignedTemplateHelper = bailDecisionUnsignedTemplateHelper;
    }

    public String getName() {
        return templateName;
    }

    @Override
    public Map<String, Object> mapFieldValues(
        CaseDetails<BailCase> caseDetails
    ) {
        final BailCase bailCase = caseDetails.getCaseData();

        final Map<String, Object> fieldValues = bailDecisionUnsignedTemplateHelper.getCommonMapFieldValues(caseDetails);

        setGrantConditions(bailCase, fieldValues);

        fieldValues.put("recordFinancialConditionYesOrNo", bailCase.read(RECORD_FINANCIAL_CONDITION_YES_OR_NO, YesOrNo.class).orElse(YesOrNo.NO));

        setSupporterDetails(bailCase, fieldValues);
        setSupporter2Details(bailCase, fieldValues);
        setSupporter3Details(bailCase, fieldValues);
        setSupporter4Details(bailCase, fieldValues);

        fieldValues.put("bailTransferYesOrNo", bailCase.read(BAIL_TRANSFER_YES_OR_NO, YesOrNo.class).orElse(YesOrNo.NO));

        if (bailCase.read(BAIL_TRANSFER_YES_OR_NO, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES) {
            fieldValues.put("bailTransferDirections", bailCase.read(BAIL_TRANSFER_DIRECTIONS, String.class).orElse(""));
        }

        return fieldValues;
    }

    private void setGrantConditions(BailCase bailCase, Map<String, Object> fieldValues) {

        Optional<List<String>> conditionsMultiSelectList = bailCase.read(CONDITIONS_FOR_BAIL);

        if (conditionsMultiSelectList.isPresent() && !conditionsMultiSelectList.get().isEmpty()) {
            var conditionsList = conditionsMultiSelectList.get();

            if (conditionsList.contains("bailAppearance")) {
                fieldValues.put("conditionsForBailAppearance", bailCase.read(CONDITIONS_FOR_BAIL_APPEARANCE, String.class).orElse(""));
            }

            if (conditionsList.contains("bailActivities")) {
                fieldValues.put("conditionsForBailActivities", bailCase.read(CONDITIONS_FOR_BAIL_ACTIVITIES, String.class).orElse(""));
            }

            if (conditionsList.contains("bailResidence")) {
                fieldValues.put("conditionsForBailResidence", bailCase.read(CONDITIONS_FOR_BAIL_RESIDENCE, String.class).orElse(""));
            }

            if (conditionsList.contains("bailElectronicMonitoring")) {
                fieldValues.put("conditionsForBailElectronicMonitoring", bailCase.read(CONDITIONS_FOR_BAIL_ELECTRONIC_MONITORING, String.class).orElse(""));
            }

            if (conditionsList.contains("bailReporting")) {
                fieldValues.put("conditionsForBailReporting", bailCase.read(CONDITIONS_FOR_BAIL_REPORTING, String.class).orElse(""));
            }

            if (conditionsList.contains("bailOther")) {
                fieldValues.put("conditionsForBailOther", bailCase.read(CONDITIONS_FOR_BAIL_OTHER, String.class).orElse(""));
            }
        }
    }

    private void setSupporterDetails(BailCase bailCase, Map<String, Object> fieldValues) {
        fieldValues.put("judgeHasAgreedToSupporter1", bailCase.read(JUDGE_HAS_AGREED_TO_SUPPORTER1, YesOrNo.class).orElse(YesOrNo.NO));
        if (bailCase.read(JUDGE_HAS_AGREED_TO_SUPPORTER1, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES) {
            fieldValues.put("supporterGivenNames", bailCase.read(SUPPORTER_GIVEN_NAMES, String.class).orElse(""));
            fieldValues.put("supporterFamilyNames", bailCase.read(SUPPORTER_FAMILY_NAMES, String.class).orElse(""));
            Optional<AddressUk> optionalSupporterAddress = bailCase.read(SUPPORTER_ADDRESS_DETAILS, AddressUk.class);
            if (optionalSupporterAddress.isPresent()) {
                AddressUk supporterAddress = optionalSupporterAddress.get();

                fieldValues.put(
                        "supporterAddressDetails",
                        ImmutableMap
                                .builder()
                                .put("supporterAddressLine1", supporterAddress.getAddressLine1().orElse(""))
                                .put("supporterAddressLine2", supporterAddress.getAddressLine2().orElse(""))
                                .put("supporterAddressLine3", supporterAddress.getAddressLine3().orElse(""))
                                .put("supporterAddressPostTown", supporterAddress.getPostTown().orElse(""))
                                .put("supporterAddressCounty", supporterAddress.getCounty().orElse(""))
                                .put("supporterAddressPostCode", supporterAddress.getPostCode().orElse(""))
                                .put("supporterAddressCountry", supporterAddress.getCountry().orElse(""))
                                .build()
                );
            }

            fieldValues.put("financialAmountSupporterUndertakes1", bailCase.read(FINANCIAL_AMOUNT_SUPPORTER_UNDERTAKES_1, String.class).orElse(""));
        }
    }

    private void setSupporter2Details(BailCase bailCase, Map<String, Object> fieldValues) {
        fieldValues.put("judgeHasAgreedToSupporter2", bailCase.read(JUDGE_HAS_AGREED_TO_SUPPORTER2, YesOrNo.class).orElse(YesOrNo.NO));
        if (bailCase.read(JUDGE_HAS_AGREED_TO_SUPPORTER2, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES) {
            fieldValues.put("supporter2GivenNames", bailCase.read(SUPPORTER_2_GIVEN_NAMES, String.class).orElse(""));
            fieldValues.put("supporter2FamilyNames", bailCase.read(SUPPORTER_2_FAMILY_NAMES, String.class).orElse(""));
            Optional<AddressUk> optionalSupporterAddress = bailCase.read(SUPPORTER_2_ADDRESS_DETAILS, AddressUk.class);
            if (optionalSupporterAddress.isPresent()) {
                AddressUk supporterAddress = optionalSupporterAddress.get();

                fieldValues.put(
                        "supporter2AddressDetails",
                        ImmutableMap
                                .builder()
                                .put("supporter2AddressLine1", supporterAddress.getAddressLine1().orElse(""))
                                .put("supporter2AddressLine2", supporterAddress.getAddressLine2().orElse(""))
                                .put("supporter2AddressLine3", supporterAddress.getAddressLine3().orElse(""))
                                .put("supporter2AddressPostTown", supporterAddress.getPostTown().orElse(""))
                                .put("supporter2AddressCounty", supporterAddress.getCounty().orElse(""))
                                .put("supporter2AddressPostCode", supporterAddress.getPostCode().orElse(""))
                                .put("supporter2AddressCountry", supporterAddress.getCountry().orElse(""))
                                .build()
                );
            }

            fieldValues.put("financialAmountSupporter2Undertakes1", bailCase.read(FINANCIAL_AMOUNT_SUPPORTER_2_UNDERTAKES_1, String.class).orElse(""));
        }
    }

    private void setSupporter3Details(BailCase bailCase, Map<String, Object> fieldValues) {
        fieldValues.put("judgeHasAgreedToSupporter3", bailCase.read(JUDGE_HAS_AGREED_TO_SUPPORTER3, YesOrNo.class).orElse(YesOrNo.NO));
        if (bailCase.read(JUDGE_HAS_AGREED_TO_SUPPORTER3, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES) {
            fieldValues.put("supporter3GivenNames", bailCase.read(SUPPORTER_3_GIVEN_NAMES, String.class).orElse(""));
            fieldValues.put("supporter3FamilyNames", bailCase.read(SUPPORTER_3_FAMILY_NAMES, String.class).orElse(""));
            Optional<AddressUk> optionalSupporterAddress = bailCase.read(SUPPORTER_3_ADDRESS_DETAILS, AddressUk.class);
            if (optionalSupporterAddress.isPresent()) {
                AddressUk supporterAddress = optionalSupporterAddress.get();

                fieldValues.put(
                        "supporter3AddressDetails",
                        ImmutableMap
                                .builder()
                                .put("supporter3AddressLine1", supporterAddress.getAddressLine1().orElse(""))
                                .put("supporter3AddressLine2", supporterAddress.getAddressLine2().orElse(""))
                                .put("supporter3AddressLine3", supporterAddress.getAddressLine3().orElse(""))
                                .put("supporter3AddressPostTown", supporterAddress.getPostTown().orElse(""))
                                .put("supporter3AddressCounty", supporterAddress.getCounty().orElse(""))
                                .put("supporter3AddressPostCode", supporterAddress.getPostCode().orElse(""))
                                .put("supporter3AddressCountry", supporterAddress.getCountry().orElse(""))
                                .build()
                );
            }

            fieldValues.put("financialAmountSupporter3Undertakes1", bailCase.read(FINANCIAL_AMOUNT_SUPPORTER_3_UNDERTAKES_1, String.class).orElse(""));
        }
    }

    private void setSupporter4Details(BailCase bailCase, Map<String, Object> fieldValues) {
        fieldValues.put("judgeHasAgreedToSupporter4", bailCase.read(JUDGE_HAS_AGREED_TO_SUPPORTER4, YesOrNo.class).orElse(YesOrNo.NO));
        if (bailCase.read(JUDGE_HAS_AGREED_TO_SUPPORTER4, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES) {
            fieldValues.put("supporter4GivenNames", bailCase.read(SUPPORTER_4_GIVEN_NAMES, String.class).orElse(""));
            fieldValues.put("supporter4FamilyNames", bailCase.read(SUPPORTER_4_FAMILY_NAMES, String.class).orElse(""));
            Optional<AddressUk> optionalSupporterAddress = bailCase.read(SUPPORTER_4_ADDRESS_DETAILS, AddressUk.class);
            if (optionalSupporterAddress.isPresent()) {
                AddressUk supporterAddress = optionalSupporterAddress.get();
                fieldValues.put(
                        "supporter4AddressDetails",
                        ImmutableMap
                                .builder()
                                .put("supporter4AddressLine1", supporterAddress.getAddressLine1().orElse(""))
                                .put("supporter4AddressLine2", supporterAddress.getAddressLine2().orElse(""))
                                .put("supporter4AddressLine3", supporterAddress.getAddressLine3().orElse(""))
                                .put("supporter4AddressPostTown", supporterAddress.getPostTown().orElse(""))
                                .put("supporter4AddressCounty", supporterAddress.getCounty().orElse(""))
                                .put("supporter4AddressPostCode", supporterAddress.getPostCode().orElse(""))
                                .put("supporter4AddressCountry", supporterAddress.getCountry().orElse(""))
                                .build()
                );
            }

            fieldValues.put("financialAmountSupporter4Undertakes1", bailCase.read(FINANCIAL_AMOUNT_SUPPORTER_4_UNDERTAKES_1, String.class).orElse(""));
        }
    }
}
