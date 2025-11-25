package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.adminofficer;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.REMISSION_TYPE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.RemissionType.HELP_WITH_FEES;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.RemissionType.HO_WAIVER_REMISSION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.RemissionType.NO_REMISSION;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.RemissionType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailNotificationPersonalisation;


@Service
public class AdminOfficerAppealSubmittedPayOfflinePersonalisation implements EmailNotificationPersonalisation {

    private final String caseAdminOfficerAppealSubmittedPayOfflineTemplateId;
    private final String reviewHearingRequirementsAdminOfficerEmailAddress;
    private final String paymentExceptionsAdminOfficerEmailAddress;
    private final AdminOfficerPersonalisationProvider adminOfficerPersonalisationProvider;

    public AdminOfficerAppealSubmittedPayOfflinePersonalisation(
        @NotNull(message = "reListCaseAdminOfficerTemplateId cannot be null")
        @Value("${govnotify.template.appealSubmitted.adminOfficer.pendingPaymentPa.email}")
            String caseAdminOfficerAppealSubmittedPayOfflineTemplateId,
        @Value("${feesAdminOfficerEmailAddress}")
            String feesAdminOfficerEmailAddress,
        @Value("${paymentExceptionsAdminOfficerEmailAddress}")
            String paymentExceptionsAdminOfficerEmailAddress,
        AdminOfficerPersonalisationProvider adminOfficerPersonalisationProvider
    ) {
        this.caseAdminOfficerAppealSubmittedPayOfflineTemplateId = caseAdminOfficerAppealSubmittedPayOfflineTemplateId;
        this.reviewHearingRequirementsAdminOfficerEmailAddress = feesAdminOfficerEmailAddress;
        this.paymentExceptionsAdminOfficerEmailAddress = paymentExceptionsAdminOfficerEmailAddress;
        this.adminOfficerPersonalisationProvider = adminOfficerPersonalisationProvider;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_APPEAL_SUBMITTED_PAY_OFFLINE_ADMIN_OFFICER";
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return caseAdminOfficerAppealSubmittedPayOfflineTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        RemissionType remissionType = asylumCase
            .read(REMISSION_TYPE, RemissionType.class).orElse(NO_REMISSION);

        if (Arrays.asList(HO_WAIVER_REMISSION, HELP_WITH_FEES, EXCEPTIONAL_CIRCUMSTANCES_REMISSION)
            .contains(remissionType)) {
            return Collections.singleton(paymentExceptionsAdminOfficerEmailAddress);
        }
        return Collections.singleton(reviewHearingRequirementsAdminOfficerEmailAddress);
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");
        return adminOfficerPersonalisationProvider.getChangeToHearingRequirementsPersonalisation(asylumCase);

    }
}
