package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.CCD_REFERENCE_NUMBER_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.convertAsylumCaseFeeValue;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getAppellantPersonalisation;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.DateUtils.formatDateForNotificationAttachmentDocument;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InternalDetainedAppealRemissionGrantedInTimeTemplateTest {

    private static final String TEMPLATE_NAME = "detained-remission-template";

    @Mock
    private CustomerServicesProvider customerServicesProvider;

    @Mock
    private CaseDetails<AsylumCase> caseDetails;

    @Mock
    private AsylumCase asylumCase;

    private InternalDetainedAppealRemissionGrantedInTimeTemplate template;

    @BeforeEach
    void setUp() {
        template = new InternalDetainedAppealRemissionGrantedInTimeTemplate(
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
    void should_map_all_field_values() {
        when(asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY)).thenReturn(Optional.of("CASE123"));

        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase)).thenReturn("0300 123 456");
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase)).thenReturn("internal@example.com");

        try (MockedStatic<uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils> asylumCaseUtils =
                     mockStatic(uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.class);
             MockedStatic<uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.DateUtils> dateUtils =
                     mockStatic(uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.DateUtils.class)) {

            asylumCaseUtils.when(() -> convertAsylumCaseFeeValue("140.00")).thenReturn("£140");
            Map<String, Object> appellantMap = new HashMap<>();
            appellantMap.put("appellantName", "John Doe");
            asylumCaseUtils.when(() -> getAppellantPersonalisation(asylumCase)).thenReturn(appellantMap);

            dateUtils.when(() -> formatDateForNotificationAttachmentDocument(any(LocalDate.class)))
                    .thenReturn("2025-08-28");

            Map<String, Object> result = template.mapFieldValues(caseDetails);

            assertThat(result)
                    .containsEntry("dateLetterSent", "2025-08-28")
                    .containsEntry("appellantName", "John Doe")
                    .containsEntry("onlineCaseRefNumber", Optional.of("CASE123"))
                    .containsEntry("customerServicesTelephone", "0300 123 456")
                    .containsEntry("customerServicesEmail", "internal@example.com");
        }
    }
}
