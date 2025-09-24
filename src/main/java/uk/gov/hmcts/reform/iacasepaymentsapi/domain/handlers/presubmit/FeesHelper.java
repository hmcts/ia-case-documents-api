package uk.gov.hmcts.reform.iacasepaymentsapi.domain.handlers.presubmit;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.DECISION_HEARING_FEE_OPTION;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_AMOUNT_GBP;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_CODE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_DESCRIPTION;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_PAYMENT_APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_VERSION;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_WITHOUT_HEARING;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_WITH_HEARING;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_DESCRIPTION;

import java.math.BigDecimal;
import java.util.Optional;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.Fee;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.FeeType;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.service.FeeService;

public class FeesHelper {

    private FeesHelper() {
    }

    public static final Fee findFeeByHearingType(FeeService feeService, AsylumCase asylumCase) {
        Fee fee;
        Optional<String> decisionHearingFeeOption = asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class);
        if (decisionHearingFeeOption.isPresent()) {

            FeeType feeType = decisionHearingFeeOption.get().equals("decisionWithHearing")
                ? FeeType.FEE_WITH_HEARING
                : FeeType.FEE_WITHOUT_HEARING;

            fee = feeService.getFee(feeType);

            if (!isNull(fee)) {

                String feeAmountInPence =
                    String.valueOf(new BigDecimal(fee.getAmountAsString()).multiply(new BigDecimal("100")));
                asylumCase.write(FEE_CODE, fee.getCode());
                asylumCase.write(FEE_DESCRIPTION, fee.getDescription());
                asylumCase.write(FEE_VERSION, fee.getVersion());
                asylumCase.write(FEE_AMOUNT_GBP, feeAmountInPence);
                asylumCase.write(FEE_PAYMENT_APPEAL_TYPE, YesOrNo.YES);

                switch (decisionHearingFeeOption.get()) {
                    case "decisionWithHearing":
                        asylumCase.write(FEE_WITH_HEARING, fee.getAmountAsString());
                        asylumCase.write(PAYMENT_DESCRIPTION, "Appeal determined with a hearing");
                        return fee;

                    case "decisionWithoutHearing":
                        asylumCase.write(FEE_WITHOUT_HEARING, fee.getAmountAsString());
                        asylumCase.write(PAYMENT_DESCRIPTION, "Appeal determined without a hearing");
                        return fee;

                    default:
                        break;
                }
            }
        }
        return null;
    }
}
