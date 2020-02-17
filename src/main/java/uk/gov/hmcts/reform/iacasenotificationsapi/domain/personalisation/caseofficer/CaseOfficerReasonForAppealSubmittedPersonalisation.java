package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;

@Service
public class CaseOfficerReasonForAppealSubmittedPersonalisation implements EmailNotificationPersonalisation {

    private final String reasonsForAppealSubmittedCaseOfficerTemplateId;
    private final String iaCcdFrontendUrl;
    private final EmailAddressFinder emailAddressFinder;


    public CaseOfficerReasonForAppealSubmittedPersonalisation(
        @NotNull(message = "reasonsForAppealSubmittedCaseOfficerTemplateId cannot be null") @Value("${govnotify.template.submitReasonsForAppealCaseOfficer}") String reasonsForAppealSubmittedCaseOfficerTemplateId,
        @Value("${iaCcdFrontendUrl}") String iaCcdFrontendUrl,
        EmailAddressFinder emailAddressFinder
    ) {
        this.reasonsForAppealSubmittedCaseOfficerTemplateId = reasonsForAppealSubmittedCaseOfficerTemplateId;
        this.iaCcdFrontendUrl = iaCcdFrontendUrl;
        this.emailAddressFinder = emailAddressFinder;
    }

    @Override
    public String getTemplateId() {
        return reasonsForAppealSubmittedCaseOfficerTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(emailAddressFinder.getEmailAddress(asylumCase));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_REASONS_FOR_APPEAL_SUBMITTED_CASE_OFFICER";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase cannot be null");

        return
            ImmutableMap
                .<String, String>builder()
                .put("Appeal Ref Number", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("Appellant Given names", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("Appellant Family name", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("Hyperlink to service", iaCcdFrontendUrl)
                .build();
    }
}
