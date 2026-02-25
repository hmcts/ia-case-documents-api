package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InternalDetainedAppealHoUploadBundleAppellantTemplateTest {

    private static final String TEMPLATE_NAME = "test-template";
    private static final String CASE_REF = "123456";
    private static final String CUSTOMER_PHONE = "0300 123 456";
    private static final String CUSTOMER_EMAIL = "contact@test.com";

    @Mock
    private AsylumCase asylumCase;

    @Mock
    private CaseDetails<AsylumCase> caseDetails;

    @Mock
    private CustomerServicesProvider customerServicesProvider;

    private InternalDetainedAppealHoUploadBundleAppellantTemplate template;

    @BeforeEach
    void setUp() {
        template = new InternalDetainedAppealHoUploadBundleAppellantTemplate(
                TEMPLATE_NAME,
                customerServicesProvider
        );

        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    void should_return_template_name() {
        assertThat(template.getName()).isEqualTo(TEMPLATE_NAME);
    }

    @Test
    void should_map_field_values_with_direction_due_date() {
        Direction direction = mock(Direction.class);
        when(direction.getDateDue()).thenReturn("2025-10-20");

        when(asylumCase.read(LAST_MODIFIED_DIRECTION, Direction.class)).thenReturn(Optional.of(direction));
        when(asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY)).thenReturn(Optional.of(CASE_REF));
        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase)).thenReturn(CUSTOMER_PHONE);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase)).thenReturn(CUSTOMER_EMAIL);

        Map<String, Object> fieldValues = template.mapFieldValues(caseDetails);

        assertThat(fieldValues.get("onlineCaseRefNumber")).isEqualTo(CASE_REF);
        assertThat(fieldValues.get("directionDueDate")).isNotNull();
        assertThat(fieldValues.get("customerServicesTelephone")).isEqualTo(CUSTOMER_PHONE);
        assertThat(fieldValues.get("customerServicesEmail")).isEqualTo(CUSTOMER_EMAIL);

        assertThat(fieldValues.get("dateLetterSent")).isNotNull();
    }

    @Test
    void should_map_field_values_with_empty_direction_due_date_if_not_present() {
        when(asylumCase.read(LAST_MODIFIED_DIRECTION, Direction.class)).thenReturn(Optional.empty());
        when(asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY)).thenReturn(Optional.of(CASE_REF));
        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase)).thenReturn(CUSTOMER_PHONE);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase)).thenReturn(CUSTOMER_EMAIL);

        Map<String, Object> fieldValues = template.mapFieldValues(caseDetails);

        assertThat(fieldValues.get("directionDueDate")).isEqualTo("");
        assertThat(fieldValues.get("onlineCaseRefNumber")).isEqualTo(CASE_REF);
    }
}
