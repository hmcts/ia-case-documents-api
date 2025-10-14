package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isInternalCase;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class CustomerServicesProvider {

    private final String customerServicesTelephone;
    private final String standardCustomerServicesEmail;
    private final String internalCaseCustomerServicesEmail;

    private String customerServicesEmail;

    public CustomerServicesProvider(
        @Value("${customerServices.telephoneNumber}") String customerServicesTelephone,
        @Value("${customerServices.emailAddress}") String standardCustomerServicesEmail,
        @Value("${customerServices.internalCaseEmailAddress}") String internalCaseCustomerServicesEmail
    ) {
        requireNonNull(customerServicesTelephone);
        requireNonNull(standardCustomerServicesEmail);
        requireNonNull(internalCaseCustomerServicesEmail);

        this.customerServicesTelephone = customerServicesTelephone;
        this.standardCustomerServicesEmail = standardCustomerServicesEmail;
        this.internalCaseCustomerServicesEmail = internalCaseCustomerServicesEmail;
        this.customerServicesEmail = standardCustomerServicesEmail;
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
}
