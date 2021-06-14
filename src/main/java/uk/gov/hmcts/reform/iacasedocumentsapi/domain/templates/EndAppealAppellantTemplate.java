package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.END_APPEAL_APPROVER_TYPE;

import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.helper.EndAppealTemplateHelper;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.EmailAddressFinder;


@Component
public class EndAppealAppellantTemplate implements DocumentTemplate<AsylumCase> {

    private final String templateName;
    private final EndAppealTemplateHelper endAppealTemplateHelper;
    private final EmailAddressFinder emailAddressFinder;

    public EndAppealAppellantTemplate(
        @Value("${endAppeal.appellant.templateName}") String templateName,
        EndAppealTemplateHelper endAppealTemplateHelper,
        EmailAddressFinder emailAddressFinder
    ) {
        this.templateName = templateName;
        this.endAppealTemplateHelper = endAppealTemplateHelper;
        this.emailAddressFinder = emailAddressFinder;
    }

    public String getName() {
        return templateName;
    }

    public Map<String, Object> mapFieldValues(
        CaseDetails<AsylumCase> caseDetails
    ) {
        Map<String, Object> fieldValues = endAppealTemplateHelper.getCommonMapFieldValues(caseDetails);
        fieldValues.put("designatedHearingCentre", isAppealListed(caseDetails.getCaseData())
                ? emailAddressFinder.getListCaseHearingCentreEmailAddress(caseDetails.getCaseData())
                : emailAddressFinder.getHearingCentreEmailAddress(caseDetails.getCaseData()));
        fieldValues.put("endAppealApprover", getEndAppealApprover(caseDetails));
        return fieldValues;
    }

    private String getEndAppealApprover(CaseDetails<AsylumCase> caseDetails) {
        String endAppealApprover = caseDetails.getCaseData().read(END_APPEAL_APPROVER_TYPE, String.class).orElse("");
        return endAppealApprover.equalsIgnoreCase("Case Worker") ? "Tribunal Caseworker" : endAppealApprover;
    }

    protected boolean isAppealListed(AsylumCase asylumCase) {
        final Optional<HearingCentre> appealListed = asylumCase
                .read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class);

        return appealListed.isPresent();
    }
}
