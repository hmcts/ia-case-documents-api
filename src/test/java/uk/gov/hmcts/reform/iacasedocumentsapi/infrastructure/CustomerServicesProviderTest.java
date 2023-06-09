package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;


@ExtendWith(MockitoExtension.class)
public class CustomerServicesProviderTest {

    @Mock CustomerServicesProvider customerServicesProvider;

    private String customerServicesTelephone = "555 555";
    private String customerServicesEmail = "some.email@example.com";
    private String internalAdaCustomerServicesTelephone = "111 111";
    private String internalAdaCustomerServicesEmail = "some.email@example.com";

    @BeforeEach
    public void setUp() {
        customerServicesProvider = new CustomerServicesProvider(
            customerServicesTelephone,
            customerServicesEmail
        );
    }

    @Test
    public void should_return_customer_services_personalisation() {

        Map<String, String> customerServicesPersonalisation = customerServicesProvider.getCustomerServicesPersonalisation();

        assertEquals(customerServicesPersonalisation.get("customerServicesTelephone"), customerServicesTelephone);

        assertEquals(customerServicesPersonalisation.get("customerServicesEmail"), customerServicesEmail);
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

    @Test
    public void should_return_internal_ada_customer_services_telephone_number_and_email() {
        ReflectionTestUtils.setField(customerServicesProvider, "internalAdaCustomerServicesTelephone", internalAdaCustomerServicesTelephone);
        ReflectionTestUtils.setField(customerServicesProvider, "internalAdaCustomerServicesEmail", internalAdaCustomerServicesEmail);

        assertEquals(internalAdaCustomerServicesTelephone, customerServicesProvider.getInternalAdaCustomerServicesTelephone());

        assertEquals(internalAdaCustomerServicesEmail, customerServicesProvider.getInternalAdaCustomerServicesEmail());
    }
}
