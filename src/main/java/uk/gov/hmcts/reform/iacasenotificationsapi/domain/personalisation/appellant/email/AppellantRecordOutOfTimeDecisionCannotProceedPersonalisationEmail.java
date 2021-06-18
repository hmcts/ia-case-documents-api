package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;


@Service
public class AppellantRecordOutOfTimeDecisionCannotProceedPersonalisationEmail implements EmailNotificationPersonalisation {

    private final String appellantRecordOutOfTimeDecisionCannotProceedEmailTemplateId;
    private final String iaAipFrontendUrl;
    private final RecipientsFinder recipientsFinder;
    private final EmailAddressFinder emailAddressFinder;
    private final CustomerServicesProvider customerServicesProvider;

    public AppellantRecordOutOfTimeDecisionCannotProceedPersonalisationEmail(
            @Value("${govnotify.template.recordOutOfTimeDecision.appellant.cannotProceed.email}") String appellantRecordOutOfTimeDecisionCannotProceedEmailTemplateId,
            @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
            RecipientsFinder recipientsFinder,
            EmailAddressFinder emailAddressFinder,
            CustomerServicesProvider customerServicesProvider
    ) {
        this.appellantRecordOutOfTimeDecisionCannotProceedEmailTemplateId = appellantRecordOutOfTimeDecisionCannotProceedEmailTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.recipientsFinder = recipientsFinder;
        this.emailAddressFinder = emailAddressFinder;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return appellantRecordOutOfTimeDecisionCannotProceedEmailTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.EMAIL);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_RECORD_OUT_OF_TIME_DECISION_CANNOT_PROCEED_AIP_EMAIL";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return
                ImmutableMap
                        .<String, String>builder()
                        .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
                        .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                        .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
                        .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                        .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
                        .put("Hyperlink to service", iaAipFrontendUrl)
                        .put("designated hearing centre", isAppealListed(asylumCase)
                                ? emailAddressFinder.getListCaseHearingCentreEmailAddress(asylumCase)
                                : emailAddressFinder.getHearingCentreEmailAddress(asylumCase))
                        .build();
    }

    protected boolean isAppealListed(AsylumCase asylumCase) {
        final Optional<HearingCentre> appealListed = asylumCase
                .read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class);

        return appealListed.isPresent();
    }

}
