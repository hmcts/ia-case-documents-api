package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.adminofficer;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailNotificationPersonalisation;

@Service
public class AdminOfficerAppealOutcomePersonalisation implements EmailNotificationPersonalisation {

    private final String decisionAndReasonUploadedTemplateId;

    private final AdminOfficerPersonalisationProvider adminOfficerPersonalisationProvider;

    private final EmailAddressFinder emailAddressFinder;

    public AdminOfficerAppealOutcomePersonalisation(
            @NotNull(message = "decisionAndReasonUploadedTemplateId cannot be null")
            @Value("${govnotify.template.decisionAndReasonsTemplateUploaded.admin.email}") String decisionAndReasonUploadedTemplateId,
            AdminOfficerPersonalisationProvider adminOfficerPersonalisationProvider,
            EmailAddressFinder emailAddressFinder) {
        this.decisionAndReasonUploadedTemplateId = decisionAndReasonUploadedTemplateId;
        this.adminOfficerPersonalisationProvider = adminOfficerPersonalisationProvider;
        this.emailAddressFinder = emailAddressFinder;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return decisionAndReasonUploadedTemplateId;
    }


    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {

        return Collections.singleton(emailAddressFinder.getAdminEmailAddress(asylumCase));

    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_APPEAL_OUTCOME_ADMIN";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {

        requireNonNull(asylumCase, "asylumCase must not be null");

        return adminOfficerPersonalisationProvider.getAdminPersonalisation(asylumCase);
    }
}
