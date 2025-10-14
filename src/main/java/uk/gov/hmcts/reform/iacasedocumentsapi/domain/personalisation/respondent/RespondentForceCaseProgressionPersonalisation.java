package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.respondent;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.*;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@Service
public class RespondentForceCaseProgressionPersonalisation implements EmailNotificationPersonalisation {

    private final String respondentEmailTemplateId;
    private final String respondentEmailAddress;
    private final String iaExUiFrontendUrl;
    private final CustomerServicesProvider customerServicesProvider;

    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;

    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;

    public RespondentForceCaseProgressionPersonalisation(
        @NotNull(message = "appealDocumentDeletedTemplateId cannot be null")
        @Value("${govnotify.template.forceCaseProgression.awaitingRespondentEvidence.to.caseBuilding.respondent.email}")
            String respondentEmailTemplateId,
        @Value("${respondentEmailAddresses.respondentEvidenceDirection}") String respondentEmailAddress,
        @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
        CustomerServicesProvider customerServicesProvider) {

        this.respondentEmailTemplateId = respondentEmailTemplateId;
        this.respondentEmailAddress = respondentEmailAddress;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId() {
        return respondentEmailTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(respondentEmailAddress);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_RESPONDENT_FORCE_CASE_PROGRESSION";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");
        return ImmutableMap.<String, String>builder()
            .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix)
            .put("appealReferenceNumber",
                asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("homeOfficeReferenceNumber",
                asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("linkToOnlineService", iaExUiFrontendUrl)
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .build();
    }
}
