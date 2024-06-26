package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import static java.util.Collections.emptyList;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.REASONS_FOR_APPEAL_DECISION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.REASONS_FOR_APPEAL_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getAppellantPersonalisationWithoutUserImage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@Component
public class AppealReasonsTemplate implements DocumentTemplate<AsylumCase> {

    private final CustomerServicesProvider customerServicesProvider;

    private final String templateName;

    public AppealReasonsTemplate(
        @Value("${appealReasons.templateName}") String templateName,
        CustomerServicesProvider customerServicesProvider
    ) {
        this.templateName = templateName;
        this.customerServicesProvider = customerServicesProvider;
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

        fieldValues.putAll(getAppellantPersonalisationWithoutUserImage(asylumCase));
        fieldValues.put("appealReasons", asylumCase.read(REASONS_FOR_APPEAL_DECISION, String.class).orElse(""));
        fieldValues.put("customerServicesTelephone", customerServicesProvider.getCustomerServicesTelephone());
        fieldValues.put("customerServicesEmail", customerServicesProvider.getCustomerServicesEmail());

        Optional<List<IdValue<DocumentWithMetadata>>> appealReasonsEvidenceDocuments =
            asylumCase.read(REASONS_FOR_APPEAL_DOCUMENTS);

        final List<String> evidenceFileNames = appealReasonsEvidenceDocuments.orElse(emptyList()).stream()
            .map(IdValue::getValue)
            .map(documentWithMetadata -> documentWithMetadata.getDocument().getDocumentFilename())
            .collect(Collectors.toList());

        final String appealReasonsEvidence = evidenceFileNames.isEmpty() ? "No supporting evidence provided" : StringUtils.join(evidenceFileNames, "\n");

        fieldValues.put("appealReasonsSupportingEvidence", appealReasonsEvidence);

        return fieldValues;
    }
}
