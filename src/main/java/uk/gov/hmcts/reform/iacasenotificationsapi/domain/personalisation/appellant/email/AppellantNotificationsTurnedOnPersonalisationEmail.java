package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.ARIA_LISTING_REFERENCE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.EMAIL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isLegalRepEjp;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.PinInPostDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@Service
public class AppellantNotificationsTurnedOnPersonalisationEmail implements EmailNotificationPersonalisation {

    private final String representedAppellantTransferredToFirstTierEmailTemplateId;
    private final String unrepresentedAppellantTransferredToFirstTierEmailTemplateId;
    private final CustomerServicesProvider customerServicesProvider;
    private final String iaExUiFrontendUrl;

    public AppellantNotificationsTurnedOnPersonalisationEmail(
        @Value("${govnotify.template.turnOnNotifications.appellant.represented.email}") String representedAppellantTransferredToFirstTierEmailTemplateId,
        @Value("${govnotify.template.turnOnNotifications.appellant.unrepresented.email}") String unrepresentedAppellantTransferredToFirstTierEmailTemplateId,
        @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
        CustomerServicesProvider customerServicesProvider
    ) {
        this.representedAppellantTransferredToFirstTierEmailTemplateId =
            representedAppellantTransferredToFirstTierEmailTemplateId;
        this.unrepresentedAppellantTransferredToFirstTierEmailTemplateId =
            unrepresentedAppellantTransferredToFirstTierEmailTemplateId;
        this.customerServicesProvider = customerServicesProvider;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        if (isLegalRepEjp(asylumCase)) {
            return representedAppellantTransferredToFirstTierEmailTemplateId;
        }

        return unrepresentedAppellantTransferredToFirstTierEmailTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_APPELLANT_NOTIFICATIONS_TURNED_ON";
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return asylumCase.read(EMAIL, String.class)
            .map(email -> Collections.singleton(email))
            .orElse(Collections.emptySet());
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        final String dateOfBirth = asylumCase
            .read(AsylumCaseDefinition.APPELLANT_DATE_OF_BIRTH, String.class)
            .orElseThrow(() -> new IllegalStateException("Appellant's birth of date is not present"));

        final String formattedDateOfBirth = LocalDate.parse(dateOfBirth).format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        String listingReferenceLine = asylumCase.read(ARIA_LISTING_REFERENCE, String.class)
            .map(ref -> "\nListing reference: " + ref)
            .orElse("");

        ImmutableMap.Builder<String, String> personalizationBuilder = ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("listingReferenceLine", listingReferenceLine)
            .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("ccdReferenceNumberForDisplay", asylumCase.read(AsylumCaseDefinition.CCD_REFERENCE_NUMBER_FOR_DISPLAY, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("dateOfBirth", formattedDateOfBirth)
            .put("linkToOnlineService", iaExUiFrontendUrl);

        PinInPostDetails pip = AsylumCaseUtils.generateAppellantPinIfNotPresent(asylumCase);
        personalizationBuilder.put("securityCode", pip.getAccessCode());
        personalizationBuilder.put("validDate", defaultDateFormat(pip.getExpiryDate()));

        return personalizationBuilder.build();
    }
}
