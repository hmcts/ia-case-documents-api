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
public class AdminOfficerDecidedOrEndedAppealPendingPayment implements EmailNotificationPersonalisation {

    private final String appealPendingPaymentTemplateId;
    private final String ctscAdminPendingPaymentEmailAddress;
    private final AdminOfficerPersonalisationProvider adminOfficerPersonalisationProvider;

    public AdminOfficerDecidedOrEndedAppealPendingPayment(@NotNull(message = "appealPendingPaymentTemplateId cannot be null") @Value("${govnotify.template.pendingPayment.ctsc.email}") String appealPendingPaymentTemplateId,
                                                          @Value("${ctscAdminPendingPaymentEmailAddress}") String ctscAdminPendingPaymentEmailAddress,
                                                          AdminOfficerPersonalisationProvider adminOfficerPersonalisationProvider) {
        this.appealPendingPaymentTemplateId = appealPendingPaymentTemplateId;
        this.ctscAdminPendingPaymentEmailAddress = ctscAdminPendingPaymentEmailAddress;
        this.adminOfficerPersonalisationProvider = adminOfficerPersonalisationProvider;
    }

    @Override
    public String getTemplateId() {
        return appealPendingPaymentTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_APPEAL_PENDING_PAYMENT_ADMIN_OFFICER";
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(ctscAdminPendingPaymentEmailAddress);
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {

        requireNonNull(asylumCase, "asylumCase must not be null");
        return adminOfficerPersonalisationProvider.getDefaultPersonalisation(asylumCase);
    }
}
