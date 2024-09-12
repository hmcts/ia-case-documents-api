package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.MakeAnApplication;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.AppealService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.MakeAnApplicationService;

@Service
public class CaseOfficerMakeAnApplicationPersonalisation implements EmailNotificationPersonalisation {

    private final String makeAnApplicationCaseOfficerBeforeListingTemplateId;
    private final String makeAnApplicationCaseOfficerAfterListingTemplateId;

    private final String makeAnApplicationCaseOfficerJudgeReviewBeforeListingTemplateId;
    private final String makeAnApplicationCaseOfficerJudgeReviewAfterListingTemplateId;

    private final String iaExUiFrontendUrl;
    private final EmailAddressFinder emailAddressFinder;
    private final AppealService appealService;
    private final MakeAnApplicationService makeAnApplicationService;
    private final FeatureToggler featureToggler;

    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;

    public CaseOfficerMakeAnApplicationPersonalisation(
            @Value("${govnotify.template.makeAnApplication.beforeListing.caseOfficer.other.email}") String makeAnApplicationCaseOfficerBeforeListingTemplateId,
            @Value("${govnotify.template.makeAnApplication.afterListing.caseOfficer.other.email}") String makeAnApplicationCaseOfficerAfterListingTemplateId,
            @Value("${govnotify.template.makeAnApplication.beforeListing.caseOfficer.judgeReview.email}") String makeAnApplicationCaseOfficerJudgeReviewBeforeListingTemplateId,
            @Value("${govnotify.template.makeAnApplication.afterListing.caseOfficer.judgeReview.email}") String makeAnApplicationCaseOfficerJudgeReviewAfterListingTemplateId,

            @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
            EmailAddressFinder emailAddressFinder,
            AppealService appealService,
            MakeAnApplicationService makeAnApplicationService,
            FeatureToggler featureToggler) {
        this.featureToggler = featureToggler;
        requireNonNull(iaExUiFrontendUrl, "iaExUiFrontendUrl must not be null");

        this.makeAnApplicationCaseOfficerBeforeListingTemplateId = makeAnApplicationCaseOfficerBeforeListingTemplateId;
        this.makeAnApplicationCaseOfficerAfterListingTemplateId = makeAnApplicationCaseOfficerAfterListingTemplateId;
        this.makeAnApplicationCaseOfficerJudgeReviewBeforeListingTemplateId = makeAnApplicationCaseOfficerJudgeReviewBeforeListingTemplateId;
        this.makeAnApplicationCaseOfficerJudgeReviewAfterListingTemplateId = makeAnApplicationCaseOfficerJudgeReviewAfterListingTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.emailAddressFinder = emailAddressFinder;
        this.appealService = appealService;
        this.makeAnApplicationService = makeAnApplicationService;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        boolean isAppealListed = appealService.isAppealListed(asylumCase);
        String applicationType = makeAnApplicationService.getMakeAnApplication(asylumCase, false).map(MakeAnApplication::getType).orElse("");
        boolean isJudgeReviewApplicationType = applicationType.equals("Judge's review of application decision")
            || applicationType.equals("Judge's review of Legal Officer decision");
        if (isAppealListed) {
            return isJudgeReviewApplicationType ? makeAnApplicationCaseOfficerJudgeReviewAfterListingTemplateId : makeAnApplicationCaseOfficerAfterListingTemplateId;
        } else {
            return isJudgeReviewApplicationType ? makeAnApplicationCaseOfficerJudgeReviewBeforeListingTemplateId : makeAnApplicationCaseOfficerBeforeListingTemplateId;
        }

    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return featureToggler.getValue("tcw-application-notifications-feature", true)
                ? Collections.singleton(emailAddressFinder.getHearingCentreEmailAddress(asylumCase))
                : Collections.emptySet();
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_MAKE_AN_APPLICATION_CASE_OFFICER";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return
            ImmutableMap
                .<String, String>builder()
                .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix)
                .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("ariaListingReference", asylumCase.read(ARIA_LISTING_REFERENCE, String.class).orElse(""))
                .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("linkToOnlineService", iaExUiFrontendUrl)
                .build();
    }
}
