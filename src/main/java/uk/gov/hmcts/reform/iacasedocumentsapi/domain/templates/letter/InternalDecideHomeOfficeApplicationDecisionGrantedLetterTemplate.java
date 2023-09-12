package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getAppellantPersonalisation;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.DateUtils.formatDateForNotificationAttachmentDocument;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.MakeAnApplication;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.MakeAnApplicationTypes;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.DocumentTemplate;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.MakeAnApplicationService;


@Component
public class InternalDecideHomeOfficeApplicationDecisionGrantedLetterTemplate implements DocumentTemplate<AsylumCase> {
    private static final String timeExtentionContent = "The Tribunal will give the Home Office more time to complete its next task. You will get a notification with the new date soon.";
    private static final String adjournExpediteTransferOrUpdateHearingReqsContent = "The details of the hearing will be updated and you will be sent a new Notice of Hearing with the agreed changes.";
    private static final String judgesReviewContent = "The decision on the Home Office’s original request will be overturned. You will be notified if there is something you need to do next.";
    private static final String linkOrUnlinkContent = "This appeal will be linked to or unlinked from the appeal in the Home Office application. You will be notified when this happens.";
    private static final String withdrawnContent = "The Tribunal will end the appeal. You will be notified when this happens.";
    private static final String reinstateAppealContent = "This appeal will be reinstated and will continue from the point where it was ended. You will be notified when this happens.";
    private static final String applicationTypeOtherContent = "You will be notified when the Tribunal makes the changes the Home Office asked for.";
    private final String templateName;
    private final DateProvider dateProvider;
    private final CustomerServicesProvider customerServicesProvider;
    private final MakeAnApplicationService makeAnApplicationService;


    public InternalDecideHomeOfficeApplicationDecisionGrantedLetterTemplate(
            @Value("${internalDecideHomeOfficeApplicationDecisionGrantedLetter.templateName}") String templateName,
            DateProvider dateProvider,
            CustomerServicesProvider customerServicesProvider,
            MakeAnApplicationService makeAnApplicationService) {
        this.templateName = templateName;
        this.dateProvider = dateProvider;
        this.customerServicesProvider = customerServicesProvider;
        this.makeAnApplicationService = makeAnApplicationService;
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
        String applicationDecisionReason = "";
        if (optionalMakeAnApplication.isPresent()) {
            MakeAnApplication makeAnApplication = optionalMakeAnApplication.get();
            applicationType = makeAnApplication.getType();
            applicationDecision = makeAnApplication.getDecision();
            applicationDecisionReason = makeAnApplication.getDecisionReason();
        } else {
            throw new IllegalStateException("Home Office application not found.");
        }

        Optional<MakeAnApplicationTypes> optionalApplicationType = MakeAnApplicationTypes.from(applicationType);
        MakeAnApplicationTypes makeAnApplicationTypes;
        if (optionalApplicationType.isPresent()) {
            makeAnApplicationTypes = optionalApplicationType.get();
        } else {
            throw new IllegalStateException("Application type could not be parsed");
        }

        fieldValues.putAll(getAppellantPersonalisation(asylumCase));
        fieldValues.put("dateLetterSent", formatDateForNotificationAttachmentDocument(dateProvider.now()));
        fieldValues.put("customerServicesTelephone", customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase));
        fieldValues.put("customerServicesEmail", customerServicesProvider.getInternalCustomerServicesEmail(asylumCase));
        fieldValues.put("applicationType", applicationType);
        fieldValues.put("applicationReason", applicationDecisionReason);
        fieldValues.put("whatHappensNextContent", getWhatHappensNextContent(makeAnApplicationTypes));

        return fieldValues;
    }

    private Optional<MakeAnApplication> getMakeAnApplication(AsylumCase asylumCase) {
        return makeAnApplicationService.getMakeAnApplication(asylumCase, true);
    }

    private String getWhatHappensNextContent(MakeAnApplicationTypes makeAnApplicationTypes) {
        return switch (makeAnApplicationTypes) {
            case TIME_EXTENSION -> timeExtentionContent;
            case ADJOURN, EXPEDITE, TRANSFER, UPDATE_HEARING_REQUIREMENTS -> adjournExpediteTransferOrUpdateHearingReqsContent;
            case JUDGE_REVIEW, JUDGE_REVIEW_LO -> judgesReviewContent;
            case LINK_OR_UNLINK -> linkOrUnlinkContent;
            case WITHDRAW -> withdrawnContent;
            case REINSTATE -> reinstateAppealContent;
            case OTHER -> applicationTypeOtherContent;
            default -> "";
        };
    }

}
