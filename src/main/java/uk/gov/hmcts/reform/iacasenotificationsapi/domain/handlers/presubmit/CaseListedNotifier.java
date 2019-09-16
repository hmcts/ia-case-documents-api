package uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.NOTIFICATIONS_SENT;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.NotificationSender;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.CaseOfficerPersonalisationFactory;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.HomeOfficePersonalisationFactory;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.LegalRepresentativePersonalisationFactory;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.NotificationIdAppender;

@Component
public class CaseListedNotifier implements PreSubmitCallbackHandler<AsylumCase> {

    private final String caseListedCaseOfficerTemplateId;
    private final String caseListedLegalRepresentativeTemplateId;
    private final String caseListedHomeOfficeTemplateId;

    private final CaseOfficerCaseListedNotifier caseOfficerCaseListedNotifier;
    private final LegalRepresentativeCaseListedNotifier legalRepresentativeCaseListedNotifier;
    private final HomeOfficeCaseListedNotifier homeOfficeCaseListedNotifier;
    private final CaseOfficerPersonalisationFactory caseOfficerPersonalisationFactory;
    private final LegalRepresentativePersonalisationFactory legalRepresentativePersonalisationFactory;
    private final HomeOfficePersonalisationFactory homeOfficePersonalisationFactory;

    private final Map<HearingCentre, String> hearingCentreEmailAddresses;
    private final NotificationSender notificationSender;
    private final NotificationIdAppender notificationIdAppender;

    public CaseListedNotifier(
        @Value("${govnotify.template.caseOfficerCaseListed}") String caseOfficerCaseListedTemplateId,
        @Value("${govnotify.template.legalRepresentativeCaseListed}") String legalRepresentativeCaseListedTemplateId,
        @Value("${govnotify.template.homeOfficeCaseListed}") String homeOfficeCaseListedTemplateId,
        CaseOfficerCaseListedNotifier caseOfficerCaseListedNotifier,
        LegalRepresentativeCaseListedNotifier legalRepresentativeCaseListedNotifier,
        HomeOfficeCaseListedNotifier homeOfficeCaseListedNotifier,
        CaseOfficerPersonalisationFactory caseOfficerPersonalisationFactory,
        LegalRepresentativePersonalisationFactory legalRepresentativePersonalisationFactory,
        HomeOfficePersonalisationFactory homeOfficePersonalisationFactory,
        Map<HearingCentre, String> hearingCentreEmailAddresses,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {
        requireNonNull(caseOfficerCaseListedTemplateId, "caseOfficerCaseListedTemplateId must not be null");
        requireNonNull(legalRepresentativeCaseListedTemplateId, "legalRepresentativeCaseListedTemplateId must not be null");
        requireNonNull(homeOfficeCaseListedTemplateId, "homeOfficeCaseListedTemplateId must not be null");

        this.caseListedCaseOfficerTemplateId = caseOfficerCaseListedTemplateId;
        this.caseListedLegalRepresentativeTemplateId = legalRepresentativeCaseListedTemplateId;
        this.caseListedHomeOfficeTemplateId = homeOfficeCaseListedTemplateId;
        this.caseOfficerPersonalisationFactory = caseOfficerPersonalisationFactory;
        this.legalRepresentativePersonalisationFactory = legalRepresentativePersonalisationFactory;
        this.homeOfficePersonalisationFactory = homeOfficePersonalisationFactory;
        this.hearingCentreEmailAddresses = hearingCentreEmailAddresses;
        this.notificationSender = notificationSender;
        this.notificationIdAppender = notificationIdAppender;
        this.caseOfficerCaseListedNotifier = caseOfficerCaseListedNotifier;
        this.legalRepresentativeCaseListedNotifier = legalRepresentativeCaseListedNotifier;
        this.homeOfficeCaseListedNotifier = homeOfficeCaseListedNotifier;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.LIST_CASE;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        handleCaseOfficer(callback, asylumCase);

        handleLegalRepresentative(callback, asylumCase);

        handleHomeOffice(callback, asylumCase);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    protected void handleCaseOfficer(
        Callback<AsylumCase> callback,
        AsylumCase asylumCase
    ) {

        sendGovNotifyEmail(
            callback,
            "CASE_OFFICER",
            caseListedCaseOfficerTemplateId,
            caseOfficerCaseListedNotifier.getEmailAddress(asylumCase),
            caseOfficerCaseListedNotifier.getPersonalisation(asylumCase),
            asylumCase);
    }

    protected void handleLegalRepresentative(
        Callback<AsylumCase> callback,
        AsylumCase asylumCase
    ) {

        sendGovNotifyEmail(
            callback,
            "LEGAL_REPRESENTATIVE",
            caseListedLegalRepresentativeTemplateId,
            legalRepresentativeCaseListedNotifier.getEmailAddress(asylumCase),
            legalRepresentativeCaseListedNotifier.getPersonalisation(asylumCase),
            asylumCase);
    }

    protected void handleHomeOffice(
        Callback<AsylumCase> callback,
        AsylumCase asylumCase
    ) {

        sendGovNotifyEmail(
            callback,
            "HOME_OFFICE",
            caseListedHomeOfficeTemplateId,
            homeOfficeCaseListedNotifier.getEmailAddress(asylumCase),
            homeOfficeCaseListedNotifier.getPersonalisation(asylumCase),
            asylumCase);
    }

    protected void sendGovNotifyEmail(
        Callback<AsylumCase> callback,
        String referenceSuffix,
        String templateId,
        String emailAddress,
        Map<String, String> personalisation,
        AsylumCase asylumCase
    ) {

        String reference =
            callback.getCaseDetails().getId()
            + "_CASE_LISTED_" + referenceSuffix;

        String notificationId =
            notificationSender.sendEmail(
                templateId,
                emailAddress,
                personalisation,
                reference
            );

        Optional<List<IdValue<String>>> maybeNotificationSent = asylumCase.read(NOTIFICATIONS_SENT);

        List<IdValue<String>> notificationsSent = maybeNotificationSent
            .orElseGet(ArrayList::new);

        asylumCase.write(NOTIFICATIONS_SENT,
            notificationIdAppender.append(
                notificationsSent,
                reference,
                notificationId
            )
        );
    }
}
