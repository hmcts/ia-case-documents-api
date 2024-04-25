package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;


@ExtendWith(MockitoExtension.class)
public class CustomerServicesProviderTest {

    @Mock CustomerServicesProvider customerServicesProvider;

    @Mock
    private AsylumCase asylumCase;

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

        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        assertEquals(internalAdaCustomerServicesTelephone,
                customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase));

        assertEquals(internalAdaCustomerServicesEmail,
                customerServicesProvider.getInternalCustomerServicesEmail(asylumCase));
    }

    @Test
    public void should_return_internal_non_ada_customer_services_telephone_number_and_email() {
        ReflectionTestUtils.setField(customerServicesProvider, "internalAdaCustomerServicesTelephone", internalAdaCustomerServicesTelephone);
        ReflectionTestUtils.setField(customerServicesProvider, "internalAdaCustomerServicesEmail", internalAdaCustomerServicesEmail);

        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        assertEquals(customerServicesTelephone,
                customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase));

        assertEquals(customerServicesEmail,
                customerServicesProvider.getInternalCustomerServicesEmail(asylumCase));
    }
}
