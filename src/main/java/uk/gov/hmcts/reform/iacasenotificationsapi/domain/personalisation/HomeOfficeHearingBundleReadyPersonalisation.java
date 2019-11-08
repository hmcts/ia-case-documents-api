package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.springframework.stereotype.Service;

import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config.GovNotifyTemplateIdConfiguration;

@Service
public class HomeOfficeHearingBundleReadyPersonalisation implements NotificationPersonalisation {

    private final GovNotifyTemplateIdConfiguration govNotifyTemplateIdConfiguration;
    private final EmailAddressFinder emailAddressFinder;

    public HomeOfficeHearingBundleReadyPersonalisation(
            GovNotifyTemplateIdConfiguration govNotifyTemplateIdConfiguration,
            EmailAddressFinder emailAddressFinder) {
        this.govNotifyTemplateIdConfiguration = govNotifyTemplateIdConfiguration;
        this.emailAddressFinder = emailAddressFinder;
    }


    @Override
    public String getTemplateId() {
        return govNotifyTemplateIdConfiguration.getHearingBundleReadyHomeOfficeTemplateId();
    }

    @Override
    public String getEmailAddress(AsylumCase asylumCase) {
        return emailAddressFinder.getHomeOfficeEmailAddress(asylumCase);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_HEARING_BUNDLE_IS_READY_HOME_OFFICE";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
                .<String, String>builder()
                .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("ariaListingReference", asylumCase.read(ARIA_LISTING_REFERENCE, String.class).orElse(""))
                .put("homeOfficeReferenceNumber", asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
                .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .build();
    }
}
