package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.legalrepresentative;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.REMISSION_TYPE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.RemissionType.HELP_WITH_FEES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.RemissionType.HO_WAIVER_REMISSION;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.RemissionType.NO_REMISSION;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;


@Service
public class LegalRepresentativeAppealSubmittedPendingPaymentPersonalisation
    implements LegalRepresentativeEmailNotificationPersonalisation {

    private final String legalRepAppealSubmittedPendingPaymentTemplateId;
    private final String legalRepAppealSubmittedPendingPaymentWithRemissionTemplateId;
    private final String iaExUiFrontendUrl;
    private final CustomerServicesProvider customerServicesProvider;

    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;

    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;

    public LegalRepresentativeAppealSubmittedPendingPaymentPersonalisation(
        @NotNull(message = "pendingPaymentLegalRepTemplateId cannot be null")
        @Value("${govnotify.template.appealSubmitted.legalRep.pendingPayment.email}")
            String legalRepAppealSubmittedPendingPaymentTemplateId,
        @NotNull(message = "pendingPaymentLegalRepWithRemissionTemplateId cannot be null")
        @Value("${govnotify.template.appealSubmitted.legalRep.remission.email}")
            String legalRepAppealSubmittedPendingPaymentWithRemissionTemplateId,
        @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
        CustomerServicesProvider customerServicesProvider
    ) {
        this.legalRepAppealSubmittedPendingPaymentTemplateId = legalRepAppealSubmittedPendingPaymentTemplateId;
        this.legalRepAppealSubmittedPendingPaymentWithRemissionTemplateId =
            legalRepAppealSubmittedPendingPaymentWithRemissionTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_APPEAL_SUBMITTED_PENDING_PAYMENT_LEGAL_REP";
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        RemissionType remissionType = asylumCase
            .read(REMISSION_TYPE, RemissionType.class).orElse(NO_REMISSION);

        if (Arrays.asList(HO_WAIVER_REMISSION, HELP_WITH_FEES, EXCEPTIONAL_CIRCUMSTANCES_REMISSION)
            .contains(remissionType)) {
            return legalRepAppealSubmittedPendingPaymentWithRemissionTemplateId;
        }
        return legalRepAppealSubmittedPendingPaymentTemplateId;
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");
        return ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix)
            .put("appealReferenceNumber",
                asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("legalRepReferenceNumber",
                asylumCase.read(AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames",
                asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName",
                asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("linkToOnlineService", iaExUiFrontendUrl)
            .build();
    }
}
