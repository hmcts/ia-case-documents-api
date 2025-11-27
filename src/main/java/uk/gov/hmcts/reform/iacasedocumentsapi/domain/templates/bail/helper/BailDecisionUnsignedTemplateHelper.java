package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.bail.helper;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.*;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@Component
public class BailDecisionUnsignedTemplateHelper {

    private final CustomerServicesProvider customerServicesProvider;
    private final String govCallChargesUrl;

    public BailDecisionUnsignedTemplateHelper(@Value("${govCallChargesUrl}") String govCallChargesUrl,
                                              CustomerServicesProvider customerServicesProvider) {
        this.govCallChargesUrl = govCallChargesUrl;
        this.customerServicesProvider = customerServicesProvider;
    }

    public Map<String, Object> getCommonMapFieldValues(
            CaseDetails<BailCase> caseDetails
    ) {
        final BailCase bailCase = caseDetails.getCaseData();
        final String applicantDetainedLoc = "applicantDetainedLoc";
        final Map<String, Object> fieldValues = new HashMap<>();
        final boolean hasLegalRep = bailCase.read(HAS_LEGAL_REP, YesOrNo.class).orElse(YesOrNo.NO).equals(YesOrNo.YES);

        fieldValues.put("hmcts", "[userImage:hmcts.png]");
        fieldValues.put("applicantGivenNames", bailCase.read(APPLICANT_GIVEN_NAMES, String.class));
        fieldValues.put("applicantFamilyName", bailCase.read(APPLICANT_FAMILY_NAME, String.class));

        boolean isLegalRep = false;
        if (bailCase.read(IS_ADMIN, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES) {
            isLegalRep = (bailCase.read(SENT_BY_CHECKLIST, String.class).orElse("").equalsIgnoreCase("legal representative"));
        } else if (bailCase.read(IS_LEGAL_REP, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES) {
            isLegalRep = true;
        }

        final boolean isLegallyRepresentedCase = isLegalRep || hasLegalRep;

        fieldValues.put("bailReferenceNumber", bailCase.read(BAIL_REFERENCE_NUMBER, String.class).orElse(""));
        fieldValues.put("homeOfficeReferenceNumber", bailCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""));

        fieldValues.put(applicantDetainedLoc, bailCase.read(APPLICANT_DETAINED_LOC, String.class).orElse(""));
        if (bailCase.read(APPLICANT_DETAINED_LOC, String.class).orElse("").equals("prison")) {
            fieldValues.put(applicantDetainedLoc, "Prison");
            fieldValues.put("prisonName", bailCase.read(PRISON_NAME, String.class).orElse(""));
            fieldValues.put("applicantPrisonDetails", bailCase.read(APPLICANT_PRISON_DETAILS, String.class).orElse(""));
        }

        if (bailCase.read(APPLICANT_DETAINED_LOC, String.class).orElse("").equals("immigrationRemovalCentre")) {
            fieldValues.put(applicantDetainedLoc, "Immigration Removal Centre");
            fieldValues.put("ircName", bailCase.read(IRC_NAME, String.class).orElse(""));
        }

        fieldValues.put("isLegallyRepresentedForFlag", isLegallyRepresentedCase ? YesOrNo.YES : YesOrNo.NO);
        if (isLegallyRepresentedCase) {
            fieldValues.put("legalRepReference", bailCase.read(LEGAL_REP_REFERENCE, String.class).orElse(""));
        }

        fieldValues.put("judgeDetailsName", bailCase.read(JUDGE_DETAILS_NAME, String.class).orElse(""));
        fieldValues.put("customerServicesTelephone", customerServicesProvider.getCustomerServicesTelephone());
        fieldValues.put("customerServicesEmail", customerServicesProvider.getCustomerServicesEmail());
        fieldValues.put("govCallChargesLink", govCallChargesUrl);

        return fieldValues;
    }
}
