package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

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
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.DateTimeExtractor;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@Service
public class LegalRepresentativeListCasePersonalisation implements EmailNotificationPersonalisation {

    private final String legalRepresentativeCaseListedTemplateId;
    private final StringProvider stringProvider;
    private final DateTimeExtractor dateTimeExtractor;

    public LegalRepresentativeListCasePersonalisation(
        @Value("${govnotify.template.legalRepresentativeCaseListed}") String legalRepresentativeCaseListedTemplateId,
        StringProvider stringProvider,
        DateTimeExtractor dateTimeExtractor
    ) {
        this.legalRepresentativeCaseListedTemplateId = legalRepresentativeCaseListedTemplateId;
        this.stringProvider = stringProvider;
        this.dateTimeExtractor = dateTimeExtractor;
    }

    @Override
    public String getTemplateId() {
        return legalRepresentativeCaseListedTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(asylumCase
            .read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class)
            .orElseThrow(() -> new IllegalStateException("legalRepresentativeEmailAddress is not present")));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_CASE_LISTED_LEGAL_REPRESENTATIVE";
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
            .put("Legal Rep Ref Number", asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""))
            .put("Appellant Given Names", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("Appellant Family Name", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("Hearing Date", dateTimeExtractor.extractHearingDate(hearingDateTime))
            .put("Hearing Time", dateTimeExtractor.extractHearingTime(hearingDateTime))
            .put("Hearing Centre Address", hearingCentreAddress);

        PersonalisationProvider.buildHearingRequirementsFields(asylumCase, listCaseFields);

        return listCaseFields.build();

    }
}
