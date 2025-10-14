package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.legalrepresentative;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.CURRENT_CASE_STATE_VISIBLE_TO_LEGAL_REPRESENTATIVE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.PersonalisationProvider;

@Service
public class LegalRepresentativeChangeDirectionDueDatePersonalisation implements LegalRepresentativeEmailNotificationPersonalisation {

    private static final String legalRepChangeDirectionDueDateSuffix = "_LEGAL_REP_CHANGE_DIRECTION_DUE_DATE";

    private final String legalRepChangeDirectionDueDateAfterListingTemplateId;
    private final String legalRepChangeDirectionDueDateBeforeListingTemplateId;
    private final String iaExUiFrontendUrl;
    private final PersonalisationProvider personalisationProvider;
    private final CustomerServicesProvider customerServicesProvider;

    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;

    public LegalRepresentativeChangeDirectionDueDatePersonalisation(
        @Value("${govnotify.template.changeDirectionDueDate.legalRep.afterListing.email}") String legalRepChangeDirectionDueDateAfterListingTemplateId,
        @Value("${govnotify.template.changeDirectionDueDate.legalRep.beforeListing.email}") String legalRepChangeDirectionDueDateBeforeListingTemplateId,
        @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
        PersonalisationProvider personalisationProvider,
        CustomerServicesProvider customerServicesProvider
    ) {
        this.legalRepChangeDirectionDueDateAfterListingTemplateId = legalRepChangeDirectionDueDateAfterListingTemplateId;
        this.legalRepChangeDirectionDueDateBeforeListingTemplateId = legalRepChangeDirectionDueDateBeforeListingTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.personalisationProvider = personalisationProvider;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {

        return asylumCase.read(CURRENT_CASE_STATE_VISIBLE_TO_LEGAL_REPRESENTATIVE, State.class)
            .map(s -> {
                if (Arrays.asList(
                    State.APPEAL_SUBMITTED,
                    State.PENDING_PAYMENT,
                    State.APPEAL_SUBMITTED_OUT_OF_TIME,
                    State.AWAITING_RESPONDENT_EVIDENCE,
                    State.CASE_BUILDING,
                    State.CASE_UNDER_REVIEW,
                    State.RESPONDENT_REVIEW,
                    State.SUBMIT_HEARING_REQUIREMENTS
                ).contains(s)) {
                    return legalRepChangeDirectionDueDateBeforeListingTemplateId;
                }

                return legalRepChangeDirectionDueDateAfterListingTemplateId;
            })
            .orElseThrow(() -> new IllegalStateException("currentCaseStateVisibleToLegalRepresentative flag is not present"));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + legalRepChangeDirectionDueDateSuffix;
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        final ImmutableMap.Builder<String, String> listCaseFields = ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("subjectPrefix", isAcceleratedDetainedAppeal(callback.getCaseDetails().getCaseData())
                ? adaPrefix
                : nonAdaPrefix)
            .put("linkToOnlineService", iaExUiFrontendUrl)
            .putAll(personalisationProvider.getPersonalisation(callback));

        return listCaseFields.build();
    }
}
