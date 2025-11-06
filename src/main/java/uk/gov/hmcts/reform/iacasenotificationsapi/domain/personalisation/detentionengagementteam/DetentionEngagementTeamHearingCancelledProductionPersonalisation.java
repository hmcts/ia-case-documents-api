package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.detentionengagementteam;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.PrisonNomsNumber;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DetentionFacilityEmailService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.DateTimeExtractor;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.HearingDetailsFinder;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.DETENTION_BUILDING;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.DETENTION_FACILITY;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.PRISON_NOMS;

@Slf4j
@Service
public class DetentionEngagementTeamHearingCancelledProductionPersonalisation implements EmailNotificationPersonalisation {

    private final String hearingCancelledProductionDetainedTemplateId;
    private final DetentionFacilityEmailService detentionFacilityEmailService;
    private final DateTimeExtractor dateTimeExtractor;
    private final HearingDetailsFinder hearingDetailsFinder;
    private final String subjectPrefix;

    public DetentionEngagementTeamHearingCancelledProductionPersonalisation(
        @Value("${govnotify.template.hearingCancelled.detentionEngagementTeam.production.email}") String hearingCancelledProductionDetainedTemplateId,
        DetentionFacilityEmailService detentionFacilityEmailService,
        DateTimeExtractor dateTimeExtractor,
        HearingDetailsFinder hearingDetailsFinder,
        @Value("${govnotify.emailPrefix.nonAdaInPerson}") String subjectPrefix
    ) {
        this.hearingCancelledProductionDetainedTemplateId = hearingCancelledProductionDetainedTemplateId;
        this.detentionFacilityEmailService = detentionFacilityEmailService;
        this.dateTimeExtractor = dateTimeExtractor;
        this.hearingDetailsFinder = hearingDetailsFinder;
        this.subjectPrefix = subjectPrefix;
    }

    @Override
    public String getTemplateId() {
        return hearingCancelledProductionDetainedTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(detentionFacilityEmailService.getDetentionEmailAddress(asylumCase));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_DETAINED_HEARING_CANCELLED_PRODUCTION_DET";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "asylumCase must not be null");

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        Optional<CaseDetails<AsylumCase>> caseDetailsBefore = callback.getCaseDetailsBefore();

        String hearingDate;
        String hearingTime;
        String hearingCentreAddress;
        if (caseDetailsBefore.isPresent()) {
            AsylumCase asylumCaseBefore = caseDetailsBefore.get().getCaseData();
            hearingDate = dateTimeExtractor.extractHearingDate(hearingDetailsFinder.getHearingDateTime(asylumCaseBefore));
            hearingTime = dateTimeExtractor.extractHearingTime(hearingDetailsFinder.getHearingDateTime(asylumCaseBefore));
            hearingCentreAddress = hearingDetailsFinder.getHearingCentreAddress(asylumCaseBefore);
        } else {
            hearingDate = "";
            hearingTime = "";
            hearingCentreAddress = "";
        }

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
            .put("hearingDate", hearingDate)
            .put("hearingTime", hearingTime)
            .put("hearingCentreAddress", hearingCentreAddress)
            .put("detentionBuilding", asylumCase.read(DETENTION_BUILDING, String.class).orElse(""))
            .build();
    }
}
