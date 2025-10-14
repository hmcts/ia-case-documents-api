package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.caseofficer;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailNotificationPersonalisation;

@Service
public class CaseOfficerChangeHearingCentrePersonalisation implements EmailNotificationPersonalisation {

    private final String changeHearingCentreHomeOfficeTemplateId;
    private EmailAddressFinder emailAddressFinder;
    private final String listCaseCaseOfficerEmailAddress;


    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;

    public CaseOfficerChangeHearingCentrePersonalisation(
            @Value("${govnotify.template.changeHearingCentre.caseOfficer.email}") String changeHearingCentreTemplateId,
            EmailAddressFinder emailAddressFinder,
            @Value("${listCaseCaseOfficerEmailAddress}") String listCaseCaseOfficerEmailAddress) {
        this.changeHearingCentreHomeOfficeTemplateId = changeHearingCentreTemplateId;
        this.emailAddressFinder = emailAddressFinder;
        this.listCaseCaseOfficerEmailAddress = listCaseCaseOfficerEmailAddress;
    }

    @Override
    public String getTemplateId() {
        return changeHearingCentreHomeOfficeTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return asylumCase.read(HEARING_CENTRE, HearingCentre.class).map(hearingcentre -> {
            if (Arrays.asList(HearingCentre.GLASGOW, HearingCentre.BELFAST).contains(hearingcentre)) {
                return Collections.singleton(listCaseCaseOfficerEmailAddress);
            } else {
                return Collections.singleton(emailAddressFinder.getHearingCentreEmailAddress(asylumCase));
            }
        }).orElseThrow(() -> new IllegalStateException("Hearing centre email Address cannot be found"));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_CHANGE_HEARING_CENTRE_CASE_OFFICER";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
            .<String, String>builder()
            .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix)
            .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .build();
    }
}
