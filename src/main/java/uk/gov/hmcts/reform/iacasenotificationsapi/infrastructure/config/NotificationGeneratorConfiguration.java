package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.NotificationSender;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Message;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer.editdocument.CaseOfficerEditDocumentsPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice.linkunlinkappeal.HomeOfficeLinkAppealPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice.linkunlinkappeal.HomeOfficeUnlinkAppealPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative.linkunlinkappeal.LegalRepresentativeLinkAppealPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative.linkunlinkappeal.LegalRepresentativeUnlinkAppealPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.respondent.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.*;

@Configuration
public class NotificationGeneratorConfiguration {

    @Value("${featureFlag.homeOfficeGovNotifyEnabled}")
    private boolean isHomeOfficeGovNotifyEnabled;

    @Bean("forceCaseProgressionNotificationGenerator")
    public List<NotificationGenerator> forceCaseProgressionNotificationGenerator(
        RespondentForceCaseProgressionPersonalisation homeOfficePersonalisation,
        LegalRepresentativeRequestCaseBuildingPersonalisation legalRepresentativeRequestCaseBuildingPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Collections.singletonList(new EmailNotificationGenerator(
            newArrayList(homeOfficePersonalisation, legalRepresentativeRequestCaseBuildingPersonalisation),
            notificationSender,
            notificationIdAppender)
        );
    }

    @Bean("editDocumentsNotificationGenerator")
    public List<NotificationGenerator> editDocumentsNotificationGenerator(
        CaseOfficerEditDocumentsPersonalisation personalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Collections.singletonList(
            new EmailNotificationGenerator(newArrayList(personalisation), notificationSender, notificationIdAppender)
        );
    }

    @Bean("unlinkAppealNotificationGenerator")
    public List<NotificationGenerator> unlinkAppealNotificationGenerator(
        LegalRepresentativeUnlinkAppealPersonalisation legalRepresentativeUnlinkAppealPersonalisation,
        HomeOfficeUnlinkAppealPersonalisation homeOfficeUnlinkAppealPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Collections.singletonList(
            new EmailNotificationGenerator(
                newArrayList(legalRepresentativeUnlinkAppealPersonalisation, homeOfficeUnlinkAppealPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("linkAppealNotificationGenerator")
    public List<NotificationGenerator> linkAppealNotificationGenerator(
        LegalRepresentativeLinkAppealPersonalisation legalRepresentativeLinkAppealPersonalisation,
        HomeOfficeLinkAppealPersonalisation homeOfficeLinkAppealPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Collections.singletonList(
            new EmailNotificationGenerator(
                newArrayList(legalRepresentativeLinkAppealPersonalisation, homeOfficeLinkAppealPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("reListCaseNotificationGenerator")
    public List<NotificationGenerator> reListCaseNotificationGenerator(
        AdminOfficerReListCasePersonalisation adminOfficerReListCasePersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Collections.singletonList(
            new EmailNotificationGenerator(
                newArrayList(adminOfficerReListCasePersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("requestCaseEditNotificationGenerator")
    public List<NotificationGenerator> requestCaseEditNotificationGenerator(
        LegalRepresentativeRequestCaseEditPersonalisation legalRepresentativeRequestCaseEditPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Collections.singletonList(
            new EmailNotificationGenerator(
                Collections.singletonList(legalRepresentativeRequestCaseEditPersonalisation),
                notificationSender,
                notificationIdAppender)
        );
    }

    @Bean("endAppealNotificationGenerator")
    public List<NotificationGenerator> endAppealNotificationGenerator(
        HomeOfficeEndAppealPersonalisation homeOfficeEndAppealPersonalisation,
        LegalRepresentativeEndAppealPersonalisation legalRepresentativeEndAppealPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(homeOfficeEndAppealPersonalisation, legalRepresentativeEndAppealPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("appealOutcomeNotificationGenerator")
    public List<NotificationGenerator> appealOutcomeNotificationGenerator(
        HomeOfficeAppealOutcomePersonalisation homeOfficeAppealOutcomePersonalisation,
        LegalRepresentativeAppealOutcomePersonalisation legalRepresentativeAppealOutcomePersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        // RIA-3361 - sendDecisionAndReasons
        List<EmailNotificationPersonalisation> personalisations = isHomeOfficeGovNotifyEnabled
            ?  newArrayList(homeOfficeAppealOutcomePersonalisation, legalRepresentativeAppealOutcomePersonalisation)
            : newArrayList(legalRepresentativeAppealOutcomePersonalisation);


        return Arrays.asList(
            new EmailNotificationGenerator(
                personalisations,
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("legalRepChangeDirectionDueDateNotificationGenerator")
    public List<NotificationGenerator> legalRepChangeDirectionDueDateNotificationGenerator(
        LegalRepresentativeChangeDirectionDueDatePersonalisation legalRepresentativeChangeDirectionDueDatePersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(legalRepresentativeChangeDirectionDueDatePersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("appealOutcomeHomeOfficeNotificationFailedNotificationGenerator")
    public List<NotificationGenerator> appealOutcomeHomeOfficeNotificationFailedNotificationGenerator(
        CaseOfficerAppealOutcomeHomeOfficeNotificationFailedPersonalisation caseOfficerAppealOutcomeHomeOfficeNotificationFailedPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(
                    caseOfficerAppealOutcomeHomeOfficeNotificationFailedPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("respondentChangeDirectionDueDateNotificationGenerator")
    public List<NotificationGenerator> respondentChangeDirectionDueDateNotificationGenerator(
        RespondentChangeDirectionDueDatePersonalisation respondentChangeDirectionDueDatePersonalisation,
        LegalRepresentativeChangeDirectionDueDateOfHomeOfficePersonalisation legalRepresentativeChangeDirectionDueDateOfHomeOfficePersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(respondentChangeDirectionDueDatePersonalisation, legalRepresentativeChangeDirectionDueDateOfHomeOfficePersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("respondentChangeDirectionDueDateForHomeOfficeApiEventsNotificationGenerator")
    public List<NotificationGenerator> respondentChangeDirectionDueDateForHomeOfficeApiEventsNotificationGenerator(
        RespondentChangeDirectionDueDatePersonalisation respondentChangeDirectionDueDatePersonalisation,
        LegalRepresentativeChangeDirectionDueDateOfHomeOfficePersonalisation legalRepresentativeChangeDirectionDueDateOfHomeOfficePersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        // RIA-3116 - changeDirectionDueDate (requestEvidenceBundle, amendRequestBundle, requestRespondentReview, awaitingRespondentEvidence)
        List<EmailNotificationPersonalisation> personalisations = isHomeOfficeGovNotifyEnabled
            ?  newArrayList(respondentChangeDirectionDueDatePersonalisation, legalRepresentativeChangeDirectionDueDateOfHomeOfficePersonalisation)
            : newArrayList(legalRepresentativeChangeDirectionDueDateOfHomeOfficePersonalisation);

        return Arrays.asList(
            new EmailNotificationGenerator(
                personalisations,
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("bothPartiesChangeDirectionDueDateNotificationGenerator")
    public List<NotificationGenerator> bothPartiesChangeDirectionDueDateNotificationGenerator(
        LegalRepresentativeChangeDirectionDueDatePersonalisation legalRepresentativeChangeDirectionDueDatePersonalisation,
        RespondentChangeDirectionDueDatePersonalisation respondentChangeDirectionDueDatePersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(
                    legalRepresentativeChangeDirectionDueDatePersonalisation,
                    respondentChangeDirectionDueDatePersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("listCaseNotificationGenerator")
    public List<NotificationGenerator> listCaseNotificationGenerator(
        CaseOfficerListCasePersonalisation caseOfficerListCasePersonalisation,
        LegalRepresentativeListCasePersonalisation legalRepresentativeListCasePersonalisation,
        HomeOfficeListCasePersonalisation homeOfficeListCasePersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        // RIA-3361 - listCase
        List<EmailNotificationPersonalisation> personalisations = isHomeOfficeGovNotifyEnabled
            ?  newArrayList(caseOfficerListCasePersonalisation, legalRepresentativeListCasePersonalisation, homeOfficeListCasePersonalisation)
            : newArrayList(caseOfficerListCasePersonalisation, legalRepresentativeListCasePersonalisation);

        return Arrays.asList(
            new EmailNotificationGenerator(
                personalisations,
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("submitAppealAipNotificationGenerator")
    public List<NotificationGenerator> submitAppealAipNotificationGenerator(
        CaseOfficerSubmitAppealPersonalisation caseOfficerSubmitAppealPersonalisation,
        AppellantSubmitAppealPersonalisationSms appellantSubmitAppealPersonalisationSms,
        AppellantSubmitAppealPersonalisationEmail appellantSubmitAppealPersonalisationEmail,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(appellantSubmitAppealPersonalisationEmail, caseOfficerSubmitAppealPersonalisation),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(appellantSubmitAppealPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("submitAppealRepNotificationGenerator")
    public List<NotificationGenerator> submitAppealRepNotificationGenerator(
        CaseOfficerSubmitAppealPersonalisation caseOfficerSubmitAppealPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(caseOfficerSubmitAppealPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("submitAppealHoNotificationGenerator")
    public List<NotificationGenerator> submitAppealHoNotificationGenerator(
        HomeOfficeSubmitAppealPersonalisation homeOfficeSubmitAppealPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        // RIA-3631 - submitAppeal
        return Arrays.asList(
            new EmailNotificationGenerator(
                isHomeOfficeGovNotifyEnabled ? newArrayList(homeOfficeSubmitAppealPersonalisation) : emptyList(),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("submitCaseRepSubmitToRepNotificationGenerator")
    public List<NotificationGenerator> submitCaseRepSubmitToRepNotificationGenerator(
        LegalRepresentativeSubmitCasePersonalisation legalRepresentativeSubmitAppealPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Collections.singletonList(
            new EmailNotificationGenerator(
                newArrayList(legalRepresentativeSubmitAppealPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("submitAppealOutOfTimeAipNotificationGenerator")
    public List<NotificationGenerator> submitAppealOutOfTimeAipNotificationGenerator(
        CaseOfficerSubmitAppealPersonalisation caseOfficerSubmitAppealPersonalisation,
        AppellantSubmitAppealOutOfTimePersonalisationSms appellantSubmitAppealOutOfTimePersonalisationSms,
        AppellantSubmitAppealOutOfTimePersonalisationEmail appellantSubmitAppealOutOfTimePersonalisationEmail,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(appellantSubmitAppealOutOfTimePersonalisationEmail, caseOfficerSubmitAppealPersonalisation),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(appellantSubmitAppealOutOfTimePersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("submitCaseNotificationGenerator")
    public List<NotificationGenerator> submitCaseNotificationGenerator(
        CaseOfficerSubmitCasePersonalisation caseOfficerSubmitCasePersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Collections.singletonList(
            new EmailNotificationGenerator(
                newArrayList(caseOfficerSubmitCasePersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("uploadRespondentNotificationGenerator")
    public List<NotificationGenerator> uploadRespondentNotificationGenerator(
        LegalRepresentativeUploadRespondentEvidencePersonalisation legalRepresentativeUploadRespondentEvidencePersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(legalRepresentativeUploadRespondentEvidencePersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("requestReasonsForAppealAipNotificationGenerator")
    public List<NotificationGenerator> requestReasonsForAppealAipNotificationGenerator(
        AppellantRequestReasonsForAppealPersonalisationEmail appellantRequestReasonsForAppealPersonalisationEmail,
        AppellantRequestReasonsForAppealPersonalisationSms appellantRequestReasonsForAppealPersonalisationSms,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(appellantRequestReasonsForAppealPersonalisationEmail),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(appellantRequestReasonsForAppealPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("submitReasonsForAppealAipNotificationGenerator")
    public List<NotificationGenerator> submitReasonsForAppealAipNotificationGenerator(
        CaseOfficerReasonForAppealSubmittedPersonalisation caseOfficerReasonForAppealSubmittedPersonalisation,
        AppellantSubmitReasonsForAppealPersonalisationEmail appellantSubmitReasonsForAppealPersonalisationEmail,
        AppellantSubmitReasonsForAppealPersonalisationSms appellantSubmitReasonsForAppealPersonalisationSms,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(caseOfficerReasonForAppealSubmittedPersonalisation, appellantSubmitReasonsForAppealPersonalisationEmail),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(appellantSubmitReasonsForAppealPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("hearingRequirementsNotificationGenerator")
    public List<NotificationGenerator> hearingRequirementsNotificationGenerator(
        LegalRepresentativeHearingRequirementsPersonalisation legalRepresentativeHearingRequirementsPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(legalRepresentativeHearingRequirementsPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("requestHearingRequirementsNotificationGenerator")
    public List<NotificationGenerator> requestHearingRequirementsNotificationGenerator(
        LegalRepresentativeRequestHearingRequirementsPersonalisation legalRepresentativeRequestHearingRequirementsPersonalisation,
        CaseOfficerRequestHearingRequirementsPersonalisation caseOfficerRequestHearingRequirementsPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Collections.singletonList(
            new EmailNotificationGenerator(
                newArrayList(legalRepresentativeRequestHearingRequirementsPersonalisation, caseOfficerRequestHearingRequirementsPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("requestNewHearingRequirementsNotificationGenerator")
    public List<NotificationGenerator> requestNewHearingRequirementsNotificationGenerator(
        LegalRepresentativeRequestNewHearingRequirementsPersonalisation legalRepresentativeRequestNewHearingRequirementsPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Collections.singletonList(
            new EmailNotificationGenerator(
                newArrayList(legalRepresentativeRequestNewHearingRequirementsPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("addAppealNotificationGenerator")
    public List<NotificationGenerator> addAppealNotificationGenerator(
        LegalRepresentativeAddAppealPersonalisation legalRepresentativeAddAppealPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(legalRepresentativeAddAppealPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("respondentReviewNotificationGenerator")
    public List<NotificationGenerator> respondentReviewNotificationGenerator(
        RespondentDirectionPersonalisation respondentDirectionPersonalisation,
        LegalRepresentativeRespondentReviewPersonalisation legalRepresentativeRespondentReviewPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        // RIA-3631 - requestRespondentReview
        List<EmailNotificationPersonalisation> personalisations = isHomeOfficeGovNotifyEnabled
            ?  newArrayList(respondentDirectionPersonalisation, legalRepresentativeRespondentReviewPersonalisation)
            : newArrayList(legalRepresentativeRespondentReviewPersonalisation);


        return Collections.singletonList(
            new EmailNotificationGenerator(
                personalisations,
                notificationSender,
                notificationIdAppender
            )
        );
    }


    @Bean("respondentEvidenceAipNotificationGenerator")
    public List<NotificationGenerator> respondentEvidenceAipNotificationGenerator(
        RespondentEvidenceDirectionPersonalisation respondentEvidenceDirectionPersonalisation,
        AppellantRequestRespondentEvidencePersonalisationEmail appellantRequestRespondentEvidencePersonalisationEmail,
        AppellantRequestRespondentEvidencePersonalisationSms appellantRequestRespondentEvidencePersonalisationSms,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(respondentEvidenceDirectionPersonalisation, appellantRequestRespondentEvidencePersonalisationEmail),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(appellantRequestRespondentEvidencePersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("respondentEvidenceRepNotificationGenerator")
    public List<NotificationGenerator> respondentEvidenceRepNotificationGenerator(
        RespondentEvidenceDirectionPersonalisation respondentEvidenceDirectionPersonalisation,
        LegalRepresentativeRequestHomeOfficeBundlePersonalisation legalRepresentativeRequestHomeOfficeBundlePersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        // RIA-3631 - requestRespondentEvidence
        List<EmailNotificationPersonalisation> personalisations = isHomeOfficeGovNotifyEnabled
            ?  newArrayList(respondentEvidenceDirectionPersonalisation, legalRepresentativeRequestHomeOfficeBundlePersonalisation)
            : newArrayList(legalRepresentativeRequestHomeOfficeBundlePersonalisation);

        return Collections.singletonList(
            new EmailNotificationGenerator(
                personalisations,
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("respondentDirectionNotificationGenerator")
    public List<NotificationGenerator> respondentDirectionNotificationGenerator(
        RespondentNonStandardDirectionPersonalisation respondentNonStandardDirectionPersonalisation,
        LegalRepresentativeNonStandardDirectionOfHomeOfficePersonalisation legalRepresentativeNonStandardDirectionOfHomeOfficePersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(respondentNonStandardDirectionPersonalisation, legalRepresentativeNonStandardDirectionOfHomeOfficePersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("awaitingRespondentDirectionNotificationGenerator")
    public List<NotificationGenerator> awaitingRespondentDirectionNotificationGenerator(
        RespondentNonStandardDirectionPersonalisation respondentNonStandardDirectionPersonalisation,
        LegalRepresentativeNonStandardDirectionOfHomeOfficePersonalisation legalRepresentativeNonStandardDirectionOfHomeOfficePersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        // RIA-3631 sendDirection (awaitingRespondentEvidence only)
        List<EmailNotificationPersonalisation> personalisations = isHomeOfficeGovNotifyEnabled
            ?  newArrayList(respondentNonStandardDirectionPersonalisation, legalRepresentativeNonStandardDirectionOfHomeOfficePersonalisation)
            : newArrayList(legalRepresentativeNonStandardDirectionOfHomeOfficePersonalisation);

        return Arrays.asList(
            new EmailNotificationGenerator(
                personalisations,
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("legalRepDirectionNotificationGenerator")
    public List<NotificationGenerator> legalRepDirectionNotificationGenerator(
        RespondentNonStandardDirectionPersonalisation respondentNonStandardDirectionPersonalisation,
        LegalRepresentativeNonStandardDirectionPersonalisation legalRepresentativeNonStandardDirectionPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(respondentNonStandardDirectionPersonalisation, legalRepresentativeNonStandardDirectionPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("recordApplicationNotificationGenerator")
    public List<NotificationGenerator> recordApplicationNotificationGenerator(
        HomeOfficeRecordApplicationPersonalisation homeOfficeRecordApplicationPersonalisation,
        LegalRepresentativeRecordApplicationPersonalisation legalRepresentativeRecordApplicationPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(homeOfficeRecordApplicationPersonalisation, legalRepresentativeRecordApplicationPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("editCaseListingNotificationGenerator")
    public List<NotificationGenerator> editCaseListingNotificationGenerator(
        CaseOfficerEditListingPersonalisation caseOfficerEditListingPersonalisation,
        HomeOfficeEditListingPersonalisation homeOfficeEditListingPersonalisation,
        LegalRepresentativeEditListingPersonalisation legalRepresentativeEditListingPersonalisation,
        LegalRepresentativeEditListingNoChangePersonalisation legalRepresentativeEditListingNoChangePersonalisation,
        HomeOfficeEditListingNoChangePersonalisation homeOfficeEditListingNoChangePersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        // RIA-3631 - editCaseListing
        List<EmailNotificationPersonalisation> personalisations = isHomeOfficeGovNotifyEnabled
            ?  newArrayList(caseOfficerEditListingPersonalisation, homeOfficeEditListingPersonalisation, legalRepresentativeEditListingPersonalisation, legalRepresentativeEditListingNoChangePersonalisation, homeOfficeEditListingNoChangePersonalisation)
            : newArrayList(caseOfficerEditListingPersonalisation, legalRepresentativeEditListingPersonalisation, legalRepresentativeEditListingNoChangePersonalisation);

        return Arrays.asList(
            new EditListingEmailNotificationGenerator(
                personalisations,
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("uploadHomeOfficeAppealResponseNotificationGenerator")
    public List<NotificationGenerator> uploadHomeOfficeAppealResponseNotificationGenerator(
        CaseOfficerHomeOfficeResponseUploadedPersonalisation caseOfficerHomeOfficeResponseUploadedPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(caseOfficerHomeOfficeResponseUploadedPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("requestCaseBuildingNotificationGenerator")
    public List<NotificationGenerator> requestCaseBuildingNotificationGenerator(
        LegalRepresentativeRequestCaseBuildingPersonalisation legalRepresentativeRequestCaseBuildingPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(legalRepresentativeRequestCaseBuildingPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("requestResponseReviewNotificationGenerator")
    public List<NotificationGenerator> requestResponseReviewNotificationGenerator(
        LegalRepresentativeRequestResponseReviewPersonalisation legalRepresentativeRequestResponseReviewPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(legalRepresentativeRequestResponseReviewPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("respondentEvidenceSubmitted")
    public List<NotificationGenerator> respondentEvidenceSubmitted(
        CaseOfficerRespondentEvidenceSubmittedPersonalisation caseOfficerRespondentEvidenceSubmittedPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {
        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(caseOfficerRespondentEvidenceSubmittedPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("hearingBundleReadyNotificationGenerator")
    public List<NotificationGenerator> hearingBundleReadyNotificationGenerator(
        HomeOfficeHearingBundleReadyPersonalisation homeOfficeHearingBundleReadyPersonalisation,
        LegalRepresentativeHearingBundleReadyPersonalisation legalRepresentativeHearingBundleReadyPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        //RIA-3316 - Hearing Bundle Ready (generateHearingBundle, customiseHearingBundle)
        List<EmailNotificationPersonalisation> personalisations = isHomeOfficeGovNotifyEnabled
            ?  newArrayList(homeOfficeHearingBundleReadyPersonalisation, legalRepresentativeHearingBundleReadyPersonalisation)
            : newArrayList(legalRepresentativeHearingBundleReadyPersonalisation);

        return Arrays.asList(
            new EmailNotificationGenerator(
                personalisations,
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("hearingBundleFailedNotificationGenerator")
    public List<NotificationGenerator> hearingBundleFailedNotificationGenerator(
        CaseOfficerHearingBundleFailedPersonalisation caseOfficerHearingBundleFailedPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(caseOfficerHearingBundleFailedPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("asyncStitchingCompleteHomeOfficeNotificationFailedNotificationGenerator")
    public List<NotificationGenerator> asyncStitchingCompleteHomeOfficeNotificationFailed(
        CaseOfficerAsyncStitchingHomeOfficeNotificationFailedPersonalisation caseOfficerAsyncStitchingHomeOfficeNotificationFailedPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(
                    caseOfficerAsyncStitchingHomeOfficeNotificationFailedPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("submittedHearingRequirementsNotificationGenerator")
    public List<NotificationGenerator> submittedHearingRequirementsNotificationGenerator(
        CaseOfficerSubmittedHearingRequirementsPersonalisation caseOfficerSubmittedHearingRequirementsPersonalisation,
        LegalRepresentativeSubmittedHearingRequirementsPersonalisation legalRepresentativeSubmittedHearingRequirementsPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return Collections.singletonList(
            new EmailNotificationGenerator(
                newArrayList(caseOfficerSubmittedHearingRequirementsPersonalisation, legalRepresentativeSubmittedHearingRequirementsPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("adjustedHearingRequirementsNotificationGenerator")
    public List<NotificationGenerator> adjustedHearingRequirementsNotificationGenerator(
        AdminOfficerReviewHearingRequirementsPersonalisation adminOfficerReviewHearingRequirementsPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(adminOfficerReviewHearingRequirementsPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("withoutHearingRequirementsNotificationGenerator")
    public List<NotificationGenerator> withoutHearingRequirementsNotificationGenerator(
        AdminOfficerWithoutHearingRequirementsPersonalisation adminOfficerWithoutHearingRequirementsPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(adminOfficerWithoutHearingRequirementsPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("uploadAdditionalEvidence")
    public List<NotificationGenerator> uploadAdditionalEvidence(
        CaseOfficerUploadAdditionalEvidencePersonalisation caseOfficerUploadAdditionalEvidencePersonalisation,
        HomeOfficeUploadAdditionalEvidencePersonalisation homeOfficeUploadAdditionalEvidencePersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(caseOfficerUploadAdditionalEvidencePersonalisation, homeOfficeUploadAdditionalEvidencePersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("uploadAdditionalEvidenceHomeOffice")
    public List<NotificationGenerator> uploadAdditionalEvidenceHomeOffice(
        CaseOfficerUploadAdditionalEvidencePersonalisation caseOfficerUploadAdditionalEvidencePersonalisation,
        LegalRepresentativeUploadAdditionalEvidencePersonalisation legalRepresentativeUploadAdditionalEvidencePersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(caseOfficerUploadAdditionalEvidencePersonalisation, legalRepresentativeUploadAdditionalEvidencePersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("uploadAddendumEvidenceCaseOfficer")
    public List<NotificationGenerator> uploadAddendumEvidenceCaseOfficer(
        HomeOfficeUploadAddendumEvidencePersonalisation homeOfficeUploadAddendumEvidencePersonalisation,
        LegalRepresentativeUploadAddendumEvidencePersonalisation legalRepresentativeUploadAddendumEvidencePersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender

    ) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(homeOfficeUploadAddendumEvidencePersonalisation, legalRepresentativeUploadAddendumEvidencePersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("uploadAddendumEvidenceHomeOffice")
    public List<NotificationGenerator> uploadAddendumEvidenceHomeOffice(
        CaseOfficerUploadAddendumEvidencePersonalisation caseOfficerUploadAddendumEvidencePersonalisation,
        LegalRepresentativeUploadAddendumEvidencePersonalisation legalRepresentativeUploadAddendumEvidencePersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender

    ) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(caseOfficerUploadAddendumEvidencePersonalisation, legalRepresentativeUploadAddendumEvidencePersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("uploadAddendumEvidenceLegalRep")
    public List<NotificationGenerator> uploadAddendumEvidenceLegalRep(
        CaseOfficerUploadAddendumEvidencePersonalisation caseOfficerUploadAddendumEvidencePersonalisation,
        HomeOfficeUploadAddendumEvidencePersonalisation homeOfficeUploadAddendumEvidencePersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(caseOfficerUploadAddendumEvidencePersonalisation, homeOfficeUploadAddendumEvidencePersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }


    @Bean("uploadAddendumEvidenceAdminOfficer")
    public List<NotificationGenerator> uploadAddendumEvidenceAdminOfficer(
        CaseOfficerUploadAddendumEvidencePersonalisation caseOfficerUploadAddendumEvidencePersonalisation,
        HomeOfficeUploadAddendumEvidencePersonalisation homeOfficeUploadAddendumEvidencePersonalisation,
        LegalRepresentativeUploadAddendumEvidencePersonalisation legalRepresentativeUploadAddendumEvidencePersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender

    ) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(caseOfficerUploadAddendumEvidencePersonalisation, homeOfficeUploadAddendumEvidencePersonalisation, legalRepresentativeUploadAddendumEvidencePersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("changeToHearingRequirementsNotificationGenerator")
    public List<NotificationGenerator> changeToHearingRequirementsNotificationGenerator(
        AdminOfficerChangeToHearingRequirementsPersonalisation adminOfficerChangeToHearingRequirementsPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(adminOfficerChangeToHearingRequirementsPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("appealExitedOnlineNotificationGenerator")
    public List<NotificationGenerator> appealExitedOnlineNotificationGenerator(
        HomeOfficeAppealExitedOnlinePersonalisation homeOfficeAppealExitedOnlinePersonalisation,
        LegalRepresentativeAppealExitedOnlinePersonalisation legalRepresentativeAppealExitedOnlinePersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(homeOfficeAppealExitedOnlinePersonalisation, legalRepresentativeAppealExitedOnlinePersonalisation),
                notificationSender,
                notificationIdAppender)
        );
    }

    @Bean("changeHearingCentreNotificationGenerator")
    public List<NotificationGenerator> changeHearingCentreNotificationGenerator(
        LegalRepresentativeChangeHearingCentrePersonalisation legalRepresentativeChangeHearingCentrePersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(legalRepresentativeChangeHearingCentrePersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("changeHearingCentreNotificationGenerator")
    public List<NotificationGenerator> changeHearingCentreNotificationGenerator(
        LegalRepresentativeChangeHearingCentrePersonalisation legalRepresentativeChangeHearingCentrePersonalisation,
        CaseOfficerChangeHearingCentrePersonalisation caseOfficerChangeHearingCentrePersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(
                    legalRepresentativeChangeHearingCentrePersonalisation,
                    caseOfficerChangeHearingCentrePersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("ftpaSubmittedLegalRepNotificationGenerator")
    public List<NotificationGenerator> ftpaSubmittedLegalRep(
        LegalRepresentativeFtpaSubmittedPersonalisation legalRepresentativeFtpaSubmittedPersonalisation,
        AdminOfficerFtpaSubmittedPersonalisation adminOfficerFtpaSubmittedPersonalisation,
        RespondentAppellantFtpaSubmittedPersonalisation respondentAppellantFtpaSubmittedPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        // RIA-3316 - applyForFTPAAppellant
        List<EmailNotificationPersonalisation> personalisations = isHomeOfficeGovNotifyEnabled
            ?  newArrayList(legalRepresentativeFtpaSubmittedPersonalisation, adminOfficerFtpaSubmittedPersonalisation, respondentAppellantFtpaSubmittedPersonalisation)
            : newArrayList(legalRepresentativeFtpaSubmittedPersonalisation, adminOfficerFtpaSubmittedPersonalisation);


        return Arrays.asList(
            new EmailNotificationGenerator(
                personalisations,
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("ftpaSubmittedHomeOfficeNotificationFailedCaseOfficerNotificationGenerator")
    public List<NotificationGenerator> ftpaSubmittedHomeOfficeNotificationFailed(
        CaseOfficerFtpaSubmittedHomeOfficeNotificationFailedPersonalisation ftpaSubmittedHomeOfficeNotificationFailedPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(
                    ftpaSubmittedHomeOfficeNotificationFailedPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("ftpaSubmittedRespondentNotificationGenerator")
    public List<NotificationGenerator> ftpaSubmittedRespondent(
        RespondentFtpaSubmittedPersonalisation respondentFtpaSubmittedPersonalisation,
        AdminOfficerFtpaSubmittedPersonalisation adminOfficerFtpaSubmittedPersonalisation,
        LegalRepresentativeRespondentFtpaSubmittedPersonalisation legalRepresentativeRespondentFtpaSubmittedPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        // RIA-3316 - applyForFTPARespondent
        List<EmailNotificationPersonalisation> personalisations = isHomeOfficeGovNotifyEnabled
            ?  newArrayList(respondentFtpaSubmittedPersonalisation, adminOfficerFtpaSubmittedPersonalisation, legalRepresentativeRespondentFtpaSubmittedPersonalisation)
            : newArrayList(adminOfficerFtpaSubmittedPersonalisation, legalRepresentativeRespondentFtpaSubmittedPersonalisation);

        return Arrays.asList(
            new EmailNotificationGenerator(
                personalisations,
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("submitTimeExtensionAipNotificationGenerator")
    public List<NotificationGenerator> submitTimeExtensionAipNotificationGenerator(
        CaseOfficerSubmitTimeExtensionPersonalisation caseOfficerSubmitTimeExtensionPersonalisation,
        AppellantSubmitTimeExtensionPersonalisationEmail appellantSubmitTimeExtensionPersonalisationEmail,
        AppellantSubmitTimeExtensionPersonalisationSms appellantSubmitTimeExtensionPersonalisationSms,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(caseOfficerSubmitTimeExtensionPersonalisation, appellantSubmitTimeExtensionPersonalisationEmail),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(appellantSubmitTimeExtensionPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("reviewTimeExtensionGrantedGenerator")
    public List<NotificationGenerator> reviewTimeExtensionGrantedGenerator(
        AppellantReviewTimeExtensionGrantedPersonalisationEmail appellantReviewTimeExtensionGrantedPersonalisationEmail,
        AppellantReviewTimeExtensionGrantedPersonalisationSms appellantReviewTimeExtensionGrantedPersonalisationSms,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(appellantReviewTimeExtensionGrantedPersonalisationEmail),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(appellantReviewTimeExtensionGrantedPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("reviewTimeExtensionRefusedGenerator")
    public List<NotificationGenerator> reviewTimeExtensionRefusedGenerator(
        AppellantReviewTimeExtensionRefusedPersonalisationEmail appellantReviewTimeExtensionRefusedPersonalisationEmail,
        AppellantReviewTimeExtensionRefusedPersonalisationSms appellantReviewTimeExtensionRefusedPersonalisationSms,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(appellantReviewTimeExtensionRefusedPersonalisationEmail),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(appellantReviewTimeExtensionRefusedPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("requestClarifyingQuestionsAipNotificationGenerator")
    public List<NotificationGenerator> requestClarifyingQuestionsAipNotificationGenerator(
        AppellantRequestClarifyingQuestionsPersonalisationEmail appellantRequestClarifyingQuestionsPersonalisationEmail,
        AppellantRequestClarifyingQuestionsPersonalisationSms appellantRequestClarifyingQuestionsPersonalisationSms,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(appellantRequestClarifyingQuestionsPersonalisationEmail),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(appellantRequestClarifyingQuestionsPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("submitClarifyingQuestionAnswersNotificationGenerator")
    public List<NotificationGenerator> submitClarifyingQuestionAnswersNotificationGenerator(
        CaseOfficerClarifyingQuestionsAnswersSubmittedPersonalisation caseOfficerClarifyingQuestionsAnswersSubmittedPersonalisation,
        AppellantSubmitClarifyingQuestionAnswersPersonalisationSms appellantSubmitClarifyingQuestionAnswersPersonalisationSms,
        AppellantSubmitClarifyingQuestionAnswersPersonalisationEmail appellantSubmitClarifyingQuestionAnswersPersonalisationEmail,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(appellantSubmitClarifyingQuestionAnswersPersonalisationEmail, caseOfficerClarifyingQuestionsAnswersSubmittedPersonalisation),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(appellantSubmitClarifyingQuestionAnswersPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("forceCaseProgressionToCaseUnderReviewNotificationGenerator")
    public List<NotificationGenerator> forceCaseProgressionToCaseUnderReviewNotificationGenerator(
        LegalRepresentativeForceCaseProgressionToCaseUnderReviewPersonalisation forceCaseProgressionToCaseUnderReviewPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return Collections.singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    forceCaseProgressionToCaseUnderReviewPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("forceCaseToSubmitHearingRequirementsNotificationGenerator")
    public List<NotificationGenerator> forceCaseToSubmitHearingRequirementsNotificationGenerator(
        RespondentForceCaseToSubmitHearingRequirementsPersonalisation respondentForceCaseToSubmitHearingRequirementsPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return Collections.singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    respondentForceCaseToSubmitHearingRequirementsPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("adjournHearingWithoutDateNotificationGenerator")
    public List<NotificationGenerator> adjournHearingWithoutDateNotificationGenerator(
        LegalRepresentativeAdjournHearingWithoutDatePersonalisation legalRepresentativeAdjournHearingWithoutDatePersonalisation,
        RespondentAdjournHearingWithoutDatePersonalisation respondentAdjournHearingWithoutDatePersonalisation,
        CaseOfficerAdjournHearingWithoutDatePersonalisation caseOfficerAdjournHearingWithoutDatePersonalisation,
        AdminOfficerAdjournHearingWithoutDatePersonalisation adminOfficerAdjournHearingWithoutDatePersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        // RIA-3631 adjournHearingWithoutDate
        List<EmailNotificationPersonalisation> personalisations = isHomeOfficeGovNotifyEnabled
            ?  newArrayList(legalRepresentativeAdjournHearingWithoutDatePersonalisation, respondentAdjournHearingWithoutDatePersonalisation, caseOfficerAdjournHearingWithoutDatePersonalisation, adminOfficerAdjournHearingWithoutDatePersonalisation)
            : newArrayList(legalRepresentativeAdjournHearingWithoutDatePersonalisation, caseOfficerAdjournHearingWithoutDatePersonalisation, adminOfficerAdjournHearingWithoutDatePersonalisation);

        return Collections.singletonList(
            new EmailNotificationGenerator(
                personalisations,
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("requestCmaRequirementsAipNotificationGenerator")
    public List<NotificationGenerator> requestCmaRequirementsAipNotificationGenerator(
        AppellantRequestCmaRequirementsPersonalisationEmail appellantRequestCmaRequirementsPersonalisationEmail,
        AppellantRequestCmaRequirementsPersonalisationSms appellantRequestCmaRequirementsPersonalisationSms,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(appellantRequestCmaRequirementsPersonalisationEmail),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(appellantRequestCmaRequirementsPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("submitCmaRequirementsAipNotificationGenerator")
    public List<NotificationGenerator> submitCmaRequirementsAipNotificationGenerator(
        CaseOfficerCmaRequirementsSubmittedPersonalisation caseOfficerCmaRequirementsSubmittedPersonalisation,
        AppellantSubmitCmaRequirementsPersonalisationEmail appellantSubmitCmaRequirementsPersonalisationEmail,
        AppellantSubmitCmaRequirementsPersonalisationSms appellantSubmitCmaRequirementsPersonalisationSms,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(caseOfficerCmaRequirementsSubmittedPersonalisation, appellantSubmitCmaRequirementsPersonalisationEmail),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(appellantSubmitCmaRequirementsPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("bothPartiesNonStandardDirectionGenerator")
    public List<NotificationGenerator> bothPartiesNonStandardDirectionGenerator(
        RespondentNonStandardDirectionPersonalisation respondentNonStandardDirectionPersonalisation,
        LegalRepresentativeNonStandardDirectionPersonalisation legalRepresentativeNonStandardDirectionPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(respondentNonStandardDirectionPersonalisation, legalRepresentativeNonStandardDirectionPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("requestResponseAmendDirectionGenerator")
    public List<NotificationGenerator> requestResponseAmendDirectionGenerator(
            RespondentRequestResponseAmendPersonalisation respondentRequestResponseAmendPersonalisation,
            NotificationSender notificationSender,
            NotificationIdAppender notificationIdAppender) {

        // RIA-3631 requestResponseAmend
        return Arrays.asList(
                new EmailNotificationGenerator(
                        isHomeOfficeGovNotifyEnabled ? newArrayList(respondentRequestResponseAmendPersonalisation) : emptyList(),
                        notificationSender,
                        notificationIdAppender
                )
        );
    }

    @Bean("listCmaAipNotificationGenerator")
    public List<NotificationGenerator> listCmaAipNotificationGenerator(
        AppellantListCmaPersonalisationEmail appellantListCmaPersonalisationEmail,
        AppellantListCmaPersonalisationSms appellantListCmaPersonalisationSms,
        HomeOfficeListCmaPersonalisation homeOfficeListCmaPersonalisation,
        CaseOfficerListCmaPersonalisation caseOfficerListCmaPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(appellantListCmaPersonalisationEmail, homeOfficeListCmaPersonalisation, caseOfficerListCmaPersonalisation),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(appellantListCmaPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("editAppealAfterSubmitNotificationGenerator")
    public List<NotificationGenerator> editAppealAfterSubmitNotificationGenerator(
        LegalRepresentativeEditAppealAfterSubmitPersonalisation legalRepresentativeEditAppealAfterSubmitPersonalisation,
        RespondentEditAppealAfterSubmitPersonalisation respondentEditAppealAfterSubmitPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Collections.singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    legalRepresentativeEditAppealAfterSubmitPersonalisation,
                    respondentEditAppealAfterSubmitPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("ftpaApplicationDecisionRefusedOrNotAdmittedAppellantNotificationGenerator")
    public List<NotificationGenerator> ftpaApplicationDecisionRefusedOrNotAdmittedAppellantNotificationGenerator(
        HomeOfficeFtpaApplicationDecisionAppellantPersonalisation homeOfficeFtpaApplicationDecisionAppellantPersonalisation,
        LegalRepresentativeFtpaApplicationDecisionAppellantPersonalisation legalRepresentativeFtpaApplicationDecisionAppellantPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        //RIA-3116 leadership/resident judge decision
        List<EmailNotificationPersonalisation> personalisations = isHomeOfficeGovNotifyEnabled
            ?  newArrayList(homeOfficeFtpaApplicationDecisionAppellantPersonalisation, legalRepresentativeFtpaApplicationDecisionAppellantPersonalisation)
            : newArrayList(legalRepresentativeFtpaApplicationDecisionAppellantPersonalisation);

        return Arrays.asList(
            new EmailNotificationGenerator(
                personalisations,
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("ftpaApplicationDecisionGrantedOrPartiallyGrantedAppellantNotificationGenerator")
    public List<NotificationGenerator> ftpaApplicationDecisionGrantedOrPartiallyAppellantGrantedNotificationGenerator(
        HomeOfficeFtpaApplicationDecisionAppellantPersonalisation homeOfficeFtpaApplicationDecisionAppellantPersonalisation,
        LegalRepresentativeFtpaApplicationDecisionAppellantPersonalisation legalRepresentativeFtpaApplicationDecisionAppellantPersonalisation,
        AdminOfficerFtpaDecisionAppellantPersonalisation adminOfficerFtpaDecisionAppellantPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        //RIA-3116 leadership/resident judge decision
        List<EmailNotificationPersonalisation> personalisations = isHomeOfficeGovNotifyEnabled
            ?  newArrayList(homeOfficeFtpaApplicationDecisionAppellantPersonalisation, legalRepresentativeFtpaApplicationDecisionAppellantPersonalisation, adminOfficerFtpaDecisionAppellantPersonalisation)
            : newArrayList(legalRepresentativeFtpaApplicationDecisionAppellantPersonalisation, adminOfficerFtpaDecisionAppellantPersonalisation);

        return Arrays.asList(
            new EmailNotificationGenerator(
                personalisations,
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("ftpaApplicationDecisionRefusedOrNotAdmittedRespondentNotificationGenerator")
    public List<NotificationGenerator> ftpaApplicationDecisionRefusedOrNotAdmittedRespondentNotificationGenerator(
        HomeOfficeFtpaApplicationDecisionRespondentPersonalisation homeOfficeFtpaApplicationDecisionRespondentPersonalisation,
        LegalRepresentativeFtpaApplicationDecisionRespondentPersonalisation legalRepresentativeFtpaApplicationDecisionRespondentPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        // RIA-3361 leadershipJudgeFtpaDecision
        List<EmailNotificationPersonalisation> personalisations = isHomeOfficeGovNotifyEnabled
            ?  newArrayList(homeOfficeFtpaApplicationDecisionRespondentPersonalisation, legalRepresentativeFtpaApplicationDecisionRespondentPersonalisation)
            : newArrayList(legalRepresentativeFtpaApplicationDecisionRespondentPersonalisation);

        return Arrays.asList(
            new EmailNotificationGenerator(
                personalisations,
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("ftpaApplicationDecisionGrantedOrPartiallyGrantedRespondentNotificationGenerator")
    public List<NotificationGenerator> ftpaApplicationDecisionGrantedOrPartiallyRespondentGrantedNotificationGenerator(
        HomeOfficeFtpaApplicationDecisionRespondentPersonalisation homeOfficeFtpaApplicationDecisionRespondentPersonalisation,
        LegalRepresentativeFtpaApplicationDecisionRespondentPersonalisation legalRepresentativeFtpaApplicationDecisionRespondentPersonalisation,
        AdminOfficerFtpaDecisionRespondentPersonalisation adminOfficerFtpaDecisionRespondentPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        // RIA-3361 leadershipJudgeFtpaDecision
        List<EmailNotificationPersonalisation> personalisations = isHomeOfficeGovNotifyEnabled
            ?  newArrayList(homeOfficeFtpaApplicationDecisionRespondentPersonalisation, legalRepresentativeFtpaApplicationDecisionRespondentPersonalisation, adminOfficerFtpaDecisionRespondentPersonalisation)
            : newArrayList(legalRepresentativeFtpaApplicationDecisionRespondentPersonalisation, adminOfficerFtpaDecisionRespondentPersonalisation);

        return Arrays.asList(
            new EmailNotificationGenerator(
                personalisations,
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("ftpaDecisionHomeOfficeNotificationFailedNotificationGenerator")
    public List<NotificationGenerator> ftpaDecisionHomeOfficeNotificationFailedNotificationGenerator(
        CaseOfficerFtpaDecisionHomeOfficeNotificationFailedPersonalisation caseOfficerFtpaDecisionHomeOfficeNotificationFailedPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(
                    caseOfficerFtpaDecisionHomeOfficeNotificationFailedPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("ftpaApplicationDecisionReheardAppellantNotificationGenerator")
    public List<NotificationGenerator> ftpaApplicationDecisionReheardAppellantNotificationGenerator(
        HomeOfficeFtpaApplicationDecisionAppellantPersonalisation homeOfficeFtpaApplicationDecisionAppellantPersonalisation,
        LegalRepresentativeFtpaApplicationDecisionAppellantPersonalisation legalRepresentativeFtpaApplicationDecisionAppellantPersonalisation,
        CaseOfficerFtpaDecisionPersonalisation caseOfficerFtpaDecisionPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        // RIA-3116 reheard FTPA application (resident Judge)
        List<EmailNotificationPersonalisation> personalisations = isHomeOfficeGovNotifyEnabled
            ?  newArrayList(homeOfficeFtpaApplicationDecisionAppellantPersonalisation, legalRepresentativeFtpaApplicationDecisionAppellantPersonalisation, caseOfficerFtpaDecisionPersonalisation)
            : newArrayList(legalRepresentativeFtpaApplicationDecisionAppellantPersonalisation, caseOfficerFtpaDecisionPersonalisation);

        return Arrays.asList(
            new EmailNotificationGenerator(
                personalisations,
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("ftpaApplicationDecisionReheardRespondentNotificationGenerator")
    public List<NotificationGenerator> ftpaApplicationDecisionReheardRespondentNotificationGenerator(
        HomeOfficeFtpaApplicationDecisionRespondentPersonalisation homeOfficeFtpaApplicationDecisionRespondentPersonalisation,
        LegalRepresentativeFtpaApplicationDecisionRespondentPersonalisation legalRepresentativeFtpaApplicationDecisionRespondentPersonalisation,
        CaseOfficerFtpaDecisionPersonalisation caseOfficerFtpaDecisionPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        // RIA-3361 residentJudgeFtpaDecision
        List<EmailNotificationPersonalisation> personalisations = isHomeOfficeGovNotifyEnabled
            ?  newArrayList(homeOfficeFtpaApplicationDecisionRespondentPersonalisation, legalRepresentativeFtpaApplicationDecisionRespondentPersonalisation, caseOfficerFtpaDecisionPersonalisation)
            : newArrayList(legalRepresentativeFtpaApplicationDecisionRespondentPersonalisation, caseOfficerFtpaDecisionPersonalisation);

        return Arrays.asList(
            new EmailNotificationGenerator(
                personalisations,
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("submitAppealPaidNotificationGenerator")
    public List<NotificationGenerator> submitAppealPaidLegalRepNotificationHandler(
        LegalRepresentativeAppealSubmittedPaidPersonalisation legalRepresentativeAppealSubmittedPaidPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return Collections.singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    legalRepresentativeAppealSubmittedPaidPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("submitAppealLegalRepPayLaterNotificationGenerator")
    public List<NotificationGenerator> submitAppealLegalRepPayLaterNotificationHandler(
        LegalRepresentativeAppealSubmittedPayLaterPersonalisation legalRepresentativeAppealSubmittedPayLaterPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return Collections.singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                        legalRepresentativeAppealSubmittedPayLaterPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("submitAppealLegalRepNotificationGenerator")
    public List<NotificationGenerator> submitAppealLegalRepNotificationHandler(
        LegalRepresentativeAppealSubmittedPersonalisation legalRepresentativeAppealSubmittedPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return Collections.singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                        legalRepresentativeAppealSubmittedPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }


    @Bean("paymentPaidNotificationGenerator")
    public List<NotificationGenerator> paymentPaidLegalRepNotificationHandler(
        LegalRepresentativePaymentPaidPersonalisation legalRepresentativePaymentPaidPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return Collections.singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    legalRepresentativePaymentPaidPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("submitAppealPayOfflineNotificationGenerator")
    public List<NotificationGenerator> submitAppealPayOfflineNotificationHandler(
            LegalRepresentativeAppealSubmittedPayOfflinePersonalisation legalRepresentativeAppealSubmittedPayOfflinePersonalisation,
            AdminOfficerAppealSubmittedPayOfflinePersonalisation adminOfficerAppealSubmittedPayOfflinePersonalisation,
            NotificationSender notificationSender,
            NotificationIdAppender notificationIdAppender
    ) {

        return Collections.singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                        legalRepresentativeAppealSubmittedPayOfflinePersonalisation,
                        adminOfficerAppealSubmittedPayOfflinePersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("submitAppealPendingPaymentNotificationGenerator")
    public List<NotificationGenerator> submitAppealPendingPaymentNotificationHandler(
            LegalRepresentativeAppealSubmittedPendingPaymentPersonalisation legalRepresentativeAppealSubmittedPendingPaymentPersonalisation,
            HomeOfficeAppealSubmittedPendingPaymentPersonalisation homeOfficeAppealSubmittedPendingPaymentPersonalisation,
            AdminOfficerAppealSubmittedPendingPaymentPersonalisation adminOfficerAppealSubmittedPendingPaymentPersonalisation,
            NotificationSender notificationSender,
            NotificationIdAppender notificationIdAppender
    ) {

        // RIA-3631 - submitAppeal This needs to be changed as per ACs
        List<EmailNotificationPersonalisation> personalisations = isHomeOfficeGovNotifyEnabled
            ?  newArrayList(legalRepresentativeAppealSubmittedPendingPaymentPersonalisation, homeOfficeAppealSubmittedPendingPaymentPersonalisation, adminOfficerAppealSubmittedPendingPaymentPersonalisation)
            : newArrayList(legalRepresentativeAppealSubmittedPendingPaymentPersonalisation, adminOfficerAppealSubmittedPendingPaymentPersonalisation);

        return Collections.singletonList(
            new EmailNotificationGenerator(
                personalisations,
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("paymentPendingPaidLegalRepNotificationGenerator")
    public List<NotificationGenerator> paymentPendingPaidNotificationHandler(
            LegalRepresentativePendingPaymentPaidPersonalisation legalRepresentativePendingPaymentPaidPersonalisation,
            NotificationSender notificationSender,
            NotificationIdAppender notificationIdAppender
    ) {

        return Collections.singletonList(
                new EmailNotificationGenerator(
                        newArrayList(
                                legalRepresentativePendingPaymentPaidPersonalisation
                        ),
                        notificationSender,
                        notificationIdAppender
                )
        );
    }

    @Bean("paymentPendingPaidCaseOfficerNotificationGenerator")
    public List<NotificationGenerator> paymentPendingPaidCaseOfficerNotificationHandler(
            CaseOfficerPendingPaymentPaidPersonalisation caseOfficerPendingPaymentPaidPersonalisation,
            NotificationSender notificationSender,
            NotificationIdAppender notificationIdAppender
    ) {

        return Collections.singletonList(
                new EmailNotificationGenerator(
                        newArrayList(
                                caseOfficerPendingPaymentPaidPersonalisation
                        ),
                        notificationSender,
                        notificationIdAppender
                )
        );
    }

    @Bean("reinstateAppealNotificationGenerator")
    public List<NotificationGenerator> reinstateAppealNotificationHandler(
            LegalRepresentativeReinstateAppealPersonalisation legalRepresentativeReinstateAppealPersonalisation,
            HomeOfficeReinstateAppealPersonalisation homeOfficeReinstateAppealPersonalisation,
            NotificationSender notificationSender,
            NotificationIdAppender notificationIdAppender
    ) {

        return Collections.singletonList(
                new EmailNotificationGenerator(
                        newArrayList(
                                legalRepresentativeReinstateAppealPersonalisation,
                                homeOfficeReinstateAppealPersonalisation
                        ),
                        notificationSender,
                        notificationIdAppender
                )
        );
    }

    @Bean("makeAnApplicationNotificationGenerator")
    public List<NotificationGenerator> makeAnApplicationNotificationHandler(
            LegalRepresentativeMakeAnApplicationPersonalisation legalRepresentativeMakeApplicationPersonalisation,
            HomeOfficeMakeAnApplicationPersonalisation homeOfficeMakeAnApplicationPersonalisation,
            CaseOfficerMakeAnApplicationPersonalisation caseOfficerMakeAnApplicationPersonalisation,
            NotificationSender notificationSender,
            NotificationIdAppender notificationIdAppender
    ) {

        return Collections.singletonList(
                new EmailNotificationGenerator(
                        newArrayList(
                                legalRepresentativeMakeApplicationPersonalisation,
                                homeOfficeMakeAnApplicationPersonalisation,
                                caseOfficerMakeAnApplicationPersonalisation
                        ),
                        notificationSender,
                        notificationIdAppender
                )
        );
    }

    @Bean("decideAnApplicationNotificationGenerator")
    public List<NotificationGenerator> decideAnApplicationNotificationHandler(
        LegalRepresentativeDecideAnApplicationPersonalisation legalRepresentativeDecideAnApplicationPersonalisation,
        HomeOfficeDecideAnApplicationPersonalisation homeOfficeDecideAnApplicationPersonalisation,

        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return Collections.singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    legalRepresentativeDecideAnApplicationPersonalisation,
                    homeOfficeDecideAnApplicationPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("remissionDecisionApprovedNotificationGenerator")
    public List<NotificationGenerator> remissionDecisionApprovedNotificationHandler(
        AdminOfficerAppealRemissionApprovedPersonalisation adminOfficerAppealRemissionApprovedPersonalisation,
        LegalRepresentativeRemissionDecisionApprovedPersonalisation legalRepresentativeRemissionDecisionApprovedPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return Collections.singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    adminOfficerAppealRemissionApprovedPersonalisation,
                    legalRepresentativeRemissionDecisionApprovedPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("remissionDecisionPartiallyApprovedNotificationGenerator")
    public List<NotificationGenerator> remissionDecisionPartiallyApprovedNotificationHandler(
        AdminOfficerRemissionDecisionPartiallyApprovedPersonalisation adminOfficerRemissionDecisionPartiallyApprovedPersonalisation,
        LegalRepresentativeRemissionDecisionPartiallyApprovedPersonalisation legalRepresentativeRemissionDecisionPartiallyApprovedPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return Collections.singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    adminOfficerRemissionDecisionPartiallyApprovedPersonalisation,
                    legalRepresentativeRemissionDecisionPartiallyApprovedPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("remissionDecisionRejectedNotificationGenerator")
    public List<NotificationGenerator> remissionDecisionRejectedNotificationHandler(
        LegalRepresentativeRemissionDecisionRejectedPersonalisation legalRepresentativeRemissionDecisionRejectedPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return Collections.singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    legalRepresentativeRemissionDecisionRejectedPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("nocRequestDecisionNotificationGenerator")
    public List<NotificationGenerator> nocRequestDecisionNotificationHandler(
        LegalRepresentativeNocRequestDecisionPersonalisation legalRepresentativeNocRequestDecisionPersonalisation,
        HomeOfficeNocRequestDecisionPersonalisation homeOfficeNocRequestDecisionPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return Collections.singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    legalRepresentativeNocRequestDecisionPersonalisation,
                    homeOfficeNocRequestDecisionPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("removeRepresentationNotificationGenerator")
    public List<NotificationGenerator> removeRepresentationNotificationHandler(
        LegalRepresentativeRemoveRepresentationPersonalisation legalRepresentativeRemoveRepresentationPersonalisation,
        CaseOfficerRemoveRepresentationPersonalisation caseOfficerRemoveRepresentationPersonalisation,
        HomeOfficeRemoveRepresentationPersonalisation homeOfficeRemoveRepresentationPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return Collections.singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    legalRepresentativeRemoveRepresentationPersonalisation,
                    caseOfficerRemoveRepresentationPersonalisation,
                    homeOfficeRemoveRepresentationPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("removeRepresentationAppellantEmailNotificationGenerator")
    public List<NotificationGenerator> removeRepresentationAppellantEmailNotificationHandler(
        AppellantRemoveRepresentationPersonalisationEmail appellantRemoveRepresentationPersonalisationEmail,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return Collections.singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    appellantRemoveRepresentationPersonalisationEmail
                ),
                notificationSender,
                notificationIdAppender
            ) {
                @Override
                public Message getSuccessMessage() {
                    return new Message("header","body");
                }
            }
        );
    }

    @Bean("removeRepresentationAppellantSmsNotificationGenerator")
    public List<NotificationGenerator> removeRepresentationAppellantSmsNotificationHandler(
        AppellantRemoveRepresentationPersonalisationSms appellantRemoveRepresentationPersonalisationSms,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return Collections.singletonList(
            new SmsNotificationGenerator(
                newArrayList(appellantRemoveRepresentationPersonalisationSms),
                notificationSender,
                notificationIdAppender
            ) {
                @Override
                public Message getSuccessMessage() {
                    return new Message("header","body");
                }
            }
        );
    }

    @Bean("removeRepresentativeAppellantEmailNotificationGenerator")
    public List<NotificationGenerator> removeRepresentativeAppellantEmailNotificationHandler(
        AppellantRemoveRepresentationPersonalisationEmail appellantRemoveRepresentationPersonalisationEmail,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return Collections.singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    appellantRemoveRepresentationPersonalisationEmail
                ),
                notificationSender,
                notificationIdAppender
            ) {
                @Override
                public Message getSuccessMessage() {
                    return new Message("header","body");
                }
            }
        );
    }

    @Bean("removeRepresentativeAppellantSmsNotificationGenerator")
    public List<NotificationGenerator> removeRepresentativeAppellantSmsNotificationHandler(
        AppellantRemoveRepresentationPersonalisationSms appellantRemoveRepresentationPersonalisationSms,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return Collections.singletonList(
            new SmsNotificationGenerator(
                newArrayList(appellantRemoveRepresentationPersonalisationSms),
                notificationSender,
                notificationIdAppender
            ) {
                @Override
                public Message getSuccessMessage() {
                    return new Message("header","body");
                }
            }
        );
    }

    @Bean("requestFeeRemissionNotificationGenerator")
    public List<NotificationGenerator> requestFeeRemissionNotificationHandler(
        LegalRepresentativeRequestFeeRemissionPersonalisation legalRepresentativeRequestFeeRemissionPersonalisation,
        AdminOfficerRequestFeeRemissionPersonalisation adminOfficerRequestFeeRemissionPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return Collections.singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    legalRepresentativeRequestFeeRemissionPersonalisation,
                    adminOfficerRequestFeeRemissionPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("caseOfficerManageFeeUpdateGenerator")
    public List<NotificationGenerator> manageFeeUpdateNotificationHandler(
        CaseOfficerManageFeeUpdatePersonalisation caseOfficerManageFeeUpdatePersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {
        return Collections.singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    caseOfficerManageFeeUpdatePersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }


    @Bean("nocRequestDecisionAppellantEmailNotificationGenerator")
    public List<NotificationGenerator> nocRequestDecisionAppellantEmailNotificationHandler(
        AppellantNocRequestDecisionPersonalisationEmail appellantNocRequestDecisionPersonalisationEmail,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return Collections.singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    appellantNocRequestDecisionPersonalisationEmail
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("nocRequestDecisionAppellantSmsNotificationGenerator")
    public List<NotificationGenerator> nocRequestDecisionAppellantSmsNotificationHandler(
        AppellantNocRequestDecisionPersonalisationSms appellantNocRequestDecisionPersonalisationSms,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return Collections.singletonList(
            new SmsNotificationGenerator(
                newArrayList(
                    appellantNocRequestDecisionPersonalisationSms
                ),
                notificationSender,
                notificationIdAppender

            ) {
                @Override
                public Message getSuccessMessage() {
                    return new Message("header","body");
                }
            }
        );
    }

    @Bean("submitAppealAppellantEmailNotificationGenerator")
    public List<NotificationGenerator> submitAppealAppellantEmailNotificationGenerator(
        AppellantSubmitAppealPersonalisationEmail appellantSubmitAppealPersonalisationEmail,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(appellantSubmitAppealPersonalisationEmail),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("submitAppealAppellantSmsNotificationGenerator")
    public List<NotificationGenerator> submitAppealAppellantSmsNotificationGenerator(
        AppellantSubmitAppealPersonalisationSms appellantSubmitAppealPersonalisationSms,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new SmsNotificationGenerator(
                newArrayList(appellantSubmitAppealPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("manageFeeUpdateRefundInstructedNotificationGenerator")
    public List<NotificationGenerator> manageFeeUpdateRefundInstructedNotificationHandler(
        LegalRepresentativeManageFeeUpdatePersonalisation legalRepresentativeManageFeeUpdatePersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return Collections.singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    legalRepresentativeManageFeeUpdatePersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("recordOfTimeDecisionCanProceedEmailNotificationGenerator")
    public List<NotificationGenerator> recordOfTimeDecisionCanProceedEmailNotificationHandler(
        LegalRepresentativeRecordOutOfTimeDecisionCanProceed legalRepresentativeRecordOutOfTimeDecisionCanProceed,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Collections.singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    legalRepresentativeRecordOutOfTimeDecisionCanProceed
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("recordOfTimeDecisionCannotProceedEmailNotificationGenerator")
    public List<NotificationGenerator> recordOfTimeDecisionCannotProceedEmailNotificationHandler(
        LegalRepresentativeRecordOutOfTimeDecisionCannotProceed legalRepresentativeRecordOutOfTimeDecisionCannotProceed,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Collections.singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    legalRepresentativeRecordOutOfTimeDecisionCannotProceed
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }
}
