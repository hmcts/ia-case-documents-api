package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InternalDetainedAppealHearingAdjournedNoDateAppellantTemplateTest {

    private static final String TEMPLATE_NAME = "test-template";
    private static final String CASE_REF = "123456";
    private static final String CUSTOMER_PHONE = "0300 123 456";
    private static final String CUSTOMER_EMAIL = "contact@test.com";

    @Mock
    private AsylumCase asylumCase;

    @Mock
    private CaseDetails<AsylumCase> caseDetails;

    @Mock
    private AsylumCase asylumCaseBefore;

    @Mock
    private CaseDetails<AsylumCase> caseDetailsBefore;

    @Mock
    private HearingCentre hearingCentre;

    @Mock
    private CustomerServicesProvider customerServicesProvider;

    private InternalDetainedAppealHearingAdjournedNoDateAppellantTemplate template;

    @BeforeEach
    void setUp() {
        template = new InternalDetainedAppealHearingAdjournedNoDateAppellantTemplate(
                TEMPLATE_NAME,
                customerServicesProvider
        );

        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetailsBefore.getCaseData()).thenReturn(asylumCaseBefore);
    }

    @Test
    void should_return_template_name() {
        assertThat(template.getName()).isEqualTo(TEMPLATE_NAME);
    }

    @Test
    void should_map_field_values_with_direction_due_date() {
        when(asylumCaseBefore.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(hearingCentre));
        when(hearingCentre.getValue()).thenReturn("Hatton Cross");
        when(asylumCase.read(DATE_BEFORE_ADJOURN_WITHOUT_DATE, String.class)).thenReturn(Optional.of("2025-10-20"));
        when(asylumCase.read(ADJOURN_HEARING_WITHOUT_DATE_REASONS, String.class)).thenReturn(Optional.of("Some reason"));
        when(asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY)).thenReturn(Optional.of(CASE_REF));
        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase)).thenReturn(CUSTOMER_PHONE);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase)).thenReturn(CUSTOMER_EMAIL);

        Map<String, Object> fieldValues = template.mapFieldValues(caseDetails, caseDetailsBefore);

        assertThat(fieldValues.get("onlineCaseRefNumber")).isEqualTo(CASE_REF);
        assertThat(fieldValues.get("customerServicesTelephone")).isEqualTo(CUSTOMER_PHONE);
        assertThat(fieldValues.get("customerServicesEmail")).isEqualTo(CUSTOMER_EMAIL);
        assertThat(fieldValues.get("dateLetterSent")).isNotNull();
        assertThat(fieldValues.get("oldHearingCentre")).isEqualTo("Hatton Cross");
        assertThat(fieldValues.get("oldHearingDate")).isEqualTo("2025-10-20");
        assertThat(fieldValues.get("reasonForAdjournedHearing")).isEqualTo("Some reason");
    }

    @Test
    void should_map_field_values_with_empty_direction_due_date_if_not_present() {
        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());
        when(asylumCase.read(DATE_BEFORE_ADJOURN_WITHOUT_DATE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(ADJOURN_HEARING_WITHOUT_DATE_REASONS, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY)).thenReturn(Optional.of(CASE_REF));
        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase)).thenReturn(CUSTOMER_PHONE);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase)).thenReturn(CUSTOMER_EMAIL);

        Map<String, Object> fieldValues = template.mapFieldValues(caseDetails, caseDetailsBefore);

        assertThat(fieldValues.get("oldHearingCentre")).isEqualTo("");
        assertThat(fieldValues.get("oldHearingDate")).isEqualTo("");
        assertThat(fieldValues.get("reasonForAdjournedHearing")).isEqualTo("");
        assertThat(fieldValues.get("onlineCaseRefNumber")).isEqualTo(CASE_REF);
    }
}
