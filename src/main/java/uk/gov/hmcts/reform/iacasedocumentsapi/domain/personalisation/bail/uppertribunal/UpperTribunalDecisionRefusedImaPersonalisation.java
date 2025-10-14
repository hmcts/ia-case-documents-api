package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.bail.uppertribunal;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.RECORD_DECISION_TYPE;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.RecordDecisionType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.BailEmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@Service
public class UpperTribunalDecisionRefusedImaPersonalisation implements BailEmailNotificationPersonalisation {

    private final String upperTribunalDecisionRefusedImaTemplateId;
    private final CustomerServicesProvider customerServicesProvider;
    private final String bailUpperTribunalEmailAddress;

    public UpperTribunalDecisionRefusedImaPersonalisation(
        @NotNull(message = "homeOfficeBailApplicationSubmittedPersonalisationTemplateId cannot be null")
        @Value("${govnotify.bail.template.decisionRefusedIma.email}") String upperTribunalDecisionRefusedImaTemplateId,
        @Value("${bailUpperTribunalEmailAddress}") String bailUpperTribunalEmailAddress,
        CustomerServicesProvider customerServicesProvider

    ) {
        this.upperTribunalDecisionRefusedImaTemplateId = upperTribunalDecisionRefusedImaTemplateId;
        this.bailUpperTribunalEmailAddress = bailUpperTribunalEmailAddress;
        this.customerServicesProvider =  customerServicesProvider;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_BAIL_RECORD_DECISION_IMA_UPPER_TRIBUNAL";
    }

    @Override
    public String getTemplateId() {
        return upperTribunalDecisionRefusedImaTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(BailCase bailCase) {
        return Collections.singleton(bailUpperTribunalEmailAddress);
    }

    @Override
    public Map<String, String> getPersonalisation(BailCase bailCase) {
        requireNonNull(bailCase, "bailCase must not be null");

        RecordDecisionType recordDecisionType = bailCase.read(RECORD_DECISION_TYPE, RecordDecisionType.class)
            .orElseThrow(() -> new IllegalStateException("Record Decision Type is not present"));
        String decision = "";

        switch (recordDecisionType) {
            case GRANTED -> decision = " Granted";
            case REFUSED -> decision = " Refused";
            case REFUSED_UNDER_IMA -> decision = " Refused under IMA because 28 days have not expired since the date of detention";
            default -> throw new RequiredFieldMissingException("Conditional bail is not handled for IMA");
        }

        return ImmutableMap
            .<String, String>builder()
            .put("bailReferenceNumber", bailCase.read(BailCaseFieldDefinition.BAIL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("decision", decision)
            .put("legalRepReference", bailCase.read(BailCaseFieldDefinition.LEGAL_REP_REFERENCE, String.class).orElse(""))
            .put("applicantGivenNames", bailCase.read(BailCaseFieldDefinition.APPLICANT_GIVEN_NAMES, String.class).orElse(""))
            .put("applicantFamilyName", bailCase.read(BailCaseFieldDefinition.APPLICANT_FAMILY_NAME, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", bailCase.read(BailCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .build();
    }
}
