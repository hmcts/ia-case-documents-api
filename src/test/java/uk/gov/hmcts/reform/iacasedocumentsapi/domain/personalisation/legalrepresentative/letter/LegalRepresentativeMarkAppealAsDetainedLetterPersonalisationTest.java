package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.legalrepresentative.letter;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.SystemDateProvider;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class LegalRepresentativeMarkAppealAsDetainedLetterPersonalisationTest {

    private static final String TEMPLATE_ID = "template-xyz";
    private static final long CASE_ID = 11111L;

    @Mock private CustomerServicesProvider customerServicesProvider;
    @Mock private SystemDateProvider systemDateProvider;
    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    private LegalRepresentativeMarkAppealAsDetainedLetterPersonalisation personalisation;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        personalisation = new LegalRepresentativeMarkAppealAsDetainedLetterPersonalisation(
            TEMPLATE_ID,
            customerServicesProvider,
            systemDateProvider
        );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(customerServicesProvider.getCustomerServicesPersonalisation())
            .thenReturn(ImmutableMap.of("customerServices", "value"));
    }

    @Test
    void should_return_template_id() {
        assertThat(personalisation.getTemplateId()).isEqualTo(TEMPLATE_ID);
    }

    @Test
    void should_return_reference_id() {
        assertThat(personalisation.getReferenceId(CASE_ID))
            .isEqualTo(CASE_ID + "_MARK_APPEAL_AS_DETAINED_LEGAL_REP_LETTER");
    }

    @Test
    void should_throw_if_callback_is_null() {
        Callback<AsylumCase> callback = null;
        assertThrows(NullPointerException.class, () -> personalisation.getPersonalisation(callback));
    }

    @Test
    void should_return_personalisation_with_case_fields() {
        when(asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("appealRef123"));
        when(asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("homeRef456"));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of("Grace"));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of("Hopper"));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(AsylumCaseDefinition.LEGAL_REP_HAS_ADDRESS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseDefinition.LEGAL_REP_ADDRESS_U_K, AddressUk.class))
            .thenReturn(
                Optional.of(new AddressUk("10", "Main St", "", "Birmingham", "", "BM2 3DR", "UK"))
            );

        Map<String, String> result = personalisation.getPersonalisation(callback);

        assertThat(result)
            .containsEntry("appealReferenceNumber", "appealRef123")
            .containsEntry("homeOfficeReferenceNumber", "homeRef456")
            .containsEntry("appellantGivenNames", "Grace")
            .containsEntry("appellantFamilyName", "Hopper")
            .containsEntry("customerServices", "value")
            .containsEntry("dateLetterSent", LocalDate.now().format(DateTimeFormatter.ofPattern("d MMM uuuu")));
    }
}
