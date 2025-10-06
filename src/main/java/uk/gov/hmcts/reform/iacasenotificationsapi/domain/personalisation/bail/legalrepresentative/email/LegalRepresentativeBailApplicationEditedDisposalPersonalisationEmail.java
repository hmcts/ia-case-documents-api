package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.legalrepresentative.email;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.legalrepresentative.LegalRepresentativeBailEmailNotificationPersonalisation;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotNull;

import static java.util.Objects.requireNonNull;

@Service
public class LegalRepresentativeBailApplicationEditedDisposalPersonalisationEmail implements LegalRepresentativeBailEmailNotificationPersonalisation {

    private final String bailApplicationEditedDisposalLegalRepresentativeTemplateId;
    private final String iaExUiFrontendUrl;
    private final UserDetailsProvider userDetailsProvider;

    public LegalRepresentativeBailApplicationEditedDisposalPersonalisationEmail(
        @NotNull(message = "bailApplicationEditedDisposalLegalRepresentativeTemplateId cannot be null")
        @Value("${govnotify.bail.template.editApplication.disposal.email}") String bailApplicationEditedDisposalLegalRepresentativeTemplateId,
        @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
        UserDetailsProvider userDetailsProvider
    ) {
        this.bailApplicationEditedDisposalLegalRepresentativeTemplateId = bailApplicationEditedDisposalLegalRepresentativeTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.userDetailsProvider = userDetailsProvider;
    }

    @Override
    public Set<String> getRecipientsList(BailCase bailCase) {
        return Collections.singleton(userDetailsProvider.getUserDetails().getEmailAddress());
    }

    @Override
    public String getTemplateId() {
        return bailApplicationEditedDisposalLegalRepresentativeTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_BAIL_APPLICATION_EDITED_DISPOSAL_LEGAL_REPRESENTATIVE";
    }

    @Override
    public Map<String, String> getPersonalisation(BailCase bailCase) {
        requireNonNull(bailCase, "bailCase must not be null");

        return ImmutableMap
            .<String, String>builder()
            .put("legalRepReference", bailCase.read(BailCaseFieldDefinition.LEGAL_REP_REFERENCE, String.class).orElse(""))
            .put("legalRepName", bailCase.read(BailCaseFieldDefinition.LEGAL_REP_NAME, String.class).orElse(""))
            .put("legalRepFamilyName", bailCase.read(BailCaseFieldDefinition.LEGAL_REP_FAMILY_NAME, String.class).orElse(""))
            .put("linkToOnlineService", iaExUiFrontendUrl)
            .put("editingDate", LocalDate.now().format(DateTimeFormatter.ofPattern("d MMM yyyy")))
            .build();
    }
}
