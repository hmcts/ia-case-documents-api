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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.NotificationIdAppender;

@Component
public class LegalRepresentativeHearingRequirementsDirectionNotifier implements PreSubmitCallbackHandler<AsylumCase> {

    private final String legalRepresentativeHearingRequirementsDirectionTemplateId;
    private final LegalRepresentativePersonalisationFactory legalRepresentativePersonalisationFactory;
    private final DirectionFinder directionFinder;
    private final NotificationSender notificationSender;
    private final NotificationIdAppender notificationIdAppender;

    public LegalRepresentativeHearingRequirementsDirectionNotifier(
        @Value("${govnotify.template.legalRepresentativeHearingRequirementsDirection}") String legalRepresentativeHearingRequirementsDirectionTemplateId,
        LegalRepresentativePersonalisationFactory legalRepresentativePersonalisationFactory,
        DirectionFinder directionFinder,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {
        requireNonNull(legalRepresentativeHearingRequirementsDirectionTemplateId, "legalRepresentativeHearingRequirementsDirectionTemplateId must not be null");

        this.legalRepresentativeHearingRequirementsDirectionTemplateId = legalRepresentativeHearingRequirementsDirectionTemplateId;
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
               && callback.getEvent() == Event.REQUEST_HEARING_REQUIREMENTS;
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

        Direction legalRepresentativeHearingRequirementsDirection =
            directionFinder
                .findFirst(asylumCase, DirectionTag.LEGAL_REPRESENTATIVE_HEARING_REQUIREMENTS)
                .orElseThrow(() -> new IllegalStateException("legal representative hearing requirements direction is not present"));

        Map<String, String> personalisation =
            legalRepresentativePersonalisationFactory
                .create(asylumCase, legalRepresentativeHearingRequirementsDirection);

        String reference =
            callback.getCaseDetails().getId()
            + "_LEGAL_REPRESENTATIVE_HEARING_REQUIREMENTS_DIRECTION";

        String notificationId =
            notificationSender.sendEmail(
                legalRepresentativeHearingRequirementsDirectionTemplateId,
                legalRepresentativeEmailAddress,
                personalisation,
                reference
            );

        List<IdValue<String>> notificationsSent =
            asylumCase
                .getNotificationsSent()
                .orElseGet(ArrayList::new);

        asylumCase.setNotificationsSent(
            notificationIdAppender.append(
                notificationsSent,
                reference,
                notificationId
            )
        );

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
