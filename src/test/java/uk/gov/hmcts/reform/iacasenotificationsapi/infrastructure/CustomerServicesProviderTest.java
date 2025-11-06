package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_ADMIN;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class CustomerServicesProviderTest {

    private String customerServicesTelephone = "555 555";
    private String standardCustomerServicesEmail = "some.email@example.com";
    private String internalCaseCustomerServicesEmail = "some.internal.email@example.com";
    private String appealIaCustomerServicesEmail = "some.appeal.email@example.com";

    private CustomerServicesProvider customerServicesProvider;

    @BeforeEach
    public void setUp() {

        customerServicesProvider = new CustomerServicesProvider(
            customerServicesTelephone,
            standardCustomerServicesEmail,
            internalCaseCustomerServicesEmail,
            appealIaCustomerServicesEmail
        );
    }

    @Test
    public void should_return_customer_services_personalisation() {

        Map<String, String> customerServicesPersonalisation =
            customerServicesProvider.getCustomerServicesPersonalisation();

        assertThat(customerServicesPersonalisation.get("customerServicesTelephone"))
            .isEqualTo(customerServicesTelephone);
        assertThat(customerServicesPersonalisation.get("customerServicesEmail")).isEqualTo(standardCustomerServicesEmail);
        assertThat(customerServicesPersonalisation.get("AppealIAEmail"))
                .isEqualTo(appealIaCustomerServicesEmail);
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> new CustomerServicesProvider(
            null,
            standardCustomerServicesEmail,
            internalCaseCustomerServicesEmail,
            appealIaCustomerServicesEmail))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new CustomerServicesProvider(
            customerServicesTelephone,
            null,
            internalCaseCustomerServicesEmail,
            appealIaCustomerServicesEmail))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new CustomerServicesProvider(
            customerServicesTelephone,
            standardCustomerServicesEmail,
            null,
            appealIaCustomerServicesEmail))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new CustomerServicesProvider(
                customerServicesTelephone,
                standardCustomerServicesEmail,
                internalCaseCustomerServicesEmail,
                null))
                .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    public void should_return_customer_services_telephone_number_and_email() {

        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());

        assertEquals(standardCustomerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }

    @ParameterizedTest
    @CsvSource({ "YES, YES", "NO, YES", "YES, NO", "NO, NO" })
    public void should_set_correct_email_based_on_asylum_case(YesOrNo isAdmin, YesOrNo isAda) {
        AsylumCase asylumCase = mock(AsylumCase.class);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(isAdmin));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        customerServicesProvider.setCorrectEmail(asylumCase);

        if (isAdmin.equals(YES) && isAda.equals(YES)) {
            assertEquals(internalCaseCustomerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
        } else {
            assertEquals(standardCustomerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
        }

    }
}
