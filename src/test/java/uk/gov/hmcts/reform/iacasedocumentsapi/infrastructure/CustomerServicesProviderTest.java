package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;


@ExtendWith(MockitoExtension.class)
public class CustomerServicesProviderTest {

    @Mock
    CustomerServicesProvider customerServicesProvider;

    @Mock(lenient = true)
    private AsylumCase asylumCase;

    private final String customerServicesTelephone = "555 555";
    private final String internalAdaCustomerServicesTelephone = "111 111";
    private final String internalAdaCustomerServicesEmail = "some.internal.email@example.com";
    private final String standardCustomerServicesEmail = "some.standard.email@example.com";
    private final String appealIaCustomerServicesEmail = "some.appeal.ia.email@example.com";

    @BeforeEach
    public void setUp() {
        customerServicesProvider = new CustomerServicesProvider(
            customerServicesTelephone,
            standardCustomerServicesEmail,
            appealIaCustomerServicesEmail,
            internalAdaCustomerServicesEmail,
            internalAdaCustomerServicesTelephone
        );
    }

    @Test
    public void should_return_customer_services_personalisation() {

        Map<String, String> customerServicesPersonalisation = customerServicesProvider.getCustomerServicesPersonalisation();

        assertEquals(customerServicesPersonalisation.get("customerServicesTelephone"), customerServicesTelephone);
        assertEquals(customerServicesPersonalisation.get("customerServicesEmail"), standardCustomerServicesEmail);
        assertEquals(customerServicesPersonalisation.get("AppealIAEmail"), appealIaCustomerServicesEmail);
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThrows(
            NullPointerException.class, () -> new CustomerServicesProvider(
                null,
                standardCustomerServicesEmail,
                appealIaCustomerServicesEmail,
                internalAdaCustomerServicesEmail,
                internalAdaCustomerServicesTelephone
            )
        );

        assertThrows(
            NullPointerException.class, () -> new CustomerServicesProvider(
                customerServicesTelephone,
                null,
                appealIaCustomerServicesEmail,
                internalAdaCustomerServicesEmail,
                internalAdaCustomerServicesTelephone
            )
        );

        assertThrows(
            NullPointerException.class, () -> new CustomerServicesProvider(
                customerServicesTelephone,
                standardCustomerServicesEmail,
                null,
                internalAdaCustomerServicesEmail,
                internalAdaCustomerServicesTelephone
            )
        );

        assertThrows(
            NullPointerException.class, () -> new CustomerServicesProvider(
                customerServicesTelephone,
                standardCustomerServicesEmail,
                appealIaCustomerServicesEmail,
                null,
                internalAdaCustomerServicesTelephone
            )
        );

        assertThrows(
            NullPointerException.class, () -> new CustomerServicesProvider(
                customerServicesTelephone,
                standardCustomerServicesEmail,
                appealIaCustomerServicesEmail,
                internalAdaCustomerServicesEmail,
                null
            )
        );

    }

    @Test
    public void should_return_customer_services_telephone_number_and_email() {
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(standardCustomerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }

    @Test
    public void should_return_internal_ada_customer_services_telephone_number_and_email() {
        ReflectionTestUtils.setField(
            customerServicesProvider,
            "internalAdaCustomerServicesTelephone",
            internalAdaCustomerServicesTelephone
        );
        ReflectionTestUtils.setField(
            customerServicesProvider,
            "internalAdaCustomerServicesEmail",
            internalAdaCustomerServicesEmail
        );

        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        assertEquals(
            internalAdaCustomerServicesTelephone,
            customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase)
        );

        assertEquals(
            internalAdaCustomerServicesEmail,
            customerServicesProvider.getInternalCustomerServicesEmail(asylumCase)
        );
    }

    @Test
    public void should_return_internal_non_ada_customer_services_telephone_number_and_email() {
        ReflectionTestUtils.setField(
            customerServicesProvider,
            "internalAdaCustomerServicesTelephone",
            internalAdaCustomerServicesTelephone
        );
        ReflectionTestUtils.setField(
            customerServicesProvider,
            "internalAdaCustomerServicesEmail",
            internalAdaCustomerServicesEmail
        );

        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        assertEquals(
            customerServicesTelephone,
            customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase)
        );

        assertEquals(
            standardCustomerServicesEmail,
            customerServicesProvider.getInternalCustomerServicesEmail(asylumCase)
        );
    }

    @ParameterizedTest
    @CsvSource({"YES, YES", "NO, YES", "YES, NO", "NO, NO"})
    public void should_set_correct_email_based_on_asylum_case(YesOrNo isAdmin, YesOrNo isAda) {
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(isAdmin));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        customerServicesProvider.setCorrectEmail(asylumCase);

        if (isAdmin.equals(YES) && isAda.equals(YES)) {
            assertEquals(internalAdaCustomerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
        } else {
            assertEquals(standardCustomerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
        }

    }
}
