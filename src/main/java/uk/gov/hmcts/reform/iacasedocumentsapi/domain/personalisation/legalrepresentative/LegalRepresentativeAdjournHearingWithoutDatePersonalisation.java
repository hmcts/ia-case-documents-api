package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.legalrepresentative;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LegalRepresentativeAdjournHearingWithoutDatePersonalisation implements LegalRepresentativeEmailNotificationPersonalisation {

    private final String legalRepresentativeAdjournHearingWithoutDateTemplateId;

    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;

    public LegalRepresentativeAdjournHearingWithoutDatePersonalisation(
        @Value("${govnotify.template.adjournHearingWithoutDate.legalRep.email}") String legalRepresentativeAdjournHearingWithoutDateTemplateId
    ) {
        this.legalRepresentativeAdjournHearingWithoutDateTemplateId = legalRepresentativeAdjournHearingWithoutDateTemplateId;
    }

    @Override
    public String getTemplateId() {
        return legalRepresentativeAdjournHearingWithoutDateTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_LEGAL_REPRESENTATIVE_ADJOURN_HEARING_WITHOUT_DATE";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return
            ImmutableMap
                .<String, String>builder()
                .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix)
                .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("legalRepReferenceNumber", asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""))
                .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("insertAdjournmentReason", asylumCase.read(ADJOURN_HEARING_WITHOUT_DATE_REASONS, String.class).orElse(""))
                .put("ariaListingReference", asylumCase.read(ARIA_LISTING_REFERENCE, String.class).orElse(""))
                .build();
    }
}
