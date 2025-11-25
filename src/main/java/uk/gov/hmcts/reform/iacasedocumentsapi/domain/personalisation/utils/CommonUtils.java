package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.apache.commons.lang3.StringUtils;

public final class CommonUtils {
    private CommonUtils() {
        // private constructor to prevent sonar warning
    }

    public static String convertAsylumCaseFeeValue(String amountFromAsylumCase) {
        return StringUtils.isNotBlank(amountFromAsylumCase)
            ? new BigDecimal(String.valueOf(Double.parseDouble(amountFromAsylumCase) / 100))
            .setScale(2, RoundingMode.DOWN).toString()
            : "";
    }
}
