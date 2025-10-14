package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.legalrepresentative;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.legalrepresentative.LegalRepresentativeUpdateTribunalDecisionRule32PersonalisationEmail;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre.BIRMINGHAM;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unchecked")
class LegalRepresentativeUpdateTribunalDecisionRule32PersonalisationEmailTest {
    @Mock
    Callback<AsylumCase> callback;
    @Mock
    AsylumCase asylumCase;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    CustomerServicesProvider customerServicesProvider;

    private Long caseId = 12345L;
    private final String legalRepresentativeUpdateTribunalDecisionRule32EmailTemplateId = "legalRepresentativeUpdateTribunalDecisionRule32EmailTemplateId";
    private String exUiFrontendUrl = "http://localhost";
    private String mockedAppealReferenceNumber = "someReferenceNumber";
    private String appealReferenceNumber = "someReferenceNumber";
    private String legalRepReferenceNumber = "someLRReferenceNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";
    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "cust.services@example.com";
    private LegalRepresentativeUpdateTribunalDecisionRule32PersonalisationEmail legalRepresentativeUpdateTribunalDecisionRule32PersonalisationEmail;

    @BeforeEach
    public void setup() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getCaseDetails().getId()).thenReturn(caseId);

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(BIRMINGHAM));
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);

        legalRepresentativeUpdateTribunalDecisionRule32PersonalisationEmail = new LegalRepresentativeUpdateTribunalDecisionRule32PersonalisationEmail(
            legalRepresentativeUpdateTribunalDecisionRule32EmailTemplateId,
            exUiFrontendUrl,
            customerServicesProvider
        );
    }

    @Test
    void should_return_given_template_id() {

        assertEquals(legalRepresentativeUpdateTribunalDecisionRule32EmailTemplateId, legalRepresentativeUpdateTribunalDecisionRule32PersonalisationEmail.getTemplateId(asylumCase));
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_LEGAL_REPRESENTATIVE_UPDATE_TRIBUNAL_DECISION_RULE_32_EMAIL",
                legalRepresentativeUpdateTribunalDecisionRule32PersonalisationEmail.getReferenceId(caseId));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> legalRepresentativeUpdateTribunalDecisionRule32PersonalisationEmail.getPersonalisation((Callback<AsylumCase>) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");
    }

    @Test
    void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation = legalRepresentativeUpdateTribunalDecisionRule32PersonalisationEmail.getPersonalisation(callback);

        assertEquals(mockedAppealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(legalRepReferenceNumber, personalisation.get("legalRepReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(exUiFrontendUrl, personalisation.get("linkToService"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());

    }

}
