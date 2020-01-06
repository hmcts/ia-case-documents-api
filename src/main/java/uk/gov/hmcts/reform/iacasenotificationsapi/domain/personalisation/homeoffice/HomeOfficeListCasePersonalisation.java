package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.collect.ImmutableMap;

import com.google.common.collect.ImmutableMap.Builder;
import java.util.Collections;
import java.util.Map;

import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.StringProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.BasePersonalisationProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.DateTimeExtractor;

@Service
public class HomeOfficeListCasePersonalisation implements EmailNotificationPersonalisation {

    private final String homeOfficeCaseListedTemplateId;
    private final StringProvider stringProvider;
    private final DateTimeExtractor dateTimeExtractor;
    private final Map<HearingCentre, String> homeOfficeEmailAddresses;

    public HomeOfficeListCasePersonalisation(
        @Value("${govnotify.template.homeOfficeCaseListed}") String homeOfficeCaseListedTemplateId,
        StringProvider stringProvider,
        DateTimeExtractor dateTimeExtractor,
        Map<HearingCentre, String> homeOfficeEmailAddresses
    ) {
        this.homeOfficeCaseListedTemplateId = homeOfficeCaseListedTemplateId;
        this.stringProvider = stringProvider;
        this.dateTimeExtractor = dateTimeExtractor;
        this.homeOfficeEmailAddresses = homeOfficeEmailAddresses;
    }

    @Override
    public String getTemplateId() {
        return homeOfficeCaseListedTemplateId;
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
        return caseId + "_CASE_LISTED_HOME_OFFICE";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

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

        final Builder<String, String> listCaseFields = ImmutableMap
            .<String, String>builder()
            .put("Appeal Ref Number", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("Listing Ref Number", asylumCase.read(ARIA_LISTING_REFERENCE, String.class).orElse(""))
            .put("Home Office Ref Number", asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("Appellant Given Names", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("Appellant Family Name", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("Hearing Date", dateTimeExtractor.extractHearingDate(hearingDateTime))
            .put("Hearing Time", dateTimeExtractor.extractHearingTime(hearingDateTime))
            .put("Hearing Centre Address", hearingCentreAddress);

        BasePersonalisationProvider.buildHearingRequirementsFields(asylumCase, listCaseFields);

        return listCaseFields.build();
    }
}
