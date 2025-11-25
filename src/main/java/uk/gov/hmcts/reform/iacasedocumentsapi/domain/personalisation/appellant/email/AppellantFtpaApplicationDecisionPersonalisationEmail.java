package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.email;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_IN_UK;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.ARIA_LISTING_REFERENCE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.FTPA_APPLICANT_TYPE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.YES;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ApplicantType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.utils.FtpaNotificationPersonalisationUtil;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@Service
public class AppellantFtpaApplicationDecisionPersonalisationEmail implements EmailNotificationPersonalisation, FtpaNotificationPersonalisationUtil {

    private final String ftpaRespondentDecisionGrantedPartiallyGrantedToAppellantEmailTemplateId;
    private final String ftpaRespondentDecisionNotAdmittedToAppellantEmailTemplateId;
    private final String ftpaRespondentDecisionRefusedToAppellantEmailTemplateId;
    private final String ftpaAppellantDecisionGrantedToAppellantEmailTemplateId;
    private final String ftpaAppellantDecisionPartiallyGrantedToAppellantEmailTemplateId;
    private final String ftpaAppellantDecisionNotAdmittedToAppellantEmailTemplateId;
    private final String ftpaAppellantDecisionRefusedToAppellantEmailTemplateId;
    private final String iaAipFrontendUrl;
    private final long oocDays;
    private final long inCountryDays;
    private final RecipientsFinder recipientsFinder;
    private final CustomerServicesProvider customerServicesProvider;

    public AppellantFtpaApplicationDecisionPersonalisationEmail(
        @Value("${govnotify.template.applicationGranted.otherParty.citizen.email}") String ftpaRespondentDecisionGrantedPartiallyGrantedToAppellantEmailTemplateId,
        @Value("${govnotify.template.applicationNotAdmitted.otherParty.citizen.email}") String ftpaRespondentDecisionNotAdmittedToAppellantEmailTemplateId,
        @Value("${govnotify.template.applicationRefused.otherParty.citizen.email}") String ftpaRespondentDecisionRefusedToAppellantEmailTemplateId,
        @Value("${govnotify.template.applicationGranted.applicant.citizen.email}") String ftpaAppellantDecisionGrantedToAppellantEmailTemplateId,
        @Value("${govnotify.template.applicationPartiallyGranted.applicant.citizen.email}") String ftpaAppellantDecisionPartiallyGrantedToAppellantEmailTemplateId,
        @Value("${govnotify.template.applicationNotAdmitted.applicant.citizen.email}") String ftpaAppellantDecisionNotAdmittedToAppellantEmailTemplateId,
        @Value("${govnotify.template.applicationRefused.applicant.citizen.email}") String ftpaAppellantDecisionRefusedToAppellantEmailTemplateId,
        @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
        @Value("${ftpaOutOfCountryDays}") long oocDays,
        @Value("${ftpaInCountryDays}") long inCountryDays,
        RecipientsFinder recipientsFinder,
        CustomerServicesProvider customerServicesProvider
    ) {
        this.ftpaRespondentDecisionGrantedPartiallyGrantedToAppellantEmailTemplateId = ftpaRespondentDecisionGrantedPartiallyGrantedToAppellantEmailTemplateId;
        this.ftpaRespondentDecisionNotAdmittedToAppellantEmailTemplateId = ftpaRespondentDecisionNotAdmittedToAppellantEmailTemplateId;
        this.ftpaRespondentDecisionRefusedToAppellantEmailTemplateId = ftpaRespondentDecisionRefusedToAppellantEmailTemplateId;
        this.ftpaAppellantDecisionGrantedToAppellantEmailTemplateId = ftpaAppellantDecisionGrantedToAppellantEmailTemplateId;
        this.ftpaAppellantDecisionPartiallyGrantedToAppellantEmailTemplateId = ftpaAppellantDecisionPartiallyGrantedToAppellantEmailTemplateId;
        this.ftpaAppellantDecisionNotAdmittedToAppellantEmailTemplateId = ftpaAppellantDecisionNotAdmittedToAppellantEmailTemplateId;
        this.ftpaAppellantDecisionRefusedToAppellantEmailTemplateId = ftpaAppellantDecisionRefusedToAppellantEmailTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.oocDays = oocDays;
        this.inCountryDays = inCountryDays;
        this.recipientsFinder = recipientsFinder;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {

        String applicantType = asylumCase
            .read(FTPA_APPLICANT_TYPE, ApplicantType.class)
            .map(ApplicantType::getValue)
            .orElseThrow(() -> new IllegalStateException("ftpaApplicantType is not present"));;

        switch (getDecisionOutcomeType(asylumCase)) {
            case FTPA_GRANTED:
                return applicantType.equals(APPELLANT_APPLICANT)
                    ? ftpaAppellantDecisionGrantedToAppellantEmailTemplateId
                    : ftpaRespondentDecisionGrantedPartiallyGrantedToAppellantEmailTemplateId;
            case FTPA_PARTIALLY_GRANTED:
                return applicantType.equals(APPELLANT_APPLICANT)
                    ? ftpaAppellantDecisionPartiallyGrantedToAppellantEmailTemplateId
                    : ftpaRespondentDecisionGrantedPartiallyGrantedToAppellantEmailTemplateId;
            case FTPA_REFUSED:
                return applicantType.equals(APPELLANT_APPLICANT)
                    ? ftpaAppellantDecisionRefusedToAppellantEmailTemplateId
                    : ftpaRespondentDecisionRefusedToAppellantEmailTemplateId;
            default:
                return applicantType.equals(APPELLANT_APPLICANT)
                    ? ftpaAppellantDecisionNotAdmittedToAppellantEmailTemplateId
                    : ftpaRespondentDecisionNotAdmittedToAppellantEmailTemplateId;
        }
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.EMAIL);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_FTPA_APPLICATION_DECISION_TO_APPELLANT_EMAIL";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        String listingReferenceLine = asylumCase.read(ARIA_LISTING_REFERENCE, String.class)
            .map(ref -> "\nListing reference: " + ref)
            .orElse("");

        return
            ImmutableMap
                .<String, String>builder()
                .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
                .put("listingReferenceLine", listingReferenceLine)
                .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("homeOfficeReferenceNumber", asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
                .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("linkToService", iaAipFrontendUrl)
                .put("applicationDecision", ftpaDecisionVerbalization(getDecisionOutcomeType(asylumCase)))
                .put("dueDate", asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)
                    .map(inUk -> inUk.equals(YES) ? dueDate(inCountryDays) : dueDate(oocDays)).orElse(""))
                .build();
    }

}
