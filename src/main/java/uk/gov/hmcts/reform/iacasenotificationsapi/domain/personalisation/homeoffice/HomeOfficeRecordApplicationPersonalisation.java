package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE;

import com.google.common.collect.ImmutableMap;
import com.microsoft.applicationinsights.core.dependencies.apachecommons.lang3.StringUtils;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;

@Service
public class HomeOfficeRecordApplicationPersonalisation implements EmailNotificationPersonalisation {

    private final String recordRefusedApplicationHomeOfficeTemplateId;
    private final String recordApplicationHomeOfficeEmailAddress;
    private final Map<HearingCentre, String> homeOfficeEmailAddresses;

    public HomeOfficeRecordApplicationPersonalisation(
        @Value("${endAppealHomeOfficeEmailAddress}") String recordApplicationHomeOfficeEmailAddress,
        @Value("${govnotify.template.recordRefusedApplicationHomeOfficeTemplateId}") String recordRefusedApplicationHomeOfficeTemplateId,
        Map<HearingCentre, String> homeOfficeEmailAddresses) {

        this.recordRefusedApplicationHomeOfficeTemplateId = recordRefusedApplicationHomeOfficeTemplateId;
        this.recordApplicationHomeOfficeEmailAddress = recordApplicationHomeOfficeEmailAddress;
        this.homeOfficeEmailAddresses = homeOfficeEmailAddresses;
    }

    @Override
    public String getTemplateId() {
        return recordRefusedApplicationHomeOfficeTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(asylumCase
            .read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)
            .map(homeOfficeEmailAddresses::get)
            .orElse(recordApplicationHomeOfficeEmailAddress));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_RECORD_APPLICATION_HOME_OFFICE";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("applicationType", asylumCase.read(AsylumCaseDefinition.APPLICATION_TYPE, String.class).map(StringUtils::lowerCase).orElse(""))
            .put("applicationDecisionReason", asylumCase.read(AsylumCaseDefinition.APPLICATION_DECISION_REASON, String.class)
                .filter(StringUtils::isNotBlank)
                .orElse("No reason given")
            )
            .put("applicationSupplier", asylumCase.read(AsylumCaseDefinition.APPLICATION_SUPPLIER, String.class).map(StringUtils::lowerCase).orElse(""))
            .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .build();
    }
}
