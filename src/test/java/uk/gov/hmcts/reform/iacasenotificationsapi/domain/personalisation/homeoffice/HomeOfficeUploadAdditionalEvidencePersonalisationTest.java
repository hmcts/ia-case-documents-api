package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config.GovNotifyTemplateIdConfiguration;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class HomeOfficeUploadAdditionalEvidencePersonalisationTest {

    @Mock Callback<AsylumCase> callback;
    @Mock CaseDetails<AsylumCase> caseDetails;
    @Mock AsylumCase asylumCase;

    @Mock GovNotifyTemplateIdConfiguration govNotifyTemplateIdConfiguration;
    @Mock PersonalisationProvider personalisationProvider;

    private Long caseId = 12345L;
    private String templateId = "someTemplateId";

    private String homeOfficeEmailAddress = "homeOffice@example.com";

    private String hmctsReference = "hmctsReference";
    private String legalRepReference = "legalRepresentativeReference";
    private String homeOfficeReference = "homeOfficeReference";
    private String listingReference = "listingReference";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";

    private HomeOfficeUploadAdditionalEvidencePersonalisation homeOfficeUploadAdditionalEvidencePersonalisation;

    @Before
    public void setUp() {
        when(govNotifyTemplateIdConfiguration.getUploadedAdditionalEvidenceTemplateId()).thenReturn(templateId);

        homeOfficeUploadAdditionalEvidencePersonalisation = new HomeOfficeUploadAdditionalEvidencePersonalisation(govNotifyTemplateIdConfiguration, personalisationProvider, homeOfficeEmailAddress);
    }

    @Test
    public void should_return_given_email_address() {
        assertEquals(Collections.singleton(homeOfficeEmailAddress), homeOfficeUploadAdditionalEvidencePersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, homeOfficeUploadAdditionalEvidencePersonalisation.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_UPLOADED_ADDITIONAL_EVIDENCE_HOME_OFFICE", homeOfficeUploadAdditionalEvidencePersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {
        when(personalisationProvider.getPersonalisation(callback)).thenReturn(getPersonalisationForHomeOffice());

        Map<String, String> personalisation = homeOfficeUploadAdditionalEvidencePersonalisation.getPersonalisation(callback);

        assertThat(asylumCase).isEqualToComparingOnlyGivenFields(personalisation);
    }

    @Test
    public void should_throw_exception_when_callback_is_null() {

        assertThatThrownBy(() -> homeOfficeUploadAdditionalEvidencePersonalisation.getPersonalisation((Callback<AsylumCase>) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");
    }

    private Map<String, String> getPersonalisationForHomeOffice() {
        return ImmutableMap
            .<String, String>builder()
            .put("hmctsReference", hmctsReference)
            .put("legalRepReference", legalRepReference)
            .put("homeOfficeReference", homeOfficeReference)
            .put("listingReference", listingReference)
            .put("appellantGivenNames", appellantGivenNames)
            .put("appellantFamilyName", appellantFamilyName)
            .build();
    }


}
