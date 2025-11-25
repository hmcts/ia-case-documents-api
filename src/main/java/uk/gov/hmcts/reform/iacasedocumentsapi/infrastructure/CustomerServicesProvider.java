package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isAppellantInDetention;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isInternalCase;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;


@Service
public class CustomerServicesProvider {

    private final String customerServicesTelephone;
    private String customerServicesEmail;
    @Value("${customerServices.internal.telephoneNumber.ada}")
    private String internalAdaCustomerServicesTelephone;
    @Value("${customerServices.internal.emailAddress.ada}")
    private String internalAdaCustomerServicesEmail;
    private final String standardCustomerServicesEmail;
    private final String internalCaseCustomerServicesEmail;

    private String appealIaCustomerServicesEmail;

    public CustomerServicesProvider(
        @Value("${customerServices.telephoneNumber}") String customerServicesTelephone,
        @Value("${customerServices.emailAddress}") String standardCustomerServicesEmail,
        @Value("${customerServices.internalCaseEmailAddress}") String internalCaseCustomerServicesEmail,
        @Value("${customerServices.appealIaEmailAddress}") String appealIaCustomerServicesEmail
    ) {
        requireNonNull(customerServicesTelephone);
        requireNonNull(standardCustomerServicesEmail);
        requireNonNull(internalCaseCustomerServicesEmail);
        requireNonNull(appealIaCustomerServicesEmail);

        this.customerServicesTelephone = customerServicesTelephone;
        this.standardCustomerServicesEmail = standardCustomerServicesEmail;
        this.internalCaseCustomerServicesEmail = internalCaseCustomerServicesEmail;
        this.customerServicesEmail = standardCustomerServicesEmail;
        this.appealIaCustomerServicesEmail = appealIaCustomerServicesEmail;
    }

    public void setCorrectEmail(AsylumCase asylumCase) {
        this.customerServicesEmail = isInternalCase(asylumCase) && isAcceleratedDetainedAppeal(asylumCase)
            ? internalCaseCustomerServicesEmail
            : standardCustomerServicesEmail;
    }

    public Map<String, String> getCustomerServicesPersonalisation() {

        final Builder<String, String> customerServicesValues = ImmutableMap
            .<String, String>builder()
            .put("customerServicesTelephone", customerServicesTelephone)
            .put("customerServicesEmail", customerServicesEmail)
            .put("AppealIAEmail", appealIaCustomerServicesEmail);

        return customerServicesValues.build();
    }

    public String getCustomerServicesTelephone() {
        requireNonNull(customerServicesTelephone);
        return customerServicesTelephone;
    }

    public String getCustomerServicesEmail() {
        requireNonNull(customerServicesEmail);
        return customerServicesEmail;
    }

    public String getInternalCustomerServicesTelephone(AsylumCase asylumCase) {
        return isInternalCase(asylumCase) && isAppellantInDetention(asylumCase)
                ? internalAdaCustomerServicesTelephone
                : customerServicesTelephone;
    }

    public String getInternalCustomerServicesEmail(AsylumCase asylumCase) {
        return isInternalCase(asylumCase) && isAcceleratedDetainedAppeal(asylumCase)
                ? internalAdaCustomerServicesEmail
                : customerServicesEmail;
    }
}
