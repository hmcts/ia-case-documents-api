package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities;

import com.fasterxml.jackson.core.type.TypeReference;
import java.math.BigDecimal;

public enum AsylumCaseDefinition {

    APPEAL_REFERENCE_NUMBER(
        "appealReferenceNumber", new TypeReference<String>(){}),
    FEE_HEARING_AMOUNT_FOR_DISPLAY(
        "feeHearingAmountForDisplay", new TypeReference<String>(){}),
    FEE_WITHOUT_HEARING_AMOUNT_FOR_DISPLAY(
        "feeWithoutHearingAmountForDisplay", new TypeReference<String>(){}),
    APPEAL_FEE_HEARING_DESC(
        "appealFeeHearingDesc", new TypeReference<String>(){}),
    APPEAL_FEE_WITHOUT_HEARING_DESC(
        "appealFeeWithoutHearingDesc", new TypeReference<String>(){}),
    FEE_CODE(
        "feeCode", new TypeReference<String>(){}),
    FEE_DESCRIPTION(
        "feeDescription", new TypeReference<String>(){}),
    FEE_VERSION(
        "feeVersion", new TypeReference<String>(){}),
    FEE_AMOUNT(
        "feeAmount", new TypeReference<BigDecimal>(){}),
    PAYMENT_STATUS(
        "paymentStatus", new TypeReference<String>(){}),
    FEE_PAYMENT_APPEAL_TYPE(
        "feePaymentAppealType", new TypeReference<String>(){}),
    PBA_NUMBER(
        "pbaNumber", new TypeReference<String>(){}),
    PAYMENT_DESCRIPTION(
        "paymentDescription", new TypeReference<String>(){}),
    HOME_OFFICE_REFERENCE_NUMBER(
        "homeOfficeReferenceNumber", new TypeReference<String>(){}),
    PAYMENT_REFERENCE(
        "paymentReference", new TypeReference<String>(){}),
    ERROR_CODE(
        "errorCode", new TypeReference<String>(){}),
    ERROR_MESSAGE(
        "errorMessage", new TypeReference<String>(){}),
    APPEAL_TYPE(
        "appealType", new TypeReference<AppealType>(){}),
    DECISION_WITH_HEARING(
        "decisionWithHearing", new TypeReference<String>(){}),
    DECISION_WITHOUT_HEARING(
        "decisionWithoutHearing", new TypeReference<String>(){}),
    DECISION_HEARING_FEE_OPTION(
        "decisionHearingFeeOption", new TypeReference<String>(){});

    private final String value;
    private final TypeReference typeReference;

    AsylumCaseDefinition(String value, TypeReference typeReference) {
        this.value = value;
        this.typeReference = typeReference;
    }

    public String value() {
        return value;
    }

    public TypeReference getTypeReference() {
        return typeReference;
    }
}
