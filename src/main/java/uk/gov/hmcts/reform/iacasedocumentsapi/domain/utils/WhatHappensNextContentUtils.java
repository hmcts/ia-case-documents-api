package uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils;

import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.MakeAnApplicationTypes;

public final class WhatHappensNextContentUtils {

    private WhatHappensNextContentUtils() {
        // prevent public constructor for Sonar
    }

    private static final String timeExtentionContent = "The Tribunal will give you more time to complete your next task. You will get a notification with the new date soon.";
    private static final String adjournExpediteTransferOrUpdateHearingReqsContent = "The details of your hearing will be updated. The Tribunal will contact you when this happens.";
    private static final String judgesReviewContent = "The decision on your original request will be overturned. The Tribunal will contact you if there is something you need to do next.";
    private static final String linkOrUnlinkContent = "This appeal will be linked or unlinked. The Tribunal will contact you when this happens.";
    private static final String withdrawnContent = "The Tribunal will end the appeal. The Tribunal will contact you when this happens.";
    private static final String updateUpdateDetailsOrOtherContent = "The Tribunal will contact you when it makes the changes you requested.";
    private static final String transferOutOfAdaContent = "Your appeal will continue but will no longer be decided within 25 working days. The Tribunal will change the date of your hearing. The Tribunal will contact you with a new date for your hearing and to tell you what will happen next with your appeal.";
    private static final String reinstateAppealContent = "This appeal will be reinstated and will continue from the point where it was ended. You will be notified when this happens.";

    //Home office constants
    private static final String homeOfficetimeExtentionContent = "The Tribunal will give the Home Office more time to complete its next task. You will get a notification with the new date soon.";
    private static final String homeOfficeAdjournExpediteTransferOrUpdateHearingReqsContent = "The details of the hearing will be updated and you will be sent a new Notice of Hearing with the agreed changes.";
    private static final String homeOfficeJudgesReviewContent = "The decision on the Home Office’s original request will be overturned. You will be notified if there is something you need to do next.";
    private static final String homeOfficeLinkOrUnlinkContent = "This appeal will be linked to or unlinked from the appeal in the Home Office application. You will be notified when this happens.";
    private static final String homeOfficeWithdrawnContent = "The Tribunal will end the appeal. You will be notified when this happens.";
    private static final String homeOfficeReinstateAppealContent = "This appeal will be reinstated and will continue from the point where it was ended. You will be notified when this happens.";
    private static final String applicationTypeOtherContent = "You will be notified when the Tribunal makes the changes the Home Office asked for.";

    public static String getWhatHappensNextContent(MakeAnApplicationTypes makeAnApplicationTypes, boolean isAppelantApplication) {
        if (isAppelantApplication) {
            return switch (makeAnApplicationTypes) {
                case TIME_EXTENSION -> timeExtentionContent;
                case ADJOURN, EXPEDITE, TRANSFER, UPDATE_HEARING_REQUIREMENTS ->
                        adjournExpediteTransferOrUpdateHearingReqsContent;
                case JUDGE_REVIEW, JUDGE_REVIEW_LO -> judgesReviewContent;
                case LINK_OR_UNLINK -> linkOrUnlinkContent;
                case WITHDRAW -> withdrawnContent;
                case REINSTATE -> reinstateAppealContent;
                case UPDATE_APPEAL_DETAILS, OTHER -> updateUpdateDetailsOrOtherContent;
                case TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS -> transferOutOfAdaContent;
                default -> "Unknown";
            };
        } else {
            return switch (makeAnApplicationTypes) {
                case TIME_EXTENSION -> homeOfficetimeExtentionContent;
                case ADJOURN, EXPEDITE, TRANSFER, UPDATE_HEARING_REQUIREMENTS ->
                        homeOfficeAdjournExpediteTransferOrUpdateHearingReqsContent;
                case JUDGE_REVIEW, JUDGE_REVIEW_LO -> homeOfficeJudgesReviewContent;
                case LINK_OR_UNLINK -> homeOfficeLinkOrUnlinkContent;
                case WITHDRAW -> homeOfficeWithdrawnContent;
                case REINSTATE -> homeOfficeReinstateAppealContent;
                case OTHER -> applicationTypeOtherContent;
                default -> "";
            };
        }
    }

}
