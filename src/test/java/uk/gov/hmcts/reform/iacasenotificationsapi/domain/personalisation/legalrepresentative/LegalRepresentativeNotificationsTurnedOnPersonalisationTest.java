package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LegalRepresentativeNotificationsTurnedOnPersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    Callback<AsylumCase> callback;
    @Mock
    PersonalisationProvider personalisationProvider;
    private final String beforeListingTemplateId = "beforeListingTemplateId";
    private final String afterListingTemplateId = "afterListingTemplateId";
    private final Long caseId = 12345L;
    private final String iaExUiFrontendUrl = "http://localhost";
    private final String appealReferenceNumber = "appealReferenceNumber";
    private final String ariaListingReference = "someAriaListingReference";
    private final String legalRepReferenceEjp = "someLegalRepReferenceNumber";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private final String dateOfBirth = "1 Mar 2020";
    private final String legalRepEmailEjp = "legalRep@example.com";
    private final String ccdReferenceNumberForDisplay = "someRefNumber";

    private LegalRepresentativeNotificationsTurnedOnPersonalisation legalRepresentativeNotificationsTurnedOnPersonalisation;

    @BeforeEach
    public void setup() {
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaListingReference));
        when(asylumCase.read(LEGAL_REP_REFERENCE_EJP, String.class)).thenReturn(Optional.of(legalRepReferenceEjp));
        when(asylumCase.read(LEGAL_REP_EMAIL_EJP, String.class)).thenReturn(Optional.of(legalRepEmailEjp));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.of(dateOfBirth));
        when(asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY, String.class)).thenReturn(Optional.of(ccdReferenceNumberForDisplay));

        legalRepresentativeNotificationsTurnedOnPersonalisation = new LegalRepresentativeNotificationsTurnedOnPersonalisation(
            beforeListingTemplateId,
            afterListingTemplateId,
            personalisationProvider,
            iaExUiFrontendUrl
        );
    }

    @Test
    public void should_return_given_template_id() {
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
            .thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));
        assertEquals(afterListingTemplateId,
            legalRepresentativeNotificationsTurnedOnPersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
            .thenReturn(Optional.empty());
        assertEquals(beforeListingTemplateId,
            legalRepresentativeNotificationsTurnedOnPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {

        assertThat(legalRepresentativeNotificationsTurnedOnPersonalisation.getReferenceId(caseId))
            .isEqualTo(caseId + "_NOTIFICATIONS_TURNED_ON");
    }

    @Test
    public void should_return_given_email_address() {
        assertTrue(
            legalRepresentativeNotificationsTurnedOnPersonalisation.getRecipientsList(asylumCase).contains(legalRepEmailEjp));
    }

    @Test
    public void should_throw_exception_when_email_address_is_null() {

        when(asylumCase.read(LEGAL_REP_EMAIL_EJP, String.class)).thenReturn(Optional.empty());
        assertThatThrownBy(
            () -> legalRepresentativeNotificationsTurnedOnPersonalisation.getRecipientsList(asylumCase))
            .hasMessage("legalRepresentativeEmailAddress is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }



    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
            () -> legalRepresentativeNotificationsTurnedOnPersonalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation =
            legalRepresentativeNotificationsTurnedOnPersonalisation.getPersonalisation(asylumCase);

        when(personalisationProvider.getPersonalisation(callback)).thenReturn(getPersonalisationMapWithGivenValues());
        assertEquals(legalRepReferenceEjp, personalisation.get("legalRepReferenceNumberEjp"));
        assertEquals(dateOfBirth, personalisation.get("dateOfBirth"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));
        assertEquals(ccdReferenceNumberForDisplay, personalisation.get("ccdReferenceNumberForDisplay"));
    }

    private Map<String, String> getPersonalisationMapWithGivenValues() {
        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", appealReferenceNumber)
            .put("ariaListingReference", ariaListingReference)
            .put("appellantGivenNames", appellantGivenNames)
            .put("appellantFamilyName", appellantFamilyName)
            .build();
    }
}
