package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.collect.ImmutableMap;
import com.microsoft.applicationinsights.core.dependencies.apachecommons.lang3.StringUtils;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;

@Service
public class EndAppealPersonalisationFactory extends AbstractPersonalisationFactory {

    public Map<String, String> create(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        Map<String, String> immutableMap =  super.create(asylumCase);
        return ImmutableMap
                    .<String, String>builder()
                    .putAll(immutableMap)
                    .put("endAppealApprover", asylumCase.read(END_APPEAL_APPROVER_TYPE, String.class).orElse(""))
                    .put("outcomeOfAppeal", asylumCase.read(END_APPEAL_OUTCOME, String.class).orElse(""))
                    .put("reasonsOfOutcome", asylumCase.read(END_APPEAL_OUTCOME_REASON, String.class)
                            .filter(StringUtils::isNotBlank)
                            .orElse("No reason")
                    )
                    .put("endAppealDate", asylumCase.read(END_APPEAL_DATE, String.class)
                            .map(date -> LocalDate.parse(date).format(DateTimeFormatter.ofPattern("d MMM yyyy")))
                            .orElse("")
                    )
                    .build();
    }
}
