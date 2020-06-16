package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.collect.ImmutableMap;
import java.util.*;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.*;
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

        Map<String, Object>  fieldValues =  fieldMapper.mapFields(asylumCase);

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

        return fieldValues;
    }
}
