package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.letter;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppellantRemoveRepresentationDetainedOtherPersonalisationTest {

    private static final String TEMPLATE_ID = "template123";
    private static final Long CASE_ID = 12345L;

    @Mock
    private CustomerServicesProvider customerServicesProvider;

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private CaseDetails<AsylumCase> caseDetailsBefore;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private AsylumCase asylumCaseBefore;

    private AppellantRemoveRepresentationDetainedOtherPersonalisation personalisation;

    @BeforeEach
    void setUp() {
        personalisation = new AppellantRemoveRepresentationDetainedOtherPersonalisation(
            TEMPLATE_ID,
            customerServicesProvider
        );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(caseDetailsBefore.getCaseData()).thenReturn(asylumCaseBefore);

        // Default stubs
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_DATE_OF_BIRTH, String.class))
            .thenReturn(Optional.of(LocalDate.of(1990, 5, 15).toString()));

        when(customerServicesProvider.getCustomerServicesPersonalisation())
            .thenReturn(ImmutableMap.of("customerServices", "value"));
    }

    @Test
    void should_return_template_id() {
        assertThat(personalisation.getTemplateId()).isEqualTo(TEMPLATE_ID);
    }

    @Test
    void should_return_reference_id() {
        String reference = personalisation.getReferenceId(CASE_ID);
        assertThat(reference).isEqualTo(CASE_ID + "_REMOVE_REPRESENTATION_DETAINED_OTHER_APPELLANT_LETTER");
    }

    @Test
    void should_throw_if_callback_is_null() {
        Callback<AsylumCase> callback = null;
        assertThrows(NullPointerException.class, () -> personalisation.getPersonalisation(callback));
    }

    @Test
    void should_return_personalisation_with_all_fields() {
        when(asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("appealRef123"));
        when(asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("homeOffice123"));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of("John"));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of("Doe"));
        when(asylumCaseBefore.read(AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("legalRep456"));

        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_ADDRESS, AddressUk.class))
            .thenReturn(Optional.of(
                new AddressUk("10", "Main St", "", "Sometown", "", "CM3 4DC", "UK"))
            );

        Map<String, String> result = personalisation.getPersonalisation(callback);

        assertThat(result)
            .containsEntry("appealReferenceNumber", "appealRef123")
            .containsEntry("homeOfficeReferenceNumber", "homeOffice123")
            .containsEntry("appellantGivenNames", "John")
            .containsEntry("appellantFamilyName", "Doe")
            .containsEntry("dateofBirth", "15 May 1990")
            .containsEntry("legalRepRef", "legalRep456")
            .containsEntry("customerServices", "value")
            .containsEntry("address_line_1", "10")
            .containsEntry("address_line_2", "Main St")
            .containsEntry("address_line_3", "")
            .containsEntry("address_line_4", "Sometown")
            .containsEntry("address_line_5", "CM3 4DC");
    }

    @Test
    void should_throw_if_date_of_birth_missing() {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_DATE_OF_BIRTH, String.class))
            .thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> personalisation.getPersonalisation(callback));
    }
}
