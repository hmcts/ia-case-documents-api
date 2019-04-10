package uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

@Component
public class BuildCaseDirectionNotifier implements PreSubmitCallbackHandler<AsylumCase> {

    private final String buildCaseDirectionTemplateId;
    private final LegalRepresentativePersonalisationFactory legalRepresentativePersonalisationFactory;
    private final DirectionFinder directionFinder;
    private final NotificationSender notificationSender;

    public BuildCaseDirectionNotifier(
        @Value("${govnotify.template.buildCaseDirection}") String buildCaseDirectionTemplateId,
        LegalRepresentativePersonalisationFactory legalRepresentativePersonalisationFactory,
        DirectionFinder directionFinder,
        NotificationSender notificationSender
    ) {
        requireNonNull(buildCaseDirectionTemplateId, "buildCaseDirectionTemplateId must not be null");

        this.buildCaseDirectionTemplateId = buildCaseDirectionTemplateId;
        this.legalRepresentativePersonalisationFactory = legalRepresentativePersonalisationFactory;
        this.directionFinder = directionFinder;
        this.notificationSender = notificationSender;
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
                .getLegalRepresentativeEmailAddress()
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

        List<IdValue<String>> notificationsSent =
            asylumCase
                .getNotificationsSent()
                .orElseGet(ArrayList::new);

        notificationsSent.add(new IdValue<>(reference, notificationId));

        asylumCase.setNotificationsSent(notificationsSent);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
