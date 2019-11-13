package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config;

import static com.google.common.collect.Lists.newArrayList;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.NotificationSender;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.NotificationGenerator;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.NotificationIdAppender;

@Configuration
public class NotificationGeneratorConfiguration {

    @Bean("endAppealNotificationGenerator")
    public NotificationGenerator endAppealNotificationGenerator(
        HomeOfficeEndAppealPersonalisation homeOfficeEndAppealPersonalisation,
        LegalRepresentativeEndAppealPersonalisation legalRepresentativeEndAppealPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return new NotificationGenerator(
            newArrayList(homeOfficeEndAppealPersonalisation, legalRepresentativeEndAppealPersonalisation),
            notificationSender,
            notificationIdAppender
        );
    }

    @Bean("appealOutcomeNotificationGenerator")
    public NotificationGenerator appealOutcomeNotificationGenerator(
        HomeOfficeAppealOutcomePersonalisation homeOfficeAppealOutcomePersonalisation,
        LegalRepresentativeAppealOutcomePersonalisation legalRepresentativeAppealOutcomePersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return new NotificationGenerator(
            newArrayList(homeOfficeAppealOutcomePersonalisation, legalRepresentativeAppealOutcomePersonalisation),
            notificationSender,
            notificationIdAppender
        );
    }

    @Bean("listCaseNotificationGenerator")
    public NotificationGenerator listCaseNotificationGenerator(
        CaseOfficerListCasePersonalisation caseOfficerListCasePersonalisation,
        LegalRepresentativeListCasePersonalisation legalRepresentativeListCasePersonalisation,
        HomeOfficeListCasePersonalisation homeOfficeListCasePersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return new NotificationGenerator(
            newArrayList(caseOfficerListCasePersonalisation, legalRepresentativeListCasePersonalisation, homeOfficeListCasePersonalisation),
            notificationSender,
            notificationIdAppender
        );
    }

    @Bean("submitAppealNotificationGenerator")
    public NotificationGenerator submitAppealNotificationGenerator(
        CaseOfficerSubmitAppealPersonalisation caseOfficerSubmitAppealPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return new NotificationGenerator(
            newArrayList(caseOfficerSubmitAppealPersonalisation),
            notificationSender,
            notificationIdAppender
        );
    }

    @Bean("submitCaseNotificationGenerator")
    public NotificationGenerator submitCaseNotificationGenerator(
        CaseOfficerSubmitCasePersonalisation caseOfficerSubmitCasePersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return new NotificationGenerator(
            newArrayList(caseOfficerSubmitCasePersonalisation),
            notificationSender,
            notificationIdAppender
        );
    }

    @Bean("uploadRespondentNotificationGenerator")
    public NotificationGenerator uploadRespondentNotificationGenerator(
        LegalRepresentativeUploadRespondentEvidencePersonalisation legalRepresentativeUploadRespondentEvidencePersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return new NotificationGenerator(
            newArrayList(legalRepresentativeUploadRespondentEvidencePersonalisation),
            notificationSender,
            notificationIdAppender
        );
    }

    @Bean("hearingRequirementsNotificationGenerator")
    public NotificationGenerator hearingRequirementsNotificationGenerator(
        LegalRepresentativeHearingRequirementsPersonalisation legalRepresentativeHearingRequirementsPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return new NotificationGenerator(
            newArrayList(legalRepresentativeHearingRequirementsPersonalisation),
            notificationSender,
            notificationIdAppender
        );
    }

    @Bean("addAppealNotificationGenerator")
    public NotificationGenerator addAppealNotificationGenerator(
        LegalRepresentativeAddAppealPersonalisation legalRepresentativeAddAppealPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return new NotificationGenerator(
            newArrayList(legalRepresentativeAddAppealPersonalisation),
            notificationSender,
            notificationIdAppender
        );
    }

    @Bean("respondentReviewNotificationGenerator")
    public NotificationGenerator respondentReviewNotificationGenerator(
        RespondentDirectionPersonalisation respondentDirectionPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return new NotificationGenerator(
            newArrayList(respondentDirectionPersonalisation),
            notificationSender,
            notificationIdAppender
        );
    }


    @Bean("respondentEvidenceNotificationGenerator")
    public NotificationGenerator respondentEvidenceNotificationGenerator(
        RespondentEvidenceDirectionPersonalisation respondentEvidenceDirectionPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return new NotificationGenerator(
            newArrayList(respondentEvidenceDirectionPersonalisation),
            notificationSender,
            notificationIdAppender
        );
    }

    @Bean("sendDirectionNotificationGenerator")
    public NotificationGenerator sendDirectionNotificationGenerator(
        RespondentNonStandardDirectionPersonalisation respondentNonStandardDirectionPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return new NotificationGenerator(
            newArrayList(respondentNonStandardDirectionPersonalisation),
            notificationSender,
            notificationIdAppender
        );
    }

    @Bean("recordApplicationNotificationGenerator")
    public NotificationGenerator recordApplicationNotificationGenerator(
        HomeOfficeRecordApplicationPersonalisation homeOfficeRecordApplicationPersonalisation,
        LegalRepresentativeRecordApplicationPersonalisation legalRepresentativeRecordApplicationPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return new NotificationGenerator(
            newArrayList(homeOfficeRecordApplicationPersonalisation, legalRepresentativeRecordApplicationPersonalisation),
            notificationSender,
            notificationIdAppender
        );
    }

    @Bean("editCaseListingNotificationGenerator")
    public NotificationGenerator editCaseListingNotificationGenerator(
        CaseOfficerEditListingPersonalisation caseOfficerEditListingPersonalisation,
        HomeOfficeEditListingPersonalisation homeOfficeEditListingPersonalisation,
        LegalRepresentativeEditListingPersonalisation legalRepresentativeEditListingPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return new NotificationGenerator(
            newArrayList(caseOfficerEditListingPersonalisation, homeOfficeEditListingPersonalisation, legalRepresentativeEditListingPersonalisation),
            notificationSender,
            notificationIdAppender
        );
    }

    @Bean("uploadHomeOfficeAppealResponseNotificationGenerator")
    public NotificationGenerator uploadHomeOfficeAppealResponseNotificationGenerator(
        CaseOfficerHomeOfficeResponseUploadedPersonalisation caseOfficerHomeOfficeResponseUploadedPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return new NotificationGenerator(
            newArrayList(caseOfficerHomeOfficeResponseUploadedPersonalisation),
            notificationSender,
            notificationIdAppender
        );
    }

    @Bean("requestCaseBuildingNotificationGenerator")
    public NotificationGenerator requestCaseBuildingNotificationGenerator(
        LegalRepresentativeRequestCaseBuildingPersonalisation legalRepresentativeRequestCaseBuildingPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return new NotificationGenerator(
            newArrayList(legalRepresentativeRequestCaseBuildingPersonalisation),
            notificationSender,
            notificationIdAppender
        );
    }

    @Bean("requestResponseReviewNotificationGenerator")
    public NotificationGenerator requestResponseReviewNotificationGenerator(
        LegalRepresentativeRequestResponseReviewPersonalisation legalRepresentativeRequestResponseReviewPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return new NotificationGenerator(
            newArrayList(legalRepresentativeRequestResponseReviewPersonalisation),
            notificationSender,
            notificationIdAppender
        );
    }

    @Bean("respondentEvidenceSubmitted")
    public NotificationGenerator respondentEvidenceSubmitted(
        CaseOfficerRespondentEvidenceSubmittedPersonalisation caseOfficerRespondentEvidenceSubmittedPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {
        return new NotificationGenerator(
            newArrayList(caseOfficerRespondentEvidenceSubmittedPersonalisation),
            notificationSender,
            notificationIdAppender
        );
    }

    @Bean("hearingBundleReadyNotificationGenerator")
    public NotificationGenerator hearingBundleReadyNotificationGenerator(
            HomeOfficeHearingBundleReadyPersonalisation homeOfficeHearingBundleReadyPersonalisation,
            LegalRepresentativeHearingBundleReadyPersonalisation legalRepresentativeHearingBundleReadyPersonalisation,
            NotificationSender notificationSender,
            NotificationIdAppender notificationIdAppender) {

        return new NotificationGenerator(
                newArrayList(homeOfficeHearingBundleReadyPersonalisation, legalRepresentativeHearingBundleReadyPersonalisation),
                notificationSender,
                notificationIdAppender

        );
    }
}
