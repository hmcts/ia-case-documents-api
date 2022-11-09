package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.*;

@Service
public class AppointmentRequirementsFieldMapper {

    public AppointmentRequirementsFieldMapper() {
    }

    public Map<String, Object> mapFields(AsylumCase asylumCase) {

        final Map<String, Object> fieldValues = new HashMap<>();

        fieldValues.put("hmcts", "[userImage:hmcts.png]");
        fieldValues.put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""));
        fieldValues.put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""));
        fieldValues.put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""));
        fieldValues.put("homeOfficeReferenceNumber", asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""));
        fieldValues.put("legalRepReferenceNumber", asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""));

        YesOrNo interpreterServicesNeeded = asylumCase.read(IS_INTERPRETER_SERVICES_NEEDED, YesOrNo.class).orElse(YesOrNo.NO);
        fieldValues.put("isInterpreterServicesNeeded", interpreterServicesNeeded);

        Optional<List<IdValue<InterpreterLanguage>>> interpreterLanguagesOptional = asylumCase.read(INTERPRETER_LANGUAGE);
        List<Map<String, String>> interpreterLanguages = interpreterLanguagesOptional
            .orElse(Collections.emptyList())
            .stream()
            .filter(languageIdValue -> languageIdValue.getValue().getLanguage() != null)
            .map(languageIdValue -> ImmutableMap.of("language", languageIdValue.getValue().getLanguage()))
            .collect(Collectors.toList());

        if (interpreterServicesNeeded.equals(YesOrNo.YES) && interpreterLanguages.isEmpty()) {
            throw new IllegalStateException("Interpreter language is required for requested interpreter services");
        }
        if (interpreterServicesNeeded.equals(YesOrNo.NO)) {
            fieldValues.put("language", Collections.emptyList());
            fieldValues.put("languageDialect", Collections.emptyList());
        } else {
            fieldValues.put("language", interpreterLanguages);
            fieldValues.put(
                "languageDialect",
                interpreterLanguagesOptional
                    .orElse(Collections.emptyList())
                    .stream()
                    .filter(languageIdValue -> languageIdValue.getValue().getLanguageDialect() != null)
                    .map(languageIdValue -> ImmutableMap.of("languageDialect", languageIdValue.getValue().getLanguageDialect()))
                    .collect(Collectors.toList())
            );
        }

        fieldValues.put("isHearingRoomNeeded", asylumCase.read(IS_HEARING_ROOM_NEEDED, YesOrNo.class).orElse(YesOrNo.NO));
        fieldValues.put("isHearingLoopNeeded", asylumCase.read(IS_HEARING_LOOP_NEEDED, YesOrNo.class).orElse(YesOrNo.NO));
        fieldValues.put("physicalOrMentalHealthIssues", asylumCase.read(PHYSICAL_OR_MENTAL_HEALTH_ISSUES, YesOrNo.class).orElse(YesOrNo.NO));
        fieldValues.put("physicalOrMentalHealthIssuesDescription", asylumCase.read(PHYSICAL_OR_MENTAL_HEALTH_ISSUES_DESCRIPTION, String.class).orElse(""));
        fieldValues.put("pastExperiences", asylumCase.read(PAST_EXPERIENCES, YesOrNo.class).orElse(YesOrNo.NO));
        fieldValues.put("pastExperiencesDescription", asylumCase.read(PAST_EXPERIENCES_DESCRIPTION, String.class).orElse(""));
        fieldValues.put("isOutOfCountryEnabled", asylumCase.read(IS_OUT_OF_COUNTRY_ENABLED, YesOrNo.class).orElse(YesOrNo.NO));
        fieldValues.put("remoteVideoCall", asylumCase.read(REMOTE_VIDEO_CALL, YesOrNo.class).orElse(YesOrNo.NO));
        fieldValues.put("remoteVideoCallDescription", asylumCase.read(REMOTE_VIDEO_CALL_DESCRIPTION, String.class).orElse(""));
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
