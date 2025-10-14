package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.email;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.ARIA_LISTING_REFERENCE;


@Service
public class AppellantUploadAdditionalEvidencePersonalisationEmail implements EmailNotificationPersonalisation {

    private final String uploadAdditionalEvidenceEmailBeforeListingNotificationTemplateId;
    private final String uploadAdditionalEvidenceEmailAfterListingNotificationTemplateId;
    private final String iaAipFrontendUrl;
    private final RecipientsFinder recipientsFinder;
    private final CustomerServicesProvider customerServicesProvider;

    public AppellantUploadAdditionalEvidencePersonalisationEmail(
            @Value("${govnotify.template.uploadedAdditionalEvidenceBeforeListing.appellant.email}") String uploadAdditionalEvidenceEmailBeforeListingNotificationTemplateId,
            @Value("${govnotify.template.uploadedAdditionalEvidenceAfterListing.appellant.email}") String uploadAdditionalEvidenceEmailAfterListingNotificationTemplateId,
            @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
            RecipientsFinder recipientsFinder,
            CustomerServicesProvider customerServicesProvider
    ) {
        this.uploadAdditionalEvidenceEmailBeforeListingNotificationTemplateId = uploadAdditionalEvidenceEmailBeforeListingNotificationTemplateId;
        this.uploadAdditionalEvidenceEmailAfterListingNotificationTemplateId = uploadAdditionalEvidenceEmailAfterListingNotificationTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.recipientsFinder = recipientsFinder;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return isAppealListed(asylumCase)
                ? uploadAdditionalEvidenceEmailAfterListingNotificationTemplateId : uploadAdditionalEvidenceEmailBeforeListingNotificationTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.EMAIL);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_UPLOADED_ADDITIONAL_EVIDENCE_AIP_APPELLANT_EMAIL";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return
                ImmutableMap
                        .<String, String>builder()
                        .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
                        .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                        .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
                        .put("ariaListingReference", asylumCase.read(ARIA_LISTING_REFERENCE, String.class).orElse(""))
                        .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                        .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
                        .put("hyperlink to service", iaAipFrontendUrl)
                        .build();
    }

    protected boolean isAppealListed(AsylumCase asylumCase) {
        final Optional<HearingCentre> appealListed = asylumCase
                .read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class);

        return appealListed.isPresent();
    }
}
