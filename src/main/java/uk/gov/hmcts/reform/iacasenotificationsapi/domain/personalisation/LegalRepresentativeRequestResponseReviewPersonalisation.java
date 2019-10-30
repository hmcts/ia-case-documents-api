package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DirectionFinder;

@Service
public class LegalRepresentativeRequestResponseReviewPersonalisation implements NotificationPersonalisation {

    private final String legalRepresentativeRequestResponseReviewTemplateId;
    private final String iaCcdFrontendUrl;
    private final DirectionFinder directionFinder;

    public LegalRepresentativeRequestResponseReviewPersonalisation(
            @Value("${govnotify.template.responseReviewDirection}") String legalRepresentativeRequestResponseReviewTemplateId,
            @Value("${iaCcdFrontendUrl}") String iaCcdFrontendUrl,
            DirectionFinder directionFinder
    ) {
        requireNonNull(iaCcdFrontendUrl, "iaCcdFrontendUrl must not be null");

        this.legalRepresentativeRequestResponseReviewTemplateId = legalRepresentativeRequestResponseReviewTemplateId;
        this.iaCcdFrontendUrl = iaCcdFrontendUrl;
        this.directionFinder = directionFinder;
    }

    @Override
    public String getTemplateId() {
        return legalRepresentativeRequestResponseReviewTemplateId;
    }

    @Override
    public String getEmailAddress(AsylumCase asylumCase) {
        return asylumCase
                .read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class)
                .orElseThrow(() -> new IllegalStateException("legalRepresentativeEmailAddress is not present"));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_LEGAL_REPRESENTATIVE_REQUEST_RESPONSE_REVIEW";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        Direction direction =
                directionFinder
                        .findFirst(asylumCase, DirectionTag.REQUEST_RESPONSE_REVIEW)
                        .orElseThrow(() -> new IllegalStateException("legal representative request response review is not present"));

        final String directionDueDate =
                LocalDate
                        .parse(direction.getDateDue())
                        .format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        return
                ImmutableMap
                        .<String, String>builder()
                        .put("Appeal Ref Number", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                        .put("LR reference", asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""))
                        .put("Given names", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                        .put("Family name", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
                        .put("Hyperlink to user’s case list", iaCcdFrontendUrl)
                        .put("Explanation", direction.getExplanation())
                        .put("due date", directionDueDate)
                        .build();
    }
}
