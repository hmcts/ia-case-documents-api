package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.SystemDateProvider;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.CCD_REFERENCE_NUMBER_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.FEE_AMOUNT_GBP;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.convertAsylumCaseFeeValue;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getAppellantPersonalisation;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.DateUtils.formatDateForNotificationAttachmentDocument;

class InternalDetainedAppealRemissionGrantedInTimeTemplateTest {

    private static final String TEMPLATE_NAME = "detained-remission-template";
    private static final int DAYS_AFTER_SUBMIT = 5;

    @Mock
    private CustomerServicesProvider customerServicesProvider;

    @Mock
    private SystemDateProvider systemDateProvider;

    @Mock
    private CaseDetails<AsylumCase> caseDetails;

    @Mock
    private AsylumCase asylumCase;

    private InternalDetainedAppealRemissionGrantedInTimeTemplate template;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        template = new InternalDetainedAppealRemissionGrantedInTimeTemplate(
                TEMPLATE_NAME,
                DAYS_AFTER_SUBMIT,
                customerServicesProvider,
                systemDateProvider
        );

        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void should_return_template_name() {
        assertThat(template.getName()).isEqualTo(TEMPLATE_NAME);
    }

    @Test
    void should_map_all_field_values() {
        // --- mock asylumCase reads ---
        when(asylumCase.read(FEE_AMOUNT_GBP, String.class)).thenReturn(Optional.of("140.00"));
        when(asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY)).thenReturn(Optional.of("CASE123"));

        // --- mock dependencies ---
        when(systemDateProvider.dueDate(DAYS_AFTER_SUBMIT)).thenReturn("2025-09-10");
        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase))
                .thenReturn("0300 123 456");
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase))
                .thenReturn("internal@example.com");

        // --- mock static utils ---
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

            // --- run ---
            Map<String, Object> result = template.mapFieldValues(caseDetails);

            // --- verify ---
            assertThat(result)
                    .containsEntry("dateLetterSent", "2025-08-28")
                    .containsEntry("feeAmount", "£140")
                    .containsEntry("appellantName", "John Doe")
                    .containsEntry("onlineCaseRefNumber", Optional.of("CASE123"))
                    .containsEntry("daysAfterSubmissionDate", "2025-09-10")
                    .containsEntry("customerServicesTelephone", "0300 123 456")
                    .containsEntry("customerServicesEmail", "internal@example.com");
        }
    }
}
