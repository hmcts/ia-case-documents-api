package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
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
public class LegalRepresentativeNonStandardDirectionPersonalisationTest {

    @Mock AsylumCase asylumCase;
    @Mock EmailAddressFinder emailAddressFinder;
    @Mock PersonalisationProvider personalisationProvider;
    @Mock GovNotifyTemplateIdConfiguration govNotifyTemplateIdConfiguration;

    private Long caseId = 12345L;
    private String templateId = "someTemplateId";

    private String legalRepEmailAddress = "legalrep@example.com";
    private String appealReferenceNumber = "someReferenceNumber";
    private String legalRepReferenceNumber = "someLegalRepReferenceNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyNames = "someAppellantFamilyNames";
    private String iaCcdFrontendUrl = "http://localhost";
    private String directionExplanation = "someExplanation";
    private String directionDueDate = "2019-10-29";

    private LegalRepresentativeNonStandardDirectionPersonalisation legalRepresentativeNonStandardDirectionPersonalisation;

    @Before
    public void setUp() {
        when(govNotifyTemplateIdConfiguration.getLegalRepresentativeNonStandardDirectionTemplateId()).thenReturn(templateId);
        when(emailAddressFinder.getLegalRepEmailAddress(asylumCase)).thenReturn(legalRepEmailAddress);

        legalRepresentativeNonStandardDirectionPersonalisation = new LegalRepresentativeNonStandardDirectionPersonalisation(
            govNotifyTemplateIdConfiguration,
            personalisationProvider,
            emailAddressFinder
        );
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, legalRepresentativeNonStandardDirectionPersonalisation.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_LEGAL_REP_NON_STANDARD_DIRECTION", legalRepresentativeNonStandardDirectionPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_from_asylum_case() {
        assertTrue(legalRepresentativeNonStandardDirectionPersonalisation.getRecipientsList(asylumCase).contains(legalRepEmailAddress));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> legalRepresentativeNonStandardDirectionPersonalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {
        when(personalisationProvider.getNonStandardDirectionPersonalisation(asylumCase)).thenReturn(getPersonalisationMapWithGivenValues());

        Map<String, String> personalisation = legalRepresentativeNonStandardDirectionPersonalisation.getPersonalisation(asylumCase);

        assertThat(asylumCase).isEqualToComparingOnlyGivenFields(personalisation);
    }

    private Map<String, String> getPersonalisationMapWithGivenValues() {
        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", appealReferenceNumber)
            .put("legalRepReferenceNumber", legalRepReferenceNumber)
            .put("appellantGivenNames", appellantGivenNames)
            .put("appellantFamilyName", appellantFamilyNames)
            .put("iaCcdFrontendUrl", iaCcdFrontendUrl)
            .put("explanation", directionExplanation)
            .put("dueDate", directionDueDate)
            .build();
    }
}
