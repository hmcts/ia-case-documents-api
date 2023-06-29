package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.WitnessDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.AppointmentRequirementsFieldMapper;

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

        return fieldValues;
    }

    private String formatWitnessDetails(WitnessDetails details) {
        String givenNames = details.getWitnessName();
        String familyName = details.getWitnessFamilyName();

        return familyName == null || familyName.isEmpty()
                ? givenNames
                : String.format("%s %s", givenNames, familyName);
    }
}
