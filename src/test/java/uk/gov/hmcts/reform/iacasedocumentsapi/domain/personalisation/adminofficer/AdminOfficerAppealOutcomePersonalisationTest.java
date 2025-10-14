package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.adminofficer;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_DECISION_ALLOWED;

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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.adminofficer.AdminOfficerAppealOutcomePersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.adminofficer.AdminOfficerPersonalisationProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminOfficerAppealOutcomePersonalisationTest {

    @Mock
    AsylumCase asylumCase;

    @Mock
    private EmailAddressFinder emailAddressFinder;


    AdminOfficerPersonalisationProvider adminOfficerPersonalisationProvider;

    AdminOfficerAppealOutcomePersonalisation adminOfficerAppealOutcomePersonalisation;


    private final String decisionAndReasonUploadedTemplateId = "someTemplateId";

    private final String appealReferenceNumber = "someReferenceNumber";
    private final String ariaListingReference = "someAriaListingReference";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";


    @BeforeEach
    public void setup() {
        final String iaExUiFrontendUrl = "hhh";

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaListingReference));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));

        adminOfficerPersonalisationProvider = new AdminOfficerPersonalisationProvider(iaExUiFrontendUrl);

        adminOfficerAppealOutcomePersonalisation = new AdminOfficerAppealOutcomePersonalisation(
                decisionAndReasonUploadedTemplateId,
                adminOfficerPersonalisationProvider,
                emailAddressFinder
        );
    }


    @Test
    void should_return_given_template_id() {
        assertEquals(decisionAndReasonUploadedTemplateId, adminOfficerAppealOutcomePersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_APPEAL_OUTCOME_ADMIN",
                adminOfficerAppealOutcomePersonalisation.getReferenceId(caseId));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> adminOfficerAppealOutcomePersonalisation.getPersonalisation((AsylumCase) null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("asylumCase must not be null");
    }

    @ParameterizedTest
    @CsvSource({
        // we don't test all possible combinations here. A few will be sufficient to verify that things are not hard-coded
        "GLASGOW,ALLOWED",
        "GLASGOW,DISMISSED",
        "BELFAST,ALLOWED",
        "BELFAST,DISMISSED",
        "COVENTRY,ALLOWED"
    })
    void should_return_personalisation_when_all_information_given(String hearingCentre, String applicationDecision) {
        // Given
        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.valueOf(hearingCentre)));
        when(asylumCase.read(IS_DECISION_ALLOWED, AppealDecision.class)).thenReturn(Optional.of(AppealDecision.valueOf(applicationDecision)));

        // When
        Map<String, String> personalisation = adminOfficerPersonalisationProvider.getAdminPersonalisation(asylumCase);

        // Then
        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(ariaListingReference, personalisation.get("ariaListingReference"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(hearingCentre, personalisation.get("hearingCentre"));
        assertEquals(applicationDecision, personalisation.get("applicationDecision"));
    }
}


