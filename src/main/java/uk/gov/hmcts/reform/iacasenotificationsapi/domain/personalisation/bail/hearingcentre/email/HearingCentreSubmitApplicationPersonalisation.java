package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.hearingcentre.email;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.BailEmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;

@Service
public class HearingCentreSubmitApplicationPersonalisation implements BailEmailNotificationPersonalisation {

    private final String hearingCentreTemplateId;
    private final String hearingCentreWithoutLrTemplateId;
    private final EmailAddressFinder emailAddressFinder;

    public HearingCentreSubmitApplicationPersonalisation(
        @Value("${govnotify.bail.template.submitApplication.email}") String hearingCentreTemplateId,
        @Value("${govnotify.bail.template.submitApplicationWithoutLR.email}") String hearingCentreWithoutLrTemplateId,
        EmailAddressFinder emailAddressFinder) {
        this.hearingCentreTemplateId = hearingCentreTemplateId;
        this.hearingCentreWithoutLrTemplateId = hearingCentreWithoutLrTemplateId;
        this.emailAddressFinder = emailAddressFinder;
    }

    @Override
    public Set<String> getRecipientsList(BailCase bailCase) {
        return Collections.singleton(emailAddressFinder.getBailHearingCentreEmailAddress(bailCase));
    }

    @Override
    public String getTemplateId(BailCase bailCase) {
        return isLegallyRepresented(bailCase) ? hearingCentreTemplateId : hearingCentreWithoutLrTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_BAIL_APPLICATION_SUBMITTED_HEARING_CENTRE";
    }

    @Override
    public Map<String, String> getPersonalisation(BailCase bailCase) {
        requireNonNull(bailCase, "bailCase must not be null");

        if (isLegallyRepresented(bailCase)) {
            return ImmutableMap
                .<String, String>builder()
                .putAll(getCommonHcPersonalisation(bailCase))
                .put("legalRepReference", bailCase.read(BailCaseFieldDefinition.LEGAL_REP_REFERENCE, String.class).orElse(""))
                .build();
        }
        return ImmutableMap
            .<String,String>builder()
            .putAll(getCommonHcPersonalisation(bailCase))
            .build();
    }

    private Map<String, String> getCommonHcPersonalisation(BailCase bailCase) {

        final ImmutableMap.Builder<String, String> hearingCentreValues = ImmutableMap
            .<String, String>builder()
            .put("bailReferenceNumber", bailCase.read(BailCaseFieldDefinition.BAIL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("applicantGivenNames", bailCase.read(BailCaseFieldDefinition.APPLICANT_GIVEN_NAMES, String.class).orElse(""))
            .put("applicantFamilyName", bailCase.read(BailCaseFieldDefinition.APPLICANT_FAMILY_NAME, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", bailCase.read(BailCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""));

        return hearingCentreValues.build();
    }

}
