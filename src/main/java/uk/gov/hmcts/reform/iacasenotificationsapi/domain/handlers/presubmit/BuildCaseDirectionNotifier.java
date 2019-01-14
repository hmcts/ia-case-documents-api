package uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DirectionFinder;

@Component
public class BuildCaseDirectionNotifier implements PreSubmitCallbackHandler<AsylumCase> {

    private final String buildCaseDirectionTemplate;
    private final String iaCcdFrontendUrl;
    private final DirectionFinder directionFinder;
    private final NotificationSender notificationSender;

    public BuildCaseDirectionNotifier(
        @Value("${govnotify.template.buildCaseDirection}") String buildCaseDirectionTemplate,
        @Value("${iaCcdFrontendUrl}") String iaCcdFrontendUrl,
        DirectionFinder directionFinder,
        NotificationSender notificationSender
    ) {
        this.buildCaseDirectionTemplate = buildCaseDirectionTemplate;
        this.iaCcdFrontendUrl = iaCcdFrontendUrl;
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

        Direction buildCaseDirection =
            directionFinder
                .findFirst(asylumCase, DirectionTag.BUILD_CASE)
                .orElseThrow(() -> new IllegalStateException("build case direction is not present"));

        String emailAddress =
            asylumCase
                .getLegalRepresentativeEmailAddress()
                .orElseThrow(() -> new IllegalStateException("legalRepresentativeEmailAddress is not present"));

        String directionDueDate =
            LocalDate
                .parse(buildCaseDirection.getDateDue())
                .format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        Map<String, String> personalisation =
            ImmutableMap
                .<String, String>builder()
                .put("Appeal Ref Number", asylumCase.getAppealReferenceNumber().orElse(""))
                .put("LR reference", asylumCase.getLegalRepReferenceNumber().orElse(""))
                .put("Given names", asylumCase.getAppellantGivenNames().orElse(""))
                .put("Family name", asylumCase.getAppellantLastName().orElse(""))
                .put("Hyperlink to user’s case list", iaCcdFrontendUrl)
                .put("Explanation", buildCaseDirection.getExplanation())
                .put("due date", directionDueDate)
                .build();

        String reference =
            callback.getCaseDetails().getId()
            + "_BUILD_CASE_DIRECTION";

        String notificationId =
            notificationSender.sendEmail(
                buildCaseDirectionTemplate,
                emailAddress,
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
