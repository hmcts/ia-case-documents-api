package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Message;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer.AdminOfficerAdjournHearingWithoutDatePersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer.AdminOfficerAppealOutcomePersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer.AdminOfficerAppealRemissionApprovedPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer.AdminOfficerAppealSubmittedPayOfflinePersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer.AdminOfficerAppealSubmittedPendingPaymentPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer.AdminOfficerChangeToHearingRequirementsPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer.AdminOfficerDecidedOrEndedAppealPendingPayment;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer.AdminOfficerEditPaymentMethodPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer.AdminOfficerFtpaDecisionAppellantPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer.AdminOfficerFtpaDecisionRespondentPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer.AdminOfficerFtpaSubmittedPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer.AdminOfficerReListCasePersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer.AdminOfficerRemissionDecisionPartiallyApprovedPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer.AdminOfficerRequestFeeRemissionPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer.AdminOfficerReviewHearingRequirementsPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer.AdminOfficerUpperTribunalBundleFailedPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer.AdminOfficerWithoutHearingRequirementsPersonalisation;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.EditListingEmailNotificationGenerator;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.EmailNotificationGenerator;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.NotificationGenerator;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.NotificationIdAppender;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.SmsNotificationGenerator;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.GovNotifyNotificationSender;

@Configuration
public class NotificationGeneratorConfiguration {

    @Value("${featureFlag.homeOfficeGovNotifyEnabled}")
    private boolean isHomeOfficeGovNotifyEnabled;


    @Bean("forceCaseProgressionNotificationGenerator")
    public List<NotificationGenerator> forceCaseProgressionNotificationGenerator(
        RespondentForceCaseProgressionPersonalisation homeOfficePersonalisation,
        LegalRepresentativeRequestCaseBuildingPersonalisation legalRepresentativeRequestCaseBuildingPersonalisation,
        GovNotifyNotificationSender notificationSender,
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
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Collections.singletonList(
            new EmailNotificationGenerator(newArrayList(personalisation), notificationSender, notificationIdAppender)
        );
    }

    @Bean("caseLinkAppealNotificationGenerator")
    public List<NotificationGenerator> caseLinkAppealNotificationGenerator(
        LegalRepresentativeCaseLinkAppealPersonalisation legalRepresentativeCaseLinkAppealPersonalisation,
        HomeOfficeCaseLinkPersonalisation homeOfficeCaseLinkPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Collections.singletonList(
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

        return Collections.singletonList(
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

        return Collections.singletonList(
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

        return Collections.singletonList(
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
        GovNotifyNotificationSender notificationSender,
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
            ?  newArrayList(homeOfficeAppealOutcomePersonalisation)
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
                Collections.singletonList(appellantChangeDirectionDueDateOfAppellantPersonalisationSms),
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
                        Collections.singletonList(appellantChangeDirectionDueDateOfAppellantPersonalisationSms),
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
            ?  newArrayList(respondentChangeDirectionDueDatePersonalisation, appellantChangeDirectionDueDateOfHomeOfficePersonalisationEmail)
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
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        // RIA-3361 - listCase
        List<EmailNotificationPersonalisation> personalisations = isHomeOfficeGovNotifyEnabled
            ?  newArrayList(legalRepresentativeListCasePersonalisation, homeOfficeListCasePersonalisation, caseOfficerListCasePersonalisation)
            : newArrayList(legalRepresentativeListCasePersonalisation, caseOfficerListCasePersonalisation);

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
            ?  newArrayList(caseOfficerListCasePersonalisation, appellantListCasePersonalisationEmail, homeOfficeListCasePersonalisation)
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
        GovNotifyNotificationSender notificationSender,
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

    @Bean("respondentReviewAipNotificationGenerator")
    public List<NotificationGenerator> respondentReviewAipNotificationGenerator(
        RespondentDirectionPersonalisation respondentDirectionPersonalisation,
        AppellantRespondentReviewPersonalisationEmail appellantRespondentReviewPersonalisationEmail,
        AppellantRespondentReviewPersonalisationSms appellantRespondentReviewPersonalisationSms,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        // RIA-3631 - requestRespondentReview
        List<EmailNotificationPersonalisation> personalisations = isHomeOfficeGovNotifyEnabled
            ?  newArrayList(respondentDirectionPersonalisation, appellantRespondentReviewPersonalisationEmail)
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

    @Bean("awaitingRespondentDirectionAipNotificationGenerator")
    public List<NotificationGenerator> awaitingRespondentDirectionAipNotificationGenerator(
            RespondentNonStandardDirectionPersonalisation respondentNonStandardDirectionPersonalisation,
            GovNotifyNotificationSender notificationSender,
            NotificationIdAppender notificationIdAppender) {

        List<EmailNotificationPersonalisation> personalisations = isHomeOfficeGovNotifyEnabled
                ?  newArrayList(respondentNonStandardDirectionPersonalisation)
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
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        // RIA-3631 - editCaseListing
        List<EmailNotificationPersonalisation> personalisations = isHomeOfficeGovNotifyEnabled
            ?  newArrayList(homeOfficeEditListingPersonalisation, legalRepresentativeEditListingPersonalisation, legalRepresentativeEditListingNoChangePersonalisation, homeOfficeEditListingNoChangePersonalisation, caseOfficerEditListingPersonalisation)
            : newArrayList(legalRepresentativeEditListingPersonalisation, legalRepresentativeEditListingNoChangePersonalisation, caseOfficerEditListingPersonalisation);

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
            ?  newArrayList(homeOfficeEditListingPersonalisation, appellantEditListingPersonalisationEmail, homeOfficeEditListingNoChangePersonalisation, caseOfficerEditListingPersonalisation)
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

        return Collections.singletonList(
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

    @Bean("ftpaSubmittedLegalRepNotificationGenerator")
    public List<NotificationGenerator> ftpaSubmittedLegalRep(
        LegalRepresentativeFtpaSubmittedPersonalisation legalRepresentativeFtpaSubmittedPersonalisation,
        AdminOfficerFtpaSubmittedPersonalisation adminOfficerFtpaSubmittedPersonalisation,
        RespondentAppellantFtpaSubmittedPersonalisation respondentAppellantFtpaSubmittedPersonalisation,
        GovNotifyNotificationSender notificationSender,
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

    @Bean("ftpaSubmittedAipNotificationGenerator")
    public List<NotificationGenerator> ftpaSubmittedAip(
        AppellantFtpaSubmittedPersonalisationEmail appellantFtpaSubmittedPersonalisationEmail,
        AppellantFtpaSubmittedPersonalisationSms appellantFtpaSubmittedPersonalisationSms,
        AdminOfficerFtpaSubmittedPersonalisation adminOfficerFtpaSubmittedPersonalisation,
        RespondentAppellantFtpaSubmittedPersonalisation respondentAppellantFtpaSubmittedPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return List.of(
            new EmailNotificationGenerator(
                List.of(appellantFtpaSubmittedPersonalisationEmail,
                    adminOfficerFtpaSubmittedPersonalisation,
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
        AdminOfficerFtpaSubmittedPersonalisation adminOfficerFtpaSubmittedPersonalisation,
        LegalRepresentativeRespondentFtpaSubmittedPersonalisation legalRepresentativeRespondentFtpaSubmittedPersonalisation,
        GovNotifyNotificationSender notificationSender,
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

    @Bean("ftpaSubmittedRespondentAipJourneyNotificationGenerator")
    public List<NotificationGenerator> ftpaSubmittedRespondentAipJourney(
        RespondentFtpaSubmittedPersonalisation respondentFtpaSubmittedPersonalisation,
        AdminOfficerFtpaSubmittedPersonalisation adminOfficerFtpaSubmittedPersonalisation,
        // notification sent to appellant for FTPA submitted by HO
        AppellantRespondentFtpaSubmittedPersonalisationEmail appellantRespondentFtpaSubmittedPersonalisationEmail,
        AppellantRespondentFtpaSubmittedPersonalisationSms appellantRespondentFtpaSubmittedPersonalisationSms,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        // RIA-3316 - applyForFTPARespondent
        List<EmailNotificationPersonalisation> personalisations = isHomeOfficeGovNotifyEnabled
            ?  newArrayList(respondentFtpaSubmittedPersonalisation, adminOfficerFtpaSubmittedPersonalisation, appellantRespondentFtpaSubmittedPersonalisationEmail)
            : newArrayList(adminOfficerFtpaSubmittedPersonalisation, appellantRespondentFtpaSubmittedPersonalisationEmail);

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

        return Collections.singletonList(
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
        AdminOfficerAdjournHearingWithoutDatePersonalisation adminOfficerAdjournHearingWithoutDatePersonalisation,
        CaseOfficerAdjournHearingWithoutDatePersonalisation caseOfficerAdjournHearingWithoutDatePersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        // RIA-3631 adjournHearingWithoutDate
        List<EmailNotificationPersonalisation> personalisations = isHomeOfficeGovNotifyEnabled
            ?  newArrayList(legalRepresentativeAdjournHearingWithoutDatePersonalisation, respondentAdjournHearingWithoutDatePersonalisation, adminOfficerAdjournHearingWithoutDatePersonalisation, caseOfficerAdjournHearingWithoutDatePersonalisation)
            : newArrayList(legalRepresentativeAdjournHearingWithoutDatePersonalisation, adminOfficerAdjournHearingWithoutDatePersonalisation, caseOfficerAdjournHearingWithoutDatePersonalisation);

        return Collections.singletonList(
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
        GovNotifyNotificationSender notificationSender,
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

    @Bean("ftpaApplicationDecisionRefusedOrNotAdmittedAppellantAipJourneyNotificationGenerator")
    public List<NotificationGenerator> ftpaApplicationDecisionRefusedOrNotAdmittedAppellantAipJourneyNotificationGenerator(
        HomeOfficeFtpaApplicationDecisionAppellantPersonalisation homeOfficeFtpaApplicationDecisionAppellantPersonalisation,
        AppellantFtpaApplicationDecisionPersonalisationEmail appellantFtpaApplicationDecisionPersonalisationEmail,
        AppellantFtpaApplicationDecisionPersonalisationSms appellantFtpaApplicationDecisionPersonalisationSms,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        //RIA-3116 leadership/resident judge decision
        List<EmailNotificationPersonalisation> emailPersonalisations = isHomeOfficeGovNotifyEnabled
            ?  newArrayList(homeOfficeFtpaApplicationDecisionAppellantPersonalisation, appellantFtpaApplicationDecisionPersonalisationEmail)
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

    @Bean("ftpaApplicationDecisionRefusedOrNotAdmittedRespondentNotificationGenerator")
    public List<NotificationGenerator> ftpaApplicationDecisionRefusedOrNotAdmittedRespondentNotificationGenerator(
        HomeOfficeFtpaApplicationDecisionRespondentPersonalisation homeOfficeFtpaApplicationDecisionRespondentPersonalisation,
        LegalRepresentativeFtpaApplicationDecisionRespondentPersonalisation legalRepresentativeFtpaApplicationDecisionRespondentPersonalisation,
        GovNotifyNotificationSender notificationSender,
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

    @Bean("ftpaApplicationDecisionRefusedOrNotAdmittedRespondentAipJourneyNotificationGenerator")
    public List<NotificationGenerator> ftpaApplicationDecisionRefusedOrNotAdmittedRespondentAipJourneyNotificationGenerator(
        HomeOfficeFtpaApplicationDecisionRespondentPersonalisation homeOfficeFtpaApplicationDecisionRespondentPersonalisation,
        AppellantFtpaApplicationDecisionPersonalisationEmail appellantFtpaApplicationDecisionPersonalisationEmail,
        AppellantFtpaApplicationDecisionPersonalisationSms appellantFtpaApplicationDecisionPersonalisationSms,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        // RIA-6135
        List<EmailNotificationPersonalisation> emailPersonalisations = isHomeOfficeGovNotifyEnabled
            ?  newArrayList(homeOfficeFtpaApplicationDecisionRespondentPersonalisation, appellantFtpaApplicationDecisionPersonalisationEmail)
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
            ?  newArrayList(homeOfficeFtpaApplicationDecisionRespondentPersonalisation, appellantFtpaApplicationDecisionPersonalisationEmail, adminOfficerFtpaDecisionRespondentPersonalisation)
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
        GovNotifyNotificationSender notificationSender,
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
        GovNotifyNotificationSender notificationSender,
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
        GovNotifyNotificationSender notificationSender,
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
        GovNotifyNotificationSender notificationSender,
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

        return Collections.singletonList(
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
            AdminOfficerAppealSubmittedPendingPaymentPersonalisation adminOfficerAppealSubmittedPendingPaymentPersonalisation,
            GovNotifyNotificationSender notificationSender,
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
            GovNotifyNotificationSender notificationSender,
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
            GovNotifyNotificationSender notificationSender,
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
            GovNotifyNotificationSender notificationSender,
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

    @Bean("makeAnApplicationNotificationGenerator")
    public List<NotificationGenerator> makeAnApplicationNotificationHandler(
            LegalRepresentativeMakeAnApplicationPersonalisation legalRepresentativeMakeApplicationPersonalisation,
            HomeOfficeMakeAnApplicationPersonalisation homeOfficeMakeAnApplicationPersonalisation,
            CaseOfficerMakeAnApplicationPersonalisation caseOfficerMakeAnApplicationPersonalisation,
            GovNotifyNotificationSender notificationSender,
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

        GovNotifyNotificationSender notificationSender,
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

    @Bean("decideAnApplicationAipNotificationGenerator")
    public List<NotificationGenerator> decideAnApplicationAipNotificationHandler(
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
        AdminOfficerAppealRemissionApprovedPersonalisation adminOfficerAppealRemissionApprovedPersonalisation,
        LegalRepresentativeRemissionDecisionApprovedPersonalisation legalRepresentativeRemissionDecisionApprovedPersonalisation,
        GovNotifyNotificationSender notificationSender,
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
        GovNotifyNotificationSender notificationSender,
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
        GovNotifyNotificationSender notificationSender,
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

    @Bean("nocRequestDecisionHomeOfficeNotificationGenerator")
    public List<NotificationGenerator> nocRequestDecisionHomeOfficeNotificationGenerator(
        HomeOfficeNocRequestDecisionPersonalisation homeOfficeNocRequestDecisionPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return Collections.singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    homeOfficeNocRequestDecisionPersonalisation
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

    @Bean("nocRequestDecisionLrNotificationGenerator")
    public List<NotificationGenerator> nocRequestDecisionLrNotificationGenerator(
        LegalRepresentativeNocRequestDecisionPersonalisation legalRepresentativeNocRequestDecisionPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {
        return Collections.singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    legalRepresentativeNocRequestDecisionPersonalisation
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

    @Bean("removeRepresentationNotificationGenerator")
    public List<NotificationGenerator> removeRepresentationNotificationHandler(
        LegalRepresentativeRemoveRepresentationPersonalisation legalRepresentativeRemoveRepresentationPersonalisation,
        HomeOfficeRemoveRepresentationPersonalisation homeOfficeRemoveRepresentationPersonalisation,
        CaseOfficerRemoveRepresentationPersonalisation caseOfficerRemoveRepresentationPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return Collections.singletonList(
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
                    return new Message("success","body");
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
                    return new Message("success","body");
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

        return Collections.singletonList(
            new SmsNotificationGenerator(
                newArrayList(appellantRemoveRepresentationPersonalisationSms),
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

    @Bean("removeRepresentativeAppellantEmailNotificationGenerator")
    public List<NotificationGenerator> removeRepresentativeAppellantEmailNotificationHandler(
        AppellantRemoveRepresentationPersonalisationEmail appellantRemoveRepresentationPersonalisationEmail,
        GovNotifyNotificationSender notificationSender,
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
        GovNotifyNotificationSender notificationSender,
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
        GovNotifyNotificationSender notificationSender,
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
        GovNotifyNotificationSender notificationSender,
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
        GovNotifyNotificationSender notificationSender,
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
        GovNotifyNotificationSender notificationSender,
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
                    return new Message("success","body");
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
        GovNotifyNotificationSender notificationSender,
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
        GovNotifyNotificationSender notificationSender,
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

    @Bean("recordOfTimeDecisionCannotProceedAppellantEmailNotificationGenerator")
    public List<NotificationGenerator> recordOfTimeDecisionCannotProceedAppellantEmailNotificationGenerator(
            AppellantRecordOutOfTimeDecisionCannotProceedPersonalisationEmail appellantRecordOutOfTimeDecisionCannotProceedPersonalisationEmail,
            GovNotifyNotificationSender notificationSender,
            NotificationIdAppender notificationIdAppender) {

        return Collections.singletonList(
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

        return Collections.singletonList(
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

        return Collections.singletonList(
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

        return Collections.singletonList(
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

        return Collections.singletonList(
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
                    return new Message("success","body");
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

        return Collections.singletonList(
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
                    return new Message("success","body");
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

        return Collections.singletonList(
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
                    return new Message("success","body");
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

        return Collections.singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    legalRepresentativePaymentPaidPersonalisation
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

        return Collections.singletonList(
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

        return Collections.singletonList(
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
}
