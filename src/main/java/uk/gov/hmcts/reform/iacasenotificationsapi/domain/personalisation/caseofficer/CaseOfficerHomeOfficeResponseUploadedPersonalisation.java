package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HEARING_CENTRE;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;

@Service
public class CaseOfficerHomeOfficeResponseUploadedPersonalisation implements EmailNotificationPersonalisation {

    private final String homeOfficeResponseUploadedTemplateId;
    private final Map<HearingCentre, String> hearingCentreEmailAddresses;

    public CaseOfficerHomeOfficeResponseUploadedPersonalisation(
        @Value("${govnotify.template.homeOfficeResponseUploaded.caseOfficer.email}") String homeOfficeResponseUploadedTemplateId,
        Map<HearingCentre, String> hearingCentreEmailAddresses
    ) {
        this.homeOfficeResponseUploadedTemplateId = homeOfficeResponseUploadedTemplateId;
        this.hearingCentreEmailAddresses = hearingCentreEmailAddresses;
    }

    @Override
    public String getTemplateId() {
        return homeOfficeResponseUploadedTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(asylumCase
            .read(HEARING_CENTRE, HearingCentre.class)
            .map(centre -> Optional.ofNullable(hearingCentreEmailAddresses.get(centre))
                .orElseThrow(() -> new IllegalStateException("Hearing centre email address not found: " + centre.toString()))
            )
            .orElseThrow(() -> new IllegalStateException("hearingCentre is not present")));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_UPLOADED_HO_RESPONSE_CASE_OFFICER";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return
            ImmutableMap
                .<String, String>builder()
                .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .build();
    }
}
