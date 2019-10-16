package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_DATE;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.StringProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.DateTimeExtractor;

@Service
public class CaseOfficerEditListingPersonalisation implements NotificationPersonalisation {

    private final String caseOfficerCaseEditedTemplateId;
    private final String iaCcdFrontendUrl;
    private final StringProvider stringProvider;
    private final DateTimeExtractor dateTimeExtractor;
    private final Map<HearingCentre, String> hearingCentreEmailAddresses;

    public CaseOfficerEditListingPersonalisation(
        @Value("${govnotify.template.caseOfficerCaseEdited}") String caseOfficerCaseEditedTemplateId,
        @Value("${iaCcdFrontendUrl}") String iaCcdFrontendUrl,
        StringProvider stringProvider,
        DateTimeExtractor dateTimeExtractor,
        Map<HearingCentre, String> hearingCentreEmailAddresses
    ) {
        this.caseOfficerCaseEditedTemplateId = caseOfficerCaseEditedTemplateId;
        this.iaCcdFrontendUrl = iaCcdFrontendUrl;
        this.stringProvider = stringProvider;
        this.dateTimeExtractor = dateTimeExtractor;
        this.hearingCentreEmailAddresses = hearingCentreEmailAddresses;
    }

    @Override
    public String getTemplateId() {
        return caseOfficerCaseEditedTemplateId;
    }

    @Override
    public String getEmailAddress(AsylumCase asylumCase) {
        return asylumCase
            .read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)
            .map(it -> Optional.ofNullable(hearingCentreEmailAddresses.get(it))
                .orElseThrow(() -> new IllegalStateException("Hearing centre email address not found: " + it.toString()))
            )
            .orElseThrow(() -> new IllegalStateException("listCaseHearingCentre is not present"));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_CASE_LISTED_CASE_OFFICER";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        Optional<CaseDetails<AsylumCase>> caseDetailsBefore = callback.getCaseDetailsBefore();

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

        String hearingCentreNameBefore = "";
        String oldHearingDate = "";

        if (caseDetailsBefore.isPresent()) {

            final HearingCentre listedHearingCentreBefore =
                caseDetailsBefore.get().getCaseData()
                    .read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)
                    .orElseThrow(() -> new IllegalStateException("listCaseHearingCentre (before) is not present"));

            hearingCentreNameBefore =
                stringProvider
                    .get("hearingCentreName", listedHearingCentreBefore.toString())
                    .orElseThrow(() -> new IllegalStateException("listCaseHearingCentreName (before) is not present"));

            oldHearingDate =
                caseDetailsBefore.get().getCaseData()
                    .read(LIST_CASE_HEARING_DATE, String.class)
                    .orElseThrow(() -> new IllegalStateException("listCaseHearingDate (before) is not present"));
        }

        return
            ImmutableMap
                .<String, String>builder()
                .put("Hyperlink to user’s case list", iaCcdFrontendUrl)
                .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("ariaListingReference", asylumCase.read(AsylumCaseDefinition.ARIA_LISTING_REFERENCE, String.class).orElse(""))
                .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
                .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("oldHearingCentre", hearingCentreNameBefore)
                .put("oldHearingDate", oldHearingDate == null || oldHearingDate.isEmpty() ? "" : dateTimeExtractor.extractHearingDate(oldHearingDate))
                .put("hearingDate", dateTimeExtractor.extractHearingDate(hearingDateTime))
                .put("hearingTime", dateTimeExtractor.extractHearingTime(hearingDateTime))
                .put("hearingCentreAddress", hearingCentreAddress)
                .build();
    }
}
