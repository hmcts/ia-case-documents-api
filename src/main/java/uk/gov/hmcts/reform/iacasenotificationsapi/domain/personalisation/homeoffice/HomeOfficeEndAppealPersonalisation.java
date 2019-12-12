package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import com.microsoft.applicationinsights.core.dependencies.apachecommons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;

@Service
public class HomeOfficeEndAppealPersonalisation implements EmailNotificationPersonalisation {

    private final String endAppealHomeOfficeTemplateId;
    private final String endAppealEmailAddresses;

    public HomeOfficeEndAppealPersonalisation(
        @Value("${endAppealHomeOfficeEmailAddress}") String endAppealEmailAddresses,
        @NotNull(message = "endAppealHomeOfficeTemplateId cannot be null") @Value("${govnotify.template.endAppealHomeOfficeTemplateId}") String endAppealHomeOfficeTemplateId) {

        this.endAppealHomeOfficeTemplateId = endAppealHomeOfficeTemplateId;
        this.endAppealEmailAddresses = endAppealEmailAddresses;
    }

    @Override
    public String getTemplateId() {
        return endAppealHomeOfficeTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(endAppealEmailAddresses);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_END_APPEAL_HOME_OFFICE";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("outcomeOfAppeal", asylumCase.read(AsylumCaseDefinition.END_APPEAL_OUTCOME, String.class).orElse(""))
            .put("reasonsOfOutcome", asylumCase.read(AsylumCaseDefinition.END_APPEAL_OUTCOME_REASON, String.class)
                .filter(StringUtils::isNotBlank)
                .orElse("No reason")
            )
            .put("endAppealApprover", asylumCase.read(AsylumCaseDefinition.END_APPEAL_APPROVER_TYPE, String.class).orElse(""))
            .put("endAppealDate", asylumCase.read(AsylumCaseDefinition.END_APPEAL_DATE, String.class)
                .map(date -> LocalDate.parse(date).format(DateTimeFormatter.ofPattern("d MMM yyyy")))
                .orElse("")
            )
            .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .build();
    }
}
