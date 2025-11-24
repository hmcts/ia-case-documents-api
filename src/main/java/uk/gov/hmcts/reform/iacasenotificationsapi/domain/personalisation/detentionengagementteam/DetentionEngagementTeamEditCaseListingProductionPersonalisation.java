package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.detentionengagementteam;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.PrisonNomsNumber;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DetentionFacilityEmailService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.DateTimeExtractor;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.HearingDetailsFinder;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

@Slf4j
@Service
public class DetentionEngagementTeamEditCaseListingProductionPersonalisation implements EmailNotificationPersonalisation {

    private final String editCaseListingProductionDetainedTemplateId;
    private final DetentionFacilityEmailService detentionFacilityEmailService;
    private final DateTimeExtractor dateTimeExtractor;
    private final HearingDetailsFinder hearingDetailsFinder;
    private final String subjectPrefix;

    public DetentionEngagementTeamEditCaseListingProductionPersonalisation(
        @Value("${govnotify.template.editCaseListing.detentionEngagementTeam.production.email}") String editCaseListingProductionDetainedTemplateId,
        DetentionFacilityEmailService detentionFacilityEmailService,
        DateTimeExtractor dateTimeExtractor,
        HearingDetailsFinder hearingDetailsFinder,
        @Value("${govnotify.emailPrefix.nonAdaInPerson}") String subjectPrefix
    ) {
        this.editCaseListingProductionDetainedTemplateId = editCaseListingProductionDetainedTemplateId;
        this.detentionFacilityEmailService = detentionFacilityEmailService;
        this.dateTimeExtractor = dateTimeExtractor;
        this.hearingDetailsFinder = hearingDetailsFinder;
        this.subjectPrefix = subjectPrefix;
    }

    @Override
    public String getTemplateId() {
        return editCaseListingProductionDetainedTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(detentionFacilityEmailService.getDetentionEmailAddress(asylumCase));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_DETAINED_EDIT_CASE_LISTING_PRODUCTION_DET";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        boolean isPrison = asylumCase.read(DETENTION_FACILITY, String.class).orElse("").equals("prison");
        String prisonNomsNumber = isPrison
            ? asylumCase.read(PRISON_NOMS, PrisonNomsNumber.class)
                .map(prisonNoms -> "NOMS Ref: " + prisonNoms.getPrison()).orElse("")
            : "";

        return ImmutableMap
            .<String, String>builder()
            .put("subjectPrefix", subjectPrefix)
            .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("nomsRef", prisonNomsNumber)
            .put("hearingDate", dateTimeExtractor.extractHearingDate(hearingDetailsFinder.getHearingDateTime(asylumCase)))
            .put("hearingTime", dateTimeExtractor.extractHearingTime(hearingDetailsFinder.getHearingDateTime(asylumCase)))
            .put("hearingCentreAddress", hearingDetailsFinder.getHearingCentreAddress(asylumCase))
            .put("detentionBuilding", asylumCase.read(DETENTION_BUILDING, String.class).orElse(""))
            .build();
    }
}
