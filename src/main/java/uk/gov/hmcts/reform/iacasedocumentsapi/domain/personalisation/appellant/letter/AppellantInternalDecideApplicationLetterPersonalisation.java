package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.letter;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.DECIDE_AN_APPLICATION_ID;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.MAKE_AN_APPLICATIONS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getLegalRepAddressInCountryOrOoc;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.MakeAnApplication;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.MakeAnApplicationTypes;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.LetterNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.utils.WhatHappensNextContentUtils;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@Service
public class AppellantInternalDecideApplicationLetterPersonalisation implements LetterNotificationPersonalisation {

    private final String appellantInternalCaseDecideApplicationLetterTemplateId;
    private final CustomerServicesProvider customerServicesProvider;

    public AppellantInternalDecideApplicationLetterPersonalisation(
        @Value("${govnotify.template.decideAnApplication.otherParty.appellant.letter}") String appellantInternalCaseDecideApplicationLetterTemplateId,
        CustomerServicesProvider customerServicesProvider) {
        this.appellantInternalCaseDecideApplicationLetterTemplateId = appellantInternalCaseDecideApplicationLetterTemplateId;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId() {
        return appellantInternalCaseDecideApplicationLetterTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return hasBeenSubmittedByAppellantInternalCase(asylumCase) ?
            getAppellantAddressInCountryOrOoc(asylumCase) : getLegalRepAddressInCountryOrOoc(asylumCase);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_INTERNAL_DECIDE_APPLICATION_LETTER";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        MakeAnApplication application = getMakeAnApplication(asylumCase).orElseThrow(() -> new IllegalStateException("Decide application is missing"));
        String nextSteps = WhatHappensNextContentUtils.getWhatHappensNextContent(
            MakeAnApplicationTypes.from(application.getType()).orElseThrow(() -> new IllegalStateException("Invalid MakeAnApplicationType")),
            true, application.getDecision(), null);
        if (nextSteps.equals("Unknown")) {
            throw new IllegalStateException("Invalid MakeAnApplicationType: Couldn't find next steps.");
        }

        boolean isRefused = application.getDecision().equals("Refused");
        String decisionStr = isRefused ? "refuse" : "grant";
        ImmutableMap.Builder<String, String> personalizationBuilder = ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("decisionMaker", application.getDecisionMaker())
            .put("decision", decisionStr)
            .put("applicationType", application.getType())
            .put("decisionReason", application.getDecisionReason())
            .put("nextSteps", nextSteps)
            .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""));

        List<String> address =  getAppellantOrLegalRepAddressLetterPersonalisation(asylumCase);

        for (int i = 0; i < address.size(); i++) {
            personalizationBuilder.put("address_line_" + (i + 1), address.get(i));
        }
        return personalizationBuilder.build();
    }

    private Optional<MakeAnApplication> getMakeAnApplication(AsylumCase asylumCase) {
        String id = asylumCase.read(DECIDE_AN_APPLICATION_ID, String.class).orElse("");
        Optional<List<IdValue<MakeAnApplication>>> mayBeMakeAnApplications = asylumCase.read(MAKE_AN_APPLICATIONS);

        //get application from list of applications where application id matches to id
        return mayBeMakeAnApplications
            .flatMap(list -> list.stream()
                .filter(application -> application.getId().equals(id))
                .findFirst()
                .map(IdValue::getValue));

    }
}
