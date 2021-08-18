package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@ExtendWith(MockitoExtension.class)
public class AdminOfficerUpperTribunalBundleFailedPersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    PersonalisationProvider personalisationProvider;

    private Long caseId = 12345L;
    private String adminOfficeEmailAddress = "some-email@example.com";
    private String appealReferenceNumber = "someReferenceNumber";
    private String ariaListingReference = "ariaListingReference";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";
    private String bundleFailedTemplateId = "bundleFailedTemplateId";
    private AdminOfficerUpperTribunalBundleFailedPersonalisation adminOfficerUpperTribunalBundleFailedPersonalisation;

    @BeforeEach
    public void setup() {

        adminOfficerUpperTribunalBundleFailedPersonalisation = new AdminOfficerUpperTribunalBundleFailedPersonalisation(
            bundleFailedTemplateId,
            adminOfficeEmailAddress,
            personalisationProvider
        );
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(bundleFailedTemplateId, adminOfficerUpperTribunalBundleFailedPersonalisation.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_UPPER_TRIBUNAL_BUNDLE_FAILED_ADMIN_OFFICER",
            adminOfficerUpperTribunalBundleFailedPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_personalisation_of_all_information_given() {
        when(personalisationProvider.getTribunalHeaderPersonalisation(asylumCase))
            .thenReturn(getPersonalisationMapWithGivenValues());

        Map<String, String> personalisation =
            adminOfficerUpperTribunalBundleFailedPersonalisation.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(ariaListingReference, personalisation.get("ariaListingReference"));
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
