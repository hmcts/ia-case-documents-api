package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REPRESENTATIVE_EMAIL_ADDRESS;

import com.google.common.collect.ImmutableMap;
import com.microsoft.applicationinsights.core.dependencies.apachecommons.lang3.StringUtils;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;

@Service
public class LegalRepresentativeRecordApplicationPersonalisation implements EmailNotificationPersonalisation {

    private final String recordApplicationLegalRepresentativeTemplateId;

    public LegalRepresentativeRecordApplicationPersonalisation(
        @Value("${govnotify.template.recordRefusedApplicationLegalRepresentativeTemplateId}") String recordApplicationLegalRepresentativeTemplateId) {

        this.recordApplicationLegalRepresentativeTemplateId = recordApplicationLegalRepresentativeTemplateId;
    }

    @Override
    public String getTemplateId() {
        return recordApplicationLegalRepresentativeTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(asylumCase
            .read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class)
            .orElseThrow(() -> new IllegalStateException("legalRepresentativeEmailAddress is not present")));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_RECORD_APPLICATION_LEGAL_REPRESENTATIVE";
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
            .put("legalRepReferenceNumber", asylumCase.read(AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""))
            .build();
    }
}
