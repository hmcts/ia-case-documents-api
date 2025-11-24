package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAppealListed;

import com.google.common.collect.ImmutableMap;
import java.util.*;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@Service
public class HomeOfficeMarkAppealReadyForUtTransferPersonalisation implements EmailNotificationPersonalisation {

    private final String markReadyForUtTransferBeforeListingTemplateId;
    private final String markReadyForUtTransferAfterListingTemplateId;
    private final String iaExUiFrontendUrl;
    private final CustomerServicesProvider customerServicesProvider;
    private final EmailAddressFinder emailAddressFinder;
    private final String respondentEmailAddressUntilRespondentReview;
    private final String respondentEmailAddressAtRespondentReview;
    private final String endAppealEmailAddresses;

    private final PersonalisationProvider personalisationProvider;
    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;

    public HomeOfficeMarkAppealReadyForUtTransferPersonalisation(
            @NotNull(message = "markReadyForUtTransferBeforeListingTemplateId cannot be null")
            @Value("${govnotify.template.markAsReadyForUtTransfer.homeOffice.beforeListing.email}") String markReadyForUtTransferBeforeListingTemplateId,
            @NotNull(message = "markReadyForUtTransferAfterListingTemplateId cannot be null")
            @Value("${govnotify.template.markAsReadyForUtTransfer.homeOffice.afterListing.email}") String markReadyForUtTransferAfterListingTemplateId,
            @Value("${respondentEmailAddresses.nonStandardDirectionUntilListing}")
            String respondentEmailAddressUntilRespondentReview,
            @Value("${respondentEmailAddresses.respondentReviewDirection}") String respondentEmailAddressAtRespondentReview,
            @Value("${endAppealHomeOfficeEmailAddress}") String endAppealEmailAddresses,
            @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
            EmailAddressFinder emailAddressFinder,
            PersonalisationProvider personalisationProvider,
            CustomerServicesProvider customerServicesProvider

    ) {
        this.markReadyForUtTransferBeforeListingTemplateId = markReadyForUtTransferBeforeListingTemplateId;
        this.markReadyForUtTransferAfterListingTemplateId = markReadyForUtTransferAfterListingTemplateId;
        this.respondentEmailAddressUntilRespondentReview = respondentEmailAddressUntilRespondentReview;
        this.respondentEmailAddressAtRespondentReview = respondentEmailAddressAtRespondentReview;
        this.endAppealEmailAddresses = endAppealEmailAddresses;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.emailAddressFinder = emailAddressFinder;
        this.personalisationProvider = personalisationProvider;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {

        return isAppealListed(asylumCase)
                ? markReadyForUtTransferAfterListingTemplateId : markReadyForUtTransferBeforeListingTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {

        if (AsylumCaseUtils.isAipJourney(asylumCase)) {
            return (isAppealListed(asylumCase))
                ? Collections.singleton(emailAddressFinder.getListCaseHomeOfficeEmailAddress(asylumCase)) :
                Collections.singleton(endAppealEmailAddresses);
        } else {
            return Collections.singleton(getRespondentEmailAddress(asylumCase));
        }
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_MARK_AS_READY_FOR_UT_TRANSFER_HOME_OFFICE";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
                .<String, String>builder()
                .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix)
                .put("utAppealReferenceNumber", asylumCase.read(AsylumCaseDefinition.UT_APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
                .putAll(personalisationProvider.getRespondentHeaderPersonalisation(asylumCase))
                .put("linkToOnlineService", iaExUiFrontendUrl)
                .build();
    }

    private String getRespondentEmailAddress(AsylumCase asylumCase) {
        return asylumCase.read(STATE_BEFORE_END_APPEAL, State.class)
                .map(s -> {
                    if (Arrays.asList(
                        State.APPEAL_SUBMITTED,
                        State.PENDING_PAYMENT,
                        State.AWAITING_RESPONDENT_EVIDENCE,
                        State.CASE_BUILDING,
                        State.CASE_UNDER_REVIEW
                    ).contains(s)) {
                        return respondentEmailAddressUntilRespondentReview;
                    } else if (Arrays.asList(
                        State.RESPONDENT_REVIEW,
                        State.SUBMIT_HEARING_REQUIREMENTS,
                        State.LISTING
                    ).contains(s)) {
                        return respondentEmailAddressAtRespondentReview;
                    }

                    return emailAddressFinder.getListCaseHomeOfficeEmailAddress(asylumCase);
                })
                .orElseThrow(() -> new IllegalStateException("currentCaseStateVisibleToHomeOfficeAll flag is not present"));
    }
}
