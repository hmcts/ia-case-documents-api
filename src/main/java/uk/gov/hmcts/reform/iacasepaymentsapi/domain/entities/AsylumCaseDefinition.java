package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities;

import com.fasterxml.jackson.core.type.TypeReference;
import java.math.BigDecimal;

public enum AsylumCaseDefinition {

    APPEAL_REFERENCE_NUMBER(
        "appealReferenceNumber", new TypeReference<String>(){}),

    APPEAL_FEE_DESC(
        "appealFeeDesc", new TypeReference<String>(){}),

    ORAL_FEE_AMOUNT_FOR_DISPLAY(
        "oralFeeAmountForDisplay", new TypeReference<String>(){}),

    FEE_CODE(
        "feeCode", new TypeReference<String>(){}),

    FEE_DESCRIPTION(
        "feeDescription", new TypeReference<String>(){}),

    FEE_VERSION(
        "feeVersion", new TypeReference<Integer>(){}),

    FEE_AMOUNT(
        "feeAmount", new TypeReference<BigDecimal>(){}),

    PAYMENT_STATUS(
        "paymentStatus", new TypeReference<String>(){}),

    ACCOUNT_NUMBER(
        "accountNumber", new TypeReference<String>(){}),

    PAYMENT_DESCRIPTION(
        "paymentDescription", new TypeReference<String>(){}),

    CUSTOMER_REFERENCE(
        "customerReference", new TypeReference<String>(){}),

    PAYMENT_REFERENCE(
        "paymentReference", new TypeReference<String>(){}),

    ERROR_CODE(
        "errorCode", new TypeReference<String>(){}),

    ERROR_MESSAGE(
        "errorMessage", new TypeReference<String>(){}),

    PAY_FOR_THE_APPEAL(
        "payForTheAppeal", new TypeReference<String>(){}),

    APPEAL_TYPE(
        "appealType", new TypeReference<AppealType>(){});

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
