package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DirectionFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unchecked")
class LegalRepresentativeRequestNewHearingRequirementsPersonalisationTest {

    @Mock AsylumCase asylumCase;
    @Mock DirectionFinder directionFinder;
    @Mock Direction direction;
    @Mock EmailAddressFinder emailAddressFinder;
    @Mock CustomerServicesProvider customerServicesProvider;

    private Long caseId = 12345L;
    private String templateId = "someTemplateId";
    private String directionDueDate = "2019-08-27";
    private String directionExplanation = "someExplanation";
    private String legalRepEmailAddress = "legalrep@example.com";
    private String appealReferenceNumber = "someReferenceNumber";
    private String legalRepRefNumber = "somelegalRepRefNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";
    private String iaExUiFrontendUrl = "http://localhost";
    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "customer.services@example.com";

    private LegalRepresentativeRequestNewHearingRequirementsPersonalisation legalRepresentativeRequestNewHearingRequirementsPersonalisation;

    @BeforeEach
    void setup() {

        when((direction.getDateDue())).thenReturn(directionDueDate);
        when((direction.getExplanation())).thenReturn(directionExplanation);
        when(directionFinder.findFirst(asylumCase, DirectionTag.REQUEST_NEW_HEARING_REQUIREMENTS)).thenReturn(Optional.of(direction));

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepRefNumber));
        when(emailAddressFinder.getLegalRepEmailAddress(asylumCase)).thenReturn(legalRepEmailAddress);
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);

        legalRepresentativeRequestNewHearingRequirementsPersonalisation = new LegalRepresentativeRequestNewHearingRequirementsPersonalisation(
            templateId,
            iaExUiFrontendUrl,
            emailAddressFinder,
            directionFinder,
            customerServicesProvider
        );
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(templateId, legalRepresentativeRequestNewHearingRequirementsPersonalisation.getTemplateId());
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_LEGAL_REPRESENTATIVE_REQUEST_NEW_HEARING_REQUIREMENTS_DIRECTION", legalRepresentativeRequestNewHearingRequirementsPersonalisation.getReferenceId(caseId));
    }

    @Test
    void should_return_given_email_address_from_asylum_case() {
        assertEquals(Collections.singleton(legalRepEmailAddress), legalRepresentativeRequestNewHearingRequirementsPersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> legalRepresentativeRequestNewHearingRequirementsPersonalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation = legalRepresentativeRequestNewHearingRequirementsPersonalisation.getPersonalisation(asylumCase);

        Assertions.assertThat(asylumCase).isEqualToComparingOnlyGivenFields(personalisation);
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }

    @Test
    void should_throw_exception_on_personalisation_when_direction_is_empty() {

        when(directionFinder.findFirst(asylumCase, DirectionTag.REQUEST_NEW_HEARING_REQUIREMENTS)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> legalRepresentativeRequestNewHearingRequirementsPersonalisation.getPersonalisation(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("case officer request hearing requirements direction is not present");
    }
}
