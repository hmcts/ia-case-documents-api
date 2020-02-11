package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config.GovNotifyTemplateIdConfiguration;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class LegalRepresentativeSubmittedHearingRequirementsPersonalisationTest {

    @Mock AsylumCase asylumCase;

    @Mock EmailAddressFinder emailAddressFinder;
    @Mock GovNotifyTemplateIdConfiguration govNotifyTemplateIdConfiguration;
    @Mock PersonalisationProvider personalisationProvider;

    private Long caseId = 12345L;
    private String templateId = "someTemplateId";

    private String legalRepEmailAddress = "legalrep@something.com";

    private String appealReferenceNumber = "someReferenceNumber";
    private String legalRepReferenceNumber = "legalRepReferenceNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";

    private LegalRepresentativeSubmittedHearingRequirementsPersonalisation legalRepresentativeSubmittedHearingRequirementsPersonalisation;

    @Before
    public void setUp() {
        when(emailAddressFinder.getLegalRepEmailAddress(asylumCase)).thenReturn(legalRepEmailAddress);
        when(govNotifyTemplateIdConfiguration.getSubmittedHearingRequirementsLegalRepTemplateId()).thenReturn(templateId);

        legalRepresentativeSubmittedHearingRequirementsPersonalisation =
            new LegalRepresentativeSubmittedHearingRequirementsPersonalisation(
                personalisationProvider,
                govNotifyTemplateIdConfiguration,
                emailAddressFinder
            );
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, legalRepresentativeSubmittedHearingRequirementsPersonalisation.getTemplateId());
    }

    @Test
    public void should_return_given_email_address_from_asylum_case() {
        assertEquals(Collections.singleton(legalRepEmailAddress), legalRepresentativeSubmittedHearingRequirementsPersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_LEGAL_REP_OF_SUBMITTED_HEARING_REQUIREMENTS", legalRepresentativeSubmittedHearingRequirementsPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {
        Map<String, String> personalisation = legalRepresentativeSubmittedHearingRequirementsPersonalisation.getPersonalisation(asylumCase);
        Map<String, String> expectedPersonalisation = getPersonalisation();

        Assertions.assertThat(personalisation).isEqualToComparingOnlyGivenFields(expectedPersonalisation);
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> legalRepresentativeSubmittedHearingRequirementsPersonalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }


    private Map<String, String> getPersonalisation() {
        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", appealReferenceNumber)
            .put("legalRepReferenceNumber", legalRepReferenceNumber)
            .put("appellantGivenNames", appellantGivenNames)
            .put("appellantFamilyName", appellantFamilyName)
            .build();
    }
}
