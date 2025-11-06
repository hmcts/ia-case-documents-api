package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.utils;

import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.MakeAnApplicationTypes;

public final class WhatHappensNextContentUtils {

    private WhatHappensNextContentUtils() {
        // prevent public constructor for Sonar
    }

    private static final String TIME_EXTENSION_CONTENT = "The Tribunal will give you more time to complete your next task. You will get a notification with the new date soon.";
    private static final String ADJOURN_EXPEDITE_TRANSFER_OR_UPDATE_HEARING_REQS_CONTENT = "The details of your hearing will be updated. The Tribunal will contact you when this happens.";
    private static final String JUDGES_REVIEW_CONTENT = "The decision on your original request will be overturned. The Tribunal will contact you if there is something you need to do next.";
    private static final String LINK_OR_UNLINK_CONTENT = "This appeal will be linked or unlinked. The Tribunal will contact you when this happens.";
    private static final String WITHDRAWN_CONTENT = "The Tribunal will end the appeal. The Tribunal will contact you when this happens.";
    private static final String UPDATE_UPDATE_DETAILS_OR_OTHER_CONTENT = "The Tribunal will contact you when it makes the changes you requested.";
    private static final String REINSTATE_APPEAL_CONTENT = "This appeal will be reinstated and will continue from the point where it was ended. You will be notified when this happens.";
    private static final String REFUSED_APPELLANT_CONTENT = "If a Legal Officer made this decision, you can contact the tribunal to ask for the decision to be reviewed by a judge.";

    // Home office constants
    private static final String HOME_OFFICE_TIME_EXTENSION_CONTENT = "The tribunal will give the Home Office more time to complete its next task. You will get a notification with the new date soon.";
    private static final String HOME_OFFICE_ADJOURN_EXPEDITE_TRANSFER_CONTENT = "The details of the hearing will be updated and you will be sent a new Notice of Hearing with the agreed changes.";
    private static final String HOME_OFFICE_JUDGES_REVIEW_CONTENT = "The decision on the Home Office’s original request will be overturned. You will be notified if there is something you need to do next.";
    private static final String HOME_OFFICE_LINK_OR_UNLINK_CONTENT = "This appeal will be linked to or unlinked from the appeal in the Home Office application. You will be notified when this happens.";
    private static final String HOME_OFFICE_WITHDRAWN_CONTENT = "Your appeal may end. If you do not want the appeal to end, you should contact the tribunal by %s to explain why.";
    private static final String HOME_OFFICE_REINSTATE_APPEAL_CONTENT = "This appeal will be reinstated and will continue from the point where it was ended. You will be notified when this happens.";
    private static final String APPLICATION_TYPE_OTHER_CONTENT = "You will be notified when the tribunal makes the changes the Home Office asked for.";
    private static final String HOME_OFFICE_REFUSED_CONTENT = "The appeal will continue without any changes.";

    public static String getWhatHappensNextContent(MakeAnApplicationTypes makeAnApplicationTypes, boolean isAppellantApplication, String decisionStr, String dueDate) {
        boolean isGranted = decisionStr.equals("Granted");
        return isAppellantApplication
            ? getAppellantContent(makeAnApplicationTypes, isGranted)
            : getHomeOfficeContent(makeAnApplicationTypes, isGranted, dueDate);
    }

    private static String getAppellantContent(MakeAnApplicationTypes type, boolean isGranted) {
        if (!isGranted) {
            return REFUSED_APPELLANT_CONTENT;
        }
        return switch (type) {
            case TIME_EXTENSION -> TIME_EXTENSION_CONTENT;
            case ADJOURN, EXPEDITE, TRANSFER, UPDATE_HEARING_REQUIREMENTS ->
                ADJOURN_EXPEDITE_TRANSFER_OR_UPDATE_HEARING_REQS_CONTENT;
            case JUDGE_REVIEW, JUDGE_REVIEW_LO -> JUDGES_REVIEW_CONTENT;
            case LINK_OR_UNLINK -> LINK_OR_UNLINK_CONTENT;
            case WITHDRAW -> WITHDRAWN_CONTENT;
            case REINSTATE -> REINSTATE_APPEAL_CONTENT;
            case UPDATE_APPEAL_DETAILS, OTHER -> UPDATE_UPDATE_DETAILS_OR_OTHER_CONTENT;
            default -> "Unknown";
        };
    }

    private static String getHomeOfficeContent(MakeAnApplicationTypes type, boolean isGranted, String dueDate) {
        if (!isGranted) {
            return HOME_OFFICE_REFUSED_CONTENT;
        }
        return switch (type) {
            case TIME_EXTENSION -> HOME_OFFICE_TIME_EXTENSION_CONTENT;
            case ADJOURN, EXPEDITE, TRANSFER ->
                HOME_OFFICE_ADJOURN_EXPEDITE_TRANSFER_CONTENT;
            case JUDGE_REVIEW_LO -> HOME_OFFICE_JUDGES_REVIEW_CONTENT;
            case LINK_OR_UNLINK -> HOME_OFFICE_LINK_OR_UNLINK_CONTENT;
            case WITHDRAW -> String.format(HOME_OFFICE_WITHDRAWN_CONTENT, dueDate);
            case REINSTATE -> HOME_OFFICE_REINSTATE_APPEAL_CONTENT;
            case OTHER -> APPLICATION_TYPE_OTHER_CONTENT;
            default -> "Unknown";
        };
    }
}
