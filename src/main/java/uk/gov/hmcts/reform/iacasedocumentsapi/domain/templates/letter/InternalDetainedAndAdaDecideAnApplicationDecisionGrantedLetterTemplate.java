package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getAppellantPersonalisation;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.DateUtils.formatDateForNotificationAttachmentDocument;

import java.util.*;
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
public class InternalDetainedAndAdaDecideAnApplicationDecisionGrantedLetterTemplate implements DocumentTemplate<AsylumCase> {

    private static final String timeExtentionContent = "The Tribunal will give you more time to complete your next task. You will get a notification with the new date soon.";
    private static final String adjournExpediteTransferOrUpdateHearingReqsContent = "The details of your hearing will be updated. The Tribunal will contact you when this happens.";
    private static final String judgesReviewContent = "The decision on your original request will be overturned. The Tribunal will contact you if there is something you need to do next.";
    private static final String linkOrUnlinkContent = "This appeal will be linked or unlinked. The Tribunal will contact you when this happens.";
    private static final String withdrawnContent = "The Tribunal will end the appeal. The Tribunal will contact you when this happens.";
    private static final String updateUpdateDetailsOrOtherContent = "The Tribunal will contact you when it makes the changes you requested.";
    private static final String transferOutOfAdaContent = "Your appeal will continue but will no longer be decided within 25 working days. The Tribunal will change the date of your hearing. The Tribunal will contact you with a new date for your hearing and to tell you what will happen next with your appeal.";
    private final String templateName;
    private final DateProvider dateProvider;
    private final CustomerServicesProvider customerServicesProvider;
    private final MakeAnApplicationService makeAnApplicationService;


    public InternalDetainedAndAdaDecideAnApplicationDecisionGrantedLetterTemplate(
            @Value("${internalDetainedAndAdaDecideAnApplicationDecisionGrantedLetter.templateName}") String templateName,
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
            case UPDATE_APPEAL_DETAILS, OTHER -> updateUpdateDetailsOrOtherContent;
            case TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS -> transferOutOfAdaContent;
            default -> "Unknown";
        };
    }
}
