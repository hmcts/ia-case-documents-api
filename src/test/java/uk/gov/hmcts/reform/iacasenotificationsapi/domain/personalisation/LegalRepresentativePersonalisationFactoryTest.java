package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.collect.ImmutableMap;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.StringProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.DateTimeExtractor;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class LegalRepresentativePersonalisationFactoryTest {

    @Mock private AsylumCase asylumCase;
    @Mock private Direction direction;
    @Mock private StringProvider stringProvider;
    @Mock private DateTimeExtractor dateTimeExtractor;

    final String iaCcdFrontendUrl = "http://www.ccd.example.com";
    final String appealReferenceNumber = "PA/001/2018";
    final String legalRepReferenceNumber = "SOMETHING";
    final String appellantGivenNames = "Jane";
    final String appellantFamilyName = "Doe";
    final String directionExplanation = "Send your evidence";
    final String directionDateDue = "2018-12-31";
    final String directionDateDueFormatted = "31 Dec 2018";

    final String ariaListingReference = "LP/12345/2019";
    final String listCaseHearingDate = "2019-05-03T14:25:15.000";
    final String invalidIso8601HearingDate = "2019-05-03 14:25:15";
    final String extractedHearingDateFormatted = "3 May 2019";
    final String extractedHearingTime = "14:25";
    final String listCaseHearingCentreAddress = "IAC Taylor House, 88 Rosebery Avenue, London, EC1R 4QU";

    final String hearingRequirementVulnerabilities = "No special adjustments are being made to accommodate vulnerabilities";
    final String hearingRequirementMultimedia = "No multimedia equipment is being provided";
    final String hearingRequirementSingleSexCourt = "The court will not be single sex";
    final String hearingRequirementInCameraCourt = "The hearing will be held in public court";
    final String hearingRequirementOther = "No other adjustments are being made";

    final Map<String, String> expectedPersonalisation =
        ImmutableMap
            .<String, String>builder()
            .put("Appeal Ref Number", appealReferenceNumber)
            .put("LR reference", legalRepReferenceNumber)
            .put("Given names", appellantGivenNames)
            .put("Family name", appellantFamilyName)
            .put("Hyperlink to user’s case list", iaCcdFrontendUrl)
            .put("Explanation", directionExplanation)
            .put("due date", directionDateDueFormatted)
            .build();

    private LegalRepresentativePersonalisationFactory legalRepresentativePersonalisationFactory;

    @Before
    public void setUp() {

        dateTimeExtractor = new DateTimeExtractor();

        legalRepresentativePersonalisationFactory =
            new LegalRepresentativePersonalisationFactory(
                iaCcdFrontendUrl,
                stringProvider
            );

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));
        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(listCaseHearingDate));
        when(stringProvider.get("hearingCentreAddress", "taylorHouse")).thenReturn(Optional.of(listCaseHearingCentreAddress));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaListingReference));

        when(direction.getExplanation()).thenReturn(directionExplanation);
        when(direction.getDateDue()).thenReturn(directionDateDue);
    }

    @Test
    public void should_create_personalisation_from_case() {

        Map<String, String> actualPersonalisation =
            legalRepresentativePersonalisationFactory.create(asylumCase, direction);

        assertEquals(expectedPersonalisation, actualPersonalisation);
    }

    @Test
    public void should_create_personalisation_using_defaults_where_available() {

        final Map<String, String> expectedPersonalisation =
            ImmutableMap
                .<String, String>builder()
                .put("Appeal Ref Number", "")
                .put("LR reference", "")
                .put("Given names", "")
                .put("Family name", "")
                .put("Hyperlink to user’s case list", iaCcdFrontendUrl)
                .put("Explanation", directionExplanation)
                .put("due date", directionDateDueFormatted)
                .build();

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());

        Map<String, String> actualPersonalisation =
            legalRepresentativePersonalisationFactory.create(asylumCase, direction);

        assertEquals(expectedPersonalisation, actualPersonalisation);
    }

    @Test
    public void should_create_personalisation_for_listed_case_notification() {

        final Map<String, String> expectedPersonalisation =
            ImmutableMap
                .<String, String>builder()
                .put("Appeal Ref Number", appealReferenceNumber)
                .put("Listing Ref Number", ariaListingReference)
                .put("Legal Rep Ref Number", legalRepReferenceNumber)
                .put("Appellant Given Names", appellantGivenNames)
                .put("Appellant Family Name", appellantFamilyName)
                .put("Hearing Date", extractedHearingDateFormatted)
                .put("Hearing Time", extractedHearingTime)
                .put("Hearing Centre Address", listCaseHearingCentreAddress)
                .put("Hearing Requirement Vulnerabilities", hearingRequirementVulnerabilities)
                .put("Hearing Requirement Multimedia", hearingRequirementMultimedia)
                .put("Hearing Requirement Single Sex Court", hearingRequirementSingleSexCourt)
                .put("Hearing Requirement In Camera Court", hearingRequirementInCameraCourt)
                .put("Hearing Requirement Other", hearingRequirementOther)
                .build();

        Map<String, String> actualPersonalisation =
            legalRepresentativePersonalisationFactory.createListedCase(asylumCase);

        verify(stringProvider, times(1)).get("hearingCentreAddress", "taylorHouse");

        assertEquals(expectedPersonalisation, actualPersonalisation);
    }

    @Test
    public void should_throw_when_list_case_hearing_centre_not_present() {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> legalRepresentativePersonalisationFactory.createListedCase(asylumCase))
            .hasMessage("listCaseHearingCentre is not present")
            .isExactlyInstanceOf(IllegalStateException.class);

        verify(stringProvider, times(0)).get("hearingCentreAddress", "taylorHouse");
    }

    @Test
    public void should_throw_when_valid_iso_8601_date_time_not_present() {

        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(invalidIso8601HearingDate));

        assertThatThrownBy(() -> legalRepresentativePersonalisationFactory.createListedCase(asylumCase))
            .isExactlyInstanceOf(DateTimeParseException.class);
    }

    @Test
    public void should_throw_when_list_case_hearing_centre_address_not_present() {

        when(stringProvider.get("hearingCentreAddress", "taylorHouse")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> legalRepresentativePersonalisationFactory.createListedCase(asylumCase))
            .hasMessage("hearingCentreAddress is not present")
            .isExactlyInstanceOf(IllegalStateException.class);

        verify(stringProvider, times(1)).get("hearingCentreAddress", "taylorHouse");
    }

    @Test
    public void should_throw_when_list_case_hearing_date_time_not_present() {

        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> legalRepresentativePersonalisationFactory.createListedCase(asylumCase))
            .hasMessage("hearingDateTime is not present")
            .isExactlyInstanceOf(IllegalStateException.class);

        verify(stringProvider, times(1)).get("hearingCentreAddress", "taylorHouse");
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> legalRepresentativePersonalisationFactory.create(null, direction))
            .hasMessage("asylumCase must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> legalRepresentativePersonalisationFactory.create(asylumCase, null))
            .hasMessage("direction must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> legalRepresentativePersonalisationFactory.createListedCase(null))
            .hasMessage("asylumCase must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
