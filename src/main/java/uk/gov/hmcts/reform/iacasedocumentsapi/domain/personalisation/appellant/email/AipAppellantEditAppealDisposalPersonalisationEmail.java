package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.email;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.requireNonNull;

@Service
public class AipAppellantEditAppealDisposalPersonalisationEmail implements EmailNotificationPersonalisation {

    private final String appealEditedAppellantAipDisposalEmailTemplateId;
    private final String iaAipFrontendUrl;
    private final CustomerServicesProvider customerServicesProvider;
    private final UserDetailsProvider userDetailsProvider;

    public AipAppellantEditAppealDisposalPersonalisationEmail(
        @Value("${govnotify.template.appealEdited.appellant.aip.disposal.email}") String appealEditedAppellantAipDisposalEmailTemplateId,
        @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
        CustomerServicesProvider customerServicesProvider,
        UserDetailsProvider userDetailsProvider
    ) {
        this.appealEditedAppellantAipDisposalEmailTemplateId = appealEditedAppellantAipDisposalEmailTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.customerServicesProvider = customerServicesProvider;
        this.userDetailsProvider = userDetailsProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return appealEditedAppellantAipDisposalEmailTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return Collections.singleton(userDetailsProvider.getUserDetails().getEmailAddress());
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_APPEAL_EDITED_APPELLANT_AIP_DISPOSAL";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        Optional<String> appellantGivenamesOpt = asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class);
        Optional<String> appellantFamilyNameOpt = asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class);
        String appellantFullName;
        if (appellantGivenamesOpt.isEmpty() && appellantFamilyNameOpt.isEmpty()) {
            appellantFullName = "Appellant";
        } else {
            appellantFullName = appellantGivenamesOpt.orElse("") + " " + appellantFamilyNameOpt.orElse("");
        }
        return
            ImmutableMap
                .<String, String>builder()
                .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
                .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
                .put("appellantFullName", appellantFullName)
                .put("linkToOnlineService", iaAipFrontendUrl)
                .put("editingDate", LocalDate.now().format(DateTimeFormatter.ofPattern("d MMM yyyy")))
                .build();
    }
}
