package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.respondent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.powermock.api.mockito.PowerMockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RespondentForceCaseToCaseUnderReviewPersonalisationTest {

    String templateId = "a_template_id";
    Long caseReferenceId = 123456789098766L;
    String respondentEmail = "respondent@email.com";
    String appealReferenceNumber = "PA/12345/2022";
    String frontendUrl = "https://ia-aip-frontend.com";
    String appellantFamilyName = "Hunt";
    String appellantGivenNames = "Tomas";
    String homeOfficeReferenceNumber = "1234-56789";
    String customerServiceProvideTelephone = "0800 12345678";
    String customerServiceProvideEmail = "customer@service.com";
    Map<String, String> customerServiceMap = ImmutableMap.of(
            "customerServicesTelephone", customerServiceProvideTelephone,
            "customerServicesEmail", customerServiceProvideEmail);
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    AsylumCase asylumCase;

    RespondentForceCaseToCaseUnderReviewPersonalisation sut;

    @BeforeEach
    void setUp() {
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(EMAIL, String.class)).thenReturn(Optional.of(respondentEmail));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(customerServicesProvider.getCustomerServicesPersonalisation()).thenReturn(customerServiceMap);
        sut = new RespondentForceCaseToCaseUnderReviewPersonalisation(templateId, respondentEmail, frontendUrl, customerServicesProvider);
    }

    @Test
    void should_return_template_id() {
        assertEquals(templateId, sut.getTemplateId());
    }

    @Test
    void should_return_personalisation_email() {
        assertThat(sut.getRecipientsList(asylumCase)).contains(respondentEmail);
    }

    @Test
    void should_return_reference_id() {
        String expected = String.format(caseReferenceId + "_RESPONDENT_FORCE_CASE_PROGRESSION_TO_UNDER_REVIEW");
        assertEquals(expected, sut.getReferenceId(caseReferenceId));
    }

    @Test
    void should_return_personalization_details() {
        assertEquals(appealReferenceNumber, sut.getPersonalisation(asylumCase).get("appealReferenceNumber"));
        assertEquals(frontendUrl, sut.getPersonalisation(asylumCase).get("linkToOnlineService"));
        assertEquals(appellantFamilyName, sut.getPersonalisation(asylumCase).get("appellantFamilyName"));
        assertEquals(appellantGivenNames, sut.getPersonalisation(asylumCase).get("appellantGivenNames"));
        assertEquals(homeOfficeReferenceNumber, sut.getPersonalisation(asylumCase).get("homeOfficeReferenceNumber"));
        assertEquals(customerServiceProvideEmail, sut.getPersonalisation(asylumCase).get("customerServicesEmail"));
        assertEquals(customerServiceProvideTelephone, sut.getPersonalisation(asylumCase).get("customerServicesTelephone"));

    }

}
