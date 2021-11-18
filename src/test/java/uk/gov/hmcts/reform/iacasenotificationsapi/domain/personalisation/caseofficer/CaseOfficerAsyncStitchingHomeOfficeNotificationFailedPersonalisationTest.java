package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CaseOfficerAsyncStitchingHomeOfficeNotificationFailedPersonalisationTest {
    @Mock
    AsylumCase asylumCase;
    @Mock
    PersonalisationProvider personalisationProvider;
    @Mock
    EmailAddressFinder emailAddressFinder;

    private String caseOfficerEmailAddress = "caseOfficer@example.com";
    private Long caseId = 12345L;
    private String appealReferenceNumber = "someReferenceNumber";
    private String ariaListingReference = "ariaListingReference";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";

    private String iaExUiFrontendUrl = "frontend url";
    private String asyncStitchingHomeOfficeNotificationFailedTemplateId = "asyncStitchingHomeOfficeNotificationFailedTemplateId";

    private CaseOfficerAsyncStitchingHomeOfficeNotificationFailedPersonalisation homeOfficeNotificationFailedPersonalisation;

    @BeforeEach
    void setup() {

        homeOfficeNotificationFailedPersonalisation = new CaseOfficerAsyncStitchingHomeOfficeNotificationFailedPersonalisation(
            asyncStitchingHomeOfficeNotificationFailedTemplateId,
            personalisationProvider,
            emailAddressFinder);
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(asyncStitchingHomeOfficeNotificationFailedTemplateId, homeOfficeNotificationFailedPersonalisation.getTemplateId());
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_STITCHING_BUNDLE_HO_NOTIFICATION_FAILED_CASE_OFFICER",
            homeOfficeNotificationFailedPersonalisation.getReferenceId(caseId));
    }

    @Test
    void should_return_given_email_address_from_lookup_map() {
        when(emailAddressFinder.getListCaseHearingCentreEmailAddress(asylumCase)).thenReturn(caseOfficerEmailAddress);
        assertTrue(
                homeOfficeNotificationFailedPersonalisation.getRecipientsList(asylumCase).contains(caseOfficerEmailAddress));
    }

    @Test
    void should_return_personalisation_of_all_information_given() {
        when(personalisationProvider.getTribunalHeaderPersonalisation(asylumCase))
            .thenReturn(getPersonalisationMapWithGivenValues());
        Map<String, String> personalisation = homeOfficeNotificationFailedPersonalisation.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(ariaListingReference, personalisation.get("ariaListingReference"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));
    }

    private Map<String, String> getPersonalisationMapWithGivenValues() {
        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", appealReferenceNumber)
            .put("appellantGivenNames", appellantGivenNames)
            .put("appellantFamilyName", appellantFamilyName)
            .put("ariaListingReference", ariaListingReference)
            .put("linkToOnlineService", iaExUiFrontendUrl)
            .build();
    }
}
