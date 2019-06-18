package uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REPRESENTATIVE_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.NOTIFICATIONS_SENT;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.NotificationSender;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.LegalRepresentativePersonalisationFactory;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DirectionFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.NotificationIdAppender;

@Component
public class BuildCaseDirectionNotifier implements PreSubmitCallbackHandler<AsylumCase> {

    private final String buildCaseDirectionTemplateId;
    private final LegalRepresentativePersonalisationFactory legalRepresentativePersonalisationFactory;
    private final DirectionFinder directionFinder;
    private final NotificationSender notificationSender;
    private final NotificationIdAppender notificationIdAppender;

    public BuildCaseDirectionNotifier(
        @Value("${govnotify.template.buildCaseDirection}") String buildCaseDirectionTemplateId,
        LegalRepresentativePersonalisationFactory legalRepresentativePersonalisationFactory,
        DirectionFinder directionFinder,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {
        requireNonNull(buildCaseDirectionTemplateId, "buildCaseDirectionTemplateId must not be null");

        this.buildCaseDirectionTemplateId = buildCaseDirectionTemplateId;
        this.legalRepresentativePersonalisationFactory = legalRepresentativePersonalisationFactory;
        this.directionFinder = directionFinder;
        this.notificationSender = notificationSender;
        this.notificationIdAppender = notificationIdAppender;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.UPLOAD_RESPONDENT_EVIDENCE;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        String legalRepresentativeEmailAddress =
            asylumCase
                .read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class)
                .orElseThrow(() -> new IllegalStateException("legalRepresentativeEmailAddress is not present"));

        Direction buildCaseDirection =
            directionFinder
                .findFirst(asylumCase, DirectionTag.BUILD_CASE)
                .orElseThrow(() -> new IllegalStateException("build case direction is not present"));

        Map<String, String> personalisation =
            legalRepresentativePersonalisationFactory
                .create(asylumCase, buildCaseDirection);

        String reference =
            callback.getCaseDetails().getId()
            + "_BUILD_CASE_DIRECTION";

        String notificationId =
            notificationSender.sendEmail(
                buildCaseDirectionTemplateId,
                legalRepresentativeEmailAddress,
                personalisation,
                reference
            );

        Optional<List<IdValue<String>>> maybeNotificationSent =
                asylumCase.read(NOTIFICATIONS_SENT);

        List<IdValue<String>> notificationsSent =
            maybeNotificationSent
                .orElseGet(ArrayList::new);

        asylumCase.write(NOTIFICATIONS_SENT,
            notificationIdAppender.append(
                notificationsSent,
                reference,
                notificationId
            )
        );

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
