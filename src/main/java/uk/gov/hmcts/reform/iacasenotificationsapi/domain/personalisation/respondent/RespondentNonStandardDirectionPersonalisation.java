package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.respondent;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;

import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DirectionFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.StringProvider;

@Service
public class RespondentNonStandardDirectionPersonalisation implements EmailNotificationPersonalisation {

    private final String respondentNonStandardDirectionTemplateId;
    private final String respondentNonStandardDirectionEmailAddress;
    private final StringProvider stringProvider;
    private final DirectionFinder directionFinder;

    public RespondentNonStandardDirectionPersonalisation(@Value("${govnotify.template.nonStandardDirection.respondent.email}") String respondentNonStandardDirectionTemplateId,
        @Value("${respondentEmailAddresses.nonStandardDirectionUntilListing}") String respondentNonStandardDirectionEmailAddress,
        StringProvider stringProvider,
        DirectionFinder directionFinder) {

        this.respondentNonStandardDirectionTemplateId = respondentNonStandardDirectionTemplateId;
        this.respondentNonStandardDirectionEmailAddress = respondentNonStandardDirectionEmailAddress;
        this.stringProvider = stringProvider;
        this.directionFinder = directionFinder;
    }

    @Override
    public String getTemplateId() {
        return respondentNonStandardDirectionTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(respondentNonStandardDirectionEmailAddress);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_RESPONDENT_NON_STANDARD_DIRECTION";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        final HearingCentre hearingCentre =
            asylumCase
                .read(HEARING_CENTRE, HearingCentre.class)
                .orElseThrow(() -> new IllegalStateException("hearingCentre is not present"));

        final String hearingCentreForDisplay =
            stringProvider
                .get("hearingCentre", hearingCentre.toString())
                .orElseThrow(() -> new IllegalStateException("hearingCentre display string is not present"));

        final Direction direction =
            directionFinder
                .findFirst(asylumCase, DirectionTag.NONE)
                .orElseThrow(() -> new IllegalStateException("non-standard direction is not present"));

        final String directionDueDate =
            LocalDate
                .parse(direction.getDateDue())
                .format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        return ImmutableMap
            .<String, String>builder()
            .put("HearingCentre", hearingCentreForDisplay)
            .put("Appeal Ref Number", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("HORef", asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("Given names", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("Family name", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("Explanation", direction.getExplanation())
            .put("due date", directionDueDate)
            .build();
    }
}
