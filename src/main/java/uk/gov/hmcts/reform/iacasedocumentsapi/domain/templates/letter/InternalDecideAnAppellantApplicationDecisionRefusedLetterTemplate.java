package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getAppellantPersonalisation;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.DateUtils.formatDateForNotificationAttachmentDocument;

import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.MakeAnApplication;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.MakeAnApplicationTypes;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.DocumentTemplate;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.MakeAnApplicationService;

@Component
public class InternalDecideAnAppellantApplicationDecisionRefusedLetterTemplate implements DocumentTemplate<AsylumCase> {

    private static final String judgeRole = "caseworker-ia-iacjudge";
    private static final String legalOfficerRole = "caseworker-ia-caseofficer";
    private static final String applicationRefusedAdaFormName = "IAFT-ADA4: Make an application – Accelerated detained appeal (ADA)";
    private static final String applicationRefusedDetainedNonAdaFormName = "IAFT-DE4: Make an application – Detained appeal";
    private final String templateName;
    private final DateProvider dateProvider;
    private final CustomerServicesProvider customerServicesProvider;
    private final MakeAnApplicationService makeAnApplicationService;
    private final UserDetailsProvider userDetailsProvider;


    public InternalDecideAnAppellantApplicationDecisionRefusedLetterTemplate(
            @Value("${internalDecideAnAppellantApplicationDecisionRefusedLetter.templateName}") String templateName,
            DateProvider dateProvider,
            CustomerServicesProvider customerServicesProvider,
            MakeAnApplicationService makeAnApplicationService,
            UserDetailsProvider userDetailsProvider) {
        this.templateName = templateName;
        this.dateProvider = dateProvider;
        this.customerServicesProvider = customerServicesProvider;
        this.makeAnApplicationService = makeAnApplicationService;
        this.userDetailsProvider = userDetailsProvider;
    }

    public String getName() {
        return templateName;
    }

    public Map<String, Object> mapFieldValues(
            CaseDetails<AsylumCase> caseDetails
    ) {
        final AsylumCase asylumCase = caseDetails.getCaseData();
        final Map<String, Object> fieldValues = new HashMap<>();

        Optional<MakeAnApplication> optionalMakeAnApplication = getMakeAnApplication(asylumCase);

        String applicationType = "";
        String applicationDecision = "";
        String applicationDecisionReason = "No reason given";
        if (optionalMakeAnApplication.isPresent()) {
            MakeAnApplication makeAnApplication = optionalMakeAnApplication.get();
            applicationType = makeAnApplication.getType();
            applicationDecision = makeAnApplication.getDecision();
            applicationDecisionReason = makeAnApplication.getDecisionReason();
        }

        Optional<MakeAnApplicationTypes> optionalApplicationType = MakeAnApplicationTypes.from(applicationType);
        MakeAnApplicationTypes makeAnApplicationTypes;
        if (optionalApplicationType.isPresent()) {
            makeAnApplicationTypes = optionalApplicationType.get();
        } else {
            throw new IllegalStateException("Application type could not be parsed");
        }

        final boolean applicationDecidedByLegalOfficer = userDetailsProvider.getUserDetails().getRoles().contains(legalOfficerRole);
        final boolean applicationDecidedByJudge = userDetailsProvider.getUserDetails().getRoles().contains(judgeRole);

        String applicationDecidedBy = "";
        if (applicationDecidedByLegalOfficer) {
            applicationDecidedBy = "Legal Officer";
        } else if (applicationDecidedByJudge) {
            applicationDecidedBy = "Judge";
        }

        fieldValues.putAll(getAppellantPersonalisation(asylumCase));
        fieldValues.put("dateLetterSent", formatDateForNotificationAttachmentDocument(dateProvider.now()));
        fieldValues.put("customerServicesTelephone", customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase));
        fieldValues.put("customerServicesEmail", customerServicesProvider.getInternalCustomerServicesEmail(asylumCase));
        fieldValues.put("applicationType", applicationType);
        fieldValues.put("applicationReason", applicationDecisionReason);
        fieldValues.put("decisionMaker", applicationDecidedBy);
        fieldValues.put("formName", getFormName(asylumCase));

        return fieldValues;
    }

    private Optional<MakeAnApplication> getMakeAnApplication(AsylumCase asylumCase) {
        return makeAnApplicationService.getMakeAnApplication(asylumCase, true);
    }

    private String getFormName(AsylumCase asylumCase) {
        return isAcceleratedDetainedAppeal(asylumCase) ? applicationRefusedAdaFormName : applicationRefusedDetainedNonAdaFormName;
    }
}
