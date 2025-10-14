package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.legalrepresentative;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

@Service
public class LegalRepresentativeRecordAdjournmentDetailsPersonalisation implements LegalRepresentativeEmailNotificationPersonalisation {

    private final String legalRepresentativeRecordAdjournmentDetailsTemplateId;

    public LegalRepresentativeRecordAdjournmentDetailsPersonalisation(
        @Value("${govnotify.template.recordAdjournmentDetails.legalRep.email}") String legalRepresentativeRecordAdjournmentDetailsTemplateId
    ) {
        this.legalRepresentativeRecordAdjournmentDetailsTemplateId = legalRepresentativeRecordAdjournmentDetailsTemplateId;
    }

    @Override
    public String getTemplateId() {
        return legalRepresentativeRecordAdjournmentDetailsTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_LEGAL_REPRESENTATIVE_RECORD_ADJOURNMENT_DETAILS";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return
            ImmutableMap
                .<String, String>builder()
                .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("legalRepReferenceNumber", asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""))
                .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .build();
    }
}
