package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.config;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.bail.adminofficer.email.*;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.bail.applicant.sms.*;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.bail.hearingcentre.email.HearingCentreSubmitApplicationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.bail.homeoffice.email.*;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.bail.legalrepresentative.email.LegalRepresentativeForceCaseToHearingPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.bail.legalrepresentative.email.LegalRepresentativeBailChangeTribunalCentrePersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.bail.legalrepresentative.email.*;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.bail.uppertribunal.UpperTribunalApplicationEndedImaPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.bail.uppertribunal.UpperTribunalDecisionRefusedImaPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.BailEmailNotificationGenerator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.BailNotificationGenerator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.BailNotificationIdAppender;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.BailSmsNotificationGenerator;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.BailGovNotifyNotificationSender;

import static java.util.Collections.singletonList;

@Configuration
public class BailNotificationGeneratorConfiguration {

    @Bean("startApplicationDisposalNotificationGenerator")
    public List<BailNotificationGenerator> startApplicationDisposalNotificationGenerator(
        LegalRepresentativeBailApplicationStartedDisposalPersonalisationEmail legalRepresentativeBailApplicationStartedDisposalPersonalisationEmail,
        BailGovNotifyNotificationSender notificationSender,
        BailNotificationIdAppender notificationIdAppender
    ) {
        return singletonList(
            new BailEmailNotificationGenerator(
                newArrayList(legalRepresentativeBailApplicationStartedDisposalPersonalisationEmail),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("editBailApplicationDisposalNotificationGenerator")
    public List<BailNotificationGenerator> editBailApplicationDisposalNotificationGenerator(
        LegalRepresentativeBailApplicationEditedDisposalPersonalisationEmail legalRepresentativeBailApplicationEditedDisposalPersonalisationEmail,
        BailGovNotifyNotificationSender notificationSender,
        BailNotificationIdAppender notificationIdAppender
    ) {
        return singletonList(
            new BailEmailNotificationGenerator(
                newArrayList(legalRepresentativeBailApplicationEditedDisposalPersonalisationEmail),
                notificationSender,
                notificationIdAppender
            )
        );
    }

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

    @Bean("createBailCaseLinkNotificationGenerator")
    public List<BailNotificationGenerator> createBailCaseLinkNotificationGenerator(
        LegalRepresentativeCreateBailCaseLinkPersonalisation legalRepresentativeCreateBailCaseLinkPersonalisation,
        HomeOfficeCreateBailCaseLinkPersonalisation homeOfficeCreateBailCaseLinkPersonalisation,
        BailGovNotifyNotificationSender notificationSender,
        BailNotificationIdAppender notificationIdAppender) {

        return List.of(
            new BailEmailNotificationGenerator(
                newArrayList(legalRepresentativeCreateBailCaseLinkPersonalisation,
                    homeOfficeCreateBailCaseLinkPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("createBailCaseLinkWithoutLrNotificationGenerator")
    public List<BailNotificationGenerator> createBailCaseLinkWithoutLrNotificationGenerator(
        HomeOfficeCreateBailCaseLinkPersonalisation homeOfficeCreateBailCaseLinkPersonalisation,
        BailGovNotifyNotificationSender notificationSender,
        BailNotificationIdAppender notificationIdAppender) {

        return List.of(
            new BailEmailNotificationGenerator(
                newArrayList(homeOfficeCreateBailCaseLinkPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("maintainBailCaseLinksNotificationGenerator")
    public List<BailNotificationGenerator> maintainBailCaseLinksNotificationGenerator(
        LegalRepresentativeMaintainBailCaseLinksPersonalisation legalRepresentativeMaintainBailCaseLinksPersonalisation,
        HomeOfficeMaintainBailCaseLinksPersonalisation homeOfficeMaintainBailCaseLinksPersonalisation,
        BailGovNotifyNotificationSender notificationSender,
        BailNotificationIdAppender notificationIdAppender) {

        return List.of(
            new BailEmailNotificationGenerator(
                newArrayList(legalRepresentativeMaintainBailCaseLinksPersonalisation,
                    homeOfficeMaintainBailCaseLinksPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("maintainBailCaseLinksWithoutLrNotificationGenerator")
    public List<BailNotificationGenerator> maintainBailCaseLinksWithoutLrNotificationGenerator(
        HomeOfficeMaintainBailCaseLinksPersonalisation homeOfficeMaintainBailCaseLinksPersonalisation,
        BailGovNotifyNotificationSender notificationSender,
        BailNotificationIdAppender notificationIdAppender) {

        return List.of(
            new BailEmailNotificationGenerator(
                newArrayList(homeOfficeMaintainBailCaseLinksPersonalisation),
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

    @Bean("caseListingBailSummaryDirectionNotificationGenerator")
    public List<BailNotificationGenerator> caseListingBailSummaryDirectionNotificationGenerator(
        HomeOfficeUploadBailSummaryDirectionPersonalisation homeOfficeUploadBailSummaryDirectionPersonalisation,
        BailGovNotifyNotificationSender notificationSender,
        BailNotificationIdAppender notificationIdAppender) {

        return List.of(
            new BailEmailNotificationGenerator(
                newArrayList(homeOfficeUploadBailSummaryDirectionPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("upperTribunalDecisionRefusedImaNotificationGenerator")
    public List<BailNotificationGenerator> upperTribunalDecisionRefusedImaNotificationGenerator(
        UpperTribunalDecisionRefusedImaPersonalisation upperTribunalDecisionRefusedImaPersonalisation,
        BailGovNotifyNotificationSender notificationSender,
        BailNotificationIdAppender notificationIdAppender) {

        return List.of(
            new BailEmailNotificationGenerator(
                newArrayList(upperTribunalDecisionRefusedImaPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("endApplicationNotificationForUtGenerator")
    public List<BailNotificationGenerator> endApplicationNotificationForUtGenerator(
        UpperTribunalApplicationEndedImaPersonalisation upperTribunalApplicationEndedImaPersonalisation,
        BailGovNotifyNotificationSender notificationSender,
        BailNotificationIdAppender notificationIdAppender) {

        return List.of(
            new BailEmailNotificationGenerator(
                newArrayList(upperTribunalApplicationEndedImaPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("caseListingNotificationGenerator")
    public List<BailNotificationGenerator> caseListingNotificationGenerator(
        HomeOfficeBailCaseListingPersonalisation homeOfficeBailCaseListingPersonalisation,
        LegalRepresentativeBailCaseListingPersonalisation legalRepresentativeBailCaseListingPersonalisation,
        ApplicantBailCaseListingPersonalisationSms applicantBailCaseListingPersonalisationSms,
        BailGovNotifyNotificationSender notificationSender,
        BailNotificationIdAppender notificationIdAppender) {

        return List.of(
            new BailEmailNotificationGenerator(
                newArrayList(homeOfficeBailCaseListingPersonalisation,
                    legalRepresentativeBailCaseListingPersonalisation),
                notificationSender,
                notificationIdAppender
            ),
            new BailSmsNotificationGenerator(
                newArrayList(applicantBailCaseListingPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("caseListingNotificationGeneratorWithoutLegalRep")
    public List<BailNotificationGenerator> caseListingNotificationGeneratorWithoutLegalRep(
        HomeOfficeBailCaseListingPersonalisation homeOfficeCaseListingPersonalisation,
        ApplicantBailCaseListingPersonalisationSms applicantBailCaseListingPersonalisationSms,
        BailGovNotifyNotificationSender notificationSender,
        BailNotificationIdAppender notificationIdAppender) {

        return List.of(
            new BailEmailNotificationGenerator(
                newArrayList(homeOfficeCaseListingPersonalisation),
                notificationSender,
                notificationIdAppender
            ),
            new BailSmsNotificationGenerator(
                newArrayList(applicantBailCaseListingPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("forceCaseToHearingNotificationGenerator")
    public List<BailNotificationGenerator> forceCaseToHearingNotificationGenerator(
            HomeOfficeForceCaseToHearingPersonalisation respondentForceCaseToHearingPersonalisation,
            LegalRepresentativeForceCaseToHearingPersonalisation legalRepForceCaseToHearingPersonalisation,
            BailGovNotifyNotificationSender notificationSender,
            BailNotificationIdAppender notificationIdAppender
    ) {

        return Collections.singletonList(
                new BailEmailNotificationGenerator(
                        newArrayList(
                                legalRepForceCaseToHearingPersonalisation,
                                respondentForceCaseToHearingPersonalisation
                        ),
                        notificationSender,
                        notificationIdAppender
                )
        );
    }

    @Bean("forceCaseToHearingNotificationGeneratorWithoutLegalRep")
    public List<BailNotificationGenerator> forceCaseToHearingNotificationGeneratorWithoutLegalRep(
        HomeOfficeForceCaseToHearingPersonalisation respondentForceCaseToHearingPersonalisation,
        BailGovNotifyNotificationSender notificationSender,
        BailNotificationIdAppender notificationIdAppender
    ) {

        return Collections.singletonList(
            new BailEmailNotificationGenerator(
                newArrayList(
                    respondentForceCaseToHearingPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("bailChangeTribunalCentreNotificationGeneratorWithoutLegalRep")
    public List<BailNotificationGenerator> bailChangeTribunalCentreNotificationGeneratorWithoutLegalRep(
            AdminOfficerBailChangeTribunalCentrePersonalisation adminOfficerBailChangeTribunalCentrePersonalisation,
            BailGovNotifyNotificationSender notificationSender,
            BailNotificationIdAppender notificationIdAppender) {

        return List.of(
                new BailEmailNotificationGenerator(
                        newArrayList(adminOfficerBailChangeTribunalCentrePersonalisation),
                        notificationSender,
                        notificationIdAppender
                )
        );
    }

    @Bean("bailChangeTribunalCentreNotificationGeneratorWithLegalRep")
    public List<BailNotificationGenerator> bailChangeTribunalCentreNotificationGeneratorWithLegalRep(
            AdminOfficerBailChangeTribunalCentrePersonalisation adminOfficerBailChangeTribunalCentrePersonalisation,
            LegalRepresentativeBailChangeTribunalCentrePersonalisation legalRepresentativeBailChangeTribunalCentrePersonalisation,
            BailGovNotifyNotificationSender notificationSender,
            BailNotificationIdAppender notificationIdAppender) {

        return List.of(
                new BailEmailNotificationGenerator(
                        newArrayList(
                                adminOfficerBailChangeTribunalCentrePersonalisation,
                                legalRepresentativeBailChangeTribunalCentrePersonalisation),
                        notificationSender,
                        notificationIdAppender
                )
        );
    }
}


