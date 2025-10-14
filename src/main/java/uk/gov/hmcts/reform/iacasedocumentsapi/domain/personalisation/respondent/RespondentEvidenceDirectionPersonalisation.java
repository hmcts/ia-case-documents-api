package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.respondent;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isEjpCase;

import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DirectionFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@Service
public class RespondentEvidenceDirectionPersonalisation implements EmailNotificationPersonalisation {

    private final String respondentEvidenceDirectionTemplateId;
    private final String respondentEvidenceDirectionEjpTemplateId;

    private final String respondentEvidenceDirectionEmailAddress;
    private final String iaExUiFrontendUrl;
    private final DirectionFinder directionFinder;
    private final CustomerServicesProvider customerServicesProvider;

    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;

    public RespondentEvidenceDirectionPersonalisation(
            @Value("${govnotify.template.requestRespondentEvidenceDirection.respondent.email.nonEjp}") String respondentEvidenceDirectionTemplateId,
            @Value("${govnotify.template.requestRespondentEvidenceDirection.respondent.email.ejp}") String respondentEvidenceEjpDirectionTemplateId,
            @Value("${respondentEmailAddresses.respondentEvidenceDirection}") String respondentEvidenceDirectionEmailAddress,
            @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
            DirectionFinder directionFinder,
            CustomerServicesProvider customerServicesProvider) {

        this.respondentEvidenceDirectionTemplateId = respondentEvidenceDirectionTemplateId;
        this.respondentEvidenceDirectionEjpTemplateId = respondentEvidenceEjpDirectionTemplateId;
        this.respondentEvidenceDirectionEmailAddress = respondentEvidenceDirectionEmailAddress;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.directionFinder = directionFinder;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return isEjp(asylumCase)
                ? respondentEvidenceDirectionEjpTemplateId : respondentEvidenceDirectionTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(respondentEvidenceDirectionEmailAddress);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_RESPONDENT_EVIDENCE_DIRECTION";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        Map<String, String> personalisation = new HashMap<>();
        personalisation
                .putAll(getCommonPersonalisationFields(asylumCase));

        if (isEjp(asylumCase)) {
            personalisation.putAll(getPersonalisationForEjp(asylumCase));
        } else {
            personalisation.putAll(getLegalRepFields(asylumCase));
        }
        return personalisation;
    }

    Map<String, String> getPersonalisationForEjp(AsylumCase asylumCase) {
        String legalRepFullName = asylumCase.read(LEGAL_REP_GIVEN_NAME_EJP, String.class).orElse("") + " " + asylumCase.read(LEGAL_REP_FAMILY_NAME_EJP, String.class).orElse("");

        return ImmutableMap
                .<String, String>builder()
                .put("companyName", asylumCase.read(LEGAL_REP_COMPANY_EJP, String.class).orElse(""))
                .put("legalRepName", legalRepFullName)
                .put("legalRepEmail", asylumCase.read(LEGAL_REP_EMAIL_EJP, String.class).orElse(""))
                .put("legalRepReference", asylumCase.read(LEGAL_REP_REFERENCE_EJP, String.class).orElse(""))
                .build();
    }

    Map<String, String> getCommonPersonalisationFields(AsylumCase asylumCase) {
        final Direction direction =
                directionFinder
                        .findFirst(asylumCase, DirectionTag.RESPONDENT_EVIDENCE)
                        .orElseThrow(() -> new IllegalStateException("direction '" + DirectionTag.RESPONDENT_EVIDENCE + "' is not present"));

        final String directionDueDate =
                LocalDate
                        .parse(direction.getDateDue())
                        .format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        return ImmutableMap
                .<String, String>builder()
                .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
                .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix)
                .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("homeOfficeReferenceNumber", asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
                .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("dueDate", directionDueDate)
                .put("linkToOnlineService", iaExUiFrontendUrl)
                .build();
    }

    Map<String, String> getLegalRepFields(AsylumCase asylumCase) {
        AddressUk address = asylumCase.read(LEGAL_REP_COMPANY_ADDRESS, AddressUk.class).orElse(new AddressUk("",
                "",
                "",
                "",
                "",
                "",""));
        String companyAddress = "";

        companyAddress += address.getAddressLine1().orElse("") + " ";
        companyAddress += address.getAddressLine2().orElse("") + " ";
        companyAddress += address.getCounty().orElse("") + " ";
        companyAddress += address.getPostCode().orElse("");

        final boolean hasNoc = asylumCase.read(CHANGE_ORGANISATION_REQUEST_FIELD, ChangeOrganisationRequest.class)
                .map(it -> it.getCaseRoleId() == null)
                .orElse(false);

        String lrName = asylumCase.read(LEGAL_REP_NAME, String.class).orElse("");
        String lrLastName = asylumCase.read(LEGAL_REP_FAMILY_NAME, String.class).orElse("");
        String legalRepName = (lrName + " " + lrLastName).trim();

        return ImmutableMap
                .<String, String>builder()
                .put("companyName", hasNoc ? "" : asylumCase.read(LEGAL_REP_COMPANY, String.class).orElse(""))
                .put("companyAddress", hasNoc ? "" : companyAddress)
                .put("legalRepName", hasNoc ? "" : legalRepName)
                .put("legalRepEmail", hasNoc ? "" : asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class).orElse(""))
                .put("legalRepReference", hasNoc ? "" : asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""))
                .build();
    }

    private boolean isEjp(AsylumCase asylumCase) {
        return isEjpCase(asylumCase) && asylumCase.read(LEGAL_REP_NAME, String.class).isEmpty();
    }
}

