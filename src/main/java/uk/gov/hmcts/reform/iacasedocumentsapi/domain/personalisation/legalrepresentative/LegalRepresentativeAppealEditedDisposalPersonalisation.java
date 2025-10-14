package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.legalrepresentative;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotNull;

import static java.util.Objects.requireNonNull;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;

@Service
@Slf4j
public class LegalRepresentativeAppealEditedDisposalPersonalisation implements LegalRepresentativeEmailNotificationPersonalisation {

    private final String appealEditedLegalRepresentativeDisposalTemplateId;
    private final String iaExUiFrontendUrl;
    private final CustomerServicesProvider customerServicesProvider;
    private final UserDetailsProvider userDetailsProvider;

    public LegalRepresentativeAppealEditedDisposalPersonalisation(
        @NotNull(message = "appealEditedLegalRepresentativeDisposalTemplateId cannot be null")
        @Value("${govnotify.template.appealEdited.legalRep.disposal.email}") String appealEditedLegalRepresentativeDisposalTemplateId,
        @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
        CustomerServicesProvider customerServicesProvider,
        UserDetailsProvider userDetailsProvider
    ) {
        this.appealEditedLegalRepresentativeDisposalTemplateId = appealEditedLegalRepresentativeDisposalTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.customerServicesProvider = customerServicesProvider;
        this.userDetailsProvider = userDetailsProvider;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(userDetailsProvider.getUserDetails().getEmailAddress());
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return appealEditedLegalRepresentativeDisposalTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_APPEAL_EDITED_LEGAL_REP_DISPOSAL";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        ImmutableMap<String, String> res = ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("homeOfficeReferenceNumber", asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("legalRepReferenceNumber", asylumCase.read(AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""))
            .put("legalRepName", asylumCase.read(AsylumCaseDefinition.LEGAL_REP_NAME, String.class).orElse(""))
            .put("legalRepFamilyName", asylumCase.read(AsylumCaseDefinition.LEGAL_REP_FAMILY_NAME, String.class).orElse(""))
            .put("linkToOnlineService", iaExUiFrontendUrl)
            .put("editingDate", LocalDate.now().format(DateTimeFormatter.ofPattern("d MMM yyyy")))
            .build();

        return res;
    }
}
