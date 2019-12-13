package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DirectionFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config.GovNotifyTemplateIdConfiguration;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class CaseOfficerRequestHearingRequirementsPersonalisationTest {

    @Mock AsylumCase asylumCase;
    @Mock DirectionFinder directionFinder;
    @Mock Direction direction;

    @Mock EmailAddressFinder emailAddressFinder;
    @Mock GovNotifyTemplateIdConfiguration govNotifyTemplateIdConfiguration;

    private Long caseId = 12345L;
    private String templateId = "someTemplateId";
    private String directionDueDate = "2019-08-27";
    private String expectedDirectionDueDate = "27 Aug 2019";
    private String directionExplanation = "someExplanation";

    private String legalRepEmailAddress = "legalrep@example.com";

    private String appealReferenceNumber = "someReferenceNumber";
    private String legalRepRefNumber = "somelegalRepRefNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";

    private CaseOfficerRequestHearingRequirementsPersonalisation caseOfficerRequestHearingRequirementsPersonalisation;

    @Before
    public void setup() {

        when((direction.getDateDue())).thenReturn(directionDueDate);
        when((direction.getExplanation())).thenReturn(directionExplanation);
        when(directionFinder.findFirst(asylumCase, DirectionTag.LEGAL_REPRESENTATIVE_HEARING_REQUIREMENTS)).thenReturn(Optional.of(direction));

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepRefNumber));
        when(govNotifyTemplateIdConfiguration.getCaseOfficerRequestHearingRequirementsTemplateId()).thenReturn("someTemplateId");
        when(emailAddressFinder.getLegalRepEmailAddress(asylumCase)).thenReturn(legalRepEmailAddress);

        caseOfficerRequestHearingRequirementsPersonalisation = new CaseOfficerRequestHearingRequirementsPersonalisation(
            govNotifyTemplateIdConfiguration,
            emailAddressFinder,
            directionFinder
        );
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, caseOfficerRequestHearingRequirementsPersonalisation.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_CASE_OFFICER_REQUEST_HEARING_REQUIREMENTS_DIRECTION", caseOfficerRequestHearingRequirementsPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_from_asylum_case() {
        assertEquals(Collections.singleton(legalRepEmailAddress), caseOfficerRequestHearingRequirementsPersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> caseOfficerRequestHearingRequirementsPersonalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation = caseOfficerRequestHearingRequirementsPersonalisation.getPersonalisation(asylumCase);

        Assertions.assertThat(asylumCase).isEqualToComparingOnlyGivenFields(personalisation);
    }

    @Test
    public void should_throw_exception_on_personalisation_when_direction_is_empty() {

        when(directionFinder.findFirst(asylumCase, DirectionTag.LEGAL_REPRESENTATIVE_HEARING_REQUIREMENTS)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> caseOfficerRequestHearingRequirementsPersonalisation.getPersonalisation(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("case officer request hearing requirements direction is not present");
    }

}
