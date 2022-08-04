package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.ARIA_LISTING_REFERENCE;

import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;

@Service
public class AppellantEndAppealPersonalisationEmail implements EmailNotificationPersonalisation {

    private final String endAppealAppellantAfterListingTemplateId;
    private final String endAppealAppellantBeforeListingTemplateId;
    private final String iaAipFrontendUrl;
    private final String iaAipFrontendPathToJudgeReview;
    private final RecipientsFinder recipientsFinder;

    public AppellantEndAppealPersonalisationEmail(
            @Value("${govnotify.template.endAppealBeforeListing.appellant.email}") String endAppealAppellantBeforeListingTemplateId,
            @Value("${govnotify.template.endAppealAfterListing.appellant.email}") String endAppealAppellantAfterListingTemplateId,
            @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
            @Value("${iaAipFrontendPathToJudgeReview}") String iaAipFrontendPathToJudgeReview,
            RecipientsFinder recipientsFinder
    ) {
        this.endAppealAppellantBeforeListingTemplateId = endAppealAppellantBeforeListingTemplateId;
        this.endAppealAppellantAfterListingTemplateId = endAppealAppellantAfterListingTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.iaAipFrontendPathToJudgeReview = iaAipFrontendPathToJudgeReview;
        this.recipientsFinder = recipientsFinder;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return isAppealListed(asylumCase)
                ? endAppealAppellantAfterListingTemplateId : endAppealAppellantBeforeListingTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.EMAIL);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_END_APPEAL_AIP_APPELLANT_EMAIL";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return
                ImmutableMap
                        .<String, String>builder()
                        .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                        .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
                        .put("ariaListingReference", asylumCase.read(ARIA_LISTING_REFERENCE, String.class).orElse(""))
                        .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                        .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
                        .put("endAppealApprover", asylumCase.read(AsylumCaseDefinition.END_APPEAL_APPROVER_TYPE, String.class).orElse(""))
                        .put("endAppealDate", asylumCase.read(AsylumCaseDefinition.END_APPEAL_DATE, String.class)
                                .map(date -> LocalDate.parse(date).format(DateTimeFormatter.ofPattern("d MMM yyyy")))
                                .orElse(""))
                        .put("outcomeOfAppeal", asylumCase.read(AsylumCaseDefinition.END_APPEAL_OUTCOME, String.class).orElse(""))
                        .put("reasonsOfOutcome", asylumCase.read(AsylumCaseDefinition.END_APPEAL_OUTCOME_REASON, String.class).orElse(""))
                        .put("Hyperlink to service", iaAipFrontendUrl)
                        .put("direct link to judges’ review page", iaAipFrontendUrl + iaAipFrontendPathToJudgeReview)
                        .build();
    }

    protected boolean isAppealListed(AsylumCase asylumCase) {
        final Optional<HearingCentre> appealListed = asylumCase
                .read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class);

        return appealListed.isPresent();
    }

}
