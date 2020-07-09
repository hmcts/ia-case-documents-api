package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.StringProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.DateTimeExtractor;

@Service
public class HomeOfficeListCmaPersonalisation implements EmailNotificationPersonalisation {

    private final String listCmaAdminOfficerTemplateId;
    private final String iaExUiFrontendUrl;
    private final Map<HearingCentre, String> homeOfficeEmailAddresses;
    private final StringProvider stringProvider;
    private final DateTimeExtractor dateTimeExtractor;


    public HomeOfficeListCmaPersonalisation(
        @NotNull(message = "listCmaAdminOfficerTemplateId cannot be null") @Value("${govnotify.template.listCma.homeOffice.email}") String listCmaAdminOfficerTemplateId,
        @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
        StringProvider stringProvider,
        DateTimeExtractor dateTimeExtractor,
        Map<HearingCentre, String> homeOfficeEmailAddresses

    ) {
        this.listCmaAdminOfficerTemplateId = listCmaAdminOfficerTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.stringProvider = stringProvider;
        this.dateTimeExtractor = dateTimeExtractor;
        this.homeOfficeEmailAddresses = homeOfficeEmailAddresses;

    }

    @Override
    public String getTemplateId() {
        return listCmaAdminOfficerTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        final HearingCentre listCaseHearingCentre =
            asylumCase
                .read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)
                .orElseThrow(() -> new IllegalStateException("listCaseHearingCentre is not present"));

        final String hearingCentreEmailAddress =
            homeOfficeEmailAddresses
                .get(listCaseHearingCentre);

        if (hearingCentreEmailAddress == null) {
            throw new IllegalStateException("Hearing centre email address not found: " + listCaseHearingCentre.toString());
        }

        return Collections.singleton(hearingCentreEmailAddress);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_LIST_CMA_HOME_OFFICE_EMAIL";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase cannot be null");
        final HearingCentre listedHearingCentre =
            asylumCase
                .read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)
                .orElseThrow(() -> new IllegalStateException("listCaseHearingCentre is not present"));

        final String hearingCentreAddress =
            stringProvider
                .get("hearingCentreAddress", listedHearingCentre.toString())
                .orElseThrow(() -> new IllegalStateException("hearingCentreAddress is not present"));

        final String hearingDateTime =
            asylumCase
                .read(AsylumCaseDefinition.LIST_CASE_HEARING_DATE, String.class)
                .orElseThrow(() -> new IllegalStateException("hearingDateTime is not present"));

        return
            ImmutableMap
                .<String, String>builder()
                .put("Appeal Ref Number", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("hearingDate", dateTimeExtractor.extractHearingDate(hearingDateTime))
                .put("hearingTime", dateTimeExtractor.extractHearingTime(hearingDateTime))
                .put("hearingCentreAddress", hearingCentreAddress)
                .put("Hyperlink to service", iaExUiFrontendUrl)
                .build();
    }
}
