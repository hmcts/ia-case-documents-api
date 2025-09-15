package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.CCD_REFERENCE_NUMBER_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.DIRECTIONS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LAST_MODIFIED_DIRECTION;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InternalDetainedResponderReviewIrcPrisonTemplateTest {

    private static final String TEMPLATE_NAME = "test-template";
    private static final String CASE_REF = "123456";
    private static final String CUSTOMER_PHONE = "0300 123 456";
    private static final String CUSTOMER_EMAIL = "contact@test.com";

    @Mock private AsylumCase asylumCase;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private CustomerServicesProvider customerServicesProvider;
    @Mock private DateProvider dateProvider;

    private InternalDetainedResponderReviewIrcPrisonTemplate template;

    @BeforeEach
    void setUp() {
        template = new InternalDetainedResponderReviewIrcPrisonTemplate(
                TEMPLATE_NAME,
                dateProvider,
                customerServicesProvider
        );

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(dateProvider.now()).thenReturn(LocalDate.of(2025, 9, 15));
    }

    @Test
    void should_return_template_name() {
        assertThat(template.getName()).isEqualTo(TEMPLATE_NAME);
    }

    @Test
    void should_map_field_values_with_direction_due_date() {
        Direction direction = mock(Direction.class);
        when(direction.getDateDue()).thenReturn("2025-10-20");

        when(asylumCase.read(DIRECTIONS)).thenReturn(Optional.of(Collections.singletonList(direction)));
        when(asylumCase.read(LAST_MODIFIED_DIRECTION, Direction.class)).thenReturn(Optional.of(direction));
        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase)).thenReturn(CUSTOMER_PHONE);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase)).thenReturn(CUSTOMER_EMAIL);

        try (MockedStatic<AsylumCaseUtils> mocked = mockStatic(AsylumCaseUtils.class)) {
            mocked.when(() -> AsylumCaseUtils.getAppellantPersonalisation(asylumCase))
                    .thenReturn(Map.of("onlineCaseRefNumber", CASE_REF));

            Map<String, Object> fieldValues = template.mapFieldValues(caseDetails);

            assertThat(fieldValues.get("onlineCaseRefNumber")).isEqualTo(CASE_REF);
            assertThat(fieldValues.get("directionDueDate")).isEqualTo("20 Oct 2025");  // Formatted
            assertThat(fieldValues.get("customerServicesTelephone")).isEqualTo(CUSTOMER_PHONE);
            assertThat(fieldValues.get("customerServicesEmail")).isEqualTo(CUSTOMER_EMAIL);
            assertThat(fieldValues.get("dateLetterSent")).isEqualTo("15 Sept 2025");
        }
    }

    @Test
    void should_map_field_values_with_empty_direction_due_date_if_not_present() {
        when(asylumCase.read(DIRECTIONS)).thenReturn(Optional.of(Collections.emptyList()));
        when(asylumCase.read(LAST_MODIFIED_DIRECTION, Direction.class)).thenReturn(Optional.empty());
        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase)).thenReturn(CUSTOMER_PHONE);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase)).thenReturn(CUSTOMER_EMAIL);

        try (MockedStatic<AsylumCaseUtils> mocked = mockStatic(AsylumCaseUtils.class)) {
            mocked.when(() -> AsylumCaseUtils.getAppellantPersonalisation(asylumCase))
                    .thenReturn(Map.of("onlineCaseRefNumber", CASE_REF));

            Map<String, Object> fieldValues = template.mapFieldValues(caseDetails);

            assertThat(fieldValues.get("directionDueDate")).isEqualTo("");
            assertThat(fieldValues.get("onlineCaseRefNumber")).isEqualTo(CASE_REF);
            assertThat(fieldValues.get("dateLetterSent")).isEqualTo("15 Sept 2025");
        }
    }

}
