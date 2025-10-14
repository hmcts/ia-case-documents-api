package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.respondent;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailNotificationPersonalisation;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

@Service
public class RespondentRecordAdjournmentDetailsPersonalisation implements EmailNotificationPersonalisation {

    private final String respondentRecordAdjournmentDetailsTemplateId;
    private final EmailAddressFinder respondentEmailAddressAfterRespondentReview;

    public RespondentRecordAdjournmentDetailsPersonalisation(
        @Value("${govnotify.template.recordAdjournmentDetails.respondent.email}") String respondentRecordAdjournmentDetailsTemplateId,
        EmailAddressFinder respondentEmailAddressAfterRespondentReview
    ) {

        this.respondentRecordAdjournmentDetailsTemplateId = respondentRecordAdjournmentDetailsTemplateId;
        this.respondentEmailAddressAfterRespondentReview = respondentEmailAddressAfterRespondentReview;
    }

    @Override
    public String getTemplateId() {
        return respondentRecordAdjournmentDetailsTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(getRespondentEmailAddress(asylumCase));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_RESPONDENT_RECORD_ADJOURNMENT_DETAILS";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .build();
    }

    private String getRespondentEmailAddress(AsylumCase asylumCase) {

        return respondentEmailAddressAfterRespondentReview.getListCaseHomeOfficeEmailAddress(asylumCase);
    }
}
