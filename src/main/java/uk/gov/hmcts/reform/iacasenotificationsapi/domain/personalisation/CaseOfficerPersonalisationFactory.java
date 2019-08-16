package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE;

import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.StringProvider;

@Service
public class CaseOfficerPersonalisationFactory {

    private final String iaCcdFrontendUrl;
    private final StringProvider stringProvider;

    public CaseOfficerPersonalisationFactory(
        @Value("${iaCcdFrontendUrl}") String iaCcdFrontendUrl,
        StringProvider stringProvider
    ) {
        requireNonNull(iaCcdFrontendUrl, "iaCcdFrontendUrl must not be null");

        this.iaCcdFrontendUrl = iaCcdFrontendUrl;
        this.stringProvider = stringProvider;
    }

    public Map<String, String> create(
        AsylumCase asylumCase
    ) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return
            ImmutableMap
                .<String, String>builder()
                .put("Appeal Ref Number", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("Given names", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("Family name", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("Hyperlink to user’s case list", iaCcdFrontendUrl)
                .build();
    }

    public Map<String, String> createListedCase(
        AsylumCase asylumCase
    ) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        final HearingCentre listedHearingCentre =
            asylumCase
                .read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)
                .orElseThrow(() -> new IllegalStateException("listCaseHearingCentre is not present"));

        final String hearingCentreAddress =
            stringProvider
                .get("hearingCentreAddress", listedHearingCentre.toString())
                .orElseThrow(() -> new IllegalStateException("hearingCentreAddress is not present"));

        final String hearingDateTime =
            asylumCase
                .read(AsylumCaseDefinition.LIST_CASE_HEARING_DATE, String.class)
                .orElseThrow(() -> new IllegalStateException("hearingDateTime is not present"));

        return
            ImmutableMap
                .<String, String>builder()
                .put("Appeal Ref Number", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("Listing Ref Number", asylumCase.read(AsylumCaseDefinition.ARIA_LISTING_REFERENCE, String.class).orElse(""))
                .put("Hearing Date", extractHearingDate(hearingDateTime))
                .put("Hearing Time", extractHearingTime(hearingDateTime))
                .put("Hearing Centre Address", hearingCentreAddress)
                .build();
    }

    public String extractHearingDate(String hearingDateTime) {

        final LocalDateTime dateTimeValue;
        final LocalDate dateValue;
        final String hearingDate;

        dateTimeValue = LocalDateTime.parse(hearingDateTime, ISO_DATE_TIME);
        dateValue = dateTimeValue.toLocalDate();

        hearingDate = LocalDate
            .parse(dateValue.toString())
            .format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        return hearingDate;
    }

    public String extractHearingTime(String hearingDateTime) {

        final LocalDateTime dateTimeValue;
        final LocalTime timeValue;
        final String hearingTime;

        dateTimeValue = LocalDateTime.parse(hearingDateTime, ISO_DATE_TIME);
        timeValue = dateTimeValue.toLocalTime();

        hearingTime = LocalTime
            .parse(timeValue.toString())
            .format(DateTimeFormatter.ofPattern("HH:mm"));

        return hearingTime;
    }
}
