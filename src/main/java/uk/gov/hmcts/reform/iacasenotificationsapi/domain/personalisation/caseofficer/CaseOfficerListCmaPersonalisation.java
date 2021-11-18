package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.DateTimeExtractor;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.HearingDetailsFinder;

@Service
public class CaseOfficerListCmaPersonalisation implements EmailNotificationPersonalisation {

    private final String listCmaCaseOfficerTemplateId;
    private final String iaExUiFrontendUrl;
    private final DateTimeExtractor dateTimeExtractor;
    private final HearingDetailsFinder hearingDetailsFinder;
    private final EmailAddressFinder emailAddressFinder;
    private final FeatureToggler featureToggler;


    public CaseOfficerListCmaPersonalisation(
            @NotNull(message = "listCmaCaseOfficerTemplateId cannot be null") @Value("${govnotify.template.listCma.caseOfficer.email}") String listCmaCaseOfficerTemplateId,
            @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
            DateTimeExtractor dateTimeExtractor,
            EmailAddressFinder emailAddressFinder,
            HearingDetailsFinder hearingDetailsFinder,

            FeatureToggler featureToggler) {
        this.listCmaCaseOfficerTemplateId = listCmaCaseOfficerTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;

        this.dateTimeExtractor = dateTimeExtractor;
        this.emailAddressFinder = emailAddressFinder;
        this.hearingDetailsFinder = hearingDetailsFinder;

        this.featureToggler = featureToggler;
    }

    @Override
    public String getTemplateId() {
        return listCmaCaseOfficerTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return featureToggler.getValue("tcw-notifications-feature", false)
                ? Collections.singleton(emailAddressFinder.getHearingCentreEmailAddress(asylumCase))
                : Collections.emptySet();
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_LIST_CMA_CASE_OFFICER_EMAIL";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase cannot be null");

        return
            ImmutableMap
                .<String, String>builder()
                .put("Appeal Ref Number", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("hearingDate", dateTimeExtractor.extractHearingDate(hearingDetailsFinder.getHearingDateTime(asylumCase)))
                .put("hearingTime", dateTimeExtractor.extractHearingTime(hearingDetailsFinder.getHearingDateTime(asylumCase)))
                .put("hearingCentreAddress", hearingDetailsFinder.getHearingCentreAddress(asylumCase))
                .put("Hyperlink to service", iaExUiFrontendUrl)
                .build();
    }
}
