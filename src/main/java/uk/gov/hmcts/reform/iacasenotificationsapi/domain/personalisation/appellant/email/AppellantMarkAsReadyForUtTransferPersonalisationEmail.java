package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
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
public class AppellantMarkAsReadyForUtTransferPersonalisationEmail implements EmailNotificationPersonalisation {

    private final String markReadyForUtTransferBeforeListingAppellantTemplateId;
    private final String markReadyForUtTransferAfterListingAppellantTemplateId;
    private final RecipientsFinder recipientsFinder;

    public AppellantMarkAsReadyForUtTransferPersonalisationEmail(
        @Value("${govnotify.template.markAsReadyForUtTransfer.appellant.email.beforeListing}") String markReadyForUtTransferBeforeListingAppellantTemplateId,
        @Value("${govnotify.template.markAsReadyForUtTransfer.appellant.email.afterListing}") String markReadyForUtTransferAfterListingAppellantTemplateId,
        RecipientsFinder recipientsFinder
    ) {
        this.markReadyForUtTransferBeforeListingAppellantTemplateId = markReadyForUtTransferBeforeListingAppellantTemplateId;
        this.markReadyForUtTransferAfterListingAppellantTemplateId = markReadyForUtTransferAfterListingAppellantTemplateId;
        this.recipientsFinder = recipientsFinder;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return isAppealListed(asylumCase)
            ? markReadyForUtTransferAfterListingAppellantTemplateId : markReadyForUtTransferBeforeListingAppellantTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.EMAIL);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_MARK_AS_READY_FOR_UT_TRANSFER_APPELLANT_EMAIL";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return
            ImmutableMap
                .<String, String>builder()
                .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("ariaListingReference", asylumCase.read(AsylumCaseDefinition.ARIA_LISTING_REFERENCE, String.class).orElse(""))
                .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
                .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("utAppealReferenceNumber", asylumCase.read(AsylumCaseDefinition.UT_APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .build();
    }

    protected boolean isAppealListed(AsylumCase asylumCase) {
        final Optional<HearingCentre> appealListed = asylumCase
            .read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class);

        return appealListed.isPresent();
    }

}
