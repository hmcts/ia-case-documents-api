package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_OUT_OF_COUNTRY;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_INTERPRETER_SIGN_LANGUAGE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_INTERPRETER_SPOKEN_LANGUAGE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.IS_ANY_WITNESS_INTERPRETER_REQUIRED;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.IS_APPELLANT_ATTENDING_THE_HEARING;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.IS_APPELLANT_GIVING_ORAL_EVIDENCE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.IS_EVIDENCE_FROM_OUTSIDE_UK_IN_COUNTRY;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.IS_EVIDENCE_FROM_OUTSIDE_UK_OOC;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.IS_WITNESSES_ATTENDING;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.WITNESS_DETAILS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.InterpreterLanguagesUtils.WITNESS_N_INTERPRETER_SIGN_LANGUAGE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.InterpreterLanguagesUtils.WITNESS_N_INTERPRETER_SPOKEN_LANGUAGE;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.InterpreterLanguageRefData;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.WitnessInterpreterLanguageInformation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.WitnessDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.AppointmentRequirementsFieldMapper;

@Component
public class HearingRequirementsTemplate implements DocumentTemplate<AsylumCase> {

    private final String templateName;
    protected static final String SPOKEN_LANGUAGE = "Spoken language Interpreter";
    protected static final String SIGN_LANGUAGE = "Sign language Interpreter";

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

        final AppointmentRequirementsFieldMapper fieldMapper = new AppointmentRequirementsFieldMapper();

        Map<String, Object> fieldValues = fieldMapper.mapFields(asylumCase);

        fieldValues.put("appealOutOfCountry", asylumCase.read(APPEAL_OUT_OF_COUNTRY, YesOrNo.class).orElse(YesOrNo.NO));
        fieldValues.put("isEvidenceFromOutsideUkOoc", asylumCase.read(IS_EVIDENCE_FROM_OUTSIDE_UK_OOC, YesOrNo.class).orElse(YesOrNo.NO));
        fieldValues.put("isEvidenceFromOutsideUkInCountry", asylumCase.read(IS_EVIDENCE_FROM_OUTSIDE_UK_IN_COUNTRY, YesOrNo.class).orElse(YesOrNo.NO));
        fieldValues.put("isAppellantAttendingTheHearing", asylumCase.read(IS_APPELLANT_ATTENDING_THE_HEARING, YesOrNo.class).orElse(YesOrNo.NO));
        fieldValues.put("isAppellantGivingOralEvidence", asylumCase.read(IS_APPELLANT_GIVING_ORAL_EVIDENCE, YesOrNo.class).orElse(YesOrNo.NO));
        fieldValues.put("isWitnessesAttending", asylumCase.read(IS_WITNESSES_ATTENDING, YesOrNo.class).orElse(YesOrNo.NO));

        Optional<List<IdValue<WitnessDetails>>> witnessDetails = asylumCase.read(WITNESS_DETAILS);
        fieldValues.put(
                "witnessDetails",
                witnessDetails
                        .orElse(Collections.emptyList())
                        .stream()
                        .map(witnessIdValue ->
                                ImmutableMap.of("witnessDetails", formatWitnessDetails(witnessIdValue.getValue())))
                        .collect(Collectors.toList())
        );

        setAppellantInterpreterLanguage(asylumCase, fieldValues);
        fieldValues.put("isAnyWitnessInterpreterRequired", asylumCase.read(IS_ANY_WITNESS_INTERPRETER_REQUIRED, YesOrNo.class).orElse(YesOrNo.NO));
        setWitnessInterpreterLanguage(asylumCase, fieldValues);

        return fieldValues;
    }

    private String formatWitnessDetails(WitnessDetails details) {
        String givenNames = details.getWitnessName();
        String familyName = details.getWitnessFamilyName();

        return familyName == null || familyName.isEmpty()
                ? givenNames
                : String.format("%s %s", givenNames, familyName);
    }

    private void setWitnessInterpreterLanguage(AsylumCase asylumCase, Map<String, Object> fieldValues) {
        // set witness interpreter language information
        Optional<List<IdValue<WitnessDetails>>> witnessesOptional = asylumCase.read(WITNESS_DETAILS);
        List<IdValue<WitnessDetails>> witnesses = witnessesOptional.orElse(Collections.emptyList());

        List<WitnessInterpreterLanguageInformation> witnessInterpreterLanguageInformationList = new ArrayList<>();

        for (int i = 0; i < witnesses.size(); i++) {

            Optional<InterpreterLanguageRefData> witnessSpokenInterpreterLanguage = asylumCase.read(WITNESS_N_INTERPRETER_SPOKEN_LANGUAGE.get(i), InterpreterLanguageRefData.class)
                    .filter(language -> language.getLanguageRefData() != null || language.getLanguageManualEntryDescription() != null);
            Optional<InterpreterLanguageRefData> witnessSignInterpreterLanguage = asylumCase.read(WITNESS_N_INTERPRETER_SIGN_LANGUAGE.get(i), InterpreterLanguageRefData.class)
                    .filter(language -> language.getLanguageRefData() != null || language.getLanguageManualEntryDescription() != null);

            if (witnessSpokenInterpreterLanguage.isPresent() || witnessSignInterpreterLanguage.isPresent()) {
                StringBuilder witnessInterpreterLanguageDisplayString = new StringBuilder();
                witnessInterpreterLanguageDisplayString.append(constructInterpreterLanguageString(witnessSpokenInterpreterLanguage, SPOKEN_LANGUAGE));
                witnessInterpreterLanguageDisplayString.append(constructInterpreterLanguageString(witnessSignInterpreterLanguage, SIGN_LANGUAGE));

                witnessInterpreterLanguageInformationList.add(
                    new WitnessInterpreterLanguageInformation(
                        witnesses.get(i).getValue().buildWitnessFullName(),
                        witnessInterpreterLanguageDisplayString.toString()));
            }
        }

        fieldValues.put("witnessInterpreterInformationList", witnessInterpreterLanguageInformationList);
    }

    private void setAppellantInterpreterLanguage(AsylumCase asylumCase, Map<String, Object> fieldValues) {
        // set appellant interpreter language information
        Optional<InterpreterLanguageRefData> appellantSpokenInterpreterLanguage = asylumCase.read(APPELLANT_INTERPRETER_SPOKEN_LANGUAGE, InterpreterLanguageRefData.class)
                .filter(language -> language.getLanguageRefData() != null || language.getLanguageManualEntryDescription() != null);
        Optional<InterpreterLanguageRefData> appellantSignInterpreterLanguage = asylumCase.read(APPELLANT_INTERPRETER_SIGN_LANGUAGE, InterpreterLanguageRefData.class)
                .filter(language -> language.getLanguageRefData() != null || language.getLanguageManualEntryDescription() != null);

        StringBuilder appellantInterpreterLanguageDisplayString = new StringBuilder();
        appellantInterpreterLanguageDisplayString.append(constructInterpreterLanguageString(appellantSpokenInterpreterLanguage, SPOKEN_LANGUAGE));
        appellantInterpreterLanguageDisplayString.append(constructInterpreterLanguageString(appellantSignInterpreterLanguage, SIGN_LANGUAGE));

        fieldValues.put("appellantInterpreterLanguage", appellantInterpreterLanguageDisplayString.toString());
    }

    private StringBuilder constructInterpreterLanguageString(Optional<InterpreterLanguageRefData> interpreterLanguageRefData, String typeOfLanguage) {
        StringBuilder interpreterLanguageString = new StringBuilder();
        interpreterLanguageRefData.ifPresent(language -> {
            interpreterLanguageString.append(typeOfLanguage).append(": ");
            if (language.getLanguageRefData() != null) {
                interpreterLanguageString.append(language.getLanguageRefData().getValue().getLabel() + "\n");
            } else if (language.getLanguageManualEntry() != null && !language.getLanguageManualEntry().isEmpty()) {
                interpreterLanguageString.append(language.getLanguageManualEntryDescription() + "\n");
            }
        });
        return interpreterLanguageString;
    }
}
