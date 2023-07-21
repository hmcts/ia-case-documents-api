package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;

@Service
@Slf4j
public class CaseOfficerHearingBundleFailedPersonalisation implements EmailNotificationPersonalisation {


    private final String hearingBundleFailedCaseOfficerTemplateId;
    private final String iaExUiFrontendUrl;
    private final CustomerServicesProvider customerServicesProvider;
    private final EmailAddressFinder emailAddressFinder;

    public CaseOfficerHearingBundleFailedPersonalisation(
            @Value("${govnotify.template.hearingBundleFailed.caseOfficer.email}") String hearingBundleFailedCaseOfficerTemplateId,
            @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
            CustomerServicesProvider customerServicesProvider,
            EmailAddressFinder emailAddressFinder) {
        this.hearingBundleFailedCaseOfficerTemplateId = hearingBundleFailedCaseOfficerTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.customerServicesProvider = customerServicesProvider;
        this.emailAddressFinder = emailAddressFinder;
    }

    @Override
    public String getTemplateId() {
        return hearingBundleFailedCaseOfficerTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(emailAddressFinder.getListCaseHearingCentreEmailAddress(asylumCase));

    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_HEARING_BUNDLE_FAILED_CASE_OFFICER";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        requireNonNull(asylumCase, "asylumCase must not be null");

        String ariaListingReference = asylumCase.read(ARIA_LISTING_REFERENCE, String.class).orElse("");
        String appealReferenceNumber = asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse("");

        log.warn("Sending notification of failed bundle generation (HEARING_BUNDLE_FAILED_CASE_OFFICER). "
            + "If you believe this to be a temporary issue ask the user to try again. "
            + "Case Id: {}, ariaListingReference: {}, appealReferenceNumber: {}",
            callback.getCaseDetails().getId(),
            ariaListingReference,
            appealReferenceNumber
        );

        return getPersonalisation(asylumCase);
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
