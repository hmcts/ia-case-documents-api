package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.bail.adminofficer.email;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.BailEmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.EmailAddressFinder;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;

@Service
public class AdminOfficerBailChangeTribunalCentrePersonalisation implements BailEmailNotificationPersonalisation {

    private final String changeTribunalCentreAdminOfficerTemplateId;
    private final String changeTribunalCentreAdminOfficerWithoutLrTemplateId;
    private final EmailAddressFinder emailAddressFinder;

    public AdminOfficerBailChangeTribunalCentrePersonalisation(
            @NotNull(message = "changeTribunalCentreAdminOfficerTemplateId cannot be null")
            @Value("${govnotify.bail.template.changeTribunalCentre.adminOfficer.withLegalRep.email}") String changeTribunalCentreAdminOfficerTemplateId,
            @Value("${govnotify.bail.template.changeTribunalCentre.adminOfficer.withoutLegalRep.email}") String changeTribunalCentreAdminOfficerWithoutLrTemplateId,
            EmailAddressFinder emailAddressFinder
    ) {
        this.changeTribunalCentreAdminOfficerTemplateId = changeTribunalCentreAdminOfficerTemplateId;
        this.changeTribunalCentreAdminOfficerWithoutLrTemplateId = changeTribunalCentreAdminOfficerWithoutLrTemplateId;
        this.emailAddressFinder = emailAddressFinder;
    }

    @Override
    public String getTemplateId(BailCase bailCase) {
        return isLegallyRepresented(bailCase)
                ? changeTribunalCentreAdminOfficerTemplateId : changeTribunalCentreAdminOfficerWithoutLrTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(BailCase bailCase) {
        return Collections.singleton(emailAddressFinder.getBailHearingCentreEmailAddress(bailCase));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_CHANGE_TRIBUNAL_CENTRE_ADMIN_OFFICER";
    }

    @Override
    public Map<String, String> getPersonalisation(BailCase bailCase) {
        requireNonNull(bailCase, "bailCase must not be null");

        ImmutableMap.Builder<String, String> builder = ImmutableMap
            .<String, String>builder()
            .put("bailReferenceNumber", bailCase.read(BailCaseFieldDefinition.BAIL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("applicantGivenNames", bailCase.read(BailCaseFieldDefinition.APPLICANT_GIVEN_NAMES, String.class).orElse(""))
            .put("applicantFamilyName", bailCase.read(BailCaseFieldDefinition.APPLICANT_FAMILY_NAME, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", bailCase.read(BailCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""));

        if (isLegallyRepresented(bailCase)) {
            builder.put("legalRepReference", bailCase.read(BailCaseFieldDefinition.LEGAL_REP_REFERENCE, String.class).orElse(""));
        }
        return builder.build();
    }
}
