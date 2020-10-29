package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
public class CustomerServicesProviderTest {

    private String customerServicesTelephone = "555 555";
    private String customerServicesEmail = "some.email@example.com";

    private CustomerServicesProvider customerServicesProvider;

    @BeforeEach
    public void setUp() {

        customerServicesProvider = new CustomerServicesProvider(
            customerServicesTelephone,
            customerServicesEmail
        );
    }

    @Test
    public void should_return_customer_services_personalisation() {

        Map<String, String> customerServicesPersonalisation =
            customerServicesProvider.getCustomerServicesPersonalisation();

        assertThat(customerServicesPersonalisation.get("customerServicesTelephone"))
            .isEqualTo(customerServicesTelephone);

        assertThat(customerServicesPersonalisation.get("customerServicesEmail")).isEqualTo(customerServicesEmail);
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> new CustomerServicesProvider(null, customerServicesEmail))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new CustomerServicesProvider(customerServicesTelephone, null))
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    public void should_return_customer_services_telephone_number_and_email() {

        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());

        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }
}
