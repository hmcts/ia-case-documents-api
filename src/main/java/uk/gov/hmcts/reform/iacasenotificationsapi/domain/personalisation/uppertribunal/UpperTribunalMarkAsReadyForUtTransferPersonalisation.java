package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.uppertribunal;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@Service
public class UpperTribunalMarkAsReadyForUtTransferPersonalisation implements EmailNotificationPersonalisation {

    private final String markReadyForUtTransferBeforeListingUpperTribunalTemplateId;
    private final String markReadyForUtTransferAfterListingUpperTribunalTemplateId;
    private final String iaExUiFrontendUrl;
    private final CustomerServicesProvider customerServicesProvider;
    private final String upperTribunalUrgentAppealsEmailAddress;

    public UpperTribunalMarkAsReadyForUtTransferPersonalisation(
        @NotNull(message = "markReadyForUtTransferBeforeListingUpperTribunalTemplateId cannot be null")
        @Value("${govnotify.template.markAsReadyForUtTransfer.upperTribunal.beforeListing.email}") String markReadyForUtTransferBeforeListingUpperTribunalTemplateId,
        @NotNull(message = "markReadyForUtTransferAfterListingUpperTribunalTemplateId cannot be null")
        @Value("${govnotify.template.markAsReadyForUtTransfer.upperTribunal.afterListing.email}") String markReadyForUtTransferAfterListingUpperTribunalTemplateId,
        @Value("${upperTribunalUrgentAppealsEmailAddress}") String upperTribunalUrgentAppealsEmailAddress,
        @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
        CustomerServicesProvider customerServicesProvider
    ) {
        this.markReadyForUtTransferBeforeListingUpperTribunalTemplateId = markReadyForUtTransferBeforeListingUpperTribunalTemplateId;
        this.markReadyForUtTransferAfterListingUpperTribunalTemplateId = markReadyForUtTransferAfterListingUpperTribunalTemplateId;
        this.upperTribunalUrgentAppealsEmailAddress = upperTribunalUrgentAppealsEmailAddress;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return isAppealListed(asylumCase)
            ? markReadyForUtTransferAfterListingUpperTribunalTemplateId : markReadyForUtTransferBeforeListingUpperTribunalTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(upperTribunalUrgentAppealsEmailAddress);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_MARK_AS_READY_FOR_UT_TRANSFER_UPPER_TRIBUNAL";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("ariaListingReference", asylumCase.read(AsylumCaseDefinition.ARIA_LISTING_REFERENCE, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("utAppealReferenceNumber", asylumCase.read(AsylumCaseDefinition.UT_APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("linkToOnlineService", iaExUiFrontendUrl)
            .build();
    }

    protected boolean isAppealListed(AsylumCase asylumCase) {
        final Optional<HearingCentre> appealListed = asylumCase
            .read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class);

        return appealListed.isPresent();
    }
}
