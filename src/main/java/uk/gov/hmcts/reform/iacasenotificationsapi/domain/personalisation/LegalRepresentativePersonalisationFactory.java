package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Direction;

@Service
public class LegalRepresentativePersonalisationFactory {

    private final String iaCcdFrontendUrl;

    public LegalRepresentativePersonalisationFactory(
        @Value("${iaCcdFrontendUrl}") String iaCcdFrontendUrl
    ) {
        requireNonNull(iaCcdFrontendUrl, "iaCcdFrontendUrl must not be null");

        this.iaCcdFrontendUrl = iaCcdFrontendUrl;
    }

    public Map<String, String> create(
        AsylumCase asylumCase,
        Direction direction
    ) {
        requireNonNull(asylumCase, "asylumCase must not be null");
        requireNonNull(direction, "direction must not be null");

        final String directionDueDate =
            LocalDate
                .parse(direction.getDateDue())
                .format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        return
            ImmutableMap
                .<String, String>builder()
                .put("Appeal Ref Number", asylumCase.getAppealReferenceNumber().orElse(""))
                .put("LR reference", asylumCase.getLegalRepReferenceNumber().orElse(""))
                .put("Given names", asylumCase.getAppellantGivenNames().orElse(""))
                .put("Family name", asylumCase.getAppellantFamilyName().orElse(""))
                .put("Hyperlink to user’s case list", iaCcdFrontendUrl)
                .put("Explanation", direction.getExplanation())
                .put("due date", directionDueDate)
                .build();
    }
}
