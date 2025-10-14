package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.respondent;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

import com.google.common.collect.ImmutableMap;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@Service
public class RespondentEditAppealAfterSubmitPersonalisation implements EmailNotificationPersonalisation {

    private final String respondentEditAppealAfterSubmitBeforeListingTemplateId;
    private final String respondentEditAppealAfterSubmitAfterListingTemplateId;
    private final String respondentEmailAddressUntilRespondentReview;
    private final String respondentEmailAddressAtRespondentReview;
    private final EmailAddressFinder respondentEmailAddressAfterRespondentReview;
    private final String iaExUiFrontendUrl;
    private final CustomerServicesProvider customerServicesProvider;

    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;

    public RespondentEditAppealAfterSubmitPersonalisation(
        @Value("${govnotify.template.editAppealAfterSubmitBeforeListing.respondent.email}")
            String respondentEditAppealAfterSubmitBeforeListingTemplateId,
        @Value("${govnotify.template.editAppealAfterSubmitAfterListing.respondent.email}")
            String respondentEditAppealAfterSubmitAfterListingTemplateId,
        @Value("${respondentEmailAddresses.nonStandardDirectionUntilListing}")
            String respondentEmailAddressUntilRespondentReview,
        @Value("${respondentEmailAddresses.respondentReviewDirection}") String respondentEmailAddressAtRespondentReview,
        EmailAddressFinder respondentEmailAddressAfterRespondentReview,
        @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
        CustomerServicesProvider customerServicesProvider) {

        this.respondentEditAppealAfterSubmitBeforeListingTemplateId = respondentEditAppealAfterSubmitBeforeListingTemplateId;
        this.respondentEditAppealAfterSubmitAfterListingTemplateId = respondentEditAppealAfterSubmitAfterListingTemplateId;
        this.respondentEmailAddressUntilRespondentReview = respondentEmailAddressUntilRespondentReview;
        this.respondentEmailAddressAtRespondentReview = respondentEmailAddressAtRespondentReview;
        this.respondentEmailAddressAfterRespondentReview = respondentEmailAddressAfterRespondentReview;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return isAppealListed(asylumCase) ? respondentEditAppealAfterSubmitAfterListingTemplateId
            : respondentEditAppealAfterSubmitBeforeListingTemplateId;
    }

    protected boolean isAppealListed(AsylumCase asylumCase) {
        final Optional<HearingCentre> appealListed = asylumCase
            .read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class);

        return appealListed.isPresent();
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(getRespondentEmailAddress(asylumCase));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_EDIT_APPEAL_AFTER_SUBMIT_RESPONDENT";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
            .<String, String>builder()
            .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix)
            .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("ariaListingReference", asylumCase.read(ARIA_LISTING_REFERENCE, String.class).orElse(""))
            .put("homeOfficeReference", asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("linkToOnlineService", iaExUiFrontendUrl)
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .build();
    }

    private String getRespondentEmailAddress(AsylumCase asylumCase) {

        return asylumCase.read(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class)
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

                return respondentEmailAddressAfterRespondentReview.getListCaseHomeOfficeEmailAddress(asylumCase);
            })
            .orElseThrow(() -> new IllegalStateException("currentCaseStateVisibleToHomeOfficeAll flag is not present"));
    }
}
