package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.email;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.EMAIL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.AppealService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@Service
public class AppellantSubmitAppealPersonalisationEmail implements EmailNotificationPersonalisation {

    private final String appealSubmittedAppellantEmailTemplateId;
    private final String appealSubmittedAppellantNoHomeOfficeReferenceEmailTemplateId;
    private final String appealSubmittedAppellantLegalRepEmailTemplateId;
    private final String iaAipFrontendUrl;
    private final int daysToWaitAfterSubmission;
    private final RecipientsFinder recipientsFinder;
    private final SystemDateProvider systemDateProvider;
    private final AppealService appealService;
    private final CustomerServicesProvider customerServicesProvider;

    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;

    public AppellantSubmitAppealPersonalisationEmail(
        @Value("${govnotify.template.appealSubmitted.appellant.email}") String appealSubmittedAppellantEmailTemplateId,
        @Value("${govnotify.template.appealSubmitted.appellant.noHomeOfficeReference.email}") String appealSubmittedAppellantNoHomeOfficeReferenceEmailTemplateId,
        @Value("${govnotify.template.appealSubmitted.appellant.legalRep.email}") String appealSubmittedAppellantLegalRepEmailTemplateId,
        @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
        @Value("${appellantDaysToWait.afterSubmission}") int daysToWaitAfterSubmission,
        RecipientsFinder recipientsFinder,
        SystemDateProvider systemDateProvider,
        AppealService appealService,
        CustomerServicesProvider customerServicesProvider
    ) {
        this.appealSubmittedAppellantEmailTemplateId = appealSubmittedAppellantEmailTemplateId;
        this.appealSubmittedAppellantNoHomeOfficeReferenceEmailTemplateId = appealSubmittedAppellantNoHomeOfficeReferenceEmailTemplateId;
        this.appealSubmittedAppellantLegalRepEmailTemplateId = appealSubmittedAppellantLegalRepEmailTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.daysToWaitAfterSubmission = daysToWaitAfterSubmission;
        this.recipientsFinder = recipientsFinder;
        this.systemDateProvider = systemDateProvider;
        this.appealService = appealService;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {

        if (appealService.isAppellantInPersonJourney(asylumCase)) {
            if (asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).isEmpty()) {
                return appealSubmittedAppellantNoHomeOfficeReferenceEmailTemplateId;
            } else {
                return appealSubmittedAppellantEmailTemplateId;
            }
        } else {
            return appealSubmittedAppellantLegalRepEmailTemplateId;
        }

    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        if (appealService.isAppellantInPersonJourney(asylumCase)) {
            return recipientsFinder.findAll(asylumCase, NotificationType.EMAIL);
        } else {
            return Collections.singleton(asylumCase
                .read(EMAIL, String.class)
                .orElseThrow(() -> new IllegalStateException("appellantEmailAddress is not present")));
        }
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_APPEAL_SUBMITTED_APPELLANT_AIP_EMAIL";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        final String dateOfBirth = asylumCase
            .read(AsylumCaseDefinition.APPELLANT_DATE_OF_BIRTH, String.class)
            .orElseThrow(() -> new IllegalStateException("Appellant's birth of date is not present"));

        final String formattedDateOfBirth = LocalDate.parse(dateOfBirth).format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        String dueDate = systemDateProvider.dueDate(daysToWaitAfterSubmission);

        return
            ImmutableMap
                .<String, String>builder()
                .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
                .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix)
                .put("Ref Number", String.valueOf(callback.getCaseDetails().getId()))
                .put("Appeal Ref Number", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("Legal Rep Ref", asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""))
                .put("HO Ref Number", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
                .put("Given names", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("Family name", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("due date", dueDate)
                .put("Date Of Birth", formattedDateOfBirth)
                .put("Hyperlink to service", iaAipFrontendUrl)
                .build();
    }
}
