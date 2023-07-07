package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isInternalCase;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;


@Service
public class CustomerServicesProvider {

    private final String customerServicesTelephone;
    private final String customerServicesEmail;
    @Value("${customerServices.internal.telephoneNumber.ada}")
    private String internalAdaCustomerServicesTelephone;
    @Value("${customerServices.internal.emailAddress.ada}")
    private String internalAdaCustomerServicesEmail;


    public CustomerServicesProvider(
        @Value("${customerServices.telephoneNumber}") String customerServicesTelephone,
        @Value("${customerServices.emailAddress}") String customerServicesEmail
    ) {
        requireNonNull(customerServicesTelephone);
        requireNonNull(customerServicesEmail);

        this.customerServicesTelephone = customerServicesTelephone;
        this.customerServicesEmail = customerServicesEmail;
    }

    public Map<String, String> getCustomerServicesPersonalisation() {

        final ImmutableMap.Builder<String, String> customerServicesValues = ImmutableMap
            .<String, String>builder()
            .put("customerServicesTelephone", customerServicesTelephone)
            .put("customerServicesEmail", customerServicesEmail);

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
        return isInternalCase(asylumCase) && isAcceleratedDetainedAppeal(asylumCase)
                ? internalAdaCustomerServicesTelephone
                : customerServicesTelephone;
    }

    public String getInternalCustomerServicesEmail(AsylumCase asylumCase) {
        return isInternalCase(asylumCase) && isAcceleratedDetainedAppeal(asylumCase)
                ? internalAdaCustomerServicesEmail
                : customerServicesEmail;
    }
}
