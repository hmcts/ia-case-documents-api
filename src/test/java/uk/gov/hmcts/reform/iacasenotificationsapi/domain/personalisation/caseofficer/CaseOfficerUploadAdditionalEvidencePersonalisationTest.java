package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer;

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
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config.GovNotifyTemplateIdConfiguration;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class CaseOfficerUploadAdditionalEvidencePersonalisationTest {

    @Mock Callback<AsylumCase> callback;
    @Mock CaseDetails<AsylumCase> caseDetails;
    @Mock AsylumCase asylumCase;

    @Mock EmailAddressFinder emailAddressFinder;
    @Mock GovNotifyTemplateIdConfiguration govNotifyTemplateIdConfiguration;
    @Mock PersonalisationProvider personalisationProvider;

    private Long caseId = 12345L;
    private String templateId = "someTemplateId";

    private String hearingCentreEmailAddress = "hearingCentre@example.com";

    private String hmctsReference = "hmctsReference";
    private String legalRepReference = "legalRepresentativeReference";
    private String homeOfficeReference = "homeOfficeReference";
    private String listingReference = "listingReference";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";

    private CaseOfficerUploadAdditionalEvidencePersonalisation caseOfficerUploadAdditionalEvidencePersonalisation;

    @Before
    public void setUp() {
        when(emailAddressFinder.getEmailAddress(asylumCase)).thenReturn(hearingCentreEmailAddress);
        when(govNotifyTemplateIdConfiguration.getUploadedAdditionalEvidenceTemplateId()).thenReturn(templateId);

        caseOfficerUploadAdditionalEvidencePersonalisation = new CaseOfficerUploadAdditionalEvidencePersonalisation(govNotifyTemplateIdConfiguration, personalisationProvider, emailAddressFinder);
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, caseOfficerUploadAdditionalEvidencePersonalisation.getTemplateId());
    }

    @Test
    public void should_return_given_email_address_from_asylum_case() {
        assertEquals(Collections.singleton(hearingCentreEmailAddress), caseOfficerUploadAdditionalEvidencePersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_UPLOADED_ADDITIONAL_EVIDENCE_CASE_OFFICER", caseOfficerUploadAdditionalEvidencePersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {
        when(personalisationProvider.getUploadAdditionalEvidencePersonalisation(asylumCase)).thenReturn(getPersonalisationForCaseOfficer());
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        Map<String, String> personalisation = caseOfficerUploadAdditionalEvidencePersonalisation.getPersonalisation(callback);

        assertThat(asylumCase).isEqualToComparingOnlyGivenFields(personalisation);
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> caseOfficerUploadAdditionalEvidencePersonalisation.getPersonalisation((Callback<AsylumCase>) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");
    }

    private Map<String, String> getPersonalisationForCaseOfficer() {
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
