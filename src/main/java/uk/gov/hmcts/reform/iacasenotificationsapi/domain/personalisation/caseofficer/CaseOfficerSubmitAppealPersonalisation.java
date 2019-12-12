package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;

import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;

@Service
public class CaseOfficerSubmitAppealPersonalisation implements EmailNotificationPersonalisation {

    private final String appealSubmittedCaseOfficerTemplateId;
    private final String iaCcdFrontendUrl;
    private final EmailAddressFinder emailAddressFinder;

    public CaseOfficerSubmitAppealPersonalisation(
        @Value("${govnotify.template.appealSubmittedCaseOfficer}") String appealSubmittedCaseOfficerTemplateId,
        @Value("${iaCcdFrontendUrl}") String iaCcdFrontendUrl,
        EmailAddressFinder emailAddressFinder
    ) {
        requireNonNull(iaCcdFrontendUrl, "iaCcdFrontendUrl must not be null");

        this.appealSubmittedCaseOfficerTemplateId = appealSubmittedCaseOfficerTemplateId;
        this.iaCcdFrontendUrl = iaCcdFrontendUrl;
        this.emailAddressFinder = emailAddressFinder;
    }

    @Override
    public String getTemplateId() {
        return appealSubmittedCaseOfficerTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {

        return Collections.singleton(emailAddressFinder.getEmailAddress(asylumCase));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_APPEAL_SUBMITTED_CASE_OFFICER";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
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
}
