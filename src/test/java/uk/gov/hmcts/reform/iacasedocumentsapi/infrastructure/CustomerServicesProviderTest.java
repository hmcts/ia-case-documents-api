package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.IS_ADMIN;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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

        assertThatThrownBy(() -> new uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider(
            customerServicesTelephone,
            null,
            internalAdaCustomerServicesEmail))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider(
            customerServicesTelephone,
            customerServicesEmail,
            null))
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

        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));

        assertEquals(internalAdaCustomerServicesTelephone,
                customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase));

        assertEquals(internalAdaCustomerServicesEmail,
                customerServicesProvider.getInternalCustomerServicesEmail(asylumCase));
    }

    @ParameterizedTest
    @CsvSource({ "YES, YES", "NO, YES", "YES, NO", "NO, NO" })
    public void should_set_correct_email_based_on_asylum_case(YesOrNo isAdmin, YesOrNo isAda) {
        AsylumCase asylumCase = mock(AsylumCase.class);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(isAdmin));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        customerServicesProvider.setCorrectEmail(asylumCase);

        if (isAdmin.equals(YES) && isAda.equals(YES)) {
            assertEquals(internalAdaCustomerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
        } else {
            assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
        }
    }

    @Test
    public void should_return_internal_non_ada_customer_services_telephone_number_and_email() {
        ReflectionTestUtils.setField(customerServicesProvider, "internalAdaCustomerServicesTelephone", internalAdaCustomerServicesTelephone);
        ReflectionTestUtils.setField(customerServicesProvider, "internalAdaCustomerServicesEmail", internalAdaCustomerServicesEmail);

        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        assertEquals(customerServicesTelephone,
                customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase));

        assertEquals(customerServicesEmail,
                customerServicesProvider.getInternalCustomerServicesEmail(asylumCase));
    }
}
