package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.email;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@Service
public class AipAppellantNocRequestDecisionPersonalisationEmail implements EmailNotificationPersonalisation {

    private final String nocRequestDecisionAppellantEmailTemplateId;
    private final CustomerServicesProvider customerServicesProvider;

    private final RecipientsFinder recipientsFinder;

    @Value("${govnotify.emailPrefix.ada}")
    String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAda}")
    String nonAdaPrefix;

    public AipAppellantNocRequestDecisionPersonalisationEmail(
        @Value("${govnotify.template.nocRequestDecision.appellant.email}") String nocRequestDecisionAppellantEmailTemplateId,
        CustomerServicesProvider customerServicesProvider, RecipientsFinder recipientsFinder) {
        this.nocRequestDecisionAppellantEmailTemplateId = nocRequestDecisionAppellantEmailTemplateId;
        this.customerServicesProvider = customerServicesProvider;
        this.recipientsFinder = recipientsFinder;
    }

    @Override
    public String getTemplateId() {
        return nocRequestDecisionAppellantEmailTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.EMAIL);

    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_NOC_REQUEST_DECISION_APPELLANT_EMAIL";
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

        return
            ImmutableMap
                .<String, String>builder()
                .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
                .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix)
                .put("Ref Number", String.valueOf(callback.getCaseDetails().getId()))
                .put("Given names", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("Family name", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("Date Of Birth", formattedDateOfBirth)
                .build();
    }
}
