package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.StringProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.CMR_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.CMR_HEARING_DATE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.*;

class InternalCmrListingLrLetterTemplateTest {

    private final CustomerServicesProvider customerServicesProvider =
            mock(CustomerServicesProvider.class);

    private final StringProvider stringProvider =
            mock(StringProvider.class);

    private final CaseDetails<AsylumCase> caseDetails =
            mock(CaseDetails.class);

    private final AsylumCase asylumCase =
            mock(AsylumCase.class);

    private InternalCmrListingLrLetterTemplate template;

    @BeforeEach
    void setUp() {

        template = new InternalCmrListingLrLetterTemplate(
                "template-name",
                customerServicesProvider,
                stringProvider
        );

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(CMR_HEARING_CENTRE, HearingCentre.class))
                .thenReturn(Optional.of(HearingCentre.NEWPORT));

        when(asylumCase.read(CMR_HEARING_DATE, String.class))
                .thenReturn(Optional.of("2026-08-01T14:30:00"));

        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase))
                .thenReturn("0800 111");

        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase))
                .thenReturn("internal@test.gov.uk");

        when(stringProvider.get("hearingCentreAddress", HearingCentre.NEWPORT.toString()))
                .thenReturn(Optional.of("Line 1, Line 2, Line 3"));
    }

    @Test
    void should_return_template_name() {

        assertEquals("template-name", template.getName());
    }

    @Test
    void should_throw_if_hearing_centre_missing() {

        when(asylumCase.read(CMR_HEARING_CENTRE, HearingCentre.class))
                .thenReturn(Optional.empty());

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> template.mapFieldValues(caseDetails)
        );

        assertEquals(
                "listCaseHearingCentre is not present",
                exception.getMessage()
        );
    }

    @Test
    void should_map_field_values_for_in_country_case() {

        try (MockedStatic<uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils> mocked =
                     mockStatic(
                             uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.class,
                             CALLS_REAL_METHODS
                     )) {

            mocked.when(() -> getLegalRepPersonalisation(asylumCase))
                    .thenReturn(Map.of("legalRepReference", "ABC123"));

            mocked.when(() -> getCmrHearingChannel(asylumCase, "Unknown"))
                    .thenReturn("Video");

            mocked.when(() -> legalRepInCountryAppeal(asylumCase))
                    .thenReturn(true);

            mocked.when(() -> getLegalRepresentativeAddressAsList(asylumCase))
                    .thenReturn(List.of("Addr1", "Addr2", "Addr3"));

            Map<String, Object> fields = template.mapFieldValues(caseDetails);

            assertEquals("ABC123", fields.get("legalRepReference"));
            assertEquals("0800 111", fields.get("customerServicesTelephone"));
            assertEquals("internal@test.gov.uk", fields.get("customerServicesEmail"));
            assertEquals("Line 1\nLine 2\nLine 3", fields.get("hearingLocation"));
            assertEquals("1 August 2026", fields.get("hearingDate"));
            assertEquals("1430", fields.get("hearingTime"));
            assertEquals(
                    LocalDate.now().format(DateTimeFormatter.ofPattern("d MMMM yyyy")),
                    fields.get("dateLetterSent")
            );
            assertEquals("Video", fields.get("hearingChannel"));
            assertEquals("Addr1", fields.get("address_line_1"));
            assertEquals("Addr2", fields.get("address_line_2"));
            assertEquals("Addr3", fields.get("address_line_3"));
        }
    }

    @Test
    void should_map_field_values_for_out_of_country_case() {

        try (MockedStatic<uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils> mocked =
                     mockStatic(
                             uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.class,
                             CALLS_REAL_METHODS
                     )) {

            mocked.when(() -> getLegalRepPersonalisation(asylumCase))
                    .thenReturn(Map.of());

            mocked.when(() -> getCmrHearingChannel(asylumCase, "Unknown"))
                    .thenReturn("Face to face");

            mocked.when(() -> legalRepInCountryAppeal(asylumCase))
                    .thenReturn(false);

            mocked.when(() -> getLegalRepresentativeAddressOocAsList(asylumCase))
                    .thenReturn(List.of("OOC1", "OOC2"));

            Map<String, Object> fields = template.mapFieldValues(caseDetails);

            assertEquals("OOC1", fields.get("address_line_1"));
            assertEquals("OOC2", fields.get("address_line_2"));
            assertEquals("Face to face", fields.get("hearingChannel"));
        }
    }
}
