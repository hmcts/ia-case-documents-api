package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.email;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.collect.ImmutableMap;
import java.util.*;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailNotificationPersonalisation;

@Service
public class AppellantRemoveDetainedStatusPersonalisationEmail implements EmailNotificationPersonalisation {

    private final String appellantRemoveDetainedStatusPersonalisationBeforeListingEmailTemplateId;
    private final String appellantRemoveDetainedStatusPersonalisationAfterListingEmailTemplateId;

    public AppellantRemoveDetainedStatusPersonalisationEmail(
            @NotNull(message = "appellantRemoveDetainedStatusPersonalisationBeforeListingEmailTemplateId cannot be null")
            @Value("${govnotify.template.removeDetentionStatus.appellant.email.beforeListing}") String appellantRemoveDetainedStatusPersonalisationBeforeListingEmailTemplateId,
            @NotNull(message = "appellantRemoveDetainedStatusPersonalisationAfterListingEmailTemplateId cannot be null")
            @Value("${govnotify.template.removeDetentionStatus.appellant.email.afterListing}") String appellantRemoveDetainedStatusPersonalisationAfterListingEmailTemplateId
    ) {
        this.appellantRemoveDetainedStatusPersonalisationBeforeListingEmailTemplateId = appellantRemoveDetainedStatusPersonalisationBeforeListingEmailTemplateId;
        this.appellantRemoveDetainedStatusPersonalisationAfterListingEmailTemplateId = appellantRemoveDetainedStatusPersonalisationAfterListingEmailTemplateId;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        String ariaListingReference = asylumCase.read(ARIA_LISTING_REFERENCE, String.class).orElse("");
        return ariaListingReference.isBlank()
                ? appellantRemoveDetainedStatusPersonalisationBeforeListingEmailTemplateId :
                appellantRemoveDetainedStatusPersonalisationAfterListingEmailTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        Set<String> recipients = new HashSet<>();

        Optional<List<String>> contactPreference = asylumCase.read(CONTACT_PREFERENCE_UN_REP);
        if (!contactPreference.isPresent() || !contactPreference.get().contains(ContactPreference.WANTS_EMAIL.getValue())) {
            return recipients;
        }

        String emailAddress = asylumCase.read(EMAIL, String.class)
                .orElseThrow(() -> new RequiredFieldMissingException("Email address not found"));

        recipients.add(emailAddress);
        return recipients;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_REMOVE_DETENTION_STATUS_APPELLANT_EMAIL";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");
        String ariaListingReference = asylumCase.read(ARIA_LISTING_REFERENCE, String.class).orElse("");
        return ImmutableMap
                .<String, String>builder()
                .put("ariaListingReference", ariaListingReference)
                .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("homeOfficeReferenceNumber", asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
                .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .build();
    }
}
