package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;

@RunWith(MockitoJUnitRunner.class)
public class CaseOfficerSubmitTimeExtensionPersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    EmailAddressFinder emailAddressFinder;

    private Long caseId = 12345L;
    private String templateId = "someTemplateId";
    private String hearingCentreEmailAddress = "hearingCentre@example.com";
    private String appealReferenceNumber = "someReferenceNumber";
    private String appellantGivenName = "Pablo";
    private String appellantFamilyName = "Jimenez";
    private String iaExUiFrontendUrl = "http://localhost";


    private CaseOfficerSubmitTimeExtensionPersonalisation caseOfficerSubmitTimeExtensionPersonalisation;

    @Before
    public void setUp() {
        when(emailAddressFinder.getEmailAddress(asylumCase)).thenReturn(hearingCentreEmailAddress);

        when(asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenName));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));

        caseOfficerSubmitTimeExtensionPersonalisation =
            new CaseOfficerSubmitTimeExtensionPersonalisation(
                templateId,
                iaExUiFrontendUrl,
                emailAddressFinder
            );
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, caseOfficerSubmitTimeExtensionPersonalisation.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_SUBMIT_TIME_EXTENSION_CASE_OFFICER", caseOfficerSubmitTimeExtensionPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_from_asylum_case() {
        assertTrue(hearingCentreEmailAddress, caseOfficerSubmitTimeExtensionPersonalisation.getRecipientsList(asylumCase).contains(hearingCentreEmailAddress));
    }


    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> caseOfficerSubmitTimeExtensionPersonalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase cannot be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        final Map<String, String> expectedPersonalisation =
            ImmutableMap
                .<String, String>builder()
                .put("Appeal Ref Number", appealReferenceNumber)
                .put("Appellant Given names", appellantGivenName)
                .put("Appellant Family name", appellantFamilyName)
                .put("Hyperlink to service", iaExUiFrontendUrl)
                .build();

        Map<String, String> actualPersonalisation = caseOfficerSubmitTimeExtensionPersonalisation.getPersonalisation(asylumCase);

        assertThat(actualPersonalisation).isEqualTo(expectedPersonalisation);
    }

    @Test
    public void should_return_personalisation_when_all_mandatory_information_given() {

        final Map<String, String> expectedPersonalisation =
            ImmutableMap
                .<String, String>builder()
                .put("Appeal Ref Number", "")
                .put("Appellant Given names", "")
                .put("Appellant Family name", "")
                .put("Hyperlink to service", iaExUiFrontendUrl)
                .build();

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());

        Map<String, String> actualPersonalisation = caseOfficerSubmitTimeExtensionPersonalisation.getPersonalisation(asylumCase);

        assertThat(actualPersonalisation).isEqualTo(expectedPersonalisation);
    }
}
