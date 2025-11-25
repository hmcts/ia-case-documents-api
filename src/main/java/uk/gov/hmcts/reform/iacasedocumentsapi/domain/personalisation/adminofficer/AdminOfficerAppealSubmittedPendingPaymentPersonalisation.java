package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.adminofficer;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailNotificationPersonalisation;

@Service
public class AdminOfficerAppealSubmittedPendingPaymentPersonalisation implements EmailNotificationPersonalisation {

    private final String adminOfficerAppealSubmittedPendingPaymentTemplateId;
    private final String feesAdminOfficerEmailAddress;
    private final String paymentExceptionsAdminOfficerEmailAddress;
    private final AdminOfficerPersonalisationProvider adminOfficerPersonalisationProvider;


    public AdminOfficerAppealSubmittedPendingPaymentPersonalisation(
        @NotNull(message = "pendingPaymentAdminOfficerTemplateId cannot be null")
        @Value("${govnotify.template.appealSubmitted.adminOfficer.pendingPaymentEaHu.email}")
            String adminOfficerAppealSubmittedPendingPaymentTemplateId,
        @Value("${feesAdminOfficerEmailAddress}")
            String feesAdminOfficerEmailAddress,
        @Value("${paymentExceptionsAdminOfficerEmailAddress}")
            String paymentExceptionsAdminOfficerEmailAddress,
        AdminOfficerPersonalisationProvider adminOfficerPersonalisationProvider
    ) {
        this.adminOfficerAppealSubmittedPendingPaymentTemplateId = adminOfficerAppealSubmittedPendingPaymentTemplateId;
        this.feesAdminOfficerEmailAddress = feesAdminOfficerEmailAddress;
        this.paymentExceptionsAdminOfficerEmailAddress = paymentExceptionsAdminOfficerEmailAddress;
        this.adminOfficerPersonalisationProvider = adminOfficerPersonalisationProvider;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_APPEAL_SUBMITTED_PENDING_PAYMENT_ADMIN_OFFICER";
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return adminOfficerAppealSubmittedPendingPaymentTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(feesAdminOfficerEmailAddress);
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");
        return adminOfficerPersonalisationProvider.getChangeToHearingRequirementsPersonalisation(asylumCase);
    }
}
