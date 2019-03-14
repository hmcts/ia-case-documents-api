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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DirectionFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.StringProvider;

@Component
public class RespondentEvidenceDirectionNotifier implements PreSubmitCallbackHandler<AsylumCase> {

    private final String respondentEvidenceDirectionTemplate;
    private final String respondentEmailAddress;
    private final DirectionFinder directionFinder;
    private final NotificationSender notificationSender;
    private final StringProvider stringProvider;

    public RespondentEvidenceDirectionNotifier(
        @Value("${govnotify.template.respondentEvidenceDirection}") String respondentEvidenceDirectionTemplate,
        @Value("${respondentEmailAddresses.respondentEvidenceDirection}") String respondentEmailAddress,
        DirectionFinder directionFinder,
        NotificationSender notificationSender,
        StringProvider stringProvider
    ) {
        requireNonNull(respondentEvidenceDirectionTemplate, "respondentEvidenceDirectionTemplate must not be null");
        requireNonNull(respondentEmailAddress, "respondentEmailAddress must not be null");

        this.respondentEvidenceDirectionTemplate = respondentEvidenceDirectionTemplate;
        this.respondentEmailAddress = respondentEmailAddress;
        this.directionFinder = directionFinder;
        this.notificationSender = notificationSender;
        this.stringProvider = stringProvider;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.REQUEST_RESPONDENT_EVIDENCE;
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

        HearingCentre hearingCentre =
            asylumCase
                .getHearingCentre()
                .orElseThrow(() -> new IllegalStateException("hearingCentre is not present"));

        String hearingCentreForDisplay =
            stringProvider
                .get("hearingCentre", hearingCentre.toString())
                .orElseThrow(() -> new IllegalStateException("hearingCentre display string is not present"));

        Direction respondentEvidenceDirection =
            directionFinder
                .findFirst(asylumCase, DirectionTag.RESPONDENT_EVIDENCE)
                .orElseThrow(() -> new IllegalStateException("respondent evidence direction is not present"));

        String directionDueDate =
            LocalDate
                .parse(respondentEvidenceDirection.getDateDue())
                .format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        Map<String, String> personalisation =
            ImmutableMap
                .<String, String>builder()
                .put("HearingCentre", hearingCentreForDisplay)
                .put("Appeal Ref Number", asylumCase.getAppealReferenceNumber().orElse(""))
                .put("HORef", asylumCase.getHomeOfficeReferenceNumber().orElse(""))
                .put("Given names", asylumCase.getAppellantGivenNames().orElse(""))
                .put("Family name", asylumCase.getAppellantFamilyName().orElse(""))
                .put("Explanation", respondentEvidenceDirection.getExplanation())
                .put("due date", directionDueDate)
                .build();

        String reference =
            callback.getCaseDetails().getId()
            + "_RESPONDENT_EVIDENCE_DIRECTION";

        String notificationId =
            notificationSender.sendEmail(
                respondentEvidenceDirectionTemplate,
                respondentEmailAddress,
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
