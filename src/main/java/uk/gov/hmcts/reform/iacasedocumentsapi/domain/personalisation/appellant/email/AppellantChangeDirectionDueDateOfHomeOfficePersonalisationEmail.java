package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.email;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.AppealService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.PersonalisationProvider;


@Service
public class AppellantChangeDirectionDueDateOfHomeOfficePersonalisationEmail implements EmailNotificationPersonalisation {

    private final String afterListingTemplateId;
    private final String beforeListingTemplateId;
    private final AppealService appealService;
    private final PersonalisationProvider personalisationProvider;
    private final RecipientsFinder recipientsFinder;
    private final CustomerServicesProvider customerServicesProvider;

    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;

    public AppellantChangeDirectionDueDateOfHomeOfficePersonalisationEmail(
        @Value("${govnotify.template.changeDirectionDueDateOfHomeOffice.appellant.email.afterListing}") String afterListingTemplateId,
        @Value("${govnotify.template.changeDirectionDueDateOfHomeOffice.appellant.email.beforeListing}") String beforeListingTemplateId,
        PersonalisationProvider personalisationProvider,
        RecipientsFinder recipientsFinder,
        AppealService appealService,
        CustomerServicesProvider customerServicesProvider
    ) {
        this.afterListingTemplateId = afterListingTemplateId;
        this.beforeListingTemplateId = beforeListingTemplateId;
        this.personalisationProvider = personalisationProvider;
        this.recipientsFinder = recipientsFinder;
        this.appealService = appealService;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {

        boolean isAppealListed = appealService.isAppealListed(asylumCase);

        if (isAppealListed) {
            return afterListingTemplateId;
        } else {
            return beforeListingTemplateId;
        }
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.EMAIL);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_APPELLANT_CHANGE_DIRECTION_DUE_DATE_OF_HOME_OFFICE_EMAIL";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");
        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        final ImmutableMap.Builder<String, String> listCaseFields = ImmutableMap
            .<String, String>builder()
                .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix)
            .putAll(personalisationProvider.getPersonalisation(callback));

        return listCaseFields.build();
    }
}
