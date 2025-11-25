package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.respondent;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.HomeOfficeEmailFinder;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.PersonalisationProvider;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;


import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class RespondentTurnOnNotificationsPersonalisationTest {
    @Mock
    AsylumCase asylumCase;
    @Mock
    CaseDetails<AsylumCase> caseDetails;
    @Mock
    Callback<AsylumCase> callback;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    PersonalisationProvider personalisationProvider;
    @Mock
    HomeOfficeEmailFinder hoEmailAddressFinder;
    private final String beforeListingTemplateId = "beforeListingTemplateId";
    private final String afterListingTemplateId = "afterListingTemplateId";
    private final Long caseId = 12345L;
    private String iaExUiFrontendUrl = "http://localhost";
    private final String appealReferenceNumber = "appealReferenceNumber";
    private final String respondentReferenceNumber = "respondentReferenceNumber";
    private final String upperTribunalReferenceNumber = "upperTribunalReferenceNumber";
    private final String ariaListingReference = "someAriaListingReference";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "customer.services@example.com";
    private String homeOfficeEmailAddress = "homeOffice@example.com";
    private String cuurentStateHO = "appealSubmitted";
    private RespondentTurnOnNotificationsPersonalisation respondentTurnOnNotificationsPersonalisation;

    @BeforeEach
    public void setup() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaListingReference));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(respondentReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(UPPER_TRIBUNAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(upperTribunalReferenceNumber));
        when(asylumCase.read(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, String.class)).thenReturn(Optional.of(cuurentStateHO));
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);

        respondentTurnOnNotificationsPersonalisation = new RespondentTurnOnNotificationsPersonalisation(
                beforeListingTemplateId,
                afterListingTemplateId,
                iaExUiFrontendUrl,
                personalisationProvider,
                customerServicesProvider,
                hoEmailAddressFinder
        );
    }

    @Test
    public void should_return_given_template_id() {
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
                .thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));
        assertEquals(afterListingTemplateId,
                respondentTurnOnNotificationsPersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
                .thenReturn(Optional.empty());
        assertEquals(beforeListingTemplateId,
                respondentTurnOnNotificationsPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {

        assertThat(respondentTurnOnNotificationsPersonalisation.getReferenceId(caseId))
                .isEqualTo(caseId + "_TURN_ON_NOTIFICATIONS_RESPONDENT");
    }

    @Test
    public void should_return_given_email_address() {
        when(respondentTurnOnNotificationsPersonalisation.getRecipientsList(asylumCase)).thenReturn(Collections.singleton(homeOfficeEmailAddress));
        assertTrue(
                respondentTurnOnNotificationsPersonalisation.getRecipientsList(asylumCase).contains(homeOfficeEmailAddress));
    }


    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
                () -> respondentTurnOnNotificationsPersonalisation.getPersonalisation((Callback<AsylumCase>) null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("callback must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        when(respondentTurnOnNotificationsPersonalisation.getPersonalisation(callback)).thenReturn(getPersonalisationMapWithGivenValues());

        Map<String, String> personalisation =
                respondentTurnOnNotificationsPersonalisation.getPersonalisation(callback);

        assertThat(asylumCase).isEqualToComparingOnlyGivenFields(personalisation);
    }

    private Map<String, String> getPersonalisationMapWithGivenValues() {
        return ImmutableMap
                .<String, String>builder()
                .put("appealReferenceNumber", appealReferenceNumber)
                .put("ariaListingReference", ariaListingReference)
                .put("appellantGivenNames", appellantGivenNames)
                .put("appellantFamilyName", appellantFamilyName)
                .put("respondentReferenceNumber", respondentReferenceNumber)
                .build();
    }

}
