package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@Service
public class CaseOfficerHearingBundleFailedPersonalisation implements EmailNotificationPersonalisation {


    private final String hearingBundleFailedCaseOfficerTemplateId;
    private final String iaExUiFrontendUrl;
    private final CustomerServicesProvider customerServicesProvider;
    private final Map<HearingCentre, String> hearingCentreEmailAddresses;

    public CaseOfficerHearingBundleFailedPersonalisation(
        @Value("${govnotify.template.hearingBundleFailed.caseOfficer.email}") String hearingBundleFailedCaseOfficerTemplateId,
        @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
        CustomerServicesProvider customerServicesProvider,
        Map<HearingCentre, String> hearingCentreEmailAddresses
    ) {
        this.hearingBundleFailedCaseOfficerTemplateId = hearingBundleFailedCaseOfficerTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.customerServicesProvider = customerServicesProvider;
        this.hearingCentreEmailAddresses = hearingCentreEmailAddresses;
    }

    @Override
    public String getTemplateId() {
        return hearingBundleFailedCaseOfficerTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        final HearingCentre listCaseHearingCentre =
            asylumCase
                .read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)
                .orElseThrow(() -> new IllegalStateException("listCaseHearingCentre is not present"));

        final String hearingCentreEmailAddress =
            hearingCentreEmailAddresses
                .get(listCaseHearingCentre);

        if (hearingCentreEmailAddress == null) {
            throw new IllegalStateException("Hearing centre email address not found: " + listCaseHearingCentre.toString());
        }

        return Collections.singleton(hearingCentreEmailAddress);

    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_HEARING_BUNDLE_FAILED_CASE_OFFICER";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("ariaListingReference", asylumCase.read(ARIA_LISTING_REFERENCE, String.class).orElse(""))
            .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("linkToOnlineService", iaExUiFrontendUrl)
            .build();
    }
}
