package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Message;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer.AdminOfficerAdjournHearingWithoutDateNonDetainedPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer.AdminOfficerAdjournHearingWithoutDatePersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer.AdminOfficerAppealOutcomePersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer.AdminOfficerAppealSubmittedPayOfflinePersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer.AdminOfficerChangeToHearingRequirementsPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer.AdminOfficerDecidedOrEndedAppealPendingPayment;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer.AdminOfficerEditPaymentMethodPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer.AdminOfficerFtpaDecisionAppellantPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer.AdminOfficerFtpaDecisionRespondentPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer.AdminOfficerRecordAdjournmentDetailsPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer.AdminOfficerReListCasePersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer.AdminOfficerReviewHearingRequirementsPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer.AdminOfficerUpperTribunalBundleFailedPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer.AdminOfficerWithoutHearingRequirementsPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.letter.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.applyforcosts.AddEvidenceForCostsSubmittedSubmitterPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.applyforcosts.AdditionalEvidenceSubmittedOtherPartyNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.applyforcosts.ApplyForCostsApplicantPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.applyforcosts.ApplyForCostsRespondentPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.applyforcosts.ConsiderMakingCostOrderHoPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.applyforcosts.ConsiderMakingCostOrderLegalRepPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.applyforcosts.DecideCostsHomeOfficePersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.applyforcosts.DecideCostsLegalRepPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.applyforcosts.RespondToCostsApplicantPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.applyforcosts.RespondToCostsRespondentPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer.CaseOfficerAdjournHearingWithoutDatePersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer.CaseOfficerAppealOutcomeHomeOfficeNotificationFailedPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer.CaseOfficerAsyncStitchingHomeOfficeNotificationFailedPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer.CaseOfficerChangeHearingCentrePersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer.CaseOfficerClarifyingQuestionsAnswersSubmittedPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer.CaseOfficerCmaRequirementsSubmittedPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer.CaseOfficerEditListingPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer.CaseOfficerFtpaDecisionHomeOfficeNotificationFailedPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer.CaseOfficerFtpaDecisionPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer.CaseOfficerFtpaSubmittedHomeOfficeNotificationFailedPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer.CaseOfficerHearingBundleFailedPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer.CaseOfficerHomeOfficeResponseUploadedPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer.CaseOfficerListCasePersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer.CaseOfficerListCmaPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer.CaseOfficerMakeAnApplicationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer.CaseOfficerManageFeeUpdatePersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer.CaseOfficerPendingPaymentPaidPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer.CaseOfficerReasonForAppealSubmittedPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer.CaseOfficerRecordAdjournmentDetailsPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer.CaseOfficerRemoveRepresentationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer.CaseOfficerRequestHearingRequirementsPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer.CaseOfficerRespondentEvidenceSubmittedPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer.CaseOfficerSubmitAppealPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer.CaseOfficerSubmitCasePersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer.CaseOfficerSubmittedHearingRequirementsPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer.CaseOfficerUploadAddendumEvidencePersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer.CaseOfficerUploadAdditionalEvidencePersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer.editdocument.CaseOfficerEditDocumentsPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.detentionengagementteam.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice.HomeOfficeAdaReviewHearingRequirementsPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice.HomeOfficeAdaSuitabilityPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice.HomeOfficeAdjournHearingWithoutDateNonDetainedPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice.HomeOfficeAppealExitedOnlinePersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice.HomeOfficeAppealOutcomePersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice.HomeOfficeAppealSubmittedPayOfflinePersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice.HomeOfficeAppealSubmittedPendingPaymentPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice.HomeOfficeCaseLinkPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice.HomeOfficeCaseUnlinkPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice.HomeOfficeDecideAnApplicationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice.HomeOfficeDecisionWithoutHearingPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice.HomeOfficeEditListingNoChangePersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice.HomeOfficeEditListingPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice.HomeOfficeEndAppealAutomaticallyPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice.HomeOfficeEndAppealPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice.HomeOfficeFtpaApplicationDecidedRule31Rule32Personalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice.HomeOfficeFtpaApplicationDecisionAppellantPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice.HomeOfficeFtpaApplicationDecisionRespondentPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice.HomeOfficeGenerateHearingBundlePersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice.HomeOfficeHearingBundleReadyPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice.HomeOfficeListCasePersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice.HomeOfficeListCmaPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice.HomeOfficeMakeAnApplicationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice.HomeOfficeMarkAppealAsAdaPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice.HomeOfficeMarkAppealAsDetainedPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice.HomeOfficeMarkAppealAsRemittedPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice.HomeOfficeMarkAppealReadyForUtTransferPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice.HomeOfficeNocRequestDecisionPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice.HomeOfficeRecordApplicationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice.HomeOfficeReinstateAppealPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice.HomeOfficeRemoveDetentionStatusPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice.HomeOfficeRemoveRepresentationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice.HomeOfficeSubmitAppealPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice.HomeOfficeTransferOutOfAdaPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice.HomeOfficeUploadAddendumEvidencePersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice.HomeOfficeUploadAdditionalEvidencePersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice.linkunlinkappeal.HomeOfficeLinkAppealPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice.linkunlinkappeal.HomeOfficeUnlinkAppealPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative.letter.LegalRepresentativeMarkAppealAsDetainedLetterPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative.letter.LegalRepresentativeRemoveDetentionStatusLetterPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative.linkunlinkappeal.LegalRepresentativeLinkAppealPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative.linkunlinkappeal.LegalRepresentativeUnlinkAppealPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.respondent.RespondentAdjournHearingWithoutDatePersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.respondent.RespondentAppellantFtpaSubmittedPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.respondent.RespondentChangeDirectionDueDatePersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.respondent.RespondentDirectionPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.respondent.RespondentEditAppealAfterSubmitPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.respondent.RespondentEvidenceDirectionPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.respondent.RespondentForceCaseProgressionPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.respondent.RespondentForceCaseToCaseUnderReviewPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.respondent.RespondentForceCaseToSubmitHearingRequirementsPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.respondent.RespondentFtpaSubmittedPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.respondent.RespondentNonStandardDirectionOfAppellantPersonalization;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.respondent.RespondentNonStandardDirectionPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.respondent.RespondentRecordAdjournmentDetailsPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.respondent.RespondentReheardUnderRule35PersonalisationEmail;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.respondent.RespondentRequestResponseAmendPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.respondent.RespondentTurnOnNotificationsPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.respondent.RespondentUpdateTribunalDecisionRule31PersonalisationEmail;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.respondent.RespondentUpdateTribunalDecisionRule32PersonalisationEmail;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.uppertribunal.UpperTribunalMarkAsReadyForUtTransferPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.EditListingEmailNotificationGenerator;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.EmailNotificationGenerator;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.EmailWithLinkNotificationGenerator;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.LetterNotificationGenerator;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.NotificationGenerator;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.NotificationIdAppender;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.PrecompiledLetterNotificationGenerator;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.SmsNotificationGenerator;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.GovNotifyNotificationSender;

@Configuration
@Slf4j
public class NotificationGeneratorConfiguration {

    @Value("${featureFlag.homeOfficeGovNotifyEnabled}")
    private boolean isHomeOfficeGovNotifyEnabled;


    @Bean("forceCaseProgressionNotificationGenerator")
    public List<NotificationGenerator> forceCaseProgressionNotificationGenerator(
        RespondentForceCaseProgressionPersonalisation homeOfficePersonalisation,
        LegalRepresentativeRequestCaseBuildingPersonalisation legalRepresentativeRequestCaseBuildingPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return singletonList(new EmailNotificationGenerator(
            newArrayList(homeOfficePersonalisation, legalRepresentativeRequestCaseBuildingPersonalisation),
            notificationSender,
            notificationIdAppender)
        );
    }

    @Bean("editDocumentsNotificationGenerator")
    public List<NotificationGenerator> editDocumentsNotificationGenerator(
        CaseOfficerEditDocumentsPersonalisation personalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return singletonList(
            new EmailNotificationGenerator(newArrayList(personalisation), notificationSender, notificationIdAppender)
        );
    }

    @Bean("caseLinkAppealNotificationGenerator")
    public List<NotificationGenerator> caseLinkAppealNotificationGenerator(
        LegalRepresentativeCaseLinkAppealPersonalisation legalRepresentativeCaseLinkAppealPersonalisation,
        HomeOfficeCaseLinkPersonalisation homeOfficeCaseLinkPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(legalRepresentativeCaseLinkAppealPersonalisation, homeOfficeCaseLinkPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("caseUnlinkAppealNotificationGenerator")
    public List<NotificationGenerator> caseUnlinkAppealNotificationGenerator(
        LegalRepresentativeCaseUnlinkAppealPersonalisation legalRepresentativeCaseUnlinkAppealPersonalisation,
        HomeOfficeCaseUnlinkPersonalisation homeOfficeCaseUnlinkPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(legalRepresentativeCaseUnlinkAppealPersonalisation, homeOfficeCaseUnlinkPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("unlinkAppealNotificationGenerator")
    public List<NotificationGenerator> unlinkAppealNotificationGenerator(
        LegalRepresentativeUnlinkAppealPersonalisation legalRepresentativeUnlinkAppealPersonalisation,
        HomeOfficeUnlinkAppealPersonalisation homeOfficeUnlinkAppealPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(legalRepresentativeUnlinkAppealPersonalisation, homeOfficeUnlinkAppealPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("unlinkAppealAppellantNotificationGenerator")
    public List<NotificationGenerator> unlinkAppealAppellantNotificationGenerator(
        HomeOfficeUnlinkAppealPersonalisation homeOfficeUnlinkAppealPersonalisation,
        AppellantUnlinkAppealPersonalisationEmail appellantUnlinkAppealPersonalisationEmail,
        AppellantUnlinkAppealPersonalisationSms appellantUnlinkAppealPersonalisationSms,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(homeOfficeUnlinkAppealPersonalisation, appellantUnlinkAppealPersonalisationEmail),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(appellantUnlinkAppealPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("linkAppealNotificationGenerator")
    public List<NotificationGenerator> linkAppealNotificationGenerator(
        LegalRepresentativeLinkAppealPersonalisation legalRepresentativeLinkAppealPersonalisation,
        HomeOfficeLinkAppealPersonalisation homeOfficeLinkAppealPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(legalRepresentativeLinkAppealPersonalisation, homeOfficeLinkAppealPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("linkAppealAppellantNotificationGenerator")
    public List<NotificationGenerator> linkAppealAppellantNotificationGenerator(
        HomeOfficeLinkAppealPersonalisation homeOfficeLinkAppealPersonalisation,
        AppellantLinkAppealPersonalisationEmail appellantLinkAppealPersonalisationEmail,
        AppellantLinkAppealPersonalisationSms appellantLinkAppealPersonalisationSms,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(homeOfficeLinkAppealPersonalisation, appellantLinkAppealPersonalisationEmail),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(appellantLinkAppealPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("reListCaseNotificationGenerator")
    public List<NotificationGenerator> reListCaseNotificationGenerator(
        AdminOfficerReListCasePersonalisation adminOfficerReListCasePersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return singletonList(
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
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return singletonList(
            new EmailNotificationGenerator(
                singletonList(legalRepresentativeRequestCaseEditPersonalisation),
                notificationSender,
                notificationIdAppender)
        );
    }

    @Bean("endAppealNotificationGenerator")
    public List<NotificationGenerator> endAppealNotificationGenerator(
        HomeOfficeEndAppealPersonalisation homeOfficeEndAppealPersonalisation,
        LegalRepresentativeEndAppealPersonalisation legalRepresentativeEndAppealPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(homeOfficeEndAppealPersonalisation, legalRepresentativeEndAppealPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("endAppealInternalHoNotificationGenerator")
    public List<NotificationGenerator> endAppealInternalHoNotificationGenerator(
        HomeOfficeEndAppealPersonalisation homeOfficeEndAppealPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(homeOfficeEndAppealPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("endAppealAipEmailRespondentNotificationGenerator")
    public List<NotificationGenerator> endAppealAipEmailRespondentNotificationGenerator(
        HomeOfficeEndAppealPersonalisation homeOfficeEndAppealPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(homeOfficeEndAppealPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("endAppealAipSmsAppellantNotificationGenerator")
    public List<NotificationGenerator> endAppealAipSmsAppellantNotificationGenerator(
        AppellantEndAppealPersonalisationSms appellantEndAppealPersonalisationSms,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new SmsNotificationGenerator(
                newArrayList(appellantEndAppealPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )

        );
    }

    @Bean("endAppealAipEmailAppellantNotificationGenerator")
    public List<NotificationGenerator> endAppealAipEmailAppellantNotificationGenerator(
        AppellantEndAppealPersonalisationEmail appellantEndAppealPersonalisationEmail,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(appellantEndAppealPersonalisationEmail),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("appealOutcomeNotificationGenerator")
    public List<NotificationGenerator> appealOutcomeNotificationGenerator(
        HomeOfficeAppealOutcomePersonalisation homeOfficeAppealOutcomePersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        List<EmailNotificationPersonalisation> personalisations = isHomeOfficeGovNotifyEnabled
            ? newArrayList(homeOfficeAppealOutcomePersonalisation)
            : newArrayList();

        return Arrays.asList(
            new EmailNotificationGenerator(
                personalisations,
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("appealOutcomeAdminNotificationGenerator")
    public List<NotificationGenerator> appealOutcomeAdminNotificationGenerator(
        AdminOfficerAppealOutcomePersonalisation adminOfficerAppealOutcomePersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(adminOfficerAppealOutcomePersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("appealOutcomeRepNotificationGenerator")
    public List<NotificationGenerator> appealOutcomeRepNotificationGenerator(
        LegalRepresentativeAppealOutcomePersonalisation legalRepresentativeAppealOutcomePersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(legalRepresentativeAppealOutcomePersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("appealOutcomeAipNotificationGenerator")
    public List<NotificationGenerator> appealOutcomeAipNotificationGenerator(
        AppellantAppealOutcomePersonalisationEmail appellantAppealOutcomePersonalisationEmail,
        AppellantAppealOutcomePersonalisationSms appellantAppealOutcomePersonalisationSms,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(appellantAppealOutcomePersonalisationEmail),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(appellantAppealOutcomePersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("appealDecidedOrEndedPendingPaymentGenerator")
    public List<NotificationGenerator> appealDecidedOrEndedPendingPaymentGenerator(
        AdminOfficerDecidedOrEndedAppealPendingPayment adminOfficerDecidedOrEndedAppealPendingPayment,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(adminOfficerDecidedOrEndedAppealPendingPayment),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("legalRepChangeDirectionDueDateNotificationGenerator")
    public List<NotificationGenerator> legalRepChangeDirectionDueDateNotificationGenerator(
        LegalRepresentativeChangeDirectionDueDatePersonalisation legalRepresentativeChangeDirectionDueDatePersonalisation,
        GovNotifyNotificationSender notificationSender,
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
        GovNotifyNotificationSender notificationSender,
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
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(respondentChangeDirectionDueDatePersonalisation, legalRepresentativeChangeDirectionDueDateOfHomeOfficePersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("internalLrRespondentChangeDirectionDueDateNotificationGenerator")
    public List<NotificationGenerator> internalLrRespondentChangeDirectionDueDateNotificationGenerator(
            RespondentChangeDirectionDueDatePersonalisation respondentChangeDirectionDueDatePersonalisation,
            GovNotifyNotificationSender notificationSender,
            NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
                new EmailNotificationGenerator(
                        newArrayList(respondentChangeDirectionDueDatePersonalisation),
                        notificationSender,
                        notificationIdAppender
                )
        );
    }

    // An Appellant notification to be sent similar to LR once the templates are ready in future
    @Bean("respondentChangeDirectionDueDateAipNotificationGenerator")
    public List<NotificationGenerator> respondentChangeDirectionDueDateAipNotificationGenerator(
        RespondentChangeDirectionDueDatePersonalisation respondentChangeDirectionDueDatePersonalisation,
        AppellantChangeDirectionDueDateOfHomeOfficePersonalisationEmail appellantChangeDirectionDueDateOfHomeOfficePersonalisationEmail,
        AppellantChangeDirectionDueDateOfHomeOfficePersonalisationSms appellantChangeDirectionDueDateOfHomeOfficePersonalisationSms,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(respondentChangeDirectionDueDatePersonalisation, appellantChangeDirectionDueDateOfHomeOfficePersonalisationEmail),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(appellantChangeDirectionDueDateOfHomeOfficePersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("appellantChangeDirectionDueDateAipNotificationGenerator")
    public List<NotificationGenerator> appellantChangeDirectionDueDateAipNotificationGenerator(
        RespondentChangeDirectionDueDatePersonalisation respondentChangeDirectionDueDatePersonalisation,
        AppellantChangeDirectionDueDateOfAppellantPersonalisationEmail appellantChangeDirectionDueDateOfAppellantPersonalisationEmail,
        AppellantChangeDirectionDueDateOfAppellantPersonalisationSms appellantChangeDirectionDueDateOfAppellantPersonalisationSms,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                List.of(
                    appellantChangeDirectionDueDateOfAppellantPersonalisationEmail,
                    respondentChangeDirectionDueDatePersonalisation
                ),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                singletonList(appellantChangeDirectionDueDateOfAppellantPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("appellantAndRespondentChangeDirectionDueDateAipNotificationGenerator")
    public List<NotificationGenerator> appellantAndRespondentChangeDirectionDueDateAipNotificationGenerator(
        RespondentChangeDirectionDueDatePersonalisation respondentChangeDirectionDueDatePersonalisation,
        AppellantChangeDirectionDueDateOfAppellantPersonalisationEmail appellantChangeDirectionDueDateOfAppellantPersonalisationEmail,
        AppellantChangeDirectionDueDateOfAppellantPersonalisationSms appellantChangeDirectionDueDateOfAppellantPersonalisationSms,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                List.of(
                    appellantChangeDirectionDueDateOfAppellantPersonalisationEmail,
                    respondentChangeDirectionDueDatePersonalisation
                ),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                singletonList(appellantChangeDirectionDueDateOfAppellantPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("respondentChangeDirectionDueDateForHomeOfficeApiEventsNotificationGenerator")
    public List<NotificationGenerator> respondentChangeDirectionDueDateForHomeOfficeApiEventsNotificationGenerator(
        RespondentChangeDirectionDueDatePersonalisation respondentChangeDirectionDueDatePersonalisation,
        LegalRepresentativeChangeDirectionDueDateOfHomeOfficePersonalisation legalRepresentativeChangeDirectionDueDateOfHomeOfficePersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        // RIA-3116 - changeDirectionDueDate (requestEvidenceBundle, amendRequestBundle, requestRespondentReview, awaitingRespondentEvidence)
        List<EmailNotificationPersonalisation> personalisations = isHomeOfficeGovNotifyEnabled
            ? newArrayList(respondentChangeDirectionDueDatePersonalisation, legalRepresentativeChangeDirectionDueDateOfHomeOfficePersonalisation)
            : newArrayList(legalRepresentativeChangeDirectionDueDateOfHomeOfficePersonalisation);

        return Arrays.asList(
            new EmailNotificationGenerator(
                personalisations,
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("internalLrRespondentChangeDirectionDueDateForHomeOfficeApiEventsNotificationGenerator")
    public List<NotificationGenerator> internalLrRespondentChangeDirectionDueDateForHomeOfficeApiEventsNotificationGenerator(
            RespondentChangeDirectionDueDatePersonalisation respondentChangeDirectionDueDatePersonalisation,
            GovNotifyNotificationSender notificationSender,
            NotificationIdAppender notificationIdAppender) {

        // RIA-3116 - changeDirectionDueDate (requestEvidenceBundle, amendRequestBundle, requestRespondentReview, awaitingRespondentEvidence)
        return Arrays.asList(
                new EmailNotificationGenerator(
                        isHomeOfficeGovNotifyEnabled ? newArrayList(respondentChangeDirectionDueDatePersonalisation) : emptyList(),
                        notificationSender,
                        notificationIdAppender
                )
        );
    }

    // An Appellant notification to be sent similar to LR once the templates are ready in future
    @Bean("respondentChangeDirectionDueDateForHomeOfficeApiEventsAipNotificationGenerator")
    public List<NotificationGenerator> respondentChangeDirectionDueDateForHomeOfficeApiEventsAipNotificationGenerator(
        RespondentChangeDirectionDueDatePersonalisation respondentChangeDirectionDueDatePersonalisation,
        AppellantChangeDirectionDueDateOfHomeOfficePersonalisationEmail appellantChangeDirectionDueDateOfHomeOfficePersonalisationEmail,
        AppellantChangeDirectionDueDateOfHomeOfficePersonalisationSms appellantChangeDirectionDueDateOfHomeOfficePersonalisationSms,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        // RIA-3116 - changeDirectionDueDate (requestEvidenceBundle, amendRequestBundle, requestRespondentReview, awaitingRespondentEvidence)
        List<EmailNotificationPersonalisation> personalisations = isHomeOfficeGovNotifyEnabled
            ? newArrayList(respondentChangeDirectionDueDatePersonalisation, appellantChangeDirectionDueDateOfHomeOfficePersonalisationEmail)
            : newArrayList(appellantChangeDirectionDueDateOfHomeOfficePersonalisationEmail);

        return Arrays.asList(
            new EmailNotificationGenerator(
                personalisations,
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(appellantChangeDirectionDueDateOfHomeOfficePersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("bothPartiesChangeDirectionDueDateNotificationGenerator")
    public List<NotificationGenerator> bothPartiesChangeDirectionDueDateNotificationGenerator(
        LegalRepresentativeChangeDirectionDueDatePersonalisation legalRepresentativeChangeDirectionDueDatePersonalisation,
        RespondentChangeDirectionDueDatePersonalisation respondentChangeDirectionDueDatePersonalisation,
        GovNotifyNotificationSender notificationSender,
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
        LegalRepresentativeListCasePersonalisation legalRepresentativeListCasePersonalisation,
        HomeOfficeListCasePersonalisation homeOfficeListCasePersonalisation,
        CaseOfficerListCasePersonalisation caseOfficerListCasePersonalisation,
        AppellantListCasePersonalisationEmail legallyReppedAppellantListCasePersonalisationEmail,
        AppellantListCasePersonalisationSms legallyReppedAppellantListCasePersonalisationSms,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        // RIA-3361 - listCase
        List<EmailNotificationPersonalisation> emailPersonalisations = isHomeOfficeGovNotifyEnabled ?
            newArrayList(legalRepresentativeListCasePersonalisation,
                homeOfficeListCasePersonalisation,
                caseOfficerListCasePersonalisation,
                legallyReppedAppellantListCasePersonalisationEmail) :
            newArrayList(legalRepresentativeListCasePersonalisation,
                caseOfficerListCasePersonalisation,
                legallyReppedAppellantListCasePersonalisationEmail);

        return Arrays.asList(
            new EmailNotificationGenerator(
                emailPersonalisations,
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(legallyReppedAppellantListCasePersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("listCaseAdaNotificationGenerator")
    public List<NotificationGenerator> listCaseAdaNotificationGenerator(
        LegalRepresentativeListCaseAdaSendStandardDirectionPersonalisation legalRepresentativeListCaseAdaSendStandardDirectionPersonalisation,
        LegalRepresentativeListCasePersonalisation legalRepresentativeListCasePersonalisation,
        HomeOfficeListCasePersonalisation homeOfficeListCasePersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        // RIA-3361 - listCase
        List<EmailNotificationPersonalisation> personalisations = isHomeOfficeGovNotifyEnabled
            ? newArrayList(legalRepresentativeListCaseAdaSendStandardDirectionPersonalisation, legalRepresentativeListCasePersonalisation, homeOfficeListCasePersonalisation)
            : newArrayList(legalRepresentativeListCaseAdaSendStandardDirectionPersonalisation);

        return Arrays.asList(
            new EmailNotificationGenerator(
                personalisations,
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("listCaseAipNotificationGenerator")
    public List<NotificationGenerator> listCaseAipNotificationGenerator(
        CaseOfficerListCasePersonalisation caseOfficerListCasePersonalisation,
        AppellantListCasePersonalisationEmail appellantListCasePersonalisationEmail,
        AppellantListCasePersonalisationSms appellantListCasePersonalisationSms,
        HomeOfficeListCasePersonalisation homeOfficeListCasePersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        // RIA-3361 - listCase
        List<EmailNotificationPersonalisation> emailPersonalisations = isHomeOfficeGovNotifyEnabled
            ? newArrayList(caseOfficerListCasePersonalisation, appellantListCasePersonalisationEmail, homeOfficeListCasePersonalisation)
            : newArrayList(caseOfficerListCasePersonalisation, appellantListCasePersonalisationEmail);

        return Arrays.asList(
            new EmailNotificationGenerator(
                emailPersonalisations,
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(appellantListCasePersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("submitAppealAipNotificationGenerator")
    public List<NotificationGenerator> submitAppealAipNotificationGenerator(
        AppellantSubmitAppealPersonalisationSms appellantSubmitAppealPersonalisationSms,
        HomeOfficeSubmitAppealPersonalisation homeOfficeSubmitAppealPersonalisation,
        AppellantSubmitAppealPersonalisationEmail appellantSubmitAppealPersonalisationEmail,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(homeOfficeSubmitAppealPersonalisation, appellantSubmitAppealPersonalisationEmail),
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

    @Bean("paymentAppealAipNotificationGenerator")
    public List<NotificationGenerator> paymentAppealAipNotificationGenerator(
        AppellantSubmitAppealPersonalisationSms appellantSubmitAppealPersonalisationSms,
        AppellantSubmitAppealPersonalisationEmail appellantSubmitAppealPersonalisationEmail,
        HomeOfficeSubmitAppealPersonalisation homeOfficeSubmitAppealPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(appellantSubmitAppealPersonalisationEmail, homeOfficeSubmitAppealPersonalisation),
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
        GovNotifyNotificationSender notificationSender,
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
        GovNotifyNotificationSender notificationSender,
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
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(legalRepresentativeSubmitAppealPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("submitAppealOutOfTimeAipNotificationGenerator")
    public List<NotificationGenerator> submitAppealOutOfTimeAipNotificationGenerator(
        AppellantSubmitAppealOutOfTimePersonalisationSms appellantSubmitAppealOutOfTimePersonalisationSms,
        HomeOfficeSubmitAppealPersonalisation homeOfficeSubmitAppealPersonalisation,
        AppellantSubmitAppealOutOfTimePersonalisationEmail appellantSubmitAppealOutOfTimePersonalisationEmail,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(homeOfficeSubmitAppealPersonalisation, appellantSubmitAppealOutOfTimePersonalisationEmail),
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
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return singletonList(
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
        GovNotifyNotificationSender notificationSender,
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
        GovNotifyNotificationSender notificationSender,
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
        AppellantSubmitReasonsForAppealPersonalisationEmail appellantSubmitReasonsForAppealPersonalisationEmail,
        AppellantSubmitReasonsForAppealPersonalisationSms appellantSubmitReasonsForAppealPersonalisationSms,
        CaseOfficerReasonForAppealSubmittedPersonalisation caseOfficerReasonForAppealSubmittedPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(appellantSubmitReasonsForAppealPersonalisationEmail, caseOfficerReasonForAppealSubmittedPersonalisation),
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
        GovNotifyNotificationSender notificationSender,
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
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return singletonList(
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
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return singletonList(
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
        GovNotifyNotificationSender notificationSender,
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
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        // RIA-3631 - requestRespondentReview
        List<EmailNotificationPersonalisation> personalisations = isHomeOfficeGovNotifyEnabled
            ? newArrayList(respondentDirectionPersonalisation, legalRepresentativeRespondentReviewPersonalisation)
            : newArrayList(legalRepresentativeRespondentReviewPersonalisation);


        return singletonList(
            new EmailNotificationGenerator(
                personalisations,
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("respondentReviewAipNotificationGenerator")
    public List<NotificationGenerator> respondentReviewAipNotificationGenerator(
        RespondentDirectionPersonalisation respondentDirectionPersonalisation,
        AppellantRespondentReviewPersonalisationEmail appellantRespondentReviewPersonalisationEmail,
        AppellantRespondentReviewPersonalisationSms appellantRespondentReviewPersonalisationSms,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        // RIA-3631 - requestRespondentReview
        List<EmailNotificationPersonalisation> personalisations = isHomeOfficeGovNotifyEnabled
            ? newArrayList(respondentDirectionPersonalisation, appellantRespondentReviewPersonalisationEmail)
            : newArrayList(appellantRespondentReviewPersonalisationEmail);

        return Arrays.asList(
            new EmailNotificationGenerator(
                personalisations,
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(appellantRespondentReviewPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("respondentReviewInternalNotificationGenerator")
    public List<NotificationGenerator> respondentReviewInternalNotificationGenerator(
        RespondentDirectionPersonalisation respondentDirectionPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(respondentDirectionPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("respondentReviewInternalIrcPrisonNotificationGenerator")
    public List<NotificationGenerator> respondentReviewInternalIrcPrisonNotificationGenerator(
            DetentionEngagementTeamRespondentReviewPersonalisation detentionEngagementTeamRespondentReviewPersonalisation,
            GovNotifyNotificationSender notificationSender,
            NotificationIdAppender notificationIdAppender) {

        return singletonList(
                new EmailWithLinkNotificationGenerator(
                        newArrayList(detentionEngagementTeamRespondentReviewPersonalisation),
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
        GovNotifyNotificationSender notificationSender,
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
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        // RIA-3631 - requestRespondentEvidence
        List<EmailNotificationPersonalisation> personalisations = isHomeOfficeGovNotifyEnabled
            ? newArrayList(respondentEvidenceDirectionPersonalisation, legalRepresentativeRequestHomeOfficeBundlePersonalisation)
            : newArrayList(legalRepresentativeRequestHomeOfficeBundlePersonalisation);

        return singletonList(
            new EmailNotificationGenerator(
                personalisations,
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("respondentEvidenceInternalNotificationGenerator")
    public List<NotificationGenerator> respondentEvidenceInternalNotificationGenerator(
        RespondentEvidenceDirectionPersonalisation respondentEvidenceDirectionPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(respondentEvidenceDirectionPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("respondentDirectionNotificationGenerator")
    public List<NotificationGenerator> respondentDirectionNotificationGenerator(
        RespondentNonStandardDirectionPersonalisation respondentNonStandardDirectionPersonalisation,
        LegalRepresentativeNonStandardDirectionOfHomeOfficePersonalisation legalRepresentativeNonStandardDirectionOfHomeOfficePersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(respondentNonStandardDirectionPersonalisation, legalRepresentativeNonStandardDirectionOfHomeOfficePersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("aipRespondentDirectionNotificationGenerator")
    public List<NotificationGenerator> aipRespondentDirectionNotificationGenerator(
        RespondentNonStandardDirectionPersonalisation respondentNonStandardDirectionPersonalisation,
        AppellantNonStandardDirectionOfHomeOfficePersonalisationEmail appellantNonStandardDirectionOfHomeOfficePersonalisationEmail,
        AppellantNonStandardDirectionOfHomeOfficePersonalisationSms appellantNonStandardDirectionOfHomeOfficePersonalisationSms,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(respondentNonStandardDirectionPersonalisation,
                    appellantNonStandardDirectionOfHomeOfficePersonalisationEmail),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(appellantNonStandardDirectionOfHomeOfficePersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }


    @Bean("aipAppellantAndRespondentDirectionNotificationGenerator")
    public List<NotificationGenerator> aipAppellantAndRespondentDirectionNotificationGenerator(
        RespondentNonStandardDirectionPersonalisation respondentNonStandardDirectionPersonalisation,
        AppellantNonStandardDirectionOfHomeOfficePersonalisationEmail appellantNonStandardDirectionOfHomeOfficePersonalisationEmail,
        AppellantNonStandardDirectionOfHomeOfficePersonalisationSms appellantNonStandardDirectionOfHomeOfficePersonalisationSms,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(respondentNonStandardDirectionPersonalisation,
                    appellantNonStandardDirectionOfHomeOfficePersonalisationEmail),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(appellantNonStandardDirectionOfHomeOfficePersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("aipAppellantNonStandardDirectionNotificationGenerator")
    public List<NotificationGenerator> aipAppellantNonStandardDirectionNotificationGenerator(
        RespondentNonStandardDirectionOfAppellantPersonalization respondentNonStandardDirectionOfAppellantPersonalization,
        AppellantNonStandardDirectionPersonalisationEmail appellantNonStandardDirectionPersonalisationEmail,
        AppellantNonStandardDirectionPersonalisationSms appellantNonStandardDirectionPersonalisationSms,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(respondentNonStandardDirectionOfAppellantPersonalization, appellantNonStandardDirectionPersonalisationEmail),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(appellantNonStandardDirectionPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("awaitingRespondentDirectionNotificationGenerator")
    public List<NotificationGenerator> awaitingRespondentDirectionNotificationGenerator(
        RespondentNonStandardDirectionPersonalisation respondentNonStandardDirectionPersonalisation,
        LegalRepresentativeNonStandardDirectionOfHomeOfficePersonalisation legalRepresentativeNonStandardDirectionOfHomeOfficePersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        // RIA-3631 sendDirection (awaitingRespondentEvidence only)
        List<EmailNotificationPersonalisation> personalisations = isHomeOfficeGovNotifyEnabled
            ? newArrayList(respondentNonStandardDirectionPersonalisation, legalRepresentativeNonStandardDirectionOfHomeOfficePersonalisation)
            : newArrayList(legalRepresentativeNonStandardDirectionOfHomeOfficePersonalisation);

        return Arrays.asList(
            new EmailNotificationGenerator(
                personalisations,
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("awaitingRespondentDirectionAipNotificationGenerator")
    public List<NotificationGenerator> awaitingRespondentDirectionAipNotificationGenerator(
        RespondentNonStandardDirectionPersonalisation respondentNonStandardDirectionPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        List<EmailNotificationPersonalisation> personalisations = isHomeOfficeGovNotifyEnabled
            ? newArrayList(respondentNonStandardDirectionPersonalisation)
            : Collections.emptyList();

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
        GovNotifyNotificationSender notificationSender,
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
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(homeOfficeRecordApplicationPersonalisation, legalRepresentativeRecordApplicationPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("editCaseListingRepNotificationGenerator")
    public List<NotificationGenerator> editCaseListingRepNotificationGenerator(
        HomeOfficeEditListingPersonalisation homeOfficeEditListingPersonalisation,
        LegalRepresentativeEditListingPersonalisation legalRepresentativeEditListingPersonalisation,
        LegalRepresentativeEditListingNoChangePersonalisation legalRepresentativeEditListingNoChangePersonalisation,
        HomeOfficeEditListingNoChangePersonalisation homeOfficeEditListingNoChangePersonalisation,
        CaseOfficerEditListingPersonalisation caseOfficerEditListingPersonalisation,
        AppellantEditListingPersonalisationEmail appellantEditListingPersonalisationEmail,
        AppellantEditListingPersonalisationSms appellantEditListingPersonalisationSms,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        // RIA-3631 - editCaseListing
        List<EmailNotificationPersonalisation> personalisations = isHomeOfficeGovNotifyEnabled
            ? newArrayList(homeOfficeEditListingPersonalisation, legalRepresentativeEditListingPersonalisation, legalRepresentativeEditListingNoChangePersonalisation, homeOfficeEditListingNoChangePersonalisation, caseOfficerEditListingPersonalisation, appellantEditListingPersonalisationEmail)
            : newArrayList(legalRepresentativeEditListingPersonalisation, legalRepresentativeEditListingNoChangePersonalisation, caseOfficerEditListingPersonalisation, appellantEditListingPersonalisationEmail);

        return Arrays.asList(
            new EditListingEmailNotificationGenerator(
                personalisations,
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(appellantEditListingPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("editCaseListingAdaRepNotificationGenerator")
    public List<NotificationGenerator> editCaseListingAdaRepNotificationGenerator(
        HomeOfficeEditListingPersonalisation homeOfficeEditListingPersonalisation,
        LegalRepresentativeEditListingPersonalisation legalRepresentativeEditListingPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        List<EmailNotificationPersonalisation> personalisations = isHomeOfficeGovNotifyEnabled
            ? newArrayList(homeOfficeEditListingPersonalisation, legalRepresentativeEditListingPersonalisation)
            : newArrayList(legalRepresentativeEditListingPersonalisation);

        return Arrays.asList(
            new EditListingEmailNotificationGenerator(
                personalisations,
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("editCaseListingAipNotificationGenerator")
    public List<NotificationGenerator> editCaseListingAipNotificationGenerator(
        HomeOfficeEditListingPersonalisation homeOfficeEditListingPersonalisation,
        AppellantEditListingPersonalisationEmail appellantEditListingPersonalisationEmail,
        AppellantEditListingPersonalisationSms appellantEditListingPersonalisationSms,
        HomeOfficeEditListingNoChangePersonalisation homeOfficeEditListingNoChangePersonalisation,
        CaseOfficerEditListingPersonalisation caseOfficerEditListingPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        List<EmailNotificationPersonalisation> personalisations = isHomeOfficeGovNotifyEnabled
            ? newArrayList(homeOfficeEditListingPersonalisation, appellantEditListingPersonalisationEmail, homeOfficeEditListingNoChangePersonalisation, caseOfficerEditListingPersonalisation)
            : newArrayList(appellantEditListingPersonalisationEmail, caseOfficerEditListingPersonalisation);

        return Arrays.asList(
            new EditListingEmailNotificationGenerator(
                personalisations,
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(appellantEditListingPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("uploadHomeOfficeAppealResponseNotificationGenerator")
    public List<NotificationGenerator> uploadHomeOfficeAppealResponseNotificationGenerator(
        CaseOfficerHomeOfficeResponseUploadedPersonalisation caseOfficerHomeOfficeResponseUploadedPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(caseOfficerHomeOfficeResponseUploadedPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("uploadHomeOfficeAppealResponseInternalAdaNotificationGenerator")
    public List<NotificationGenerator> uploadHomeOfficeAppealResponseInternalAdaNotificationGenerator(
        DetentionEngagementTeamUploadAppealResponsePersonalisation detentionEngagementTeamUploadAppealResponsePersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailWithLinkNotificationGenerator(
                newArrayList(Collections.singleton(detentionEngagementTeamUploadAppealResponsePersonalisation)),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("requestCaseBuildingNotificationGenerator")
    public List<NotificationGenerator> requestCaseBuildingNotificationGenerator(
        LegalRepresentativeRequestCaseBuildingPersonalisation legalRepresentativeRequestCaseBuildingPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(legalRepresentativeRequestCaseBuildingPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("requestCaseBuildingLegalRepDetainedNotificationGenerator")
    public List<NotificationGenerator> requestCaseBuildingLegalRepDetainedNotificationGenerator(
        LegalRepresentativeRequestCaseBuildingDetainedPersonalisation legalRepresentativeRequestCaseBuildingDetainedPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(legalRepresentativeRequestCaseBuildingDetainedPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("requestCaseBuildingLegalRepInternalDetainedNotificationGenerator")
    public List<NotificationGenerator> requestCaseBuildingLegalRepInternalDetainedNotificationGenerator(
            LegalRepresentativeLetterRequestCaseBuildingDetainedPersonalisation legalRepresentativeLetterRequestCaseBuildingDetainedPersonalisation,
            GovNotifyNotificationSender notificationSender,
            NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
                new LetterNotificationGenerator(
                        newArrayList(legalRepresentativeLetterRequestCaseBuildingDetainedPersonalisation),
                        notificationSender,
                        notificationIdAppender
                )
        );
    }

    @Bean("requestResponseReviewNotificationGenerator")
    public List<NotificationGenerator> requestResponseReviewNotificationGenerator(
        LegalRepresentativeRequestResponseReviewPersonalisation legalRepresentativeRequestResponseReviewPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(legalRepresentativeRequestResponseReviewPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("requestResponseReviewAipNotificationGenerator")
    public List<NotificationGenerator> requestResponseReviewAipNotificationGenerator(
        AppellantRequestResponseReviewPersonalisationEmail appellantRequestResponseReviewPersonalisationEmail,
        AppellantRequestResponseReviewPersonalisationSms appellantRequestResponseReviewPersonalisationSms,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(appellantRequestResponseReviewPersonalisationEmail),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(appellantRequestResponseReviewPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("respondentEvidenceSubmitted")
    public List<NotificationGenerator> respondentEvidenceSubmitted(
        CaseOfficerRespondentEvidenceSubmittedPersonalisation caseOfficerRespondentEvidenceSubmittedPersonalisation,
        GovNotifyNotificationSender notificationSender,
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

    @Bean("hearingBundleReadyRepNotificationGenerator")
    public List<NotificationGenerator> hearingBundleReadyRepNotificationGenerator(
        HomeOfficeHearingBundleReadyPersonalisation homeOfficeHearingBundleReadyPersonalisation,
        LegalRepresentativeHearingBundleReadyPersonalisation legalRepresentativeHearingBundleReadyPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        //RIA-3316 - Hearing Bundle Ready (generateHearingBundle, customiseHearingBundle)
        List<EmailNotificationPersonalisation> personalisations = isHomeOfficeGovNotifyEnabled
            ? newArrayList(homeOfficeHearingBundleReadyPersonalisation, legalRepresentativeHearingBundleReadyPersonalisation)
            : newArrayList(legalRepresentativeHearingBundleReadyPersonalisation);

        return Arrays.asList(
            new EmailNotificationGenerator(
                personalisations,
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("hearingBundleReadyAipNotificationGenerator")
    public List<NotificationGenerator> hearingBundleReadyAipNotificationGenerator(
        HomeOfficeHearingBundleReadyPersonalisation homeOfficeHearingBundleReadyPersonalisation,
        AppellantHearingBundleReadyPersonalisationEmail appellantHearingBundleReadyPersonalisationEmail,
        AppellantHearingBundleReadyPersonalisationSms appellantHearingBundleReadyPersonalisationSms,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        List<EmailNotificationPersonalisation> personalisations = isHomeOfficeGovNotifyEnabled
            ? newArrayList(homeOfficeHearingBundleReadyPersonalisation, appellantHearingBundleReadyPersonalisationEmail)
            : newArrayList(appellantHearingBundleReadyPersonalisationEmail);


        return Arrays.asList(
            new EmailNotificationGenerator(
                personalisations,
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(appellantHearingBundleReadyPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("hearingBundleFailedNotificationGenerator")
    public List<NotificationGenerator> hearingBundleFailedNotificationGenerator(
        CaseOfficerHearingBundleFailedPersonalisation caseOfficerHearingBundleFailedPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(caseOfficerHearingBundleFailedPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }


    @Bean("homeOfficeHearingBundleReadyNotificationGenerator")
    public List<NotificationGenerator> homeOfficeHearingBundleReadyNotificationGenerator(
        HomeOfficeHearingBundleReadyPersonalisation homeOfficeHearingBundleReadyPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {
        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(homeOfficeHearingBundleReadyPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("asyncStitchingCompleteHomeOfficeNotificationFailedNotificationGenerator")
    public List<NotificationGenerator> asyncStitchingCompleteHomeOfficeNotificationFailed(
        CaseOfficerAsyncStitchingHomeOfficeNotificationFailedPersonalisation caseOfficerAsyncStitchingHomeOfficeNotificationFailedPersonalisation,
        GovNotifyNotificationSender notificationSender,
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
        LegalRepresentativeSubmittedHearingRequirementsPersonalisation legalRepresentativeSubmittedHearingRequirementsPersonalisation,
        CaseOfficerSubmittedHearingRequirementsPersonalisation caseOfficerSubmittedHearingRequirementsPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(legalRepresentativeSubmittedHearingRequirementsPersonalisation, caseOfficerSubmittedHearingRequirementsPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }


    @Bean("submittedHearingRequirementsAipNotificationGenerator")
    public List<NotificationGenerator> submittedHearingRequirementsAipNotificationGenerator(
        AppellantSubmittedHearingRequirementsPersonalisation appellantSubmittedHearingRequirementsPersonalisation,
        AppellantSubmittedHearingRequirementsPersonalisationSms appellantSubmittedHearingRequirementsPersonalisationSms,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(appellantSubmittedHearingRequirementsPersonalisation),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(appellantSubmittedHearingRequirementsPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("adjustedHearingRequirementsNotificationGenerator")
    public List<NotificationGenerator> adjustedHearingRequirementsNotificationGenerator(
        AdminOfficerReviewHearingRequirementsPersonalisation adminOfficerReviewHearingRequirementsPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(adminOfficerReviewHearingRequirementsPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("reviewedAdaHearingRequirementsNotificationGenerator")
    public List<NotificationGenerator> reviewedAdaHearingRequirementsNotificationGenerator(
        LegalRepresentativeAdaReviewHearingRequirementsPersonalisation legalRepresentativeAdaReviewHearingRequirementsPersonalisation,
        HomeOfficeAdaReviewHearingRequirementsPersonalisation homeOfficeAdaReviewHearingRequirementsPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(legalRepresentativeAdaReviewHearingRequirementsPersonalisation, homeOfficeAdaReviewHearingRequirementsPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("withoutHearingRequirementsNotificationGenerator")
    public List<NotificationGenerator> withoutHearingRequirementsNotificationGenerator(
        AdminOfficerWithoutHearingRequirementsPersonalisation adminOfficerWithoutHearingRequirementsPersonalisation,
        GovNotifyNotificationSender notificationSender,
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
        HomeOfficeUploadAdditionalEvidencePersonalisation homeOfficeUploadAdditionalEvidencePersonalisation,
        CaseOfficerUploadAdditionalEvidencePersonalisation caseOfficerUploadAdditionalEvidencePersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(homeOfficeUploadAdditionalEvidencePersonalisation, caseOfficerUploadAdditionalEvidencePersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("uploadAdditionalEvidenceAip")
    public List<NotificationGenerator> uploadAdditionalEvidenceAip(
        HomeOfficeUploadAdditionalEvidencePersonalisation homeOfficeUploadAdditionalEvidencePersonalisation,
        AppellantUploadAdditionalEvidencePersonalisationEmail appellantUploadAdditionalEvidencePersonalisationEmail,
        AppellantUploadAdditionalEvidencePersonalisationSms appellantUploadAdditionalEvidencePersonalisationSms,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(homeOfficeUploadAdditionalEvidencePersonalisation,
                    appellantUploadAdditionalEvidencePersonalisationEmail),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(appellantUploadAdditionalEvidencePersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("uploadAdditionalEvidenceHomeOffice")
    public List<NotificationGenerator> uploadAdditionalEvidenceHomeOffice(
        LegalRepresentativeUploadAdditionalEvidencePersonalisation legalRepresentativeUploadAdditionalEvidencePersonalisation,
        CaseOfficerUploadAdditionalEvidencePersonalisation caseOfficerUploadAdditionalEvidencePersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(legalRepresentativeUploadAdditionalEvidencePersonalisation, caseOfficerUploadAdditionalEvidencePersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("uploadAddendumEvidenceCaseOfficer")
    public List<NotificationGenerator> uploadAddendumEvidenceCaseOfficer(
        HomeOfficeUploadAddendumEvidencePersonalisation homeOfficeUploadAddendumEvidencePersonalisation,
        LegalRepresentativeUploadAddendumEvidencePersonalisation legalRepresentativeUploadAddendumEvidencePersonalisation,
        GovNotifyNotificationSender notificationSender,
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

    @Bean("uploadAddendumEvidenceCaseOfficerAip")
    public List<NotificationGenerator> uploadAddendumEvidenceCaseOfficerAip(
        HomeOfficeUploadAddendumEvidencePersonalisation homeOfficeUploadAddendumEvidencePersonalisation,
        AppellantTcwUploadAddendumEvidencePersonalisationEmail appellantTcwUploadAddendumEvidencePersonalisationEmail,
        AppellantTcwUploadAddendumEvidencePersonalisationSms appellantTcwUploadAddendumEvidencePersonalisationSms,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender

    ) {
        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(homeOfficeUploadAddendumEvidencePersonalisation, appellantTcwUploadAddendumEvidencePersonalisationEmail),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(appellantTcwUploadAddendumEvidencePersonalisationSms),
                notificationSender,
                notificationIdAppender
            ));
    }

    @Bean("uploadAddendumEvidenceHomeOffice")
    public List<NotificationGenerator> uploadAddendumEvidenceHomeOffice(
        LegalRepresentativeUploadAddendumEvidencePersonalisation legalRepresentativeUploadAddendumEvidencePersonalisation,
        CaseOfficerUploadAddendumEvidencePersonalisation caseOfficerUploadAddendumEvidencePersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender

    ) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(legalRepresentativeUploadAddendumEvidencePersonalisation, caseOfficerUploadAddendumEvidencePersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("internalUploadAddendumEvidenceHomeOffice")
    public List<NotificationGenerator> internalUploadAddendumEvidenceHomeOffice(
            CaseOfficerUploadAddendumEvidencePersonalisation caseOfficerUploadAddendumEvidencePersonalisation,
            GovNotifyNotificationSender notificationSender,
            NotificationIdAppender notificationIdAppender

    ) {

        return Arrays.asList(
                new EmailNotificationGenerator(
                        newArrayList(caseOfficerUploadAddendumEvidencePersonalisation),
                        notificationSender,
                        notificationIdAppender
                )
        );
    }

    @Bean("uploadAddendumEvidenceHomeOfficeAip")
    public List<NotificationGenerator> uploadAddendumEvidenceHomeOfficeAip(
        AppellantHomeOfficeUploadAddendumEvidencePersonalisationEmail appellantHomeOfficeOrUploadAddendumEvidencePersonalisationEmail,
        AppellantHomeOfficeUploadAddendumEvidencePersonalisationSms appellantHomeOfficeUploadAddendumEvidencePersonalisationSms,
        CaseOfficerUploadAddendumEvidencePersonalisation caseOfficerUploadAddendumEvidencePersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender

    ) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(appellantHomeOfficeOrUploadAddendumEvidencePersonalisationEmail, caseOfficerUploadAddendumEvidencePersonalisation),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(appellantHomeOfficeUploadAddendumEvidencePersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("uploadAddendumEvidenceLegalRep")
    public List<NotificationGenerator> uploadAddendumEvidenceLegalRep(
        HomeOfficeUploadAddendumEvidencePersonalisation homeOfficeUploadAddendumEvidencePersonalisation,
        CaseOfficerUploadAddendumEvidencePersonalisation caseOfficerUploadAddendumEvidencePersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(homeOfficeUploadAddendumEvidencePersonalisation, caseOfficerUploadAddendumEvidencePersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("uploadAddendumEvidenceLegalRepForAip")
    public List<NotificationGenerator> uploadAddendumEvidenceLegalRepForAip(
        HomeOfficeUploadAddendumEvidencePersonalisation homeOfficeUploadAddendumEvidencePersonalisation,
        CaseOfficerUploadAddendumEvidencePersonalisation caseOfficerUploadAddendumEvidencePersonalisation,
        AppellantUploadAddendumEvidencePersonalisationEmail appellantUploadAddendumEvidencePersonalisationEmail,
        AppellantUploadAddendumEvidencePersonalisationSms appellantUploadAddendumEvidencePersonalisationSms,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(
                    homeOfficeUploadAddendumEvidencePersonalisation,
                    caseOfficerUploadAddendumEvidencePersonalisation,
                    appellantUploadAddendumEvidencePersonalisationEmail),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(appellantUploadAddendumEvidencePersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("uploadAddendumEvidenceAdminOfficerInternal")
    public List<NotificationGenerator> uploadAddendumEvidenceAdminOfficerInternal(
        HomeOfficeUploadAddendumEvidencePersonalisation homeOfficeUploadAddendumEvidencePersonalisation,
        LegalRepresentativeUploadAddendumEvidencePersonalisation legalRepresentativeUploadAddendumEvidencePersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(
                    homeOfficeUploadAddendumEvidencePersonalisation,
                    legalRepresentativeUploadAddendumEvidencePersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("internalLegalOfficerUploadAdditionalAddendumEvidenceNotificationGenerator")
    public List<NotificationGenerator> internalUploadAddendumEvidenceLegalOfficer(
        HomeOfficeUploadAddendumEvidencePersonalisation homeOfficeUploadAddendumEvidencePersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(
                    homeOfficeUploadAddendumEvidencePersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("uploadAddendumEvidenceAdminOfficer")
    public List<NotificationGenerator> uploadAddendumEvidenceAdminOfficer(
        HomeOfficeUploadAddendumEvidencePersonalisation homeOfficeUploadAddendumEvidencePersonalisation,
        LegalRepresentativeUploadAddendumEvidencePersonalisation legalRepresentativeUploadAddendumEvidencePersonalisation,
        CaseOfficerUploadAddendumEvidencePersonalisation caseOfficerUploadAddendumEvidencePersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender

    ) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(homeOfficeUploadAddendumEvidencePersonalisation, legalRepresentativeUploadAddendumEvidencePersonalisation, caseOfficerUploadAddendumEvidencePersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("uploadAddendumEvidenceAdminOfficerAip")
    public List<NotificationGenerator> uploadAddendumEvidenceAdminOfficerAip(
        HomeOfficeUploadAddendumEvidencePersonalisation homeOfficeUploadAddendumEvidencePersonalisation,
        AppellantTcwUploadAddendumEvidencePersonalisationEmail appellantTcwUploadAddendumEvidencePersonalisationEmail,
        AppellantTcwUploadAddendumEvidencePersonalisationSms appellantTcwUploadAddendumEvidencePersonalisationSms,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {
        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(homeOfficeUploadAddendumEvidencePersonalisation, appellantTcwUploadAddendumEvidencePersonalisationEmail),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(appellantTcwUploadAddendumEvidencePersonalisationSms),
                notificationSender,
                notificationIdAppender
            ));
    }

    @Bean("changeToHearingRequirementsNotificationGenerator")
    public List<NotificationGenerator> changeToHearingRequirementsNotificationGenerator(
        AdminOfficerChangeToHearingRequirementsPersonalisation adminOfficerChangeToHearingRequirementsPersonalisation,
        GovNotifyNotificationSender notificationSender,
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
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(homeOfficeAppealExitedOnlinePersonalisation, legalRepresentativeAppealExitedOnlinePersonalisation),
                notificationSender,
                notificationIdAppender)
        );
    }

    @Bean("internalAppealExitedOnlineNotificationGenerator")
    public List<NotificationGenerator> internalAppealExitedOnlineNotificationGenerator(
        HomeOfficeAppealExitedOnlinePersonalisation homeOfficeAppealExitedOnlinePersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return List.of(
            new EmailNotificationGenerator(
                newArrayList(homeOfficeAppealExitedOnlinePersonalisation),
                notificationSender,
                notificationIdAppender)
        );
    }

    @Bean("appealExitedOnlineAppellantNotificationGenerator")
    public List<NotificationGenerator> appealExitedOnlineAppellantNotificationGenerator(
        HomeOfficeAppealExitedOnlinePersonalisation homeOfficeAppealExitedOnlinePersonalisation,
        AppellantAppealExitedOnlinePersonalisationEmail appellantAppealExitedOnlinePersonalisationEmail,
        AppellantAppealExitedOnlinePersonalisationSms appellantAppealExitedOnlinePersonalisationSms,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(homeOfficeAppealExitedOnlinePersonalisation, appellantAppealExitedOnlinePersonalisationEmail),
                notificationSender,
                notificationIdAppender),
            new SmsNotificationGenerator(
                newArrayList(appellantAppealExitedOnlinePersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("changeHearingCentreAppellantNotificationGenerator")
    public List<NotificationGenerator> changeHearingCentreAppellantNotificationGenerator(
        CaseOfficerChangeHearingCentrePersonalisation caseOfficerChangeHearingCentrePersonalisation,
        AppellantChangeHearingCentrePersonalisationEmail appellantChangeHearingCentrePersonalisationEmail,
        AppellantChangeHearingCentrePersonalisationSms appellantChangeHearingCentrePersonalisationSms,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(caseOfficerChangeHearingCentrePersonalisation, appellantChangeHearingCentrePersonalisationEmail),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(appellantChangeHearingCentrePersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("changeHearingCentreNotificationGenerator")
    public List<NotificationGenerator> changeHearingCentreNotificationGenerator(
        LegalRepresentativeChangeHearingCentrePersonalisation legalRepresentativeChangeHearingCentrePersonalisation,
        CaseOfficerChangeHearingCentrePersonalisation caseOfficerChangeHearingCentrePersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(legalRepresentativeChangeHearingCentrePersonalisation, caseOfficerChangeHearingCentrePersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("internalLrChangeHearingCentreNotificationGenerator")
    public List<NotificationGenerator> internalLrChangeHearingCentreNotificationGenerator(
            CaseOfficerChangeHearingCentrePersonalisation caseOfficerChangeHearingCentrePersonalisation,
            GovNotifyNotificationSender notificationSender,
            NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
                new EmailNotificationGenerator(
                        newArrayList(caseOfficerChangeHearingCentrePersonalisation),
                        notificationSender,
                        notificationIdAppender
                )
        );
    }

    @Bean("internalDetainedChangeHearingCentreNotificationGenerator")
    public List<NotificationGenerator> internalDetainedChangeHearingCentreNotificationGenerator(
        DetentionEngagementTeamChangeHearingCentrePersonalisation detentionEngagementTeamChangeHearingCentrePersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailWithLinkNotificationGenerator(
                newArrayList(detentionEngagementTeamChangeHearingCentrePersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("ftpaSubmittedLegalRepNotificationGenerator")
    public List<NotificationGenerator> ftpaSubmittedLegalRep(
        LegalRepresentativeFtpaSubmittedPersonalisation legalRepresentativeFtpaSubmittedPersonalisation,
        RespondentAppellantFtpaSubmittedPersonalisation respondentAppellantFtpaSubmittedPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        // RIA-3316 - applyForFTPAAppellant
        List<EmailNotificationPersonalisation> personalisations = isHomeOfficeGovNotifyEnabled
            ? newArrayList(legalRepresentativeFtpaSubmittedPersonalisation, respondentAppellantFtpaSubmittedPersonalisation)
            : newArrayList(legalRepresentativeFtpaSubmittedPersonalisation);


        return Arrays.asList(
            new EmailNotificationGenerator(
                personalisations,
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("internalDetainedFtpaSubmittedNotificationGenerator")
    public List<NotificationGenerator> internalDetainedFtpaSubmittedNotificationGenerator(
        RespondentAppellantFtpaSubmittedPersonalisation respondentAppellantFtpaSubmittedPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    respondentAppellantFtpaSubmittedPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("ftpaSubmittedAipNotificationGenerator")
    public List<NotificationGenerator> ftpaSubmittedAip(
        AppellantFtpaSubmittedPersonalisationEmail appellantFtpaSubmittedPersonalisationEmail,
        AppellantFtpaSubmittedPersonalisationSms appellantFtpaSubmittedPersonalisationSms,
        RespondentAppellantFtpaSubmittedPersonalisation respondentAppellantFtpaSubmittedPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return List.of(
            new EmailNotificationGenerator(
                List.of(appellantFtpaSubmittedPersonalisationEmail,
                    respondentAppellantFtpaSubmittedPersonalisation),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(appellantFtpaSubmittedPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("ftpaSubmittedHomeOfficeNotificationFailedCaseOfficerNotificationGenerator")
    public List<NotificationGenerator> ftpaSubmittedHomeOfficeNotificationFailed(
        CaseOfficerFtpaSubmittedHomeOfficeNotificationFailedPersonalisation ftpaSubmittedHomeOfficeNotificationFailedPersonalisation,
        GovNotifyNotificationSender notificationSender,
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
        LegalRepresentativeRespondentFtpaSubmittedPersonalisation legalRepresentativeRespondentFtpaSubmittedPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        // RIA-3316 - applyForFTPARespondent
        List<EmailNotificationPersonalisation> personalisations = isHomeOfficeGovNotifyEnabled
            ? newArrayList(respondentFtpaSubmittedPersonalisation, legalRepresentativeRespondentFtpaSubmittedPersonalisation)
            : newArrayList(legalRepresentativeRespondentFtpaSubmittedPersonalisation);

        return Arrays.asList(
            new EmailNotificationGenerator(
                personalisations,
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("respondentFtpaSubmittedNotificationGeneratorLegalRep")
    public List<NotificationGenerator> respondentFtpaSubmittedNotificationGeneratorLegalRep(
        RespondentFtpaSubmittedPersonalisation respondentFtpaSubmittedPersonalisation,
        LegalRepresentativeRespondentFtpaSubmittedPersonalisation legalRepresentativeRespondentFtpaSubmittedPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        // RIA-3316 - applyForFTPARespondent
        List<EmailNotificationPersonalisation> personalisations = isHomeOfficeGovNotifyEnabled
            ? newArrayList(respondentFtpaSubmittedPersonalisation, legalRepresentativeRespondentFtpaSubmittedPersonalisation)
            : newArrayList(legalRepresentativeRespondentFtpaSubmittedPersonalisation);

        return Arrays.asList(
            new EmailNotificationGenerator(
                personalisations,
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("internalDetainedApplyForFtpaRespondentNotificationGenerator")
    public List<NotificationGenerator> internalDetainedApplyForFtpaRespondentNotificationGenerator(
            DetentionEngagementApplyForFtpaRespondentPersonalisation detentionEngagementApplyForFtpaRespondentPersonalisation,
            GovNotifyNotificationSender notificationSender,
            NotificationIdAppender notificationIdAppender
    ) {
        return singletonList(
                new EmailWithLinkNotificationGenerator(
                        newArrayList(detentionEngagementApplyForFtpaRespondentPersonalisation),
                        notificationSender,
                        notificationIdAppender
                )
        );
    }

    @Bean("respondentFtpaSubmittedHoNotificationGenerator")
    public List<NotificationGenerator> respondentFtpaSubmittedHoNotificationGeneratorDetentionEngagementTeam(
        RespondentFtpaSubmittedPersonalisation respondentFtpaSubmittedPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {
        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(respondentFtpaSubmittedPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("ftpaSubmittedRespondentAipJourneyNotificationGenerator")
    public List<NotificationGenerator> ftpaSubmittedRespondentAipJourney(
        RespondentFtpaSubmittedPersonalisation respondentFtpaSubmittedPersonalisation,
        // notification sent to appellant for FTPA submitted by HO
        AppellantRespondentFtpaSubmittedPersonalisationEmail appellantRespondentFtpaSubmittedPersonalisationEmail,
        AppellantRespondentFtpaSubmittedPersonalisationSms appellantRespondentFtpaSubmittedPersonalisationSms,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        // RIA-3316 - applyForFTPARespondent
        List<EmailNotificationPersonalisation> personalisations = isHomeOfficeGovNotifyEnabled
            ? newArrayList(respondentFtpaSubmittedPersonalisation, appellantRespondentFtpaSubmittedPersonalisationEmail)
            : newArrayList(appellantRespondentFtpaSubmittedPersonalisationEmail);

        return Arrays.asList(
            new EmailNotificationGenerator(
                personalisations,
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(appellantRespondentFtpaSubmittedPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("makeAnApplicationAipNotificationGenerator")
    public List<NotificationGenerator> makeAnApplicationAipNotificationGenerator(
        AppellantMakeAnApplicationPersonalisationEmail appellantMakeAnApplicationPersonalisationEmail,
        AppellantMakeAnApplicationPersonalisationSms appellantMakeAnApplicationPersonalisationSms,
        HomeOfficeMakeAnApplicationPersonalisation homeOfficeMakeAnApplicationPersonalisation,
        CaseOfficerMakeAnApplicationPersonalisation caseOfficerMakeAnApplicationPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(
                    appellantMakeAnApplicationPersonalisationEmail,
                    caseOfficerMakeAnApplicationPersonalisation,
                    homeOfficeMakeAnApplicationPersonalisation),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(appellantMakeAnApplicationPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("reviewTimeExtensionGrantedGenerator")
    public List<NotificationGenerator> reviewTimeExtensionGrantedGenerator(
        AppellantReviewTimeExtensionGrantedPersonalisationEmail appellantReviewTimeExtensionGrantedPersonalisationEmail,
        AppellantReviewTimeExtensionGrantedPersonalisationSms appellantReviewTimeExtensionGrantedPersonalisationSms,
        GovNotifyNotificationSender notificationSender,
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
        GovNotifyNotificationSender notificationSender,
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
        GovNotifyNotificationSender notificationSender,
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
        AppellantSubmitClarifyingQuestionAnswersPersonalisationSms appellantSubmitClarifyingQuestionAnswersPersonalisationSms,
        AppellantSubmitClarifyingQuestionAnswersPersonalisationEmail appellantSubmitClarifyingQuestionAnswersPersonalisationEmail,
        CaseOfficerClarifyingQuestionsAnswersSubmittedPersonalisation caseOfficerClarifyingQuestionsAnswersSubmittedPersonalisation,
        GovNotifyNotificationSender notificationSender,
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
        RespondentForceCaseToCaseUnderReviewPersonalisation respondentForceCaseToCaseUnderReviewPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    forceCaseProgressionToCaseUnderReviewPersonalisation,
                    respondentForceCaseToCaseUnderReviewPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("forceAppellantCaseToCaseUnderReviewEmailNotificationGenerator")
    public List<NotificationGenerator> forceAppellantCaseToCaseUnderReviewEmailNotificationGenerator(
        AppellantForceCaseProgressionToCaseUnderReviewPersonalisationEmail appellantForceCaseProgressionToCaseUnderReviewPersonalisationEmail,
        RespondentForceCaseToCaseUnderReviewPersonalisation homeOfficePersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return List.of(
            new EmailNotificationGenerator(
                newArrayList(homeOfficePersonalisation, appellantForceCaseProgressionToCaseUnderReviewPersonalisationEmail),
                notificationSender,
                notificationIdAppender)
        );
    }

    @Bean("forceAppellantCaseToCaseUnderReviewSmsNotificationGenerator")
    public List<NotificationGenerator> forceAppellantCaseToCaseUnderReviewSmsNotificationGenerator(
        AppellantForceCaseProgressionToCaseUnderReviewPersonalisationSms appellantForceCaseProgressionToCaseUnderReviewPersonalisationSms,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return List.of(
            new SmsNotificationGenerator(
                newArrayList(appellantForceCaseProgressionToCaseUnderReviewPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("forceCaseToSubmitHearingRequirementsNotificationGenerator")
    public List<NotificationGenerator> forceCaseToSubmitHearingRequirementsNotificationGenerator(
        RespondentForceCaseToSubmitHearingRequirementsPersonalisation respondentForceCaseToSubmitHearingRequirementsPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    respondentForceCaseToSubmitHearingRequirementsPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("legalRepForceCaseToSubmitHearingRequirementsNotificationDetentionGenerator")
    public List<NotificationGenerator> forceCaseToSubmitHearingRequirementsNotificationDetentionGenerator(
        LegalRepresentativeForceCaseToSubmitHearingRequirementsDetentionPersonalisation legalRepresentativeForceCaseToSubmitHearingRequirementsDetentionPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    legalRepresentativeForceCaseToSubmitHearingRequirementsDetentionPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("adjournHearingWithoutDateNonIntegratedNotificationGenerator")
    public List<NotificationGenerator> adjournHearingWithoutDateNotificationNonIntegratedGenerator(
        LegalRepresentativeAdjournHearingWithoutDatePersonalisation legalRepresentativeAdjournHearingWithoutDatePersonalisation,
        RespondentAdjournHearingWithoutDatePersonalisation respondentAdjournHearingWithoutDatePersonalisation,
        AdminOfficerAdjournHearingWithoutDatePersonalisation adminOfficerAdjournHearingWithoutDatePersonalisation,
        CaseOfficerAdjournHearingWithoutDatePersonalisation caseOfficerAdjournHearingWithoutDatePersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        // RIA-3631 adjournHearingWithoutDate
        List<EmailNotificationPersonalisation> personalisations = isHomeOfficeGovNotifyEnabled
            ? newArrayList(legalRepresentativeAdjournHearingWithoutDatePersonalisation, respondentAdjournHearingWithoutDatePersonalisation, adminOfficerAdjournHearingWithoutDatePersonalisation, caseOfficerAdjournHearingWithoutDatePersonalisation)
            : newArrayList(legalRepresentativeAdjournHearingWithoutDatePersonalisation, adminOfficerAdjournHearingWithoutDatePersonalisation, caseOfficerAdjournHearingWithoutDatePersonalisation);

        return singletonList(
            new EmailNotificationGenerator(
                personalisations,
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("internalAdjournHearingWithoutDateNonDetainedGenerator")
    public List<NotificationGenerator> internalAdjournHearingWithoutDateNonDetainedGenerator(
        AdminOfficerAdjournHearingWithoutDateNonDetainedPersonalisation adminOfficerAdjournHearingWithoutDateNonDetainedPersonalisation,
        HomeOfficeAdjournHearingWithoutDateNonDetainedPersonalisation homeOfficeAdjournHearingWithoutDateNonDetainedPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    adminOfficerAdjournHearingWithoutDateNonDetainedPersonalisation,
                    homeOfficeAdjournHearingWithoutDateNonDetainedPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("adjournHearingWithoutDateIntegratedNotificationGenerator")
    public List<NotificationGenerator> adjournHearingWithoutDateNotificationIntegratedGenerator(
        LegalRepresentativeAdjournHearingWithoutDatePersonalisation legalRepresentativeAdjournHearingWithoutDatePersonalisation,
        RespondentAdjournHearingWithoutDatePersonalisation respondentAdjournHearingWithoutDatePersonalisation,
        CaseOfficerAdjournHearingWithoutDatePersonalisation caseOfficerAdjournHearingWithoutDatePersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        List<EmailNotificationPersonalisation> personalisations = isHomeOfficeGovNotifyEnabled
            ? newArrayList(legalRepresentativeAdjournHearingWithoutDatePersonalisation, respondentAdjournHearingWithoutDatePersonalisation, caseOfficerAdjournHearingWithoutDatePersonalisation)
            : newArrayList(legalRepresentativeAdjournHearingWithoutDatePersonalisation, caseOfficerAdjournHearingWithoutDatePersonalisation);

        return singletonList(
            new EmailNotificationGenerator(
                personalisations,
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("recordAdjournmentDetailsNonIntegratedNotificationGenerator")
    public List<NotificationGenerator> recordAdjournmentDetailsNonIntegratedNotificationGenerator(
            LegalRepresentativeRecordAdjournmentDetailsPersonalisation legalRepresentativeRecordAdjournmentDetailsPersonalisation,
            RespondentRecordAdjournmentDetailsPersonalisation respondentRecordAdjournmentDetailsPersonalisation,
            AdminOfficerRecordAdjournmentDetailsPersonalisation adminOfficerRecordAdjournmentDetailsPersonalisation,
            CaseOfficerRecordAdjournmentDetailsPersonalisation caseOfficerRecordAdjournmentDetailsPersonalisation,
            GovNotifyNotificationSender notificationSender,
            NotificationIdAppender notificationIdAppender
    ) {

        List<EmailNotificationPersonalisation> personalisations = isHomeOfficeGovNotifyEnabled
                ?  newArrayList(legalRepresentativeRecordAdjournmentDetailsPersonalisation, respondentRecordAdjournmentDetailsPersonalisation, adminOfficerRecordAdjournmentDetailsPersonalisation, caseOfficerRecordAdjournmentDetailsPersonalisation)
                : newArrayList(legalRepresentativeRecordAdjournmentDetailsPersonalisation, adminOfficerRecordAdjournmentDetailsPersonalisation, caseOfficerRecordAdjournmentDetailsPersonalisation);

        return singletonList(
                new EmailNotificationGenerator(
                        personalisations,
                        notificationSender,
                        notificationIdAppender
                )
        );
    }

    @Bean("recordAdjournmentDetailsIntegratedNotificationGenerator")
    public List<NotificationGenerator> recordAdjournmentDetailsIntegratedNotificationGenerator(
        LegalRepresentativeRecordAdjournmentDetailsPersonalisation legalRepresentativeRecordAdjournmentDetailsPersonalisation,
        RespondentRecordAdjournmentDetailsPersonalisation respondentRecordAdjournmentDetailsPersonalisation,
        CaseOfficerRecordAdjournmentDetailsPersonalisation caseOfficerRecordAdjournmentDetailsPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        List<EmailNotificationPersonalisation> personalisations = isHomeOfficeGovNotifyEnabled
            ?  newArrayList(legalRepresentativeRecordAdjournmentDetailsPersonalisation, respondentRecordAdjournmentDetailsPersonalisation, caseOfficerRecordAdjournmentDetailsPersonalisation)
            : newArrayList(legalRepresentativeRecordAdjournmentDetailsPersonalisation, caseOfficerRecordAdjournmentDetailsPersonalisation);

        return singletonList(
            new EmailNotificationGenerator(
                personalisations,
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("decisionWithoutHearingNotificationGenerator")
    public List<NotificationGenerator> decisionWithoutHearingNotificationGenerator(
        HomeOfficeDecisionWithoutHearingPersonalisation homeOfficeDecisionWithoutHearingPersonalisation,
        AppellantAppealDecisionWithoutHearingPersonalisationEmail appellantAppealDecisionWithoutHearingPersonalisationEmail,
        AppellantAppealDecisionWithoutHearingPersonalisationSms appellantAppealDecisionWithoutHearingPersonalisationSms,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(
                    homeOfficeDecisionWithoutHearingPersonalisation,
                    appellantAppealDecisionWithoutHearingPersonalisationEmail),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(appellantAppealDecisionWithoutHearingPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("decisionWithoutHearingInternalNotificationGenerator")
    public List<NotificationGenerator> decisionWithoutHearingInternalNotificationGenerator(
        HomeOfficeDecisionWithoutHearingPersonalisation homeOfficeDecisionWithoutHearingPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(homeOfficeDecisionWithoutHearingPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("internalDetainedDecisionWithoutHearingAppellantNotificationGenerator")
    public List<NotificationGenerator> internalDetainedDecisionWithoutHearingAppellantNotificationGenerator(
            DetentionEngagementTeamDecisionWithoutHearingPersonalisation detentionEngagementTeamDecisionWithoutHearingPersonalisation,
            GovNotifyNotificationSender notificationSender,
            NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
            new EmailWithLinkNotificationGenerator(
                newArrayList(Collections.singleton(detentionEngagementTeamDecisionWithoutHearingPersonalisation)),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("requestCmaRequirementsAipNotificationGenerator")
    public List<NotificationGenerator> requestCmaRequirementsAipNotificationGenerator(
        AppellantRequestCmaRequirementsPersonalisationEmail appellantRequestCmaRequirementsPersonalisationEmail,
        AppellantRequestCmaRequirementsPersonalisationSms appellantRequestCmaRequirementsPersonalisationSms,
        GovNotifyNotificationSender notificationSender,
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
        AppellantSubmitCmaRequirementsPersonalisationEmail appellantSubmitCmaRequirementsPersonalisationEmail,
        AppellantSubmitCmaRequirementsPersonalisationSms appellantSubmitCmaRequirementsPersonalisationSms,
        CaseOfficerCmaRequirementsSubmittedPersonalisation caseOfficerCmaRequirementsSubmittedPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(appellantSubmitCmaRequirementsPersonalisationEmail, caseOfficerCmaRequirementsSubmittedPersonalisation),
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
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(respondentNonStandardDirectionPersonalisation, legalRepresentativeNonStandardDirectionPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("appellantRespondentInternalNonStandardDirectionGenerator")
    public List<NotificationGenerator> appellantRespondentInternalNonStandardDirectionGenerator(
        RespondentNonStandardDirectionPersonalisation respondentNonStandardDirectionPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(respondentNonStandardDirectionPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("appellantInternalNonStandardDirectionGenerator")
    public List<NotificationGenerator> appellantInternalNonStandardDirectionGenerator(
            RespondentNonStandardDirectionOfAppellantPersonalization respondentNonStandardDirectionOfAppellantPersonalization,
            GovNotifyNotificationSender notificationSender,
            NotificationIdAppender notificationIdAppender) {

        return singletonList(
                new EmailNotificationGenerator(
                        newArrayList(respondentNonStandardDirectionOfAppellantPersonalization),
                        notificationSender,
                        notificationIdAppender
                )
        );
    }

    @Bean("appellantInternalDetainedNonStandardDirectionGenerator")
    public List<NotificationGenerator> appellantInternalDetainedNonStandardDirectionGenerator(
            DetentionEngagementTeamNonStandardDirectionPersonalisation personalisation,
            GovNotifyNotificationSender notificationSender,
            NotificationIdAppender notificationIdAppender) {

        return List.of(
                new EmailWithLinkNotificationGenerator(
                        newArrayList(personalisation),
                        notificationSender,
                        notificationIdAppender
                )
        );
    }

    @Bean("hoInternalNonStandardDirectionGenerator")
    public List<NotificationGenerator> hoInternalNonStandardDirectionGenerator(
        RespondentNonStandardDirectionPersonalisation respondentNonStandardDirectionPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(respondentNonStandardDirectionPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("requestResponseAmendDirectionGenerator")
    public List<NotificationGenerator> requestResponseAmendDirectionGenerator(
        RespondentRequestResponseAmendPersonalisation respondentRequestResponseAmendPersonalisation,
        LegalRepresentativeRequestRespondentAmendDirectionPersonalisation legalRepresentativeRequestRespondentAmendDirectionPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        // RIA-3631 requestResponseAmend
        return Arrays.asList(
            new EmailNotificationGenerator(
                isHomeOfficeGovNotifyEnabled ? newArrayList(respondentRequestResponseAmendPersonalisation, legalRepresentativeRequestRespondentAmendDirectionPersonalisation) : emptyList(),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("requestResponseAmendAipDirectionGenerator")
    public List<NotificationGenerator> requestResponseAmendAipDirectionGenerator(
        RespondentRequestResponseAmendPersonalisation respondentRequestResponseAmendPersonalisation,
        AppellantRequestResponseAmendPersonalisationEmail appellantRequestResponseAmendPersonalisationEmail,
        AppellantRequestResponseAmendPersonalisationSms appellantRequestResponseAmendPersonalisationSms,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        // RIA-3631 requestResponseAmend
        return Arrays.asList(
            new EmailNotificationGenerator(
                isHomeOfficeGovNotifyEnabled
                    ? newArrayList(
                    respondentRequestResponseAmendPersonalisation,
                    appellantRequestResponseAmendPersonalisationEmail
                )
                    : newArrayList(appellantRequestResponseAmendPersonalisationEmail),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(appellantRequestResponseAmendPersonalisationSms),
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
        GovNotifyNotificationSender notificationSender,
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
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return singletonList(
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

    @Bean("editAppealAfterSubmitInternalCaseNotificationGenerator")
    public List<NotificationGenerator> editAppealAfterSubmitInternalCaseNotificationGenerator(
        RespondentEditAppealAfterSubmitPersonalisation respondentEditAppealAfterSubmitPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return List.of(
                new EmailNotificationGenerator(
                        newArrayList(
                                respondentEditAppealAfterSubmitPersonalisation
                        ),
                        notificationSender,
                        notificationIdAppender
                )
        );
    }

    @Bean("editAppealAfterSubmitDetainedIrcPrisonInternalCaseNotificationGenerator")
    public List<NotificationGenerator> editAppealAfterSubmitDetainedIrcPrisonInternalCaseNotificationGenerator(
            DetentionEngagementTeamEditAppealPersonalisation personalisation,
            GovNotifyNotificationSender notificationSender,
            NotificationIdAppender notificationIdAppender) {

        return List.of(
                new EmailWithLinkNotificationGenerator(
                        newArrayList(personalisation),
                        notificationSender,
                        notificationIdAppender
                )
        );
    }

    @Bean("ftpaApplicationDecisionRefusedOrNotAdmittedAppellantNotificationGenerator")
    public List<NotificationGenerator> ftpaApplicationDecisionRefusedOrNotAdmittedAppellantNotificationGenerator(
        HomeOfficeFtpaApplicationDecisionAppellantPersonalisation homeOfficeFtpaApplicationDecisionAppellantPersonalisation,
        LegalRepresentativeFtpaApplicationDecisionAppellantPersonalisation legalRepresentativeFtpaApplicationDecisionAppellantPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        //RIA-3116 leadership/resident judge decision
        List<EmailNotificationPersonalisation> personalisations = isHomeOfficeGovNotifyEnabled
            ? newArrayList(homeOfficeFtpaApplicationDecisionAppellantPersonalisation, legalRepresentativeFtpaApplicationDecisionAppellantPersonalisation)
            : newArrayList(legalRepresentativeFtpaApplicationDecisionAppellantPersonalisation);

        return Arrays.asList(
            new EmailNotificationGenerator(
                personalisations,
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("ftpaApplicationDecisionRefusedOrNotAdmittedAppellantAipJourneyNotificationGenerator")
    public List<NotificationGenerator> ftpaApplicationDecisionRefusedOrNotAdmittedAppellantAipJourneyNotificationGenerator(
        HomeOfficeFtpaApplicationDecisionAppellantPersonalisation homeOfficeFtpaApplicationDecisionAppellantPersonalisation,
        AppellantFtpaApplicationDecisionPersonalisationEmail appellantFtpaApplicationDecisionPersonalisationEmail,
        AppellantFtpaApplicationDecisionPersonalisationSms appellantFtpaApplicationDecisionPersonalisationSms,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        //RIA-3116 leadership/resident judge decision
        List<EmailNotificationPersonalisation> emailPersonalisations = isHomeOfficeGovNotifyEnabled
            ? newArrayList(homeOfficeFtpaApplicationDecisionAppellantPersonalisation, appellantFtpaApplicationDecisionPersonalisationEmail)
            : newArrayList(appellantFtpaApplicationDecisionPersonalisationEmail);

        return Arrays.asList(
            new EmailNotificationGenerator(
                emailPersonalisations,
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                List.of(appellantFtpaApplicationDecisionPersonalisationSms),
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
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        //RIA-3116 leadership/resident judge decision
        List<EmailNotificationPersonalisation> personalisations = isHomeOfficeGovNotifyEnabled
            ? newArrayList(homeOfficeFtpaApplicationDecisionAppellantPersonalisation, legalRepresentativeFtpaApplicationDecisionAppellantPersonalisation, adminOfficerFtpaDecisionAppellantPersonalisation)
            : newArrayList(legalRepresentativeFtpaApplicationDecisionAppellantPersonalisation, adminOfficerFtpaDecisionAppellantPersonalisation);

        return Arrays.asList(
            new EmailNotificationGenerator(
                personalisations,
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("ftpaApplicationDecisionGrantedOrPartiallyGrantedAppellantAipJourneyNotificationGenerator")
    public List<NotificationGenerator> ftpaApplicationDecisionGrantedOrPartiallyAppellantGrantedAipJourneyNotificationGenerator(
        HomeOfficeFtpaApplicationDecisionAppellantPersonalisation homeOfficeFtpaApplicationDecisionAppellantPersonalisation,
        AppellantFtpaApplicationDecisionPersonalisationEmail appellantFtpaApplicationDecisionPersonalisationEmail,
        AppellantFtpaApplicationDecisionPersonalisationSms appellantFtpaApplicationDecisionPersonalisationSms,
        AdminOfficerFtpaDecisionAppellantPersonalisation adminOfficerFtpaDecisionAppellantPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        //RIA-3116 leadership/resident judge decision
        List<EmailNotificationPersonalisation> emailPersonalisations = isHomeOfficeGovNotifyEnabled
            ? newArrayList(homeOfficeFtpaApplicationDecisionAppellantPersonalisation, appellantFtpaApplicationDecisionPersonalisationEmail, adminOfficerFtpaDecisionAppellantPersonalisation)
            : newArrayList(appellantFtpaApplicationDecisionPersonalisationEmail, adminOfficerFtpaDecisionAppellantPersonalisation);

        return Arrays.asList(
            new EmailNotificationGenerator(
                emailPersonalisations,
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                List.of(appellantFtpaApplicationDecisionPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("internalFtpaApplicationDecisionAppellantHoNotificationGenerator")
    public List<NotificationGenerator> internalFtpaApplicationDecisionAppellantHoNotificationGenerator(
            HomeOfficeFtpaApplicationDecisionAppellantPersonalisation homeOfficeFtpaApplicationDecisionAppellantPersonalisation,
            GovNotifyNotificationSender notificationSender,
            NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
                new EmailNotificationGenerator(
                        newArrayList(homeOfficeFtpaApplicationDecisionAppellantPersonalisation),
                        notificationSender,
                        notificationIdAppender
                )
        );
    }

    @Bean("ftpaApplicationDecisionRefusedOrNotAdmittedRespondentNotificationGenerator")
    public List<NotificationGenerator> ftpaApplicationDecisionRefusedOrNotAdmittedRespondentNotificationGenerator(
        HomeOfficeFtpaApplicationDecisionRespondentPersonalisation homeOfficeFtpaApplicationDecisionRespondentPersonalisation,
        LegalRepresentativeFtpaApplicationDecisionRespondentPersonalisation legalRepresentativeFtpaApplicationDecisionRespondentPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        // RIA-3361 leadershipJudgeFtpaDecision
        List<EmailNotificationPersonalisation> personalisations = isHomeOfficeGovNotifyEnabled
            ? newArrayList(homeOfficeFtpaApplicationDecisionRespondentPersonalisation, legalRepresentativeFtpaApplicationDecisionRespondentPersonalisation)
            : newArrayList(legalRepresentativeFtpaApplicationDecisionRespondentPersonalisation);

        return Arrays.asList(
            new EmailNotificationGenerator(
                personalisations,
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("ftpaApplicationDecisionRefusedOrNotAdmittedRespondentAipJourneyNotificationGenerator")
    public List<NotificationGenerator> ftpaApplicationDecisionRefusedOrNotAdmittedRespondentAipJourneyNotificationGenerator(
        HomeOfficeFtpaApplicationDecisionRespondentPersonalisation homeOfficeFtpaApplicationDecisionRespondentPersonalisation,
        AppellantFtpaApplicationDecisionPersonalisationEmail appellantFtpaApplicationDecisionPersonalisationEmail,
        AppellantFtpaApplicationDecisionPersonalisationSms appellantFtpaApplicationDecisionPersonalisationSms,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        // RIA-6135
        List<EmailNotificationPersonalisation> emailPersonalisations = isHomeOfficeGovNotifyEnabled
            ? newArrayList(homeOfficeFtpaApplicationDecisionRespondentPersonalisation, appellantFtpaApplicationDecisionPersonalisationEmail)
            : newArrayList(appellantFtpaApplicationDecisionPersonalisationEmail);

        return Arrays.asList(
            new EmailNotificationGenerator(
                emailPersonalisations,
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                List.of(appellantFtpaApplicationDecisionPersonalisationSms),
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
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        // RIA-3361 leadershipJudgeFtpaDecision
        List<EmailNotificationPersonalisation> personalisations = isHomeOfficeGovNotifyEnabled
            ? newArrayList(homeOfficeFtpaApplicationDecisionRespondentPersonalisation, legalRepresentativeFtpaApplicationDecisionRespondentPersonalisation, adminOfficerFtpaDecisionRespondentPersonalisation)
            : newArrayList(legalRepresentativeFtpaApplicationDecisionRespondentPersonalisation, adminOfficerFtpaDecisionRespondentPersonalisation);

        return Arrays.asList(
            new EmailNotificationGenerator(
                personalisations,
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("ftpaApplicationInternalDecisionGrantedOrPartiallyGrantedRespondentNotificationGenerator")
    public List<NotificationGenerator> ftpaApplicationInternalDecisionGrantedOrPartiallyRespondentGrantedNotificationGenerator(
        AdminOfficerFtpaDecisionRespondentPersonalisation adminOfficerFtpaDecisionRespondentPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        List<EmailNotificationPersonalisation> personalisations = newArrayList(adminOfficerFtpaDecisionRespondentPersonalisation);

        return Arrays.asList(
            new EmailNotificationGenerator(
                personalisations,
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("ftpaApplicationDecisionGrantedOrPartiallyGrantedRespondentAipJourneyNotificationGenerator")
    public List<NotificationGenerator> ftpaApplicationDecisionGrantedOrPartiallyRespondentGrantedAipJourneyNotificationGenerator(
        HomeOfficeFtpaApplicationDecisionRespondentPersonalisation homeOfficeFtpaApplicationDecisionRespondentPersonalisation,
        AppellantFtpaApplicationDecisionPersonalisationEmail appellantFtpaApplicationDecisionPersonalisationEmail,
        AppellantFtpaApplicationDecisionPersonalisationSms appellantFtpaApplicationDecisionPersonalisationSms,
        AdminOfficerFtpaDecisionRespondentPersonalisation adminOfficerFtpaDecisionRespondentPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        // RIA-6116
        List<EmailNotificationPersonalisation> emailPersonalisations = isHomeOfficeGovNotifyEnabled
            ? newArrayList(homeOfficeFtpaApplicationDecisionRespondentPersonalisation, appellantFtpaApplicationDecisionPersonalisationEmail, adminOfficerFtpaDecisionRespondentPersonalisation)
            : newArrayList(appellantFtpaApplicationDecisionPersonalisationEmail, adminOfficerFtpaDecisionRespondentPersonalisation);

        return Arrays.asList(
            new EmailNotificationGenerator(
                emailPersonalisations,
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                List.of(appellantFtpaApplicationDecisionPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("ftpaDecisionHomeOfficeNotificationFailedNotificationGenerator")
    public List<NotificationGenerator> ftpaDecisionHomeOfficeNotificationFailedNotificationGenerator(
        CaseOfficerFtpaDecisionHomeOfficeNotificationFailedPersonalisation caseOfficerFtpaDecisionHomeOfficeNotificationFailedPersonalisation,
        GovNotifyNotificationSender notificationSender,
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
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        // RIA-3116 reheard FTPA application (resident Judge)
        List<EmailNotificationPersonalisation> personalisations = isHomeOfficeGovNotifyEnabled
            ? newArrayList(homeOfficeFtpaApplicationDecisionAppellantPersonalisation, legalRepresentativeFtpaApplicationDecisionAppellantPersonalisation, caseOfficerFtpaDecisionPersonalisation)
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
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        // RIA-3361 residentJudgeFtpaDecision
        List<EmailNotificationPersonalisation> personalisations = isHomeOfficeGovNotifyEnabled
            ? newArrayList(homeOfficeFtpaApplicationDecisionRespondentPersonalisation, legalRepresentativeFtpaApplicationDecisionRespondentPersonalisation, caseOfficerFtpaDecisionPersonalisation)
            : newArrayList(legalRepresentativeFtpaApplicationDecisionRespondentPersonalisation, caseOfficerFtpaDecisionPersonalisation);

        return Arrays.asList(
            new EmailNotificationGenerator(
                personalisations,
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("internalFtpaApplicationDecisionRespondentHoNotificationGenerator")
    public List<NotificationGenerator> internalFtpaApplicationDecisionRespondentHoNotificationGenerator(
            HomeOfficeFtpaApplicationDecisionRespondentPersonalisation homeOfficeFtpaApplicationDecisionRespondentPersonalisation,
            GovNotifyNotificationSender notificationSender,
            NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
                new EmailNotificationGenerator(
                        newArrayList(homeOfficeFtpaApplicationDecisionRespondentPersonalisation),
                        notificationSender,
                        notificationIdAppender
                )
        );
    }

    @Bean("submitAppealPaidNotificationGenerator")
    public List<NotificationGenerator> submitAppealPaidLegalRepNotificationHandler(
        LegalRepresentativeAppealSubmittedPaidPersonalisation legalRepresentativeAppealSubmittedPaidPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
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
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
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
        HomeOfficeSubmitAppealPersonalisation homeOfficeSubmitAppealPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    legalRepresentativeAppealSubmittedPersonalisation,
                    homeOfficeSubmitAppealPersonalisation
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
        HomeOfficeAppealSubmittedPayOfflinePersonalisation homeOfficeAppealSubmittedPayOfflinePersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {
        //RIA-6682
        List<EmailNotificationPersonalisation> personalisations = newArrayList(homeOfficeAppealSubmittedPayOfflinePersonalisation, legalRepresentativeAppealSubmittedPayOfflinePersonalisation, adminOfficerAppealSubmittedPayOfflinePersonalisation);

        return singletonList(
            new EmailNotificationGenerator(
                personalisations,
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("submitAppealPayOfflineInternalAdminNotificationGenerator")
    public List<NotificationGenerator> submitAppealPayOfflineInternalAdminNotificationGenerator(
        AdminOfficerAppealSubmittedPayOfflinePersonalisation adminOfficerAppealSubmittedPayOfflinePersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {
        //RIA-6682
        List<EmailNotificationPersonalisation> personalisations = newArrayList(adminOfficerAppealSubmittedPayOfflinePersonalisation);

        return singletonList(
            new EmailNotificationGenerator(
                personalisations,
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("submitAppealPayOfflineInternalHoNotificationGenerator")
    public List<NotificationGenerator> submitAppealPayOfflineInternalHoNotificationGenerator(
        HomeOfficeAppealSubmittedPayOfflinePersonalisation homeOfficeAppealSubmittedPayOfflinePersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {
        //RIA-6682
        List<EmailNotificationPersonalisation> personalisations = newArrayList(homeOfficeAppealSubmittedPayOfflinePersonalisation);

        return singletonList(
            new EmailNotificationGenerator(
                personalisations,
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("submitAppealPendingPaymentNotificationGenerator")
    public List<NotificationGenerator> submitAppealPendingPaymentNotificationHandler(
        LegalRepresentativeAppealSubmittedPendingPaymentPersonalisation legalRepresentativeAppealSubmittedPendingPaymentPersonalisation,
        HomeOfficeAppealSubmittedPendingPaymentPersonalisation homeOfficeAppealSubmittedPendingPaymentPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        // RIA-3631 - submitAppeal This needs to be changed as per ACs
        List<EmailNotificationPersonalisation> personalisations = isHomeOfficeGovNotifyEnabled
            ? newArrayList(legalRepresentativeAppealSubmittedPendingPaymentPersonalisation, homeOfficeAppealSubmittedPendingPaymentPersonalisation)
            : newArrayList(legalRepresentativeAppealSubmittedPendingPaymentPersonalisation);

        return singletonList(
            new EmailNotificationGenerator(
                personalisations,
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("submitAppealPendingPaymentInternalNotificationGenerator")
    public List<NotificationGenerator> submitAppealPendingPaymentInternalNotificationGenerator(
        HomeOfficeAppealSubmittedPendingPaymentPersonalisation homeOfficeAppealSubmittedPendingPaymentPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        // RIA-3631 - submitAppeal This needs to be changed as per ACs
        List<EmailNotificationPersonalisation> personalisations = isHomeOfficeGovNotifyEnabled
            ? newArrayList(homeOfficeAppealSubmittedPendingPaymentPersonalisation)
            : newArrayList();

        return singletonList(
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
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
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
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
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
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
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

    @Bean("reinstateAppealAipNotificationGenerator")
    public List<NotificationGenerator> reinstateAppealAipNotificationHandler(
        AppellantReinstateAppealPersonalisationEmail appellantReinstateAppealPersonalisationEmail,
        AppellantReinstateAppealPersonalisationSms appellantReinstateAppealPersonalisationSms,
        HomeOfficeReinstateAppealPersonalisation homeOfficeReinstateAppealPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(
                    appellantReinstateAppealPersonalisationEmail,
                    homeOfficeReinstateAppealPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(
                    appellantReinstateAppealPersonalisationSms
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("homeOfficeLegalRepReinstateAppealInternalNotificationGenerator")
    public List<NotificationGenerator> homeOfficeLegalRepReinstateAppealInternalNotificationGenerator(
            HomeOfficeReinstateAppealPersonalisation homeOfficeReinstateAppealPersonalisation,
            GovNotifyNotificationSender notificationSender,
            NotificationIdAppender notificationIdAppender
    ) {
        return singletonList(
                new EmailNotificationGenerator(
                        newArrayList(
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
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
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

    @Bean("internalMakeAnApplicationNotificationGenerator")
    public List<NotificationGenerator> internalMakeAnApplicationNotificationHandler(
        HomeOfficeMakeAnApplicationPersonalisation homeOfficeMakeAnApplicationPersonalisation,        
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    homeOfficeMakeAnApplicationPersonalisation                    
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("decideAnApplicationLegalRepNotificationGenerator")
    public List<NotificationGenerator> decideAnApplicationLegalRepNotificationGenerator(
        LegalRepresentativeDecideAnApplicationPersonalisation legalRepresentativeDecideAnApplicationPersonalisation,
        HomeOfficeDecideAnApplicationPersonalisation homeOfficeDecideAnApplicationPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
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

    @Bean("internalDetainedDecideAnApplicationNotificationGenerator")
    public List<NotificationGenerator> internalDetainedDecideAnApplicationNotificationGenerator(
        DetentionEngagementTeamDecideAnApplicationPersonalisation detentionEngagementTeamDecideAnApplicationPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {
        return singletonList(
            new EmailWithLinkNotificationGenerator(
                newArrayList(
                        detentionEngagementTeamDecideAnApplicationPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("decideAnApplicationInternalHomeOfficeNotificationGenerator")
    public List<NotificationGenerator> decideAnApplicationInternalHomeOfficeNotificationGenerator(
        HomeOfficeDecideAnApplicationPersonalisation homeOfficeDecideAnApplicationPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {
        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                        homeOfficeDecideAnApplicationPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("decideARespondentApplicationHomeOfficeNotificationGenerator")
    public List<NotificationGenerator> decideARespondentApplicationHomeOfficeNotificationGenerator(
        HomeOfficeDecideAnApplicationPersonalisation homeOfficeDecideAnApplicationPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {
        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    homeOfficeDecideAnApplicationPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("internalDetainedDecideARespondentApplicationNotificationGenerator")
    public List<NotificationGenerator> internalDetainedDecideARespondentApplicationNotificationGenerator(
        DetentionEngagementTeamDecideARespondentApplicationPersonalisation detentionEngagementTeamDecideARespondentApplicationPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {
        return singletonList(
            new EmailWithLinkNotificationGenerator(
                newArrayList(detentionEngagementTeamDecideARespondentApplicationPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("decideARespondentApplicationInternalAppellantNotificationGenerator")
    public List<NotificationGenerator> decideARespondentApplicationInternalAppellantNotificationGenerator(
        DetentionEngagementTeamHomeOfficeApplicationDecidedPersonalisation detHomeOfficeApplicationDecidedPersonalisationhomeOfficeApplicationDecidedPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return singletonList(
            new EmailWithLinkNotificationGenerator(
                    newArrayList(Collections.singleton(detHomeOfficeApplicationDecidedPersonalisationhomeOfficeApplicationDecidedPersonalisation)),
                    notificationSender,
                    notificationIdAppender
            )
        );
    }


    @Bean("decideAnApplicationAipNotificationGenerator")
    public List<NotificationGenerator> decideAnApplicationAipNotificationGenerator(
        HomeOfficeDecideAnApplicationPersonalisation homeOfficeDecideAnApplicationPersonalisation,
        AppellantDecideAnApplicationPersonalisationEmail appellantDecideAnApplicationPersonalisationEmail,
        AppellantDecideAnApplicationPersonalisationSms appellantDecideAnApplicationPersonalisationSms,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(
                    homeOfficeDecideAnApplicationPersonalisation,
                    appellantDecideAnApplicationPersonalisationEmail
                ),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(appellantDecideAnApplicationPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("remissionDecisionApprovedNotificationGenerator")
    public List<NotificationGenerator> remissionDecisionApprovedNotificationHandler(
        LegalRepresentativeRemissionDecisionApprovedPersonalisation legalRepresentativeRemissionDecisionApprovedPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    legalRepresentativeRemissionDecisionApprovedPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("remissionDecisionPartiallyApprovedNotificationGenerator")
    public List<NotificationGenerator> remissionDecisionPartiallyApprovedNotificationHandler(
        LegalRepresentativeRemissionDecisionPartiallyApprovedPersonalisation legalRepresentativeRemissionDecisionPartiallyApprovedPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    legalRepresentativeRemissionDecisionPartiallyApprovedPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("remissionDecisionPaPartiallyApprovedNotificationGenerator")
    public List<NotificationGenerator> remissionDecisionPaPartiallyApprovedNotificationHandler(
        LegalRepresentativeRemissionDecisionPaPartiallyApprovedPersonalisation legalRepresentativeRemissionDecisionPaPartiallyApprovedPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    legalRepresentativeRemissionDecisionPaPartiallyApprovedPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("remissionDecisionRejectedNotificationGenerator")
    public List<NotificationGenerator> remissionDecisionRejectedNotificationHandler(
        LegalRepresentativeRemissionDecisionRejectedPersonalisation legalRepresentativeRemissionDecisionRejectedPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    legalRepresentativeRemissionDecisionRejectedPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("remissionDecisionPaRejectedNotificationGenerator")
    public List<NotificationGenerator> remissionDecisionPaRejectedNotificationHandler(
        LegalRepresentativeRemissionDecisionPaRejectedPersonalisation legalRepresentativeRemissionDecisionPaRejectedPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    legalRepresentativeRemissionDecisionPaRejectedPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("nocRequestDecisionHomeOfficeNotificationGenerator")
    public List<NotificationGenerator> nocRequestDecisionHomeOfficeNotificationGenerator(
        HomeOfficeNocRequestDecisionPersonalisation homeOfficeNocRequestDecisionPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    homeOfficeNocRequestDecisionPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            ) {
                @Override
                public Message getSuccessMessage() {
                    return new Message("success", "body");
                }
            }
        );
    }

    @Bean("nocRequestDecisionLrNotificationGenerator")
    public List<NotificationGenerator> nocRequestDecisionLrNotificationGenerator(
        LegalRepresentativeNocRequestDecisionPersonalisation legalRepresentativeNocRequestDecisionPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {
        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    legalRepresentativeNocRequestDecisionPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            ) {
                @Override
                public Message getSuccessMessage() {
                    return new Message("success", "body");
                }
            }
        );
    }

    @Bean("removeRepresentationNotificationGenerator")
    public List<NotificationGenerator> removeRepresentationNotificationHandler(
        LegalRepresentativeRemoveRepresentationPersonalisation legalRepresentativeRemoveRepresentationPersonalisation,
        HomeOfficeRemoveRepresentationPersonalisation homeOfficeRemoveRepresentationPersonalisation,
        CaseOfficerRemoveRepresentationPersonalisation caseOfficerRemoveRepresentationPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    legalRepresentativeRemoveRepresentationPersonalisation,
                    homeOfficeRemoveRepresentationPersonalisation,
                    caseOfficerRemoveRepresentationPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            ) {
                @Override
                public Message getSuccessMessage() {
                    return new Message("success", "body");
                }
            }
        );
    }

    @Bean("removeRepresentationAppellantEmailNotificationGenerator")
    public List<NotificationGenerator> removeRepresentationAppellantEmailNotificationHandler(
        AppellantRemoveRepresentationPersonalisationEmail appellantRemoveRepresentationPersonalisationEmail,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    appellantRemoveRepresentationPersonalisationEmail
                ),
                notificationSender,
                notificationIdAppender
            ) {
                @Override
                public Message getSuccessMessage() {
                    return new Message("success", "body");
                }
            }
        );
    }

    @Bean("removeRepresentationAppellantSmsNotificationGenerator")
    public List<NotificationGenerator> removeRepresentationAppellantSmsNotificationHandler(
        AppellantRemoveRepresentationPersonalisationSms appellantRemoveRepresentationPersonalisationSms,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
            new SmsNotificationGenerator(
                newArrayList(appellantRemoveRepresentationPersonalisationSms),
                notificationSender,
                notificationIdAppender
            ) {
                @Override
                public Message getSuccessMessage() {
                    return new Message("success", "body");
                }
            }
        );
    }

    @Bean("removeRepresentativeAppellantEmailNotificationGenerator")
    public List<NotificationGenerator> removeRepresentativeAppellantEmailNotificationHandler(
        AppellantRemoveRepresentationPersonalisationEmail appellantRemoveRepresentationPersonalisationEmail,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    appellantRemoveRepresentationPersonalisationEmail
                ),
                notificationSender,
                notificationIdAppender
            ) {
                @Override
                public Message getSuccessMessage() {
                    return new Message("header", "body");
                }
            }
        );
    }

    @Bean("removeRepresentativeAppellantSmsNotificationGenerator")
    public List<NotificationGenerator> removeRepresentativeAppellantSmsNotificationHandler(
        AppellantRemoveRepresentationPersonalisationSms appellantRemoveRepresentationPersonalisationSms,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
            new SmsNotificationGenerator(
                newArrayList(appellantRemoveRepresentationPersonalisationSms),
                notificationSender,
                notificationIdAppender
            ) {
                @Override
                public Message getSuccessMessage() {
                    return new Message("header", "body");
                }
            }
        );
    }

    @Bean("removeRepresentativeAppellantDetainedOtherLetterNotificationHandler")
    public List<NotificationGenerator> removeRepresentativeAppellantDetainedOtherLetterNotificationHandler(
        AppellantRemoveRepresentationDetainedOtherPersonalisation appellantRemoveRepresentationDetainedOtherPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
            new LetterNotificationGenerator(
                newArrayList(appellantRemoveRepresentationDetainedOtherPersonalisation),
                notificationSender,
                notificationIdAppender
            ) {
                @Override
                public Message getSuccessMessage() {
                    return new Message("header", "body");
                }
            }
        );
    }

    @Bean("requestFeeRemissionNotificationGenerator")
    public List<NotificationGenerator> requestFeeRemissionNotificationHandler(
        LegalRepresentativeRequestFeeRemissionPersonalisation legalRepresentativeRequestFeeRemissionPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    legalRepresentativeRequestFeeRemissionPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("caseOfficerManageFeeUpdateGenerator")
    public List<NotificationGenerator> manageFeeUpdateNotificationHandler(
        CaseOfficerManageFeeUpdatePersonalisation caseOfficerManageFeeUpdatePersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {
        return singletonList(
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
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
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
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
            new SmsNotificationGenerator(
                newArrayList(
                    appellantNocRequestDecisionPersonalisationSms
                ),
                notificationSender,
                notificationIdAppender

            ) {
                @Override
                public Message getSuccessMessage() {
                    return new Message("success", "body");
                }
            }
        );
    }

    @Bean("aipNocRequestDecisionAppellantNotificationGenerator")
    public List<NotificationGenerator> aipNocRequestDecisionAppellantNotificationHandler(
        AipAppellantNocRequestDecisionPersonalisationEmail aipAppellantNocRequestDecisionPersonalisationEmail,
        AipAppellantNocRequestDecisionPersonalisationSms aipAppellantNocRequestDecisionPersonalisationSms,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(
                    aipAppellantNocRequestDecisionPersonalisationEmail
                ),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(aipAppellantNocRequestDecisionPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("startAppealAipAppellantDisposalNotificationGenerator")
    public List<NotificationGenerator> startAppealAipAppellantDisposalNotificationGenerator(
        AipAppellantStartAppealDisposalPersonalisationEmail aipAppellantStartAppealDisposalPersonalisationEmail,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {
        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(aipAppellantStartAppealDisposalPersonalisationEmail),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("editAppealAipAppellantDisposalNotificationGenerator")
    public List<NotificationGenerator> editAppealAipAppellantDisposalNotificationGenerator(
        AipAppellantEditAppealDisposalPersonalisationEmail appellantEditAppealDisposalPersonalisationEmail,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {
        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(appellantEditAppealDisposalPersonalisationEmail),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("submitAppealAppellantEmailNotificationGenerator")
    public List<NotificationGenerator> submitAppealAppellantEmailNotificationGenerator(
        AppellantSubmitAppealPersonalisationEmail appellantSubmitAppealPersonalisationEmail,
        GovNotifyNotificationSender notificationSender,
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
        GovNotifyNotificationSender notificationSender,
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
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    legalRepresentativeManageFeeUpdatePersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("manageFeeUpdateAdditionalPaymentRequestedNotificationGenerator")
    public List<NotificationGenerator> manageFeeUpdateAdditionalPaymentRequestedNotificationHandler(
        LegalRepresentativeManageFeeUpdateAdditionalPaymentPersonalisation legalRepresentativeManageFeeUpdateAdditionalPaymentPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    legalRepresentativeManageFeeUpdateAdditionalPaymentPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("recordOfTimeDecisionCanProceedEmailNotificationGenerator")
    public List<NotificationGenerator> recordOfTimeDecisionCanProceedEmailNotificationHandler(
        LegalRepresentativeRecordOutOfTimeDecisionCanProceed legalRepresentativeRecordOutOfTimeDecisionCanProceed,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    legalRepresentativeRecordOutOfTimeDecisionCanProceed
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("appealSubmittedLateWithExemptionEmailInternalNotificationGenerator")
    public List<NotificationGenerator> appealSubmittedLateWithExemptionEmailInternalNotificationHandler(
        DetentionEngagementTeamAppealSubmittedLateWithExemptionPersonalisation appealSubmittedLateWithExemptionPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailWithLinkNotificationGenerator(
                newArrayList(Collections.singleton(appealSubmittedLateWithExemptionPersonalisation)),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("appealSubmittedInTimeWithFeeToPayEmailInternalNotificationGenerator")
    public List<NotificationGenerator> appealSubmittedInTimeWithFeeToPayEmailInternalNotificationHandler(
        DetentionEngagementTeamAppealSubmittedInTimeWithFeeToPayPersonalisation appealSubmittedInTimeWithFeeToPayPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailWithLinkNotificationGenerator(
                newArrayList(Collections.singleton(appealSubmittedInTimeWithFeeToPayPersonalisation)),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("internalDetainedAppealHoUploadBundle")
    public List<NotificationGenerator> internalDetainedAppealHoUploadBundleLetter(
            DetentionEngagementTeamHoUploadBundlePersonalisation detentionEngagementTeamHoUploadBundlePersonalisation,
            GovNotifyNotificationSender notificationSender,
            NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
                new EmailWithLinkNotificationGenerator(
                        newArrayList(Collections.singleton(detentionEngagementTeamHoUploadBundlePersonalisation)),
                        notificationSender,
                        notificationIdAppender
                )
        );
    }

    @Bean("internalDetainedOutOfTimeDecisionAllowedEmailNotificationGenerator")
    public List<NotificationGenerator> internalDetainedOutOfTimeDecisionAllowedEmailNotificationGenerator(
            DetentionEngagementTeamOutOfTimeDecisionAllowedPersonalisation personalisation,
            GovNotifyNotificationSender notificationSender,
            NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
                new EmailWithLinkNotificationGenerator(
                        newArrayList(Collections.singleton(personalisation)),
                        notificationSender,
                        notificationIdAppender
                )
        );
    }

    @Bean("internalDetainedAppealRemissionGrantedInTimeLetter")
    public List<NotificationGenerator> internalDetainedAppealRemissionGrantedInTimeLetter(
        DetentionEngagementTeamRemissionGrantedInTimePersonalisation internalDetainedAppealRemissionGrantedInTimeLetter,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailWithLinkNotificationGenerator(
                newArrayList(Collections.singleton(internalDetainedAppealRemissionGrantedInTimeLetter)),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("appealSubmittedLateWithFeeEmailInternalNotificationGenerator")
    public List<NotificationGenerator> appealSubmittedLateWithFeeEmailInternalNotificationHandler(
        DetentionEngagementTeamAppealSubmittedLateWithFeePersonalisation appealSubmittedLateWithFeePersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailWithLinkNotificationGenerator(
                newArrayList(Collections.singleton(appealSubmittedLateWithFeePersonalisation)),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("detainedAppealHearingAdjournedNoDateAppellantEmail")
    public List<NotificationGenerator> detainedAppealHearingAdjournedNoDateAppellantEmail(
        DetentionEngagementTeamHearingAdjournedNoDateAppellantEmailPersonalisation detentionEngagementTeamHearingAdjournedNoDateAppellantEmailPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return singletonList(
            new EmailWithLinkNotificationGenerator(
                newArrayList(Collections.singleton(detentionEngagementTeamHearingAdjournedNoDateAppellantEmailPersonalisation)),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("remissionPartiallyGrantedOrRefusedInPrisonOrIrcAipManualEmailInternalNotificationGenerator")
    public List<NotificationGenerator> remissionPartiallyGrantedOrRefusedInPrisonOrIrcAipManualEmailInternalNotificationHandler(
        DetRemissionPartiallyGrantedOrRefusedInPrisonOrIrcAipManualPersonalisation remissionPartiallyGrantedOrRefusedInPrisonOrIrcAipManualPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailWithLinkNotificationGenerator(
                newArrayList(Collections.singleton(remissionPartiallyGrantedOrRefusedInPrisonOrIrcAipManualPersonalisation)),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("lateRemissionPartiallyGrantedOrRefusedInPrisonOrIrcAipManualEmailInternalNotificationGenerator")
    public List<NotificationGenerator> lateRemissionPartiallyGrantedOrRefusedInPrisonOrIrcAipManualEmailInternalNotificationHandler(
            DetLateRemissionPartiallyGrantedOrRefusedInPrisonOrIrcAipManualPersonalisation personalisation,
            GovNotifyNotificationSender notificationSender,
            NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
                new EmailWithLinkNotificationGenerator(
                    newArrayList(Collections.singleton(personalisation)),
                    notificationSender,
                    notificationIdAppender
                )
        );
    }

    @Bean("lateRemissionPartiallyGrantedInPrisonOrIrcAipManualEmailInternalNotificationGenerator")
    public List<NotificationGenerator> lateRemissionPartiallyGrantedInPrisonOrIrcAipManualEmailInternalNotificationHandler(
            DetLateRemissionGrantedInPrisonOrIrcAipManualPersonalisation personalisation,
            GovNotifyNotificationSender notificationSender,
            NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
                new EmailWithLinkNotificationGenerator(
                        newArrayList(Collections.singleton(personalisation)),
                        notificationSender,
                        notificationIdAppender
                )
        );
    }

    @Bean("lateRemissionRefusedInPrisonOrIrcAipManualEmailInternalNotificationGenerator")
    public List<NotificationGenerator> lateRemissionRefusedInPrisonOrIrcAipManualEmailInternalNotificationHandler(
            DetLateRemissionRefusedInPrisonOrIrcAipManualPersonalisation personalisation,
            GovNotifyNotificationSender notificationSender,
            NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
                new EmailWithLinkNotificationGenerator(
                        newArrayList(Collections.singleton(personalisation)),
                        notificationSender,
                        notificationIdAppender
                )
        );
    }

    @Bean("appealSubmittedWithExemptionEmailInternalNotificationGenerator")
    public List<NotificationGenerator> appealSubmittedWithExemptionEmailInternalNotificationHandler(
        DetentionEngagementTeamAppealSubmittedWithExemptionPersonalisation appealSubmittedWithExemptionPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailWithLinkNotificationGenerator(
                newArrayList(Collections.singleton(appealSubmittedWithExemptionPersonalisation)),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("recordOfTimeDecisionCannotProceedEmailNotificationGenerator")
    public List<NotificationGenerator> recordOfTimeDecisionCannotProceedEmailNotificationHandler(
        LegalRepresentativeRecordOutOfTimeDecisionCannotProceed legalRepresentativeRecordOutOfTimeDecisionCannotProceed,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    legalRepresentativeRecordOutOfTimeDecisionCannotProceed
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("recordOfTimeDecisionCannotProceedAppellantEmailNotificationGenerator")
    public List<NotificationGenerator> recordOfTimeDecisionCannotProceedAppellantEmailNotificationGenerator(
        AppellantRecordOutOfTimeDecisionCannotProceedPersonalisationEmail appellantRecordOutOfTimeDecisionCannotProceedPersonalisationEmail,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    appellantRecordOutOfTimeDecisionCannotProceedPersonalisationEmail
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("recordOfTimeDecisionCannotProceedAppellantSmsNotificationGenerator")
    public List<NotificationGenerator> recordOfTimeDecisionCannotProceedAppellantSmsNotificationGenerator(
        AppellantRecordOutOfTimeDecisionCannotProceedPersonalisationSms appellantRecordOutOfTimeDecisionCannotProceedPersonalisationSms,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new SmsNotificationGenerator(
                newArrayList(appellantRecordOutOfTimeDecisionCannotProceedPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("recordOfTimeDecisionCanProceedAppellantEmailNotificationGenerator")
    public List<NotificationGenerator> recordOfTimeDecisionCanProceedAppellantEmailNotificationGenerator(
        AppellantRecordOutOfTimeDecisionCanProceedPersonalisationEmail appellantRecordOutOfTimeDecisionCanProceedPersonalisationEmail,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    appellantRecordOutOfTimeDecisionCanProceedPersonalisationEmail
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("recordOfTimeDecisionCanProceedAppellantSmsNotificationGenerator")
    public List<NotificationGenerator> recordOfTimeDecisionCanProceedAppellantSmsNotificationGenerator(
        AppellantRecordOutOfTimeDecisionCanProceedPersonalisationSms appellantRecordOutOfTimeDecisionCanProceedPersonalisationSms,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new SmsNotificationGenerator(
                newArrayList(appellantRecordOutOfTimeDecisionCanProceedPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("editPaymentMethodNotificationGenerator")
    public List<NotificationGenerator> editPaymentMethodNotificationHandler(
        LegalRepresentativeAppealSubmittedPendingPaymentPersonalisation legalRepresentativeAppealSubmittedPendingPaymentPersonalisation,
        HomeOfficeAppealSubmittedPendingPaymentPersonalisation homeOfficeAppealSubmittedPendingPaymentPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    legalRepresentativeAppealSubmittedPendingPaymentPersonalisation,
                    homeOfficeAppealSubmittedPendingPaymentPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("editPaymentMethodAoNotificationGenerator")
    public List<NotificationGenerator> editPaymentMethodAoNotificationHandler(
        AdminOfficerEditPaymentMethodPersonalisation adminOfficerEditPaymentMethodPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    adminOfficerEditPaymentMethodPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("payAndSubmitAppealEmailNotificationGenerator")
    public List<NotificationGenerator> payAndSubmitAppealEmailNotificationHandler(
        LegalRepresentativeAppealSubmittedPaidPersonalisation legalRepresentativeAppealSubmittedPaidPersonalisation,
        HomeOfficeSubmitAppealPersonalisation homeOfficeSubmitAppealPersonalisation,
        CaseOfficerSubmitAppealPersonalisation caseOfficerSubmitAppealPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    legalRepresentativeAppealSubmittedPaidPersonalisation,
                    homeOfficeSubmitAppealPersonalisation,
                    caseOfficerSubmitAppealPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            ) {
                @Override
                public Message getSuccessMessage() {
                    return new Message("success", "body");
                }
            }
        );
    }

    @Bean("payAndSubmitAppealEmailInternalNotificationGenerator")
    public List<NotificationGenerator> payAndSubmitAppealEmailInternalNotificationGenerator(
        HomeOfficeSubmitAppealPersonalisation homeOfficeSubmitAppealPersonalisation,
        CaseOfficerSubmitAppealPersonalisation caseOfficerSubmitAppealPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    homeOfficeSubmitAppealPersonalisation,
                    caseOfficerSubmitAppealPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            ) {
                @Override
                public Message getSuccessMessage() {
                    return new Message("success", "body");
                }
            }
        );
    }

    @Bean("payAndSubmitAppealFailedEmailNotificationGenerator")
    public List<NotificationGenerator> payAndSubmitAppealFailedEmailNotificationHandler(
        LegalRepresentativeAppealSubmittedPaidPersonalisation legalRepresentativeAppealSubmittedPaidPersonalisation,
        HomeOfficeSubmitAppealPersonalisation homeOfficeSubmitAppealPersonalisation,
        CaseOfficerSubmitAppealPersonalisation caseOfficerSubmitAppealPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    legalRepresentativeAppealSubmittedPaidPersonalisation,
                    homeOfficeSubmitAppealPersonalisation,
                    caseOfficerSubmitAppealPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            ) {
                @Override
                public Message getSuccessMessage() {
                    return new Message("success", "body");
                }
            }
        );
    }

    @Bean("payAndSubmitAppealFailedEmailInternalNotificationGenerator")
    public List<NotificationGenerator> payAndSubmitAppealFailedEmailInternalNotificationGenerator(
        HomeOfficeSubmitAppealPersonalisation homeOfficeSubmitAppealPersonalisation,
        CaseOfficerSubmitAppealPersonalisation caseOfficerSubmitAppealPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    homeOfficeSubmitAppealPersonalisation,
                    caseOfficerSubmitAppealPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            ) {
                @Override
                public Message getSuccessMessage() {
                    return new Message("success", "body");
                }
            }
        );
    }

    @Bean("payAndSubmitAppealAppellantEmailNotificationGenerator")
    public List<NotificationGenerator> payAndSubmitAppealAppellantEmailNotificationHandler(
        AppellantSubmitAppealPersonalisationEmail appellantSubmitAppealPersonalisationEmail,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(appellantSubmitAppealPersonalisationEmail),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("payAndSubmitAppealAppellantSmsNotificationGenerator")
    public List<NotificationGenerator> payAndSubmitAppealAppellantSmsNotificationHandler(
        AppellantSubmitAppealPersonalisationSms appellantSubmitAppealPersonalisationSms,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new SmsNotificationGenerator(
                newArrayList(appellantSubmitAppealPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("payForAppealEmailNotificationGenerator")
    public List<NotificationGenerator> payForAppealAppealEmailNotificationHandler(
        LegalRepresentativeAppealSubmittedPaidPersonalisation legalRepresentativeAppealSubmittedPaidPersonalisation,
        HomeOfficeSubmitAppealPersonalisation homeOfficeSubmitAppealPersonalisation,
        CaseOfficerSubmitAppealPersonalisation caseOfficerSubmitAppealPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    legalRepresentativeAppealSubmittedPaidPersonalisation,
                    homeOfficeSubmitAppealPersonalisation,
                    caseOfficerSubmitAppealPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            ) {
                @Override
                public Message getSuccessMessage() {
                    return new Message("success", "body");
                }
            }
        );
    }

    @Bean("paymentPaidPostSubmitNotificationGenerator")
    public List<NotificationGenerator> paymentPaidPostSubmitLegalRepNotificationHandler(
        LegalRepresentativePaymentPaidPersonalisation legalRepresentativePaymentPaidPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    legalRepresentativePaymentPaidPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            ) {
                @Override
                public Message getSuccessMessage() {
                    return new Message("success", "body");
                }
            }
        );
    }

    @Bean("upperTribunalBundleFailedNotificationGenerator")
    public List<NotificationGenerator> upperTribunalBundleFailedNotificationGenerator(
        AdminOfficerUpperTribunalBundleFailedPersonalisation adminOfficerUpperTribunalBundleFailedPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(adminOfficerUpperTribunalBundleFailedPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("requestHearingRequirementsAipNotificationGenerator")
    public List<NotificationGenerator> requestHearingRequirementsAipNotificationGenerator(
        AppellantRequestHearingRequirementsPersonalisationEmail appellantRequestHearingRequirementsPersonalisationEmail,
        AppellantRequestHearingRequirementsPersonalisationSms appellantRequestHearingRequirementsPersonalisationSms,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(appellantRequestHearingRequirementsPersonalisationEmail),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(appellantRequestHearingRequirementsPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("appealEndedAutomaticallyNotificationGenerator")
    public List<NotificationGenerator> appealEndedAutomaticallyNotificationGenerator(
        LegalRepresentativeEndAppealAutomaticallyPersonalisation legalRepresentativeEndAppealAutomaticallyPersonalisation,
        HomeOfficeEndAppealAutomaticallyPersonalisation homeOfficeEndAppealAutomaticallyPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return List.of(
            new EmailNotificationGenerator(
                newArrayList(legalRepresentativeEndAppealAutomaticallyPersonalisation,
                    homeOfficeEndAppealAutomaticallyPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("appealEndedAutomaticallyHoNotificationGenerator")
    public List<NotificationGenerator> appealEndedAutomaticallyHoNotificationGenerator(
            HomeOfficeEndAppealAutomaticallyPersonalisation homeOfficeEndAppealAutomaticallyPersonalisation,
            GovNotifyNotificationSender notificationSender,
            NotificationIdAppender notificationIdAppender) {

        return List.of(
                new EmailNotificationGenerator(
                        newArrayList(
                                homeOfficeEndAppealAutomaticallyPersonalisation),
                        notificationSender,
                        notificationIdAppender
                )
        );
    }

    @Bean("aipAppealEndedAutomaticallyNotificationGenerator")
    public List<NotificationGenerator> aipAppealEndedAutomaticallyNotificationGenerator(
        AppellantEndAppealAutomaticallyPersonalisationEmail appellantEndAppealAutomaticallyPersonalisationEmail,
        AppellantEndAppealAutomaticallyPersonalisationSms appellantEndAppealAutomaticallyPersonalisationSms,
        HomeOfficeEndAppealAutomaticallyPersonalisation homeOfficeEndAppealAutomaticallyPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return List.of(
            new EmailNotificationGenerator(
                newArrayList(appellantEndAppealAutomaticallyPersonalisationEmail,
                    homeOfficeEndAppealAutomaticallyPersonalisation),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(appellantEndAppealAutomaticallyPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("updatePaymentStatusPaidAppealSubmittedLrHoGenerator")
    public List<NotificationGenerator> updatePaymentStatusPaidAppealSubmittedNotificationGenerator(
        LegalRepresentativeAppealSubmittedPersonalisation legalRepresentativeAppealSubmittedPersonalisation,
        HomeOfficeSubmitAppealPersonalisation homeOfficeSubmitAppealPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    legalRepresentativeAppealSubmittedPersonalisation,
                    homeOfficeSubmitAppealPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("submitAppealLrHoWaysToPayPaPayNowNotificationGenerator")
    public List<NotificationGenerator> submitAppealLrHoWaysToPayPaPayNowNotificationGenerator(
        LegalRepresentativeAppealSubmittedPersonalisation legalRepresentativeAppealSubmittedPersonalisation,
        HomeOfficeSubmitAppealPersonalisation homeOfficeSubmitAppealPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    legalRepresentativeAppealSubmittedPersonalisation,
                    homeOfficeSubmitAppealPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("submitAppealLrHoWaysToPayPaPayNowInternalNotificationGenerator")
    public List<NotificationGenerator> submitAppealLrHoWaysToPayPaPayNowInternalNotificationGenerator(
        HomeOfficeSubmitAppealPersonalisation homeOfficeSubmitAppealPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    homeOfficeSubmitAppealPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("submitAppealLegalRepAaaNotificationGenerator")
    public List<NotificationGenerator> submitAppealLegalRepAaaNotificationHandler(
        LegalRepresentativeAppealSubmittedPersonalisation legalRepresentativeAppealSubmittedPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    legalRepresentativeAppealSubmittedPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("startAppealLegalRepDisposalNotificationGenerator")
    public List<NotificationGenerator> startAppealLegalRepDisposalNotificationGenerator(
        LegalRepresentativeAppealStartedDisposalPersonalisation legalRepresentativeAppealStartedDisposalPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {
        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    legalRepresentativeAppealStartedDisposalPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("editAppealLegalRepDisposalNotificationGenerator")
    public List<NotificationGenerator> editAppealLegalRepDisposalNotificationGenerator(
        LegalRepresentativeAppealEditedDisposalPersonalisation legalRepresentativeAppealEditedDisposalPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {
        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    legalRepresentativeAppealEditedDisposalPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("adaSuitabilityNotificationGenerator")
    public List<NotificationGenerator> adaSuitabilityNotificationGenerator(
        LegalRepresentativeAdaSuitabilityPersonalisation legalRepresentativeAdaSuitabilityPersonalisation,
        HomeOfficeAdaSuitabilityPersonalisation homeOfficeAdaSuitabilityPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    legalRepresentativeAdaSuitabilityPersonalisation,
                    homeOfficeAdaSuitabilityPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("adaSuitabilityInternalAdaNotificationGenerator")
    public List<NotificationGenerator> adaSuitabilityInternalAdaNotificationGenerator(
        DetentionEngagementTeamAdaSuitabilityReviewPersonalisation detentionEngagementTeamAdaSuitabilityReviewPersonalisation,
        HomeOfficeAdaSuitabilityPersonalisation homeOfficeAdaSuitabilityPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailWithLinkNotificationGenerator(
                newArrayList(
                    detentionEngagementTeamAdaSuitabilityReviewPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            ),
            new EmailNotificationGenerator(
                newArrayList(
                    homeOfficeAdaSuitabilityPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("transferOutOfAdaNotificationGenerator")
    public List<NotificationGenerator> transferOutOfAdaNotificationGenerator(
        LegalRepresentativeTransferOutOfAdaPersonalisation legalRepresentativeTransferOutOfAdaPersonalisation,
        HomeOfficeTransferOutOfAdaPersonalisation homeOfficeTransferOutOfAdaPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    legalRepresentativeTransferOutOfAdaPersonalisation,
                    homeOfficeTransferOutOfAdaPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("markAppealAsAdaNotificationGenerator")
    public List<NotificationGenerator> markAppealAsAdaNotificationGenerator(
        LegalRepresentativeMarkAppealAsAdaPersonalisation legalRepresentativeMarkAppealAsAdaPersonalisation,
        HomeOfficeMarkAppealAsAdaPersonalisation homeOfficeMarkAppealAsAdaPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    legalRepresentativeMarkAppealAsAdaPersonalisation,
                    homeOfficeMarkAppealAsAdaPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("removeDetentionStatusNotificationGenerator")
    public List<NotificationGenerator> removeDetentionStatusNotificationGenerator(
        LegalRepresentativeRemoveDetentionStatusPersonalisation legalRepresentativeRemoveDetentionStatusPersonalisation,
        HomeOfficeRemoveDetentionStatusPersonalisation homeOfficeRemoveDetentionStatusPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    legalRepresentativeRemoveDetentionStatusPersonalisation,
                    homeOfficeRemoveDetentionStatusPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("internalLrRemoveDetentionStatusNotificationGenerator")
    public List<NotificationGenerator> internalLrRemoveDetentionStatusNotificationGenerator(
            HomeOfficeRemoveDetentionStatusPersonalisation homeOfficeRemoveDetentionStatusPersonalisation,
            GovNotifyNotificationSender notificationSender,
            NotificationIdAppender notificationIdAppender
    ) {
        return singletonList(
                new EmailNotificationGenerator(
                        newArrayList(homeOfficeRemoveDetentionStatusPersonalisation),
                        notificationSender,
                        notificationIdAppender
                )
        );
    }

    @Bean("removeDetentionStatusLegalRepManualNotificationGenerator")
    public List<NotificationGenerator> removeDetentionStatusLegalRepManualNotificationGenerator(
        LegalRepresentativeRemoveDetentionStatusLetterPersonalisation legalRepresentativeRemoveDetentionStatusLetterPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
            new LetterNotificationGenerator(
                newArrayList(
                    legalRepresentativeRemoveDetentionStatusLetterPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("removeDetentionStatusInternalNotificationGenerator")
    public List<NotificationGenerator> removeDetentionStatusInternalNotificationGenerator(
        AppellantRemoveDetainedStatusPersonalisationEmail appellantRemoveDetainedStatusPersonalisationEmail,
        AppellantRemoveDetainedStatusPersonalisationSms appellantRemoveDetainedStatusPersonalisationSms,
        HomeOfficeRemoveDetentionStatusPersonalisation homeOfficeRemoveDetentionStatusPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(
                    appellantRemoveDetainedStatusPersonalisationEmail,
                    homeOfficeRemoveDetentionStatusPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(appellantRemoveDetainedStatusPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("markAsDetainedNotificationGenerator")
    public List<NotificationGenerator> markAppealAsDetainedNotificationGenerator(
        HomeOfficeMarkAppealAsDetainedPersonalisation homeOfficeMarkAppealAsDetainedPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    homeOfficeMarkAppealAsDetainedPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("markAsDetainedLegalRepNotificationGenerator")
    public List<NotificationGenerator> markAppealAsDetainedLegalRepNotificationGenerator(
            LegalRepresentativeMarkAppealAsDetainedPersonalisation legalRepresentativeMarkAppealAsDetainedPersonalisation,
            GovNotifyNotificationSender notificationSender,
            NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
                new EmailNotificationGenerator(
                        newArrayList(
                                legalRepresentativeMarkAppealAsDetainedPersonalisation
                        ),
                        notificationSender,
                        notificationIdAppender
                )
        );
    }

    @Bean("markAsDetainedLegalRepManualNotificationGenerator")
    public List<NotificationGenerator> markAsDetainedLegalRepManualNotificationGenerator(
        LegalRepresentativeMarkAppealAsDetainedLetterPersonalisation legalRepresentativeMarkAppealAsDetainedLetterPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
            new LetterNotificationGenerator(
                newArrayList(
                    legalRepresentativeMarkAppealAsDetainedLetterPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("updateDetentionLocationLegalRepManualNotificationGenerator")
    public List<NotificationGenerator> updateDetentionLocationLegalRepManualNotificationGenerator(
            LegalRepresentativeLetterUpdateDetentionLocationPersonalisation legalRepresentativeUpdateDetentionLocationLetterPersonalisation,
            GovNotifyNotificationSender notificationSender,
            NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
                new LetterNotificationGenerator(
                        newArrayList(
                                legalRepresentativeUpdateDetentionLocationLetterPersonalisation
                        ),
                        notificationSender,
                        notificationIdAppender
                )
        );
    }

    @Bean("markAsReadyForUtTransferNotificationGenerator")
    public List<NotificationGenerator> markAsReadyForUtTransferNotificationGenerator(
        UpperTribunalMarkAsReadyForUtTransferPersonalisation upperTribunalMarkAsReadyForUtTransferPersonalisation,
        LegalRepresentativeMarkAsReadyForUtTransferPersonalisation legalRepresentativeMarkAsReadyForUtTransferPersonalisation,
        HomeOfficeMarkAppealReadyForUtTransferPersonalisation homeOfficeMarkAppealReadyForUtTransferPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    upperTribunalMarkAsReadyForUtTransferPersonalisation,
                    legalRepresentativeMarkAsReadyForUtTransferPersonalisation,
                    homeOfficeMarkAppealReadyForUtTransferPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("markAsReadyForUtTransferInternalNotificationGenerator")
    public List<NotificationGenerator> markAsReadyForUtTransferInternalNotificationGenerator(
        UpperTribunalMarkAsReadyForUtTransferPersonalisation upperTribunalMarkAsReadyForUtTransferPersonalisation,
        HomeOfficeMarkAppealReadyForUtTransferPersonalisation homeOfficeMarkAppealReadyForUtTransferPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    upperTribunalMarkAsReadyForUtTransferPersonalisation,
                    homeOfficeMarkAppealReadyForUtTransferPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("aipMarkAsReadyForUtTransferNotificationGenerator")
    public List<NotificationGenerator> aipMarkAsReadyForUtTransferNotificationGenerator(
        UpperTribunalMarkAsReadyForUtTransferPersonalisation upperTribunalMarkAsReadyForUtTransferPersonalisation,
        AppellantMarkAsReadyForUtTransferPersonalisationEmail appellantMarkAsReadyForUtTransferPersonalisationEmail,
        AppellantMarkAsReadyForUtTransferPersonalisationSms appellantMarkAsReadyForUtTransferPersonalisationSms,
        HomeOfficeMarkAppealReadyForUtTransferPersonalisation homeOfficeMarkAppealReadyForUtTransferPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return List.of(
            new EmailNotificationGenerator(
                newArrayList(
                    upperTribunalMarkAsReadyForUtTransferPersonalisation,
                    appellantMarkAsReadyForUtTransferPersonalisationEmail,
                    homeOfficeMarkAppealReadyForUtTransferPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(appellantMarkAsReadyForUtTransferPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("updateDetentionLocationNotificationGenerator")
    public List<NotificationGenerator> updateDetentionLocationNotificationGenerator(
        LegalRepresentativeUpdateDetentionLocationPersonalisation legalRepresentativeUpdateDetentionLocationPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    legalRepresentativeUpdateDetentionLocationPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("internalDetAppealDecidedNotificationGenerator")
    public List<NotificationGenerator> internalDetAppealDecidedNotificationGenerator(
        DetentionEngagementTeamAppealDecidedPersonalisation detentionEngagementTeamAppealDecidedPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {
        return Arrays.asList(
            new EmailWithLinkNotificationGenerator(
                newArrayList(Collections.singleton(detentionEngagementTeamAppealDecidedPersonalisation)),
                notificationSender,
                notificationIdAppender
            )
        );

    }

    @Bean("internalDetainedEndAppealAutomaticallyNotificationGenerator")
    public List<NotificationGenerator> internalDetainedEndAppealAutomaticallyNotificationGenerator(
        DetentionEngagementTeamEndAppealAutomaticallyPersonalisation detentionEngagementTeamEndAppealAutomaticallyPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return singletonList(
            new EmailWithLinkNotificationGenerator(
                newArrayList(detentionEngagementTeamEndAppealAutomaticallyPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }


    @Bean("homeOfficeMarkAsPaidAndHoNotificationGenerator")
    public List<NotificationGenerator> homeOfficeMarkAsPaidAndHoNotificationGenerator(
        HomeOfficeSubmitAppealPersonalisation homeOfficeSubmitAppealPersonalisation, //identical notification already existing for different event, reusing it.
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    homeOfficeSubmitAppealPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("listCaseInternalDetainedNotificationGenerator")
    public List<NotificationGenerator> listCaseInternalDetainedNotificationGenerator(
        DetentionEngagementTeamListCasePersonalisation detentionEngagementTeamListCasePersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return List.of(
            new EmailWithLinkNotificationGenerator(
                newArrayList(Collections.singleton(detentionEngagementTeamListCasePersonalisation)),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("listCaseInternalNotificationGenerator")
    public List<NotificationGenerator> listCaseInternalNotificationGenerator(
        HomeOfficeListCasePersonalisation homeOfficeListCasePersonalisation,
        CaseOfficerListCasePersonalisation caseOfficerListCasePersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return List.of(
            new EmailNotificationGenerator(
                newArrayList(homeOfficeListCasePersonalisation, caseOfficerListCasePersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("listCaseProductionDetainedNotificationGenerator")
    public List<NotificationGenerator> listCaseProductionDetainedNotificationGenerator(
        DetentionEngagementTeamListCaseProductionPersonalisation detentionEngagementTeamListCaseProductionPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return List.of(
            new EmailNotificationGenerator(
                newArrayList(Collections.singleton(detentionEngagementTeamListCaseProductionPersonalisation)),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("requestHearingRequirementsInternalDetainedNotificationGenerator")
    public List<NotificationGenerator> requestHearingRequirementsInternalDetainedNotificationGenerator(
        DetentionEngagementTeamRequestHearingRequirementPersonalisation detentionEngagementTeamRequestHearingRequirementPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return List.of(
            new EmailWithLinkNotificationGenerator(
                newArrayList(Collections.singleton(detentionEngagementTeamRequestHearingRequirementPersonalisation)),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("endAppealInternalDetainedNotificationGenerator")
    public List<NotificationGenerator> endAppealInternalDetainedNotificationGenerator(
        DetentionEngagementTeamEndAppealPersonalisation detentionEngagementTeamEndAppealPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return List.of(
            new EmailWithLinkNotificationGenerator(
                newArrayList(Collections.singleton(detentionEngagementTeamEndAppealPersonalisation)),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("internalMarkAppealAsAdaNotificationGenerator")
    public List<NotificationGenerator> internalMarkAppealAsAdaNotificationGenerator(
        DetentionEngagementTeamMarkAppealAsAdaPersonalisation detentionEngagementTeamMarkAppealAsAdaPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return List.of(
            new EmailWithLinkNotificationGenerator(
                newArrayList(Collections.singleton(detentionEngagementTeamMarkAppealAsAdaPersonalisation)),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("editCaseListingInternalDetainedNotificationGenerator")
    public List<NotificationGenerator> editCaseListingInternalDetainedNotificationGenerator(
        HomeOfficeEditListingPersonalisation homeOfficeEditListingPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return List.of(
            new EmailNotificationGenerator(
                newArrayList(Collections.singleton(homeOfficeEditListingPersonalisation)),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("editCaseListingInternalDetainedNoChangeNotificationGenerator")
    public List<NotificationGenerator> editCaseListingInternalDetainedNoChangeNotificationGenerator(
            HomeOfficeEditListingNoChangePersonalisation homeOfficeEditListingNoChangePersonalisation,
            GovNotifyNotificationSender notificationSender,
            NotificationIdAppender notificationIdAppender) {

        return List.of(
                new EmailNotificationGenerator(
                        newArrayList(Collections.singleton(homeOfficeEditListingNoChangePersonalisation)),
                        notificationSender,
                        notificationIdAppender
                )
        );
    }

    @Bean("editCaseListingInternalDetainedIrcPrisonNotificationGenerator")
    public List<NotificationGenerator> editCaseListingInternalDetainedIrcPrisonNotificationGenerator(
        DetentionEngagementTeamEditCaseListingPersonalisation detentionEngagementTeamEditCaseListingPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return singletonList(
            new EmailWithLinkNotificationGenerator(
                newArrayList(Collections.singleton(detentionEngagementTeamEditCaseListingPersonalisation)),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("internalDetainedManageFeeUpdateNotificationGenerator")
    public List<NotificationGenerator> internalDetainedManageFeeUpdateNotificationGenerator(
        DetentionEngagementTeamManageFeeUpdatePersonalisation detentionEngagementTeamManageFeeUpdatePersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return List.of(
            new EmailWithLinkNotificationGenerator(
                newArrayList(Collections.singleton(detentionEngagementTeamManageFeeUpdatePersonalisation)),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("internalDetainedTransferOutOfAdaNotificationGenerator")
    public List<NotificationGenerator> internalDetainedTransferOutOfAdaNotificationGenerator(
        DetentionEngagementTeamTransferOutOfAdaPersonalisation detentionEngagementTeamTransferOutOfAdaPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return List.of(
            new EmailWithLinkNotificationGenerator(
                newArrayList(Collections.singleton(detentionEngagementTeamTransferOutOfAdaPersonalisation)),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("internalAppellantFtpaDecidedByRjNotificationGenerator")
    public List<NotificationGenerator> internalAppellantFtpaDecidedByRjNotificationGenerator(
        AdminOfficerFtpaDecisionAppellantPersonalisation adminOfficerFtpaDecisionAppellantPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return List.of(
            new EmailNotificationGenerator(
                newArrayList(adminOfficerFtpaDecisionAppellantPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("internalUpdateTribunalDecisionRule31IrcPrisonNotificationGenerator")
    public List<NotificationGenerator> internalUpdateTribunalDecisionRule31IrcPrisonNotificationGenerator(
            DetentionEngagementTeamUpdateTribunalDecisionRule31IrcPrisonPersonalisation detentionEngagementTeamUpdateTribunalDecisionRule31IrcPrisonPersonalisation,
            GovNotifyNotificationSender notificationSender,
            NotificationIdAppender notificationIdAppender) {

        return List.of(
                new EmailWithLinkNotificationGenerator(
                        newArrayList(Collections.singleton(detentionEngagementTeamUpdateTribunalDecisionRule31IrcPrisonPersonalisation)),
                        notificationSender,
                        notificationIdAppender
                )
        );
    }

    @Bean("internalMarkAppealRemittedAipIrcPrisonNotificationGenerator")
    public List<NotificationGenerator> internalMarkAppealRemittedAipIrcPrisonNotificationGenerator(
            DetentionEngagementTeamMarkAppealRemittedAipIrcPrisonPersonalisation detentionEngagementTeamMarkAppealRemittedAipIrcPrisonPersonalisation,
            GovNotifyNotificationSender notificationSender,
            NotificationIdAppender notificationIdAppender) {
        return List.of(
                new EmailWithLinkNotificationGenerator(
                        newArrayList(Collections.singleton(detentionEngagementTeamMarkAppealRemittedAipIrcPrisonPersonalisation)),
                        notificationSender,
                        notificationIdAppender
                )
        );
    }

    @Bean("homeOfficeMaintainCaseUnlinkAppealNotificationGenerator")
    public List<NotificationGenerator> homeOfficeMaintainCaseUnlinkAppealNotificationGenerator(
        HomeOfficeCaseUnlinkPersonalisation homeOfficeCaseUnlinkPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(Collections.singleton(homeOfficeCaseUnlinkPersonalisation)),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("homeOfficeMaintainCaseLinkAppealNotificationGenerator")
    public List<NotificationGenerator> homeOfficeMaintainCaseLinkAppealNotificationGenerator(
        HomeOfficeCaseLinkPersonalisation homeOfficeCaseLinkPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(Collections.singleton(homeOfficeCaseLinkPersonalisation)),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("internalUploadAdditionalEvidenceNotificationGenerator")
    public List<NotificationGenerator> internalUploadAdditionalEvidenceNotificationGenerator(
        DetentionEngagementTeamUploadAdditionalEvidencePersonalisation detentionEngagementTeamUploadAdditionalEvidencePersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return List.of(
            new EmailWithLinkNotificationGenerator(
                newArrayList(Collections.singleton(detentionEngagementTeamUploadAdditionalEvidencePersonalisation)),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("internalDetainedAppellantOnlyChangeDirectionDueDateNotificationGenerator")
    public List<NotificationGenerator> internalDetainedAppellantOnlyChangeDirectionDueDateNotificationGenerator(
        DetentionEngagementTeamChangeDueDatePersonalisation detentionEngagementTeamChangeDueDatePersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return List.of(
            new EmailWithLinkNotificationGenerator(
                newArrayList(Collections.singleton(detentionEngagementTeamChangeDueDatePersonalisation)),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    //TODO: Add HO email personalisation
    @Bean("internalDetainedAppellantRespondentChangeDirectionDueDateNotificationGenerator")
    public List<NotificationGenerator> internalDetainedAppellantRespondentChangeDirectionDueDateNotificationGenerator(
        DetentionEngagementTeamChangeDueDatePersonalisation detentionEngagementTeamChangeDueDatePersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return List.of(
            new EmailWithLinkNotificationGenerator(
                newArrayList(Collections.singleton(detentionEngagementTeamChangeDueDatePersonalisation)),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("internalDetainedAppellantHoChangeDirectionDueDateNotificationGenerator")
    public List<NotificationGenerator> internalDetainedAppellantHoChangeDirectionDueDateNotificationGenerator(
        DetentionEngagementTeamChangeHoDirectionDueDatePersonalisation detentionEngagementTeamChangeHoDirectionDueDatePersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return List.of(
            new EmailWithLinkNotificationGenerator(
                newArrayList(Collections.singleton(detentionEngagementTeamChangeHoDirectionDueDatePersonalisation)),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("internalHomeOfficeUploadAdditionalAddendumEvidenceNotificationGenerator")
    public List<NotificationGenerator> internalHomeOfficeUploadAdditionalAddendumEvidenceNotificationGenerator(
        DetentionEngagementTeamHomeOfficeUploadAdditionalAddendumEvidencePersonalisation detentionEngagementTeamHomeOfficeUploadAdditionalAddendumEvidencePersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return List.of(
            new EmailWithLinkNotificationGenerator(
                newArrayList(Collections.singleton(detentionEngagementTeamHomeOfficeUploadAdditionalAddendumEvidencePersonalisation)),
                notificationSender,
                notificationIdAppender
            )
        );
    }


    @Bean("homeOfficeLegalOfficerUploadAddendumEvidenceNotificationGenerator")
    public List<NotificationGenerator> homeOfficeLegalOfficerUploadAddendumEvidenceNotificationGenerator(
        HomeOfficeUploadAddendumEvidencePersonalisation homeOfficeUploadAddendumEvidencePersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                        homeOfficeUploadAddendumEvidencePersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("applyForCostsNotificationGenerator")
    public List<NotificationGenerator> applyForCostsNotificationGenerator(
        ApplyForCostsRespondentPersonalisation applyForCostsRespondentPersonalisation,
        ApplyForCostsApplicantPersonalisation applyForCostsApplicantPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return singletonList(new EmailNotificationGenerator(
            newArrayList(applyForCostsRespondentPersonalisation, applyForCostsApplicantPersonalisation),
            notificationSender,
            notificationIdAppender)
        );
    }

    @Bean("decideCostsNotificationGenerator")
    public List<NotificationGenerator> decideCostsNotificationGenerator(
        DecideCostsHomeOfficePersonalisation decideCostsHomeOfficePersonalisation,
        DecideCostsLegalRepPersonalisation decideCostsLegalRepPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return singletonList(new EmailNotificationGenerator(
            newArrayList(decideCostsHomeOfficePersonalisation, decideCostsLegalRepPersonalisation),
            notificationSender,
            notificationIdAppender)
        );
    }

    @Bean("respondentTurnOnNotificationsNotificationGenerator")
    public List<NotificationGenerator> respondentTurnOnNotificationsNotificationGenerator(
        RespondentTurnOnNotificationsPersonalisation respondentTurnOnNotificationsPersonalisation,
        AppellantNotificationsTurnedOnPersonalisationEmail appellantNotificationsTurnedOnPersonalisationEmail,
        AppellantNotificationsTurnedOnPersonalisationSms appellantNotificationsTurnedOnPersonalisationSms,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return List.of(
            new EmailNotificationGenerator(
                newArrayList(Collections.singleton(respondentTurnOnNotificationsPersonalisation)),
                notificationSender,
                notificationIdAppender
            ),
            new EmailNotificationGenerator(
                newArrayList(appellantNotificationsTurnedOnPersonalisationEmail),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(appellantNotificationsTurnedOnPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("notificationsTurnedOnAppellantAndLegalRepNotificationGenerator")
    public List<NotificationGenerator> turnOnNotificationAppellantAndLegalRepNotificationGenerator(
        LegalRepresentativeNotificationsTurnedOnPersonalisation legalRepresentativeNotificationsTurnedOnPersonalisation,
        AppellantNotificationsTurnedOnPersonalisationEmail appellantNotificationsTurnedOnPersonalisationEmail,
        AppellantNotificationsTurnedOnPersonalisationSms appellantNotificationsTurnedOnPersonalisationSms,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(legalRepresentativeNotificationsTurnedOnPersonalisation),
                notificationSender,
                notificationIdAppender
            ),
            new EmailNotificationGenerator(
                newArrayList(appellantNotificationsTurnedOnPersonalisationEmail),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(appellantNotificationsTurnedOnPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("respondToCostsNotificationGenerator")
    public List<NotificationGenerator> respondToCostsNotificationGenerator(
        RespondToCostsApplicantPersonalisation respondToCostsApplicantPersonalisation,
        RespondToCostsRespondentPersonalisation respondToCostsRespondentPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return singletonList(new EmailNotificationGenerator(
                newArrayList(respondToCostsApplicantPersonalisation, respondToCostsRespondentPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("additionalEvidenceSubmittedOtherPartyGenerator")
    public List<NotificationGenerator> additionalEvidenceSubmittedOtherPartyGenerator(
        AdditionalEvidenceSubmittedOtherPartyNotificationPersonalisation additionalEvidenceSubmittedOtherPartyNotificationPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return singletonList(new EmailNotificationGenerator(
            newArrayList(additionalEvidenceSubmittedOtherPartyNotificationPersonalisation),
            notificationSender,
            notificationIdAppender)
        );
    }

    @Bean("additionalEvidenceSubmittedSubmitterGenerator")
    public List<NotificationGenerator> additionalEvidenceSubmittedSubmitterGenerator(
        AddEvidenceForCostsSubmittedSubmitterPersonalisation addEvidenceForCostsSubmittedSubmitterPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return singletonList(new EmailNotificationGenerator(
            newArrayList(addEvidenceForCostsSubmittedSubmitterPersonalisation),
            notificationSender,
            notificationIdAppender)
        );
    }

    @Bean("considerMakingCostOrderNotificationGenerator")
    public List<NotificationGenerator> considerMakingCostOrderNotificationGenerator(
        ConsiderMakingCostOrderLegalRepPersonalisation considerMakingCostOrderLegalRepPersonalisation,
        ConsiderMakingCostOrderHoPersonalisation considerMakingCostOrderHoPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return singletonList(new EmailNotificationGenerator(
            newArrayList(considerMakingCostOrderLegalRepPersonalisation, considerMakingCostOrderHoPersonalisation),
            notificationSender,
            notificationIdAppender)
        );
    }

    @Bean("aipDisposeUnderRule31Or32AppelantNotificationGenerator")
    public List<NotificationGenerator> aipDisposeUnderRule31Or32AppelantNotificationGenerator(
        HomeOfficeFtpaApplicationDecidedRule31Rule32Personalisation homeOfficeFtpaApplicationDecidedRule31Rule32Personalisation,
        AipAppellantDisposeUnderRule31Or32PersonalisationEmail aipAppellantDisposeUnderRule31Or32PersonalisationEmail,
        AipAppellantDisposeUnderRule31Or32PersonalisationSms aipAppellantDisposeUnderRule31Or32PersonalisationSms,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(
                    homeOfficeFtpaApplicationDecidedRule31Rule32Personalisation,
                    aipAppellantDisposeUnderRule31Or32PersonalisationEmail
                ),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(aipAppellantDisposeUnderRule31Or32PersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("decideFtpaApplicationLrHoRule31OrRule32NotificationGenerator")
    public List<NotificationGenerator> decideFtpaApplicationLrHoRule31OrRule32NotificationGenerator(
        LegalRepresentativeFtpaApplicationDecidedRule31Rule32Personalisation legalRepresentativeFtpaApplicationDecidedRule31Rule32Personalisation,
        HomeOfficeFtpaApplicationDecidedRule31Rule32Personalisation homeOfficeFtpaApplicationDecidedRule31Rule32Personalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    legalRepresentativeFtpaApplicationDecidedRule31Rule32Personalisation,
                    homeOfficeFtpaApplicationDecidedRule31Rule32Personalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("internalDecideFtpaApplicationHoRule31OrRule32NotificationGenerator")
    public List<NotificationGenerator> internalDecideFtpaApplicationHoRule31OrRule32NotificationGenerator(
            HomeOfficeFtpaApplicationDecidedRule31Rule32Personalisation homeOfficeFtpaApplicationDecidedRule31Rule32Personalisation,
            GovNotifyNotificationSender notificationSender,
            NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
                new EmailNotificationGenerator(
                        newArrayList(
                                homeOfficeFtpaApplicationDecidedRule31Rule32Personalisation
                        ),
                        notificationSender,
                        notificationIdAppender
                )
        );
    }

    @Bean("aipReheardUnderRule35AppelantNotificationGenerator")
    public List<NotificationGenerator> aipReheardUnderRule35AppelantNotificationGenerator(
        RespondentReheardUnderRule35PersonalisationEmail respondentReheardUnderRule35PersonalisationEmail,
        AipAppellantReheardUnderRule35PersonalisationEmail aipAppellantReheardUnderRule35PersonalisationEmail,
        AipAppellantReheardUnderRule35PersonalisationSms aipAppellantReheardUnderRule35PersonalisationSms,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(respondentReheardUnderRule35PersonalisationEmail,
                    aipAppellantReheardUnderRule35PersonalisationEmail),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(aipAppellantReheardUnderRule35PersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }


    @Bean("decideFtpaApplicationReheardUnderRule35NotificationGenerator")
    public List<NotificationGenerator> decideFtpaApplicationReheardUnderRule35NotificationGenerator(
        RespondentReheardUnderRule35PersonalisationEmail respondentReheardUnderRule35PersonalisationEmail,
        LegalRepresentativeReheardUnderRule35Personalisation legalRepresentativeReheardUnderRule35Personalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(respondentReheardUnderRule35PersonalisationEmail,
                    legalRepresentativeReheardUnderRule35Personalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("internalDecideFtpaApplicationReheardUnderRule35HoNotificationGenerator")
    public List<NotificationGenerator> internalDecideFtpaApplicationReheardUnderRule35HoNotificationGenerator(
            RespondentReheardUnderRule35PersonalisationEmail respondentReheardUnderRule35PersonalisationEmail,
            GovNotifyNotificationSender notificationSender,
            NotificationIdAppender notificationIdAppender
    ) {

        return Arrays.asList(
                new EmailNotificationGenerator(
                        newArrayList(respondentReheardUnderRule35PersonalisationEmail),
                        notificationSender,
                        notificationIdAppender
                )
        );
    }

    @Bean("updateTribunalDecisionRule31NotificationGenerator")
    public List<NotificationGenerator> updateTribunalDecisionRule31NotificationGenerator(
        RespondentUpdateTribunalDecisionRule31PersonalisationEmail respondentUpdateTribunalDecisionRule31PersonalisationEmail,
        LegalRepresentativeUpdateTribunalDecisionRule31PersonalisationEmail legalRepresentativeUpdateTribunalDecisionRule31PersonalisationEmail,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(
                    respondentUpdateTribunalDecisionRule31PersonalisationEmail,
                    legalRepresentativeUpdateTribunalDecisionRule31PersonalisationEmail),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("updateTribunalDecisionRule31HoNotificationGenerator")
    public List<NotificationGenerator> updateTribunalDecisionRule31HoNotificationGenerator(
            RespondentUpdateTribunalDecisionRule31PersonalisationEmail respondentUpdateTribunalDecisionRule31PersonalisationEmail,
            GovNotifyNotificationSender notificationSender,
            NotificationIdAppender notificationIdAppender
    ) {

        return Arrays.asList(
                new EmailNotificationGenerator(
                        newArrayList(respondentUpdateTribunalDecisionRule31PersonalisationEmail),
                        notificationSender,
                        notificationIdAppender
                )
        );
    }

    @Bean("updateTribunalDecisionRule32NotificationGenerator")
    public List<NotificationGenerator> updateTribunalDecisionRule32NotificationGenerator(
        RespondentUpdateTribunalDecisionRule32PersonalisationEmail respondentUpdateTribunalDecisionRule32PersonalisationEmail,
        LegalRepresentativeUpdateTribunalDecisionRule32PersonalisationEmail legalRepresentativeUpdateTribunalDecisionRule32PersonalisationEmail,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(
                    respondentUpdateTribunalDecisionRule32PersonalisationEmail,
                    legalRepresentativeUpdateTribunalDecisionRule32PersonalisationEmail),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("internalUpdateTribunalDecisionRule32HoNotificationGenerator")
    public List<NotificationGenerator> internalUpdateTribunalDecisionRule32HoNotificationGenerator(
            RespondentUpdateTribunalDecisionRule32PersonalisationEmail respondentUpdateTribunalDecisionRule32PersonalisationEmail,
            GovNotifyNotificationSender notificationSender,
            NotificationIdAppender notificationIdAppender
    ) {

        return Arrays.asList(
                new EmailNotificationGenerator(
                        newArrayList(respondentUpdateTribunalDecisionRule32PersonalisationEmail),
                        notificationSender,
                        notificationIdAppender
                )
        );
    }

    @Bean("updateTribunalDecisionRule32AipNotificationGenerator")
    public List<NotificationGenerator> updateTribunalDecisionRule32AipNotificationGenerator(
        RespondentUpdateTribunalDecisionRule32PersonalisationEmail respondentUpdateTribunalDecisionRule32PersonalisationEmail,
        AppellantUpdateTribunalDecisionRule32PersonalisationEmail appellantUpdateTribunalDecisionRule32PersonalisationEmail,
        AppellantUpdateTribunalDecisionRule32PersonalisationSms appellantUpdateTribunalDecisionRule32PersonalisationSms,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(
                    respondentUpdateTribunalDecisionRule32PersonalisationEmail,
                    appellantUpdateTribunalDecisionRule32PersonalisationEmail),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(appellantUpdateTribunalDecisionRule32PersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("appellantSubmittedWithRemissionRequestNotificationGenerator")
    public List<NotificationGenerator> appellantSubmittedWithRemissionRequestNotificationGenerator(
        AppellantSubmittedWithRemissionRequestPersonalisationEmail appellantSubmittedWithRemissionRequestPersonalisationEmail,
        AppellantSubmittedWithRemissionRequestPersonalisationSms appellantSubmittedWithRemissionRequestPersonalisationSms,
        HomeOfficeAppealSubmittedPendingPaymentPersonalisation homeOfficeAppealSubmittedPendingPaymentPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(appellantSubmittedWithRemissionRequestPersonalisationEmail, homeOfficeAppealSubmittedPendingPaymentPersonalisation),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(appellantSubmittedWithRemissionRequestPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("aipAppellantRecordRemissionDecisionNotificationGenerator")
    public List<NotificationGenerator> aipAppellantRecordRemissionDecisionNotificationGenerator(
        AipAppellantRecordRemissionDecisionPersonalisationEmail aipAppellantRecordRemissionDecisionPersonalisationEmail,
        AipAppellantRecordRemissionDecisionPersonalisationSms aipAppellantRecordRemissionDecisionPersonalisationSms,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {
        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(aipAppellantRecordRemissionDecisionPersonalisationEmail),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(aipAppellantRecordRemissionDecisionPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("aipAppellantRecordRemissionDecisionPaNotificationGenerator")
    public List<NotificationGenerator> aipAppellantRecordRemissionDecisionPaNotificationGenerator(
        AipAppellantRecordRemissionDecisionPaPersonalisationEmail aipAppellantRecordRemissionDecisionPaPersonalisationEmail,
        AipAppellantRecordRemissionDecisionPaPersonalisationSms aipAppellantRecordRemissionDecisionPaPersonalisationSms,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {
        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(aipAppellantRecordRemissionDecisionPaPersonalisationEmail),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(aipAppellantRecordRemissionDecisionPaPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("updateTribunalDecisionRule31AipNotificationGenerator")
    public List<NotificationGenerator> updateTribunalDecisionRule31AipNotificationGenerator(
        RespondentUpdateTribunalDecisionRule31PersonalisationEmail respondentUpdateTribunalDecisionRule31PersonalisationEmail,
        AppellantUpdateTribunalDecisionRule31PersonalisationEmail appellantUpdateTribunalDecisionRule31PersonalisationEmail,
        AppellantUpdateTribunalDecisionRule31PersonalisationSms appellantUpdateTribunalDecisionRule31PersonalisationSms,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return List.of(
            new EmailNotificationGenerator(
                newArrayList(
                    respondentUpdateTribunalDecisionRule31PersonalisationEmail,
                    appellantUpdateTribunalDecisionRule31PersonalisationEmail),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(appellantUpdateTribunalDecisionRule31PersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("appellantSubmittedWithRemissionMarkAppealAsPaidNotificationGenerator")
    public List<NotificationGenerator> appellantSubmittedWithRemissionMarkAppealAsPaidNotificationGenerator(
        AppellantSubmittedWithRemissionMarkAppealAsPaidPersonalisationEmail appellantSubmittedWithRemissionMarkAppealAsPaidPersonalisationEmail,
        AppellantSubmittedWithRemissionMarkAppealAsPaidPersonalisationSms appellantSubmittedWithRemissionMarkAppealAsPaidPersonalisationSms,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(appellantSubmittedWithRemissionMarkAppealAsPaidPersonalisationEmail),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(appellantSubmittedWithRemissionMarkAppealAsPaidPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("markAppealAsRemittedNotificationGenerator")
    public List<NotificationGenerator> markAppealAsRemittedNotificationGenerator(
        LegalRepresentativeMarkAppealAsRemittedPersonalisation legalRepresentativeMarkAppealAsRemittedPersonalisation,
        HomeOfficeMarkAppealAsRemittedPersonalisation homeOfficeMarkAppealAsRemittedPersonalisation,
        AppellantMarkAppealAsRemittedPersonalisationEmail appellantMarkAppealAsRemittedPersonalisationEmail,
        AppellantMarkAppealAsRemittedPersonalisationSms appellantMarkAppealAsRemittedPersonalisationSms,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {
        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(legalRepresentativeMarkAppealAsRemittedPersonalisation),
                notificationSender,
                notificationIdAppender
            ),
            new EmailNotificationGenerator(
                newArrayList(homeOfficeMarkAppealAsRemittedPersonalisation),
                notificationSender,
                notificationIdAppender
            ),
            new EmailNotificationGenerator(
                newArrayList(appellantMarkAppealAsRemittedPersonalisationEmail),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(appellantMarkAppealAsRemittedPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("markAppealAsRemittedInternalHoNotificationGenerator")
    public List<NotificationGenerator> markAppealAsRemittedInternalHoNotificationGenerator(
        HomeOfficeMarkAppealAsRemittedPersonalisation homeOfficeMarkAppealAsRemittedPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {
        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(homeOfficeMarkAppealAsRemittedPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("generateHearingBundleNonDetainedOrOocNotificationGenerator")
    public List<NotificationGenerator> generateHearingBundleNonDetainedOrOocNotificationGenerator(
        HomeOfficeGenerateHearingBundlePersonalisation homeOfficeGenerateHearingBundlePersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {
        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(homeOfficeGenerateHearingBundlePersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("internalSubmitAppealWithExemptionAppellantLetterNotificationGenerator")
    public List<NotificationGenerator> internalSubmitAppealWithExemptionAppellantLetterNotificationGenerator(
        AppellantInternalCaseSubmitAppealWithExemptionLetterPersonalisation appellantInternalCaseSubmitAppealWithExemptionLetterPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
            new LetterNotificationGenerator(
                newArrayList(
                    appellantInternalCaseSubmitAppealWithExemptionLetterPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            ) {
                @Override
                public Message getSuccessMessage() {
                    return new Message("success","body");
                }
            }
        );
    }

    @Bean("internalProgressMigratedCaseWithFeeAppellantLetterNotificationGenerator")
    public List<NotificationGenerator> internalProgressMigratedCaseWithFeeAppellantLetterNotificationGenerator(
        AppellantInternalCaseSubmittedOnTimeWithFeePersonalisation appellantInternalCaseSubmittedOnTimeWithFeePersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
            new LetterNotificationGenerator(
                newArrayList(
                    appellantInternalCaseSubmittedOnTimeWithFeePersonalisation
                ),
                notificationSender,
                notificationIdAppender
            ) {
                @Override
                public Message getSuccessMessage() {
                    return new Message("success","body");
                }
            }
        );
    }

    @Bean("internalSubmitAppealOnTimeWithRemissionAppellantLetterNotificationGenerator")
    public List<NotificationGenerator> internalSubmitAppealOnTimeWithExemptionAppellantLetterNotificationGenerator(
        AppellantInternalCaseSubmitAppealWithRemissionLetterPersonalisation appellantInternalCaseSubmitAppealWithRemissionLetterPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
            new LetterNotificationGenerator(
                newArrayList(
                    appellantInternalCaseSubmitAppealWithRemissionLetterPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            ) {
                @Override
                public Message getSuccessMessage() {
                    return new Message("success","body");
                }
            }
        );
    }

    @Bean("internalSubmitAppealOnTimeWithRemissionAppellantDetainedPrisonIrcLetterNotificationGenerator")
    public List<NotificationGenerator> internalSubmitAppealOnTimeWithRemissionAppellantDetainedPrisonIrcLetterNotificationG(
        DetentionEngagementTeamInternalCaseDetainedPrisonIrcSubmitAppealWithRemissionEmailPersonalisation detentionEngagementTeamInternalCaseDetainedPrisonIrcSubmitAppealWithRemissionEmailPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {
        return Arrays.asList(
            new EmailWithLinkNotificationGenerator(
                newArrayList(detentionEngagementTeamInternalCaseDetainedPrisonIrcSubmitAppealWithRemissionEmailPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("internalSubmitAppealOutOfTimeWithRemissionAppellantLetterNotificationGenerator")
    public List<NotificationGenerator> internalSubmitAppealOutOfTimeWithRemissionAppellantLetterNotificationGenerator(
        AppellantInternalCaseSubmitAppealOutOfTimeWithRemissionLetterPersonalisation appellantInternalCaseSubmitAppealOutOfTimeWithRemissionLetterPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
            new LetterNotificationGenerator(
                newArrayList(
                    appellantInternalCaseSubmitAppealOutOfTimeWithRemissionLetterPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            ) {
                @Override
                public Message getSuccessMessage() {
                    return new Message("success","body");
                }
            }
        );
    }

    @Bean("internalSubmitAppealOutOfTimeWithRemissionAppellantDetainedPrisonIrcLetterNotificationGenerator")
    public List<NotificationGenerator> internalSubmitAppealOutOfTimeWithRemissionAppellantDetainedPrisonIrcLetterNotificationGenerator(
        DetentionEngagementTeamInternalCaseDetainedPrisonIrcSubmitAppealOutOfTimeWithRemissionEmailPersonalisation detentionEngagementTeamInternalCaseDetainedPrisonIrcSubmitAppealOutOfTimeWithRemissionEmailPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {
        return Arrays.asList(
            new EmailWithLinkNotificationGenerator(
                newArrayList(detentionEngagementTeamInternalCaseDetainedPrisonIrcSubmitAppealOutOfTimeWithRemissionEmailPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("internalRemissionApprovedOutOfTimeAppellantDetainedPrisonIrcLetterNotificationGenerator")
    public List<NotificationGenerator> internalRemissionApprovedOutOfTimeAppellantDetainedPrisonIrcLetterNotificationGenerator(
            DetentionEngagementTeamInternalCaseDetainedPrisonIrcRemissionApprovedOutOfTimeEmailPersonalisation personalisation,
            GovNotifyNotificationSender notificationSender,
            NotificationIdAppender notificationIdAppender
    ) {
        return Arrays.asList(
                new EmailWithLinkNotificationGenerator(
                        newArrayList(personalisation),
                        notificationSender,
                        notificationIdAppender
                )
        );
    }

    @Bean("legalRepRemovedDetainedPrisonIrcLetterNotificationGenerator")
    public List<NotificationGenerator> legalRepRemovedDetainedPrisonIrcLetterNotificationGenerator(
            DetentionEngagementTeamDetainedPrisonIrcLegalRepRemovedEmailPersonalisation personalisation,
            GovNotifyNotificationSender notificationSender,
            NotificationIdAppender notificationIdAppender
    ) {
        return Arrays.asList(
                new EmailWithLinkNotificationGenerator(
                        newArrayList(personalisation),
                        notificationSender,
                        notificationIdAppender
                )
        );
    }

    @Bean("internalSubmitAppealWithFeeAppellantLetterNotificationGenerator")
    public List<NotificationGenerator> internalSubmitAppealOutOfTimeWithFeeAppellantLetterNotificationGenerator(
        AppellantInternalCaseSubmittedOnTimeWithFeePersonalisation appellantInternalCaseSubmittedOnTimeWithFeePersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
            new LetterNotificationGenerator(
                newArrayList(
                    appellantInternalCaseSubmittedOnTimeWithFeePersonalisation
                ),
                notificationSender,
                notificationIdAppender
            ) {
                @Override
                public Message getSuccessMessage() {
                    return new Message("success","body");
                }
            }
        );
    }

    @Bean("internalHomeOfficeDirectedToReviewAppealAipNotificationGenerator")
    public List<NotificationGenerator> internalSubmitAppealOutOfTimeWithFeeAppellantLetterNotificationGenerator(
        AppellantInternalHomeOfficeDirectedToReviewAppealPersonalisation appellantInternalHomeOfficeDirectedToReviewAppealPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
            new LetterNotificationGenerator(
                newArrayList(
                    appellantInternalHomeOfficeDirectedToReviewAppealPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            ) {
                @Override
                public Message getSuccessMessage() {
                    return new Message("success","body");
                }
            }
        );
    }

    @Bean("internalHomeOfficeUploadBundleAipNotificationGenerator")
    public List<NotificationGenerator> internalSubmitAppealOutOfTimeWithFeeAppellantLetterNotificationGenerator(
        AppellantInternalHomeOfficeDirectedToUploadBundleLetterPersonalisation appellantInternalHomeOfficeDirectedToUploadBundleLetterPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
            new LetterNotificationGenerator(
                newArrayList(
                    appellantInternalHomeOfficeDirectedToUploadBundleLetterPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            ) {
                @Override
                public Message getSuccessMessage() {
                    return new Message("success","body");
                }
            }
        );
    }

    @Bean("internalSubmitAppealWithFeeOutOfTimeAppellantLetterNotificationGenerator")
    public List<NotificationGenerator> internalSubmitAppealOutOfTimeWithFeeAppellantLetterNotificationGenerator(
            AppellantInternalCaseSubmittedOutOfTimeWithFeePersonalisation appellantInternalCaseSubmittedOutOfTimeWithFeePersonalisation,
            GovNotifyNotificationSender notificationSender,
            NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
                new LetterNotificationGenerator(
                        newArrayList(
                                appellantInternalCaseSubmittedOutOfTimeWithFeePersonalisation
                        ),
                        notificationSender,
                        notificationIdAppender
                ) {
                    @Override
                    public Message getSuccessMessage() {
                        return new Message("success","body");
                    }
                }
        );
    }

    @Bean("internalSubmitAppealOutOfTimeWithExemptionAppellantLetterNotificationGenerator")
    public List<NotificationGenerator> internalSubmitAppealOutOfTimeWithExemptionAppellantLetterNotificationGenerator(
        AppellantInternalCaseSubmitAppealOutOfTimeWithExemptionLetterPersonalisation appellantInternalCaseSubmitAppealOutOfTimeWithExemptionLetterPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
            new LetterNotificationGenerator(
                newArrayList(
                    appellantInternalCaseSubmitAppealOutOfTimeWithExemptionLetterPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            ) {
                @Override
                public Message getSuccessMessage() {
                    return new Message("success","body");
                }
            }
        );
    }

    @Bean("internalManageFeeUpdateLetterNotificationGenerator")
    public List<NotificationGenerator> internalManageFeeUpdateLetterAppellantLetterNotificationGenerator(
        AppellantInternalManageFeeUpdateLetterPersonalisation appellantInternalManageFeeUpdateLetterPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
            new LetterNotificationGenerator(
                newArrayList(
                    appellantInternalManageFeeUpdateLetterPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            ) {
                @Override
                public Message getSuccessMessage() {
                    return new Message("success","body");
                }
            }
        );
    }

    @Bean("internalEndAppealAppellantLetterNotificationGenerator")
    public List<NotificationGenerator> internalEndAppealAppellantLetterNotificationGenerator(
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender,
        DocumentDownloadClient documentDownloadClient
    ) {

        DocumentTag documentTag = DocumentTag.INTERNAL_END_APPEAL_LETTER_BUNDLE;

        return singletonList(
            new PrecompiledLetterNotificationGenerator(
                newArrayList(
                    documentTag
                ),
                notificationSender,
                notificationIdAppender,
                documentDownloadClient) {
                @Override
                public Message getSuccessMessage() {
                    return new Message("success","body");
                }
            }
        );
    }


    @Bean("internalCaseAdjournedWithoutTimeLetterNotificationGenerator")
    public List<NotificationGenerator> internalCaseAdjournedWithoutTimeLetterNotificationGenerator(
        AppellantInternalCaseAdjournedWithoutDatePersonalisation internalCaseAdjournedWithoutDatePersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {
        return singletonList(
            new LetterNotificationGenerator(
                newArrayList(
                    internalCaseAdjournedWithoutDatePersonalisation
                ),
                notificationSender,
                notificationIdAppender
            ) {
                @Override
                public Message getSuccessMessage() {
                    return new Message("success","body");
                }
            }
        );
    }

    @Bean("internalDecisionWithoutHearingAppellantLetterNotificationGenerator")
    public List<NotificationGenerator> internalDecisionWithoutHearingAppellantLetterNotificationGenerator(
            AppellantInternalCaseDecisionWithoutHearingPersonalisation appellantInternalCaseDecisionWithoutHearingPersonalisation,
            GovNotifyNotificationSender notificationSender,
            NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
                new LetterNotificationGenerator(
                        newArrayList(
                                appellantInternalCaseDecisionWithoutHearingPersonalisation
                        ),
                        notificationSender,
                        notificationIdAppender
                ) {
                    @Override
                    public Message getSuccessMessage() {
                        return new Message("success","body");
                    }
                }
        );
    }

    @Bean("internalNonStandardDirectionAppellantLetterNotificationGenerator")
    public List<NotificationGenerator> internalNonStandardDirectionAppellantLetterNotificationGenerator(
            AppellantInternalCaseNonStandardDirectionPersonalisation appellantInternalCaseNonStandardDirectionPersonalisation,
            GovNotifyNotificationSender notificationSender,
            NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
                new LetterNotificationGenerator(
                        newArrayList(
                                appellantInternalCaseNonStandardDirectionPersonalisation
                        ),
                        notificationSender,
                        notificationIdAppender
                ) {
                    @Override
                    public Message getSuccessMessage() {
                        return new Message("success","body");
                    }
                }
        );
    }

    @Bean("internalEndAppealAutomaticallyAppellantLetterNotificationGenerator")
    public List<NotificationGenerator> internalEndAppealAutomaticallyAppellantLetterNotificationGenerator(
        AppellantInternalCaseEndAppealAutomaticallyLetterPersonalisation appellantInternalCaseEndAppealAutomaticallyLetterPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
            new LetterNotificationGenerator(
                newArrayList(
                    appellantInternalCaseEndAppealAutomaticallyLetterPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            ) {
                @Override
                public Message getSuccessMessage() {
                    return new Message("success","body");
                }
            }
        );
    }

    @Bean("internalCaseListedAppellantLetterNotificationGenerator")
    public List<NotificationGenerator> internalCaseListedAppellantLetterNotificationGenerator(
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender,
        DocumentDownloadClient documentDownloadClient
    ) {

        DocumentTag documentTag = DocumentTag.INTERNAL_CASE_LISTED_LETTER_BUNDLE;

        return singletonList(
            new PrecompiledLetterNotificationGenerator(
                newArrayList(
                    documentTag
                ),
                notificationSender,
                notificationIdAppender,
                documentDownloadClient) {
                @Override
                public Message getSuccessMessage() {
                    return new Message("success","body");
                }
            }
        );
    }

    @Bean("internalCaseListedLrLetterNotificationGenerator")
    public List<NotificationGenerator> internalCaseListedAppellantLrNotificationGenerator(
            GovNotifyNotificationSender notificationSender,
            NotificationIdAppender notificationIdAppender,
            DocumentDownloadClient documentDownloadClient
    ) {

        DocumentTag documentTag = DocumentTag.INTERNAL_CASE_LISTED_LR_LETTER_BUNDLE;

        return singletonList(
                new PrecompiledLetterNotificationGenerator(
                        newArrayList(
                                documentTag
                        ),
                        notificationSender,
                        notificationIdAppender,
                        documentDownloadClient) {
                    @Override
                    public Message getSuccessMessage() {
                        return new Message("success","body");
                    }
                }
        );
    }

    @Bean("internalReinstateAppealAppellantLetterNotificationGenerator")
    public List<NotificationGenerator> internalReinstateAppealAppellantLetterNotificationGenerator(
        AppellantInternalReinstateAppealLetterPersonalisation appellantInternalReinstateAppealLetterPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
            new LetterNotificationGenerator(
                newArrayList(
                    appellantInternalReinstateAppealLetterPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            ) {
                @Override
                public Message getSuccessMessage() {
                    return new Message("success","body");
                }
            }
        );
    }

    @Bean("internalLateRemissionRefusedLetterNotificationGenerator")
    public List<NotificationGenerator> internalLateRemissionRefusedAppellantLetterNotificationGenerator(
        AppellantInternalLateRemissionRejectedLetterPersonalisation appellantInternalLateRemissionRejectedLetterPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
            new LetterNotificationGenerator(
                newArrayList(
                    appellantInternalLateRemissionRejectedLetterPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            ) {
                @Override
                public Message getSuccessMessage() {
                    return new Message("success","body");
                }
            }
        );
    }

    @Bean("internalRemissionPartiallyGrantedOrRefusedLetterNotificationGenerator")
    public List<NotificationGenerator> internalRemissionPartiallyGrantedOrRefusedLetterNotificationGenerator(
            AppellantInternalRemissionPartiallyGrantedOrRejectedLetterPersonalisation appellantInternalRemissionPartiallyGrantedOrRejectedLetterPersonalisation,
            GovNotifyNotificationSender notificationSender,
            NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
                new LetterNotificationGenerator(
                        newArrayList(
                                appellantInternalRemissionPartiallyGrantedOrRejectedLetterPersonalisation
                        ),
                        notificationSender,
                        notificationIdAppender
                ) {
                    @Override
                    public Message getSuccessMessage() {
                        return new Message("success","body");
                    }
                }
        );
    }

    @Bean("appellantInternalHomeOfficeApplyForFtpaLetterNotificationGenerator")
    public List<NotificationGenerator> appellantInternalHomeOfficeApplyForFtpaNonDetainedOrOocLetterNotificationGenerator(
        AppellantInternalHomeOfficeApplyForFtpaNonDetainedAndOutOfCountryPersonalisation appellantInternalHomeOfficeNonDetainedAndOutOfCountryPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {
        return singletonList(
            new LetterNotificationGenerator(
                newArrayList(
                    appellantInternalHomeOfficeNonDetainedAndOutOfCountryPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("internalCaseDisposeUnderRule31Or32AppellantLetterGenerator")
    public List<NotificationGenerator> internalCaseDisposeUnderRule31Or32AppellantLetterGenerator(
        AppellantInternalCaseDisposeUnderRule31Or32Personalisation appellantInternalCaseDisposeUnderRule31Or32Personalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
            new LetterNotificationGenerator(
                newArrayList(
                    appellantInternalCaseDisposeUnderRule31Or32Personalisation
                ),
                notificationSender,
                notificationIdAppender
            ) {
                @Override
                public Message getSuccessMessage() {
                    return new Message("success","body");
                }
            }
        );
    }

    @Bean("internalAipManualCaseDisposeUnderRule31Or32AppellantNotificationGenerator")
    public List<NotificationGenerator> internalAipManualCaseDisposeUnderRule31Or32AppellantNotificationGenerator(
        DetentionEngagementTeamIrcPrisonFtpaDisposedRules31or32Personalisation personalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return singletonList(
            new EmailWithLinkNotificationGenerator(
                newArrayList(personalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("internalMarkAsRemittedAppellantLetterNotificationGenerator")
    public List<NotificationGenerator> internalMarkAsRemittedAppellantLetterNotificationGenerator(
        AppellantInternalMarkAsRemittedLetterPersonalisation appellantInternalMarkAsRemittedLetterPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
            new LetterNotificationGenerator(
                newArrayList(
                    appellantInternalMarkAsRemittedLetterPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            ) {
                @Override
                public Message getSuccessMessage() {
                    return new Message("success","body");
                }
            }
        );
    }

    @Bean("internalDecideApplicationAppellantLetterNotificationGenerator")
    public List<NotificationGenerator> internalDecideApplicationAppellantLetterNotificationGenerator(
        AppellantInternalDecideApplicationLetterPersonalisation appellantInternalDecideApplicationLetterPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {
        return singletonList(
            new LetterNotificationGenerator(
                newArrayList(
                    appellantInternalDecideApplicationLetterPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            ) {
                @Override
                public Message getSuccessMessage() {
                    return new Message("success","body");
                }
            }
        );
    }

    @Bean("internalUpdateTribunalDecisionRule31LetterNotificationGenerator")
    public List<NotificationGenerator> internalUpdateTribunalDecisionRule31LetterNotificationGenerator(
        AppellantInternalUpdateTribunalDecisionRule31LetterPersonalisation appellantInternalUpdateTribunalDecisionLetterPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {
        return singletonList(
            new LetterNotificationGenerator(
                newArrayList(
                    appellantInternalUpdateTribunalDecisionLetterPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            ) {
                @Override
                public Message getSuccessMessage() {
                    return new Message("success","body");
                }
            }
        );
    }

    @Bean("internalRespondentApplicationDecidedLetterGenerator")
    public List<NotificationGenerator> internalRespondentApplicationDecidedLetterGenerator(
            AppellantInternalRespondentApplicationDecidedLetterPersonalisation appellantInternalRespondentApplicationDecidedLetterPersonalisation,
            GovNotifyNotificationSender notificationSender,
            NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
                new LetterNotificationGenerator(
                        newArrayList(
                                appellantInternalRespondentApplicationDecidedLetterPersonalisation
                        ),
                        notificationSender,
                        notificationIdAppender
                ) {
                    @Override
                    public Message getSuccessMessage() {
                        return new Message("success","body");
                    }
                }
        );
    }

    @Bean("internalEditAppealAfterSubmitLetterNotificationGenerator")
    public List<NotificationGenerator> internalEditAppealAfterSubmitLetterNotificationGenerator(
            AppellantInternalEditAppealPostSubmitLetterPersonalisation appellantInternalEditAppealPostSubmitLetterPersonalisation,
            GovNotifyNotificationSender notificationSender,
            NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
                new LetterNotificationGenerator(
                        newArrayList(
                                appellantInternalEditAppealPostSubmitLetterPersonalisation
                        ),
                        notificationSender,
                        notificationIdAppender
                ) {
                    @Override
                    public Message getSuccessMessage() {
                        return new Message("success","body");
                    }
                }
        );
    }

    @Bean("editCaseListingInternalLetterNotificationGenerator")
    public List<NotificationGenerator> editCaseListingInternalLetterNotificationGenerator(
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender,
        DocumentDownloadClient documentDownloadClient
    ) {

        DocumentTag documentTag = DocumentTag.INTERNAL_EDIT_CASE_LISTING_LETTER_BUNDLE;

        return singletonList(
            new PrecompiledLetterNotificationGenerator(
                newArrayList(
                    documentTag
                ),
                notificationSender,
                notificationIdAppender,
                documentDownloadClient) {
                @Override
                public Message getSuccessMessage() {
                    return new Message("success","body");
                }
            }
        );
    }

    @Bean("editCaseListingInternalLrLetterNotificationGenerator")
    public List<NotificationGenerator> editCaseListingInternalLrLetterNotificationGenerator(
            GovNotifyNotificationSender notificationSender,
            NotificationIdAppender notificationIdAppender,
            DocumentDownloadClient documentDownloadClient
    ) {

        DocumentTag documentTag = DocumentTag.INTERNAL_EDIT_CASE_LISTING_LR_LETTER_BUNDLE;

        return singletonList(
                new PrecompiledLetterNotificationGenerator(
                        newArrayList(
                                documentTag
                        ),
                        notificationSender,
                        notificationIdAppender,
                        documentDownloadClient) {
                    @Override
                    public Message getSuccessMessage() {
                        return new Message("success","body");
                    }
                }
        );
    }

    @Bean("hearingCancelledProductionDetainedNotificationGenerator")
    public List<NotificationGenerator> hearingCancelledProductionDetainedNotificationGenerator(
            DetentionEngagementTeamHearingCancelledProductionPersonalisation detentionEngagementTeamHearingCancelledProductionPersonalisation,
            GovNotifyNotificationSender notificationSender,
            NotificationIdAppender notificationIdAppender
    ) {
        return singletonList(
                new EmailNotificationGenerator(
                        newArrayList(
                                detentionEngagementTeamHearingCancelledProductionPersonalisation
                        ),
                        notificationSender,
                        notificationIdAppender
                ) {
                    @Override
                    public Message getSuccessMessage() {
                        return new Message("success","body");
                    }
                }
        );
    }

    @Bean("editCaseListingProductionDetainedNotificationGenerator")
    public List<NotificationGenerator> editCaseListingProductionDetainedNotificationGenerator(
            DetentionEngagementTeamEditCaseListingProductionPersonalisation detentionEngagementTeamEditCaseListingProductionPersonalisation,
            GovNotifyNotificationSender notificationSender,
            NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    detentionEngagementTeamEditCaseListingProductionPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            ) {
                @Override
                public Message getSuccessMessage() {
                    return new Message("success","body");
                }
            }
        );
    }

    @Bean("internalOutOfTimeDecisionAppellantLetterNotificationGenerator")
    public List<NotificationGenerator> internalOutOfTimeDecisionLetterNotificationGenerator(
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender,
        DocumentDownloadClient documentDownloadClient
    ) {

        DocumentTag documentTag = DocumentTag.INTERNAL_OUT_OF_TIME_DECISION_LETTER_BUNDLE;

        return singletonList(
            new PrecompiledLetterNotificationGenerator(
                newArrayList(
                    documentTag
                ),
                notificationSender,
                notificationIdAppender,
                documentDownloadClient) {
                @Override
                public Message getSuccessMessage() {
                    return new Message("success","body");
                }
            }
        );
    }

    @Bean("internalRemissionGrantedOotLetterNotificationGenerator")
    public List<NotificationGenerator> internalRemissionGrantedOotLetterNotificationGenerator(
        AppellantInternalRemissionGrantedOutOfTimeLetterPersonalisation appellantInternalRemissionGrantedOutOfTImeLetterPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
            new LetterNotificationGenerator(
                newArrayList(
                    appellantInternalRemissionGrantedOutOfTImeLetterPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            ) {
                @Override
                public Message getSuccessMessage() {
                    return new Message("success","body");
                }
            }
        );
    }

    @Bean("internalLateRemissionGrantedLetterNotificationGenerator")
    public List<NotificationGenerator> internalLateRemissionGrantedLetterNotificationGenerator(
            AppellantInternalLateRemissionPartiallyOrGrantedLetterPersonalisation appellantInternalLateRemissionPartiallyOrGrantedLetterPersonalisation,
            GovNotifyNotificationSender notificationSender,
            NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
                new LetterNotificationGenerator(
                        newArrayList(
                            appellantInternalLateRemissionPartiallyOrGrantedLetterPersonalisation
                        ),
                        notificationSender,
                        notificationIdAppender
                ) {
                    @Override
                    public Message getSuccessMessage() {
                        return new Message("success","body");
                    }
                }
        );
    }

    @Bean("internalRemissionGrantedInTimeLetterNotificationGenerator")
    public List<NotificationGenerator> internalRemissionGrantedInTimeLetterNotificationGenerator(
        AppellantInternalRemissionGrantedInTimeLetterPersonalisation appellantInternalRemissionGrantedInTImeLetterPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
            new LetterNotificationGenerator(
                newArrayList(
                    appellantInternalRemissionGrantedInTImeLetterPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            ) {
                @Override
                public Message getSuccessMessage() {
                    return new Message("success","body");
                }
            }
        );
    }

    @Bean("sendPaymentReminderInternalNotificationGenerator")
    public List<NotificationGenerator> sendPaymentReminderInternalNotificationGenerator(
        AppellantSendPaymentReminderPersonalisationEmail appellantSendPaymentReminderPersonalisationEmail,
        AppellantSendPaymentReminderPersonalisationSms appellantSendPaymentReminderPersonalisationSms,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {
        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(appellantSendPaymentReminderPersonalisationEmail),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(appellantSendPaymentReminderPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("appellantRefundRequestedAipNotificationGenerator")
    public List<NotificationGenerator> appellantRefundRequestedAipNotificationGenerator(
        AiPAppellantRefundRequestedNotificationEmail aipAppellantRefundRequestedNotificationEmail,
        AiPAppellantRefundRequestedNotificationSms aipAppellantRefundRequestedNotificationSms,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return List.of(
            new EmailNotificationGenerator(
                newArrayList(
                    aipAppellantRefundRequestedNotificationEmail),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(aipAppellantRefundRequestedNotificationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("aipAppellantRecordRefundDecisionNotificationGenerator")
    public List<NotificationGenerator> aipAppellantRecordRefundDecisionNotificationGenerator(
        AppellantRecordRefundDecisionPersonalisationEmail appellantRecordRefundDecisionPersonalisationEmail,
        AppellantRecordRefundDecisionPersonalisationSms appellantRecordRefundDecisionPersonalisationSms,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {
        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(appellantRecordRefundDecisionPersonalisationEmail),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(appellantRecordRefundDecisionPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("aipManageFeeUpdatePaymentInstructedNotificationGenerator")
    public List<NotificationGenerator> aipManageFeeUpdatePaymentInstructedNotificationGenerator(
        AipAppellantManageFeeUpdatePersonalisationEmail aipAppellantManageFeeUpdatePersonalisationEmail,
        AipAppellantManageFeeUpdatePersonalisationSms aipAppellantManageFeeUpdatePersonalisationSms,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(aipAppellantManageFeeUpdatePersonalisationEmail),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(aipAppellantManageFeeUpdatePersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }


    @Bean("legalRepRemissionPaymentReminderEmailNotificationGenerator")
    public List<NotificationGenerator> legalRepRemissionPaymentReminderEmailNotificationGenerator(
        LegalRepRemissionPaymentReminderPersonalisation legalRepRemissionPaymentReminderPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(
                    legalRepRemissionPaymentReminderPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("appellantInPersonRemissionPaymentReminderEmailNotificationGenerator")
    public List<NotificationGenerator> legalRepRemissionPaymentReminderEmailNotificationGenerator(
        AipRemissionRequestAutomaticReminderEmail aipRemissionRequestAutomaticReminderEmail,
        AipRemissionRequestAutomaticReminderSms aipRemissionRequestAutomaticReminderSms,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(
                    aipRemissionRequestAutomaticReminderEmail),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(aipRemissionRequestAutomaticReminderSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("legalRepRefundConfirmationPersonalisationEmailNotificationGenerator")
    public List<NotificationGenerator> legalRepRefundConfirmationPersonalisationEmailNotificationGenerator(
        LegalRepRefundConfirmationPersonalisation legalRepRefundConfirmationPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(
                    legalRepRefundConfirmationPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("aipRefundConfirmationNotificationGenerator")
    public List<NotificationGenerator> aipRefundConfirmationNotificationGenerator(
        AipAppellantRefundConfirmationPersonalisationEmail aipAppellantRefundConfirmationPersonalisationEmail,
        AipAppellantRefundConfirmationPersonalisationSms aipAppellantRefundConfirmationPersonalisationSms,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(aipAppellantRefundConfirmationPersonalisationEmail),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(aipAppellantRefundConfirmationPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("internalRefundConfirmationLetterNotificationGenerator")
    public List<NotificationGenerator> internalRefundConfirmationLetterNotificationGenerator(
            AppellantInternalRefundConfirmationLetterPersonalisation appellantInternalRefundConfirmationLetterPersonalisation,
            GovNotifyNotificationSender notificationSender,
            NotificationIdAppender notificationIdAppender
    ) {

        return singletonList(
                new LetterNotificationGenerator(
                        newArrayList(
                                appellantInternalRefundConfirmationLetterPersonalisation
                        ),
                        notificationSender,
                        notificationIdAppender
                ) {
                    @Override
                    public Message getSuccessMessage() {
                        return new Message("success","body");
                    }
                }
        );
    }

    @Bean("aipmDetainedInPrisonOrIrcReinstateAppealNotificationGenerator")
    public List<NotificationGenerator> aipmDetainedInPrisonOrIrcReinstateAppealNotificationGenerator(
            AipmDetainedInPrisonOrIrcReinstateAppealPersonalisation personalisation,
            GovNotifyNotificationSender notificationSender,
            NotificationIdAppender notificationIdAppender
    ) {
        return Arrays.asList(
                new EmailWithLinkNotificationGenerator(
                        newArrayList(
                            personalisation
                        ),
                        notificationSender,
                        notificationIdAppender
                )

        );
    }
}
