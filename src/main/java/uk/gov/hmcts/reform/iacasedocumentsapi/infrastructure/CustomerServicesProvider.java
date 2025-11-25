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

    private String customerServicesEmail;
    private final String customerServicesTelephone;
    private final String internalAdaCustomerServicesTelephone;
    private final String internalAdaCustomerServicesEmail;
    private final String standardCustomerServicesEmail;
    private final String appealIaCustomerServicesEmail;

    public CustomerServicesProvider(
        @Value("${customerServices.telephoneNumber}") String customerServicesTelephone,
        @Value("${customerServices.emailAddress}") String standardCustomerServicesEmail,
        @Value("${customerServices.appealIaEmailAddress}") String appealIaCustomerServicesEmail,
        @Value("${customerServices.internal.emailAddress.ada}") String internalAdaCustomerServicesEmail,
        @Value("${customerServices.internal.telephoneNumber.ada}") String internalAdaCustomerServicesTelephone
    ) {
        requireNonNull(customerServicesTelephone);
        requireNonNull(standardCustomerServicesEmail);
        requireNonNull(appealIaCustomerServicesEmail);
        requireNonNull(internalAdaCustomerServicesEmail);
        requireNonNull(internalAdaCustomerServicesTelephone);

        this.customerServicesTelephone = customerServicesTelephone;
        this.standardCustomerServicesEmail = standardCustomerServicesEmail;
        this.customerServicesEmail = standardCustomerServicesEmail;
        this.appealIaCustomerServicesEmail = appealIaCustomerServicesEmail;
        this.internalAdaCustomerServicesEmail = internalAdaCustomerServicesEmail;
        this.internalAdaCustomerServicesTelephone = internalAdaCustomerServicesTelephone;
    }

    public void setCorrectEmail(AsylumCase asylumCase) {
        this.customerServicesEmail = isInternalCase(asylumCase) && isAcceleratedDetainedAppeal(asylumCase)
            ? internalAdaCustomerServicesEmail
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
