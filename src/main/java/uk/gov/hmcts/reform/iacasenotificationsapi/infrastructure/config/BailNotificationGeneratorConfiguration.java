package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Arrays;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.adminofficer.email.AdminOfficerBailNocChangedLrPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.adminofficer.email.AdminOfficerBailStopLegalRepresentingPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.adminofficer.email.AdminOfficerBailSummaryUploadedPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.applicant.sms.ApplicantBailApplicationEndedPersonalisationSms;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.applicant.sms.ApplicantBailApplicationSubmittedPersonalisationSms;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.applicant.sms.ApplicantBailNocChangedLrPersonalisationSms;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.applicant.sms.ApplicantBailSignedDecisionNoticeUploadedPersonalisationSms;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.applicant.sms.ApplicantBailStopLegalRepresentingPersonalisationSms;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.hearingcentre.email.HearingCentreSubmitApplicationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.homeoffice.email.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.legalrepresentative.email.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.BailEmailNotificationGenerator;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.BailNotificationGenerator;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.BailNotificationIdAppender;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.BailSmsNotificationGenerator;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.BailGovNotifyNotificationSender;

@Configuration
public class BailNotificationGeneratorConfiguration {


    @Bean("submitApplicationNotificationGenerator")
    public List<BailNotificationGenerator> submitApplicationNotificationGenerator(
        HearingCentreSubmitApplicationPersonalisation hearingCentreSubmitApplicationPersonalisation,
        LegalRepresentativeBailApplicationSubmittedPersonalisation legalRepresentativeBailApplicationSubmittedPersonalisation,
        HomeOfficeBailApplicationSubmittedPersonalisation homeOfficeBailApplicationSubmittedPersonalisation,
        ApplicantBailApplicationSubmittedPersonalisationSms applicantBailApplicationSubmittedPersonalisationSms,
        BailGovNotifyNotificationSender notificationSender,
        BailNotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new BailEmailNotificationGenerator(
                    newArrayList(hearingCentreSubmitApplicationPersonalisation,
                            legalRepresentativeBailApplicationSubmittedPersonalisation,
                            homeOfficeBailApplicationSubmittedPersonalisation),
                    notificationSender,
                    notificationIdAppender
            ),
            new BailSmsNotificationGenerator(
                    newArrayList(applicantBailApplicationSubmittedPersonalisationSms),
                    notificationSender,
                    notificationIdAppender
            )
        );
    }

    @Bean("submitApplicationWithoutLegalRepNotificationGenerator")
    public List<BailNotificationGenerator> submitApplicationWithoutLegalRepNotificationGenerator(
        HearingCentreSubmitApplicationPersonalisation hearingCentreSubmitApplicationPersonalisation,
        HomeOfficeBailApplicationSubmittedPersonalisation homeOfficeBailApplicationSubmittedPersonalisation,
        ApplicantBailApplicationSubmittedPersonalisationSms applicantBailApplicationSubmittedPersonalisationSms,
        BailGovNotifyNotificationSender notificationSender,
        BailNotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new BailEmailNotificationGenerator(
                    newArrayList(hearingCentreSubmitApplicationPersonalisation,
                            homeOfficeBailApplicationSubmittedPersonalisation),
                    notificationSender,
                    notificationIdAppender
            ),
            new BailSmsNotificationGenerator(
                    newArrayList(applicantBailApplicationSubmittedPersonalisationSms),
                    notificationSender,
                    notificationIdAppender
            )
        );
    }

    @Bean("uploadSummaryNotificationGenerator")
    public List<BailNotificationGenerator> uploadSummaryNotificationGenerator(
        AdminOfficerBailSummaryUploadedPersonalisation adminOfficerBailSummaryUploadedPersonalisation,
        LegalRepresentativeBailSummaryUploadedPersonalisation legalRepresentativeBailSummaryUploadedPersonalisation,
        BailGovNotifyNotificationSender notificationSender,
        BailNotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new BailEmailNotificationGenerator(
                    newArrayList(adminOfficerBailSummaryUploadedPersonalisation,
                            legalRepresentativeBailSummaryUploadedPersonalisation),
                    notificationSender,
                    notificationIdAppender
            )
    );
    }

    @Bean("uploadSummaryWithoutLrNotificationGenerator")
    public List<BailNotificationGenerator> uploadSummaryWithoutLrNotificationGenerator(
        AdminOfficerBailSummaryUploadedPersonalisation adminOfficerBailSummaryUploadedPersonalisation,
        BailGovNotifyNotificationSender notificationSender,
        BailNotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new BailEmailNotificationGenerator(
                    newArrayList(adminOfficerBailSummaryUploadedPersonalisation),
                    notificationSender,
                    notificationIdAppender
            )
        );
    }

    @Bean("uploadSignedDecisionNoticeNotificationGenerator")
    public List<BailNotificationGenerator> uploadSignedDecisionNoticeNotificationGenerator(
        ApplicantBailSignedDecisionNoticeUploadedPersonalisationSms applicantBailSignedDecisionNoticeUploadedPersonalisationSms,
        HomeOfficeBailSignedDecisionNoticeUploadedPersonalisation homeOfficeBailSignedDecisionNoticeUploadedPersonalisation,
        LegalRepresentativeBailSignedDecisionNoticeUploadedPersonalisation legalRepresentativeBailSignedDecisionNoticeUploadedPersonalisation,
        BailGovNotifyNotificationSender notificationSender,
        BailNotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new BailSmsNotificationGenerator(
                    newArrayList(applicantBailSignedDecisionNoticeUploadedPersonalisationSms),
                    notificationSender,
                    notificationIdAppender
            ),
            new BailEmailNotificationGenerator(
                    newArrayList(homeOfficeBailSignedDecisionNoticeUploadedPersonalisation,
                            legalRepresentativeBailSignedDecisionNoticeUploadedPersonalisation),
                    notificationSender,
                    notificationIdAppender
            )
        );
    }

    @Bean("uploadSignedDecisionNoticeWithoutLrNotificationGenerator")
    public List<BailNotificationGenerator> uploadSignedDecisionNoticeWithoutLrNotificationGenerator(
        ApplicantBailSignedDecisionNoticeUploadedPersonalisationSms applicantBailSignedDecisionNoticeUploadedPersonalisationSms,
        HomeOfficeBailSignedDecisionNoticeUploadedPersonalisation homeOfficeBailSignedDecisionNoticeUploadedPersonalisation,
        BailGovNotifyNotificationSender notificationSender,
        BailNotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new BailSmsNotificationGenerator(
                    newArrayList(applicantBailSignedDecisionNoticeUploadedPersonalisationSms),
                    notificationSender,
                    notificationIdAppender
            ),
            new BailEmailNotificationGenerator(
                    newArrayList(homeOfficeBailSignedDecisionNoticeUploadedPersonalisation),
                    notificationSender,
                    notificationIdAppender
            )
        );
    }

    @Bean("endApplicationNotificationGenerator")
    public List<BailNotificationGenerator> endApplicationNotificationGenerator(
        ApplicantBailApplicationEndedPersonalisationSms applicantBailApplicationEndedPersonalisationSms,
        HomeOfficeBailApplicationEndedPersonalisation homeOfficeBailApplicationEndedPersonalisation,
        LegalRepresentativeBailApplicationEndedPersonalisation legalRepresentativeBailApplicationEndedPersonalisation,
        BailGovNotifyNotificationSender notificationSender,
        BailNotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new BailSmsNotificationGenerator(
                    newArrayList(applicantBailApplicationEndedPersonalisationSms),
                    notificationSender,
                    notificationIdAppender
            ),
            new BailEmailNotificationGenerator(
                    newArrayList(homeOfficeBailApplicationEndedPersonalisation,
                            legalRepresentativeBailApplicationEndedPersonalisation),
                    notificationSender,
                    notificationIdAppender
            )
        );
    }

    @Bean("endApplicationWithoutLrNotificationGenerator")
    public List<BailNotificationGenerator> endApplicationWithoutLrNotificationGenerator(
        ApplicantBailApplicationEndedPersonalisationSms applicantBailApplicationEndedPersonalisationSms,
        HomeOfficeBailApplicationEndedPersonalisation homeOfficeBailApplicationEndedPersonalisation,
        BailGovNotifyNotificationSender notificationSender,
        BailNotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new BailSmsNotificationGenerator(
                    newArrayList(applicantBailApplicationEndedPersonalisationSms),
                    notificationSender,
                    notificationIdAppender
            ),
            new BailEmailNotificationGenerator(
                    newArrayList(homeOfficeBailApplicationEndedPersonalisation),
                    notificationSender,
                    notificationIdAppender
            )
        );
    }

    @Bean("uploadDocumentNotificationGenerator")
    public List<BailNotificationGenerator> uploadDocumentNotificationGenerator(
        HomeOfficeBailDocumentUploadedPersonalisation homeOfficeBailDocumentUploadedPersonalisation,
        LegalRepresentativeBailDocumentUploadedPersonalisation legalRepresentativeBailDocumentUploadedPersonalisation,
        BailGovNotifyNotificationSender notificationSender,
        BailNotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new BailEmailNotificationGenerator(
                    newArrayList(homeOfficeBailDocumentUploadedPersonalisation,
                            legalRepresentativeBailDocumentUploadedPersonalisation),
                    notificationSender,
                    notificationIdAppender
            )
        );
    }

    @Bean("uploadDocumentWithoutLrNotificationGenerator")
    public List<BailNotificationGenerator> uploadDocumentWithoutLrNotificationGenerator(
        HomeOfficeBailDocumentUploadedPersonalisation homeOfficeBailDocumentUploadedPersonalisation,
        BailGovNotifyNotificationSender notificationSender,
        BailNotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new BailEmailNotificationGenerator(
                    newArrayList(homeOfficeBailDocumentUploadedPersonalisation),
                    notificationSender,
                    notificationIdAppender
            )
        );
    }

    @Bean("sendBailDirectionNotificationGenerator")
    public List<BailNotificationGenerator> sendBailDirectionNotificationGenerator(
        HomeOfficeBailDirectionSentPersonalisation homeOfficeBailDirectionSentPersonalisation,
        LegalRepresentativeBailDirectionSentPersonalisation legalRepresentativeBailDirectionSentPersonalisation,
        BailGovNotifyNotificationSender notificationSender,
        BailNotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new BailEmailNotificationGenerator(
                    newArrayList(homeOfficeBailDirectionSentPersonalisation,
                            legalRepresentativeBailDirectionSentPersonalisation),
                    notificationSender,
                    notificationIdAppender
            )
        );
    }

    @Bean("changeBailDirectionDueDateNotificationGenerator")
    public List<BailNotificationGenerator> changeBailDirectionDueDateNotificationGenerator(
        HomeOfficeBailChangeDirectionDueDatePersonalisation homeOfficeBailChangeDirectionDueDatePersonalisation,
        LegalRepresentativeBailChangeDirectionDueDatePersonalisation legalRepresentativeBailChangeDirectionDueDatePersonalisation,
        BailGovNotifyNotificationSender notificationSender,
        BailNotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new BailEmailNotificationGenerator(
                    newArrayList(homeOfficeBailChangeDirectionDueDatePersonalisation,
                            legalRepresentativeBailChangeDirectionDueDatePersonalisation),
                    notificationSender,
                    notificationIdAppender
            )
        );
    }

    @Bean("changeBailDirectionDueDateWithoutLrNotificationGenerator")
    public List<BailNotificationGenerator> changeBailDirectionDueDateWithoutLrNotificationGenerator(
        HomeOfficeBailChangeDirectionDueDatePersonalisation homeOfficeBailChangeDirectionDueDatePersonalisation,
        BailGovNotifyNotificationSender notificationSender,
        BailNotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new BailEmailNotificationGenerator(
                    newArrayList(homeOfficeBailChangeDirectionDueDatePersonalisation),
                    notificationSender,
                    notificationIdAppender
            )
        );
    }

    @Bean("editBailDocumentsNotificationGenerator")
    public List<BailNotificationGenerator> editBailDocumentsNotificationGenerator(
        HomeOfficeBailDocumentsEditedPersonalisation homeOfficeBailDocumentsEditedPersonalisation,
        LegalRepresentativeBailDocumentsEditedPersonalisation legalRepresentativeBailDocumentsEditedPersonalisation,
        BailGovNotifyNotificationSender notificationSender,
        BailNotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new BailEmailNotificationGenerator(
                newArrayList(homeOfficeBailDocumentsEditedPersonalisation,
                    legalRepresentativeBailDocumentsEditedPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("editBailDocumentsWithoutLrNotificationGenerator")
    public List<BailNotificationGenerator> editBailDocumentsWithoutLrNotificationGenerator(
        HomeOfficeBailDocumentsEditedPersonalisation homeOfficeBailDocumentsEditedPersonalisation,
        BailGovNotifyNotificationSender notificationSender,
        BailNotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new BailEmailNotificationGenerator(
                newArrayList(homeOfficeBailDocumentsEditedPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("editApplicationAfterSubmitNotificationGenerator")
    public List<BailNotificationGenerator> editApplicationAfterSubmitNotificationGenerator(
        LegalRepresentativeBailApplicationEditAfterSubmitPersonalisation legalRepresentativeBailApplicationEditAfterSubmitPersonalisation,
        HomeOfficeBailApplicationEditAfterSubmitPersonalisation homeOfficeBailApplicationEditAfterSubmitPersonalisation,
        BailGovNotifyNotificationSender notificationSender,
        BailNotificationIdAppender notificationIdAppender) {

        return List.of(
            new BailEmailNotificationGenerator(
                newArrayList(legalRepresentativeBailApplicationEditAfterSubmitPersonalisation,
                    homeOfficeBailApplicationEditAfterSubmitPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("editApplicationAfterSubmitWithoutLegalRepNotificationGenerator")
    public List<BailNotificationGenerator> editApplicationAfterSubmitWithoutLegalRepNotificationGenerator(
        HomeOfficeBailApplicationEditAfterSubmitPersonalisation homeOfficeBailApplicationEditAfterSubmitPersonalisation,
        BailGovNotifyNotificationSender notificationSender,
        BailNotificationIdAppender notificationIdAppender) {

        return List.of(
            new BailEmailNotificationGenerator(
                newArrayList(homeOfficeBailApplicationEditAfterSubmitPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("stopLegalRepresentingNotificationGenerator")
    public List<BailNotificationGenerator> stopLegalRepresentingNotificationGenerator(
            AdminOfficerBailStopLegalRepresentingPersonalisation adminOfficerBailStopLegalRepresentingPersonalisation,
            LegalRepresentativeBailStopLegalRepresentingPersonalisation legalRepresentativeBailStopLegalRepresentingPersonalisation,
            HomeOfficeBailStopLegalRepresentingPersonalisation homeOfficeBailStopLegalRepresentingPersonalisation,
            ApplicantBailStopLegalRepresentingPersonalisationSms applicantBailStopLegalRepresentingPersonalisationSms,
            BailGovNotifyNotificationSender notificationSender,
            BailNotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
                new BailEmailNotificationGenerator(
                        newArrayList(adminOfficerBailStopLegalRepresentingPersonalisation,
                                legalRepresentativeBailStopLegalRepresentingPersonalisation,
                                homeOfficeBailStopLegalRepresentingPersonalisation),
                        notificationSender,
                        notificationIdAppender
                ),
                new BailSmsNotificationGenerator(
                        newArrayList(applicantBailStopLegalRepresentingPersonalisationSms),
                        notificationSender,
                        notificationIdAppender
                )
        );
    }

    @Bean("nocChangedLegalRepNotificationGenerator")
    public List<BailNotificationGenerator> nocChangedLegalRepNotificationGenerator(
        LegalRepresentativeBailNocChangedLrPersonalisation legalRepresentativeBailNocChangedLrPersonalisation,
        AdminOfficerBailNocChangedLrPersonalisation adminOfficerBailNocChangedLrPersonalisation,
        HomeOfficeBailNocChangedLrPersonalisation homeOfficeBailNocChangedLrPersonalisation,
        ApplicantBailNocChangedLrPersonalisationSms applicantBailNocChangedLrPersonalisationSms,
        BailGovNotifyNotificationSender notificationSender,
        BailNotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new BailEmailNotificationGenerator(
                newArrayList(legalRepresentativeBailNocChangedLrPersonalisation,
                    adminOfficerBailNocChangedLrPersonalisation,
                    homeOfficeBailNocChangedLrPersonalisation),
                notificationSender,
                notificationIdAppender
            ),
            new BailSmsNotificationGenerator(
                newArrayList(applicantBailNocChangedLrPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }
}
