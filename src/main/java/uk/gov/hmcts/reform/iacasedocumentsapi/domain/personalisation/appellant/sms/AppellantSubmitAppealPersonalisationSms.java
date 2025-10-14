package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.sms;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.MOBILE_NUMBER;

import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.SmsNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.AppealService;

@Service
public class AppellantSubmitAppealPersonalisationSms implements SmsNotificationPersonalisation {

    private final String appealSubmittedAppellantSmsTemplateId;
    private final String appealSubmittedAppellantSmsRepJourneyTemplateId;
    private final String iaAipFrontendUrl;
    private final int daysToWaitAfterSubmission;
    private final RecipientsFinder recipientsFinder;
    private final SystemDateProvider systemDateProvider;
    private final AppealService appealService;


    public AppellantSubmitAppealPersonalisationSms(
        @Value("${govnotify.template.appealSubmitted.appellant.sms}") String appealSubmittedAppellantSmsTemplateId,
        @Value("${govnotify.template.appealSubmitted.appellant.legalRep.sms}") String appealSubmittedAppellantSmsRepJourneyTemplateId,
        @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
        @Value("${appellantDaysToWait.afterSubmission}") int daysToWaitAfterSubmission,
        RecipientsFinder recipientsFinder,
        SystemDateProvider systemDateProvider,
        AppealService appealService
    ) {
        this.appealSubmittedAppellantSmsTemplateId = appealSubmittedAppellantSmsTemplateId;
        this.appealSubmittedAppellantSmsRepJourneyTemplateId = appealSubmittedAppellantSmsRepJourneyTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.daysToWaitAfterSubmission = daysToWaitAfterSubmission;
        this.recipientsFinder = recipientsFinder;
        this.systemDateProvider = systemDateProvider;
        this.appealService = appealService;
    }


    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        if (appealService.isAppellantInPersonJourney(asylumCase)) {
            return appealSubmittedAppellantSmsTemplateId;
        } else {
            return appealSubmittedAppellantSmsRepJourneyTemplateId;
        }
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");
        if (appealService.isAppellantInPersonJourney(asylumCase)) {
            return recipientsFinder.findAll(asylumCase, NotificationType.SMS);
        } else {
            return Collections.singleton(asylumCase
                .read(MOBILE_NUMBER, String.class)
                .orElseThrow(() -> new IllegalStateException("appellantMobileNumber is not present")));
        }
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_APPEAL_SUBMITTED_APPELLANT_AIP_SMS";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        final String dateOfBirth = asylumCase
                .read(AsylumCaseDefinition.APPELLANT_DATE_OF_BIRTH,String.class)
                .orElseThrow(() -> new IllegalStateException("Appellant's birth of date is not present"));

        final  String formattedDateOfBirth = LocalDate.parse(dateOfBirth).format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        final String dueDate = systemDateProvider.dueDate(daysToWaitAfterSubmission);

        return
            ImmutableMap
                .<String, String>builder()
                .put("Appeal Ref Number", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("Hyperlink to service", iaAipFrontendUrl)
                .put("due date", dueDate)
                .put("Ref Number", String.valueOf(callback.getCaseDetails().getId()))
                .put("Legal Rep Ref", asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""))
                .put("HO Ref Number", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
                .put("Given names", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("Family name", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("Date Of Birth", formattedDateOfBirth)
                .build();
    }
}
