package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REPRESENTATIVE_EMAIL_ADDRESS;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DirectionFinder;

@RunWith(MockitoJUnitRunner.class)
public class LegalRepresentativeRequestCaseBuildingPersonalisationTest {

    @Mock AsylumCase asylumCase;
    @Mock DirectionFinder directionFinder;
    @Mock Direction direction;

    private Long caseId = 12345L;
    private String templateId = "someTemplateId";
    private String directionDueDate = "2019-09-10";
    private String expectedDirectionDueDate = "10 Oct 2019";
    private String directionExplanation = "someExplanation";
    private final String iaCcdFrontendUrl = "http://localhost";

    private String legalRepEmailAddress = "legalrep@example.com";

    private String appealReferenceNumber = "someReferenceNumber";
    private String legalRepRefNumber = "somelegalRepRefNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";

    private LegalRepresentativeRequestCaseBuildingPersonalisation legalRepresentativeRequestCaseBuildingPersonalisation;

    @Before
    public void setUp() {

        when((direction.getDateDue())).thenReturn(directionDueDate);
        when((direction.getExplanation())).thenReturn(directionExplanation);
        when(directionFinder.findFirst(asylumCase, DirectionTag.REQUEST_CASE_BUILDING)).thenReturn(Optional.of(direction));

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepRefNumber));
        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class)).thenReturn(Optional.of(legalRepEmailAddress));

        legalRepresentativeRequestCaseBuildingPersonalisation = new LegalRepresentativeRequestCaseBuildingPersonalisation(
                templateId,
                iaCcdFrontendUrl,
                directionFinder
        );
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, legalRepresentativeRequestCaseBuildingPersonalisation.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_LEGAL_REPRESENTATIVE_REQUEST_CASE_BUILDING", legalRepresentativeRequestCaseBuildingPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_from_asylum_case() {
        assertEquals(legalRepEmailAddress, legalRepresentativeRequestCaseBuildingPersonalisation.getEmailAddress(asylumCase));
    }

    @Test
    public void should_throw_exception_when_cannot_find_email_address_for_legal_rep() {
        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> legalRepresentativeRequestCaseBuildingPersonalisation.getEmailAddress(asylumCase))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("legalRepresentativeEmailAddress is not present");
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> legalRepresentativeRequestCaseBuildingPersonalisation.getPersonalisation((AsylumCase)null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        final Map<String, String> expectedPersonalisation =
                ImmutableMap
                        .<String, String>builder()
                        .put("appealReferenceNumber", appealReferenceNumber)
                        .put("appellantGivenNames", appellantGivenNames)
                        .put("appellantFamilyName", appellantGivenNames)
                        .put("directionExplanation", directionExplanation)
                        .put("expectedDirectionDueDate", expectedDirectionDueDate)
                        .put("iaCcdFrontendUrl", iaCcdFrontendUrl)
                        .put("legalRepRefNumber", legalRepRefNumber)
                        .build();

        Map<String, String> actualPersonalisation = legalRepresentativeRequestCaseBuildingPersonalisation.getPersonalisation(asylumCase);

        assertThat(actualPersonalisation).isEqualToComparingOnlyGivenFields(expectedPersonalisation);
    }

    @Test
    public void should_return_personalisation_when_all_mandatory_information_given() {

        final Map<String, String> expectedPersonalisation =
                ImmutableMap
                    .<String, String>builder()
                    .put("appealReferenceNumber", "")
                    .put("appellantGivenNames", "")
                    .put("appellantFamilyName", "")
                    .put("directionExplanation", "")
                    .put("expectedDirectionDueDate", "")
                    .put("legalRepRefNumber", "")
                    .build();

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());


        Map<String, String> actualPersonalisation = legalRepresentativeRequestCaseBuildingPersonalisation.getPersonalisation(asylumCase);

        assertThat(actualPersonalisation).isEqualToComparingOnlyGivenFields(expectedPersonalisation);
    }

    @Test
    public void should_throw_exception_on_personalisation_when_direction_is_empty() {

        when(directionFinder.findFirst(asylumCase, DirectionTag.REQUEST_CASE_BUILDING)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> legalRepresentativeRequestCaseBuildingPersonalisation.getPersonalisation(asylumCase))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("legal representative request case building is not present");
    }

}
