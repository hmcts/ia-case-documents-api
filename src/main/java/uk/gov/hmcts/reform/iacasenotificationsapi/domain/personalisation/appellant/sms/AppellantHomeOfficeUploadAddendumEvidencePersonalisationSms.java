package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.SmsNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;

@Service
public class AppellantHomeOfficeUploadAddendumEvidencePersonalisationSms implements SmsNotificationPersonalisation {

    private final String templateId;
    private final String directLinkToNewEvidencePage;
    private FeatureToggler featureToggler;
    private final RecipientsFinder recipientsFinder;

    public AppellantHomeOfficeUploadAddendumEvidencePersonalisationSms(
            @Value("${govnotify.template.hoOrTcwUploadedAddendumEvidence.appellant.sms}") String templateId,
            @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
            RecipientsFinder recipientsFinder,
            FeatureToggler featureToggler
    ) {

        this.templateId = templateId;
        this.directLinkToNewEvidencePage = iaAipFrontendUrl + "home-office-evidence/addendum";
        this.recipientsFinder = recipientsFinder;
        this.featureToggler = featureToggler;
    }

    @Override
    public String getTemplateId() {
        return templateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_HOME_OFFICE_UPLOADED_ADDENDUM_EVIDENCE_AIP_APPELLANT_SMS";
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return featureToggler.getValue("aip-upload-addendum-evidence-feature", false)
                ? recipientsFinder.findAll(asylumCase, NotificationType.SMS)
                : Collections.emptySet();
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
                .<String, String>builder()
                .put("Appeal Ref Number", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("Direct link to new evidence page", directLinkToNewEvidencePage)
                .build();
    }
}