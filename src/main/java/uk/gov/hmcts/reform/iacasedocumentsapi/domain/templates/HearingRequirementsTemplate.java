package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.*;

@Component
public class HearingRequirementsTemplate implements DocumentTemplate<AsylumCase> {

    private final String templateName;

    public HearingRequirementsTemplate(
        @Value("${hearingRequirementsDocument.templateName}") String templateName
    ) {
        this.templateName = templateName;
    }

    public String getName() {
        return templateName;
    }

    @Override
    public Map<String, Object> mapFieldValues(
        CaseDetails<AsylumCase> caseDetails
    ) {
        final AsylumCase asylumCase = caseDetails.getCaseData();

        final Map<String, Object> fieldValues = new HashMap<>();

        fieldValues.put("hmcts", "[userImage:hmcts.png]");
        fieldValues.put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""));
        fieldValues.put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""));
        fieldValues.put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""));
        fieldValues.put("homeOfficeReferenceNumber", asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""));
        fieldValues.put("legalRepReferenceNumber", asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""));
        fieldValues.put("isAppellantAttendingTheHearing", asylumCase.read(IS_APPELLANT_ATTENDING_THE_HEARING, YesOrNo.class).orElse(YesOrNo.NO));
        fieldValues.put("isAppellantGivingOralEvidence", asylumCase.read(IS_APPELLANT_GIVING_ORAL_EVIDENCE, YesOrNo.class).orElse(YesOrNo.NO));
        fieldValues.put("isWitnessesAttending", asylumCase.read(IS_WITNESSES_ATTENDING, YesOrNo.class).orElse(YesOrNo.NO));

        Optional<List<IdValue<WitnessDetails>>> witnessDetails = asylumCase.read(WITNESS_DETAILS);
        fieldValues.put(
            "witnessDetails",
            witnessDetails
                .orElse(Collections.emptyList())
                .stream()
                .map(witnessIdValue -> ImmutableMap.of("witnessDetails", witnessIdValue.getValue().getWitnessName()))
                .collect(Collectors.toList())
        );

        fieldValues.put("isInterpreterServicesNeeded", asylumCase.read(IS_INTERPRETER_SERVICES_NEEDED, YesOrNo.class).orElse(YesOrNo.NO));

        Optional<List<IdValue<InterpreterLanguage>>> interpreterLanguage = asylumCase.read(INTERPRETER_LANGUAGE);
        fieldValues.put(
            "language",
            interpreterLanguage
                .orElse(Collections.emptyList())
                .stream()
                .map(language -> ImmutableMap.of("language", language.getValue().getLanguage()))
                .collect(Collectors.toList())
        );
        fieldValues.put(
            "languageDialect",
            interpreterLanguage
                .orElse(Collections.emptyList())
                .stream()
                .map(languageIdValue -> ImmutableMap.of("languageDialect", languageIdValue.getValue().getLanguageDialect()))
                .collect(Collectors.toList())
        );

        fieldValues.put("isHearingRoomNeeded", asylumCase.read(IS_HEARING_ROOM_NEEDED, YesOrNo.class).orElse(YesOrNo.NO));
        fieldValues.put("isHearingLoopNeeded", asylumCase.read(IS_HEARING_LOOP_NEEDED, YesOrNo.class).orElse(YesOrNo.NO));
        fieldValues.put("physicalOrMentalHealthIssues", asylumCase.read(PHYSICAL_OR_MENTAL_HEALTH_ISSUES, YesOrNo.class).orElse(YesOrNo.NO));
        fieldValues.put("physicalOrMentalHealthIssuesDescription", asylumCase.read(PHYSICAL_OR_MENTAL_HEALTH_ISSUES_DESCRIPTION, String.class).orElse(""));
        fieldValues.put("pastExperiences", asylumCase.read(PAST_EXPERIENCES, YesOrNo.class).orElse(YesOrNo.NO));
        fieldValues.put("pastExperiencesDescription", asylumCase.read(PAST_EXPERIENCES_DESCRIPTION, String.class).orElse(""));
        fieldValues.put("multimediaEvidence", asylumCase.read(MULTIMEDIA_EVIDENCE, YesOrNo.class).orElse(YesOrNo.NO));
        fieldValues.put("multimediaEvidenceDescription", asylumCase.read(MULTIMEDIA_EVIDENCE_DESCRIPTION, String.class).orElse(""));
        fieldValues.put("singleSexCourt", asylumCase.read(SINGLE_SEX_COURT, YesOrNo.class).orElse(YesOrNo.NO));
        fieldValues.put("singleSexCourtType", asylumCase.read(SINGLE_SEX_COURT_TYPE, MaleOrFemale.class).orElse(MaleOrFemale.NONE));
        fieldValues.put("singleSexCourtTypeDescription", asylumCase.read(SINGLE_SEX_COURT_TYPE_DESCRIPTION, String.class).orElse(""));
        fieldValues.put("inCameraCourt", asylumCase.read(IN_CAMERA_COURT, YesOrNo.class).orElse(YesOrNo.NO));
        fieldValues.put("inCameraCourtDescription", asylumCase.read(IN_CAMERA_COURT_DESCRIPTION, String.class).orElse(""));
        fieldValues.put("additionalRequests", asylumCase.read(ADDITIONAL_REQUESTS, YesOrNo.class).orElse(YesOrNo.NO));
        fieldValues.put("additionalRequestsDescription", asylumCase.read(ADDITIONAL_REQUESTS_DESCRIPTION, String.class).orElse(""));
        Optional<List<IdValue<DatesToAvoid>>> datesToAvoid = asylumCase.read(DATES_TO_AVOID);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMM yyyy");

        String dateToAvoid = "dateToAvoid";
        fieldValues.put(
            dateToAvoid,
            datesToAvoid
                .orElse(Collections.emptyList())
                .stream()
                .map(date ->
                    new IdValue<>(
                        date.getId(),
                        new DatesToAvoid(
                            date.getValue().getDateToAvoid() == null ? LocalDate.parse("1900-01-01") : date.getValue().getDateToAvoid(),
                            date.getValue().getDateToAvoidReason()
                        )
                    )
                )
                .map(datesIdValue -> ImmutableMap.of(dateToAvoid, datesIdValue.getValue().getDateToAvoid().format(formatter)))
                .collect(Collectors.toList())
        );

        Optional<YesOrNo> datesToAvoidFlag = asylumCase.read(DATES_TO_AVOID_YES_NO, YesOrNo.class);
        if (datesToAvoidFlag.isPresent()) {
            fieldValues.put("datesToAvoid", datesToAvoidFlag.get());
        } else {
            // old path before introducing DATES_TO_AVOID_YES_NO flag
            if (((List) fieldValues.get(dateToAvoid)).isEmpty()) {
                fieldValues.put("datesToAvoid", YesOrNo.NO);
            } else {
                fieldValues.put("datesToAvoid", YesOrNo.YES);
            }
        }

        fieldValues.put(
            "dateToAvoidReason",
            datesToAvoid
                .orElse(Collections.emptyList())
                .stream()
                .map(date ->
                    new IdValue<>(
                        date.getId(),
                        new DatesToAvoid(
                            date.getValue().getDateToAvoid(),
                            date.getValue().getDateToAvoidReason() == null ? "" : date.getValue().getDateToAvoidReason()
                        )
                    )
                )
                .map(datesIdValue -> ImmutableMap.of("dateToAvoidReason", datesIdValue.getValue().getDateToAvoidReason()))
                .collect(Collectors.toList())
        );

        return fieldValues;
    }
}
