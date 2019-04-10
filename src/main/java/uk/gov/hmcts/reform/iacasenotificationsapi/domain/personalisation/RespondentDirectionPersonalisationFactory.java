package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.StringProvider;

@Service
public class RespondentDirectionPersonalisationFactory {

    private final StringProvider stringProvider;

    public RespondentDirectionPersonalisationFactory(
        StringProvider stringProvider
    ) {
        this.stringProvider = stringProvider;
    }

    public Map<String, String> create(
        AsylumCase asylumCase,
        Direction direction
    ) {
        requireNonNull(asylumCase, "asylumCase must not be null");
        requireNonNull(direction, "direction must not be null");

        final HearingCentre hearingCentre =
            asylumCase
                .getHearingCentre()
                .orElseThrow(() -> new IllegalStateException("hearingCentre is not present"));

        final String hearingCentreForDisplay =
            stringProvider
                .get("hearingCentre", hearingCentre.toString())
                .orElseThrow(() -> new IllegalStateException("hearingCentre display string is not present"));

        final String directionDueDate =
            LocalDate
                .parse(direction.getDateDue())
                .format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        return
            ImmutableMap
                .<String, String>builder()
                .put("HearingCentre", hearingCentreForDisplay)
                .put("Appeal Ref Number", asylumCase.getAppealReferenceNumber().orElse(""))
                .put("HORef", asylumCase.getHomeOfficeReferenceNumber().orElse(""))
                .put("Given names", asylumCase.getAppellantGivenNames().orElse(""))
                .put("Family name", asylumCase.getAppellantFamilyName().orElse(""))
                .put("Explanation", direction.getExplanation())
                .put("due date", directionDueDate)
                .build();
    }
}
