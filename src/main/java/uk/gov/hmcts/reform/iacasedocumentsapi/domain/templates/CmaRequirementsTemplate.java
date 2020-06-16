package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.AppointmentRequirementsFieldMapper;

@Component
public class CmaRequirementsTemplate implements DocumentTemplate<AsylumCase> {

    private final String templateName;

    public CmaRequirementsTemplate(
        @Value("${cmaRequirements.templateName}") String templateName
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

        return fieldMapper.mapFields(asylumCase);
    }
}
