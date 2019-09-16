package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_DATE;

import com.google.common.collect.ImmutableMap;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.StringProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.DateTimeExtractor;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class CaseOfficerCaseEditedPersonalisationFactoryTest {

    @Mock private StringProvider stringProvider;
    @Mock private AsylumCase asylumCase;
    @Mock private AsylumCase asylumCaseBefore;
    @Mock private CaseDetails<AsylumCase> caseDetailsBefore;

    final String extractedHearingDateFormatted = "3 May 2019";
    final String extractedHearingDateFormattedBefore = "2 May 2019";
    final String extractedHearingTime = "14:25";
    final String iaCcdFrontendUrl = "http://www.ccd.example.com";
    final String listCaseHearingDate = "2019-05-03T14:25:15.000";
    final String listCaseHearingDateBefore = "2019-05-02T14:25:15.000";
    final String listCaseHearingCentreAddress = "IAC Taylor House, 88 Rosebery Avenue, London, EC1R 4QU";
    private String expectedFormattedManchesterHearingCentreName = "Manchester";

    private CaseOfficerCaseEditedPersonalisationFactory caseOfficerCaseEditedPersonalisationFactory;
    private DateTimeExtractor dateTimeExtractor;

    @Before
    public void setUp() {

        dateTimeExtractor = new DateTimeExtractor();

        caseOfficerCaseEditedPersonalisationFactory =
            new CaseOfficerCaseEditedPersonalisationFactory(
                iaCcdFrontendUrl,
                stringProvider,
                dateTimeExtractor
            );

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));
        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(listCaseHearingDate));
        when(stringProvider.get("hearingCentreAddress", "taylorHouse")).thenReturn(Optional.of(listCaseHearingCentreAddress));
    }

    @Test
    public void should_create_personalisation_for_edited_case_notification() {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));
        when(asylumCaseBefore.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.MANCHESTER));
        when(asylumCaseBefore.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(listCaseHearingDateBefore));
        when(stringProvider.get("hearingCentreName", "manchester")).thenReturn(Optional.of(expectedFormattedManchesterHearingCentreName));

        final Map<String, String> expectedPersonalisation =
            ImmutableMap
                .<String, String>builder()
                .put("Hyperlink to user’s case list", iaCcdFrontendUrl)
                .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("ariaListingReference", asylumCase.read(AsylumCaseDefinition.ARIA_LISTING_REFERENCE, String.class).orElse(""))
                .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
                .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("oldHearingCentre", "Manchester")
                .put("oldHearingDate", extractedHearingDateFormattedBefore)
                .put("hearingDate", extractedHearingDateFormatted)
                .put("hearingTime", extractedHearingTime)
                .put("hearingCentreAddress", listCaseHearingCentreAddress)
                .build();

        Map<String, String> actualPersonalisation =
            caseOfficerCaseEditedPersonalisationFactory
                .createEditedCase(asylumCase, Optional.of(new CaseDetails<>(1, "someString", State.PRE_HEARING, asylumCaseBefore, LocalDateTime.now())));

        verify(stringProvider, times(1)).get("hearingCentreAddress", "taylorHouse");

        assertEquals(expectedPersonalisation, actualPersonalisation);
    }

    @Test
    public void should_throw_when_list_case_hearing_centre_not_present() {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> caseOfficerCaseEditedPersonalisationFactory.createEditedCase(asylumCase, Optional.of(caseDetailsBefore)))
            .hasMessage("listCaseHearingCentre is not present")
            .isExactlyInstanceOf(IllegalStateException.class);

        verify(stringProvider, times(0)).get("hearingCentreAddress", "taylorHouse");
    }

    @Test
    public void should_throw_when_hearing_date_time_not_present() {

        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> caseOfficerCaseEditedPersonalisationFactory.createEditedCase(asylumCase, Optional.of(caseDetailsBefore)))
            .hasMessage("hearingDateTime is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_throw_when_list_case_hearing_centre_address_not_present() {

        when(stringProvider.get("hearingCentreAddress", "taylorHouse")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> caseOfficerCaseEditedPersonalisationFactory.createEditedCase(asylumCase, Optional.of(caseDetailsBefore)))
            .hasMessage("hearingCentreAddress is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_throw_when_list_case_hearing_date_time_not_present() {

        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> caseOfficerCaseEditedPersonalisationFactory.createEditedCase(asylumCase, Optional.of(caseDetailsBefore)))
            .hasMessage("hearingDateTime is not present")
            .isExactlyInstanceOf(IllegalStateException.class);

        verify(stringProvider, times(1)).get("hearingCentreAddress", "taylorHouse");
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> caseOfficerCaseEditedPersonalisationFactory.createEditedCase(null, Optional.of(caseDetailsBefore)))
            .hasMessage("asylumCase must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> caseOfficerCaseEditedPersonalisationFactory.createEditedCase(asylumCase, null))
            .hasMessage("caseDetailsBefore must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
