package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;

@Service
public class AdminOfficerEditPaymentMethodPersonalisation implements EmailNotificationPersonalisation {

    private final String adminOfficerEditPaymentMethodEaHuTemplateId;
    private final String adminOfficerEditPaymentMethodPaTemplateId;
    private final String feesAdminOfficerEmailAddress;
    private final AdminOfficerPersonalisationProvider adminOfficerPersonalisationProvider;

    public AdminOfficerEditPaymentMethodPersonalisation(
        @NotNull(message = "adminOfficerEditPaymentMethodPendingPaymentTemplateId cannot be null")
        @Value("${govnotify.template.appealSubmitted.adminOfficer.pendingPaymentEaHu.email}") String adminOfficerEditPaymentMethodEaHuTemplateId,
        @Value("${govnotify.template.editPaymentMethod.adminOfficer.email}") String adminOfficerEditPaymentMethodPaTemplateId,
        @Value("${feesAdminOfficerEmailAddress}") String feesAdminOfficerEmailAddress,
        AdminOfficerPersonalisationProvider adminOfficerPersonalisationProvider
    ) {
        this.adminOfficerEditPaymentMethodEaHuTemplateId = adminOfficerEditPaymentMethodEaHuTemplateId;
        this.adminOfficerEditPaymentMethodPaTemplateId = adminOfficerEditPaymentMethodPaTemplateId;
        this.feesAdminOfficerEmailAddress = feesAdminOfficerEmailAddress;
        this.adminOfficerPersonalisationProvider = adminOfficerPersonalisationProvider;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_EDIT_PAYMENT_METHOD_PENDING_PAYMENT_ADMIN_OFFICER";
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return asylumCase.read(AsylumCaseDefinition.APPEAL_TYPE, AppealType.class).equals(Optional.of(AppealType.PA))
            ? adminOfficerEditPaymentMethodPaTemplateId
            : adminOfficerEditPaymentMethodEaHuTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(feesAdminOfficerEmailAddress);
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");
        return adminOfficerPersonalisationProvider.getDefaultPersonalisation(asylumCase);
    }
}
