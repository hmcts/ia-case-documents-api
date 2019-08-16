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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.StringProvider;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class CaseOfficerPersonalisationFactoryTest {

    @Mock private StringProvider stringProvider;
    @Mock private AsylumCase asylumCase;

    final String iaCcdFrontendUrl = "http://www.ccd.example.com";
    final String appealReferenceNumber = "PA/001/2018";
    final String appellantGivenNames = "Jane";
    final String appellantFamilyName = "Doe";
    final String ariaListingReference = "LP/12345/2019";
    final String listCaseHearingDate = "2019-05-03T14:25:15.000";
    final String invalidIso8601HearingDate = "2019-05-03 14:25:15";
    final String extractedHearingDateFormatted = "3 May 2019";
    final String extractedHearingTime = "14:25";
    final String listCaseHearingCentreAddress = "IAC Taylor House, 88 Rosebery Avenue, London, EC1R 4QU";

    final Map<String, String> expectedPersonalisation =
        ImmutableMap
            .<String, String>builder()
            .put("Appeal Ref Number", appealReferenceNumber)
            .put("Given names", appellantGivenNames)
            .put("Family name", appellantFamilyName)
            .put("Hyperlink to user’s case list", iaCcdFrontendUrl)
            .build();

    private CaseOfficerPersonalisationFactory caseOfficerPersonalisationFactory;

    @Before
    public void setUp() {

        caseOfficerPersonalisationFactory =
            new CaseOfficerPersonalisationFactory(
                iaCcdFrontendUrl,
                stringProvider
            );

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));

        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaListingReference));
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));

        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(listCaseHearingDate));
        when(stringProvider.get("hearingCentreAddress", "taylorHouse")).thenReturn(Optional.of(listCaseHearingCentreAddress));
    }

    @Test
    public void should_create_personalisation_from_case() {

        Map<String, String> actualPersonalisation =
            caseOfficerPersonalisationFactory.create(asylumCase);

        assertEquals(expectedPersonalisation, actualPersonalisation);
    }

    @Test
    public void should_create_personalisation_using_defaults_where_available() {

        final Map<String, String> expectedPersonalisation =
            ImmutableMap
                .<String, String>builder()
                .put("Appeal Ref Number", "")
                .put("Given names", "")
                .put("Family name", "")
                .put("Hyperlink to user’s case list", iaCcdFrontendUrl)
                .build();

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());

        Map<String, String> actualPersonalisation =
            caseOfficerPersonalisationFactory.create(asylumCase);

        assertEquals(expectedPersonalisation, actualPersonalisation);
    }

    @Test
    public void should_create_personalisation_for_listed_case_notification() {

        final Map<String, String> expectedPersonalisation =
            ImmutableMap
                .<String, String>builder()
                .put("Appeal Ref Number", appealReferenceNumber)
                .put("Listing Ref Number", ariaListingReference)
                .put("Hearing Date", extractedHearingDateFormatted)
                .put("Hearing Time", extractedHearingTime)
                .put("Hearing Centre Address", listCaseHearingCentreAddress)
                .build();

        Map<String, String> actualPersonalisation =
            caseOfficerPersonalisationFactory.createListedCase(asylumCase);

        verify(stringProvider, times(1)).get("hearingCentreAddress", "taylorHouse");

        assertEquals(expectedPersonalisation, actualPersonalisation);
    }

    @Test
    public void should_throw_when_list_case_hearing_centre_not_present() {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> caseOfficerPersonalisationFactory.createListedCase(asylumCase))
            .hasMessage("listCaseHearingCentre is not present")
            .isExactlyInstanceOf(IllegalStateException.class);

        verify(stringProvider, times(0)).get("hearingCentreAddress", "taylorHouse");
    }

    @Test
    public void should_throw_when_list_case_hearing_centre_address_not_present() {

        when(stringProvider.get("hearingCentreAddress", "taylorHouse")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> caseOfficerPersonalisationFactory.createListedCase(asylumCase))
            .hasMessage("hearingCentreAddress is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_handle_valid_iso_8601_date() {

        String actualHearingDate =
            caseOfficerPersonalisationFactory.extractHearingDate(listCaseHearingDate);

        assertEquals(extractedHearingDateFormatted, actualHearingDate);
    }

    @Test
    public void should_handle_valid_iso_8601_time() {

        String actualHearingTime =
            caseOfficerPersonalisationFactory.extractHearingTime(listCaseHearingDate);

        assertEquals(extractedHearingTime, actualHearingTime);
    }

    @Test
    public void should_throw_when_valid_iso_8601_date_not_present() {

        assertThatThrownBy(() -> caseOfficerPersonalisationFactory.extractHearingDate(invalidIso8601HearingDate))
            .isExactlyInstanceOf(DateTimeParseException.class);

        verify(stringProvider, times(0)).get("hearingCentreAddress", "taylorHouse");
    }

    @Test
    public void should_throw_when_valid_iso_8601_time_not_present() {

        assertThatThrownBy(() -> caseOfficerPersonalisationFactory.extractHearingTime(invalidIso8601HearingDate))
            .isExactlyInstanceOf(DateTimeParseException.class);

        verify(stringProvider, times(0)).get("hearingCentreAddress", "taylorHouse");
    }

    @Test
    public void should_throw_when_valid_iso_8601_date_time_not_present() {

        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(invalidIso8601HearingDate));

        assertThatThrownBy(() -> caseOfficerPersonalisationFactory.createListedCase(asylumCase))
            .isExactlyInstanceOf(DateTimeParseException.class);
    }

    @Test
    public void should_throw_when_list_case_hearing_date_time_not_present() {

        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> caseOfficerPersonalisationFactory.createListedCase(asylumCase))
            .hasMessage("hearingDateTime is not present")
            .isExactlyInstanceOf(IllegalStateException.class);

        verify(stringProvider, times(1)).get("hearingCentreAddress", "taylorHouse");
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> caseOfficerPersonalisationFactory.create(null))
            .hasMessage("asylumCase must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> caseOfficerPersonalisationFactory.createListedCase(null))
            .hasMessage("asylumCase must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
