package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.AppealSubmissionDocFieldMapper;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.StringProvider;

@Component
public class InternalOocAppealSubmissionTemplate implements DocumentTemplate<AsylumCase> {

    private final String templateName;
    private final StringProvider stringProvider;

    public InternalOocAppealSubmissionTemplate(
        @Value("${appealSubmissionDocumentInternalOoc.templateName}") String templateName,
        StringProvider stringProvider
    ) {
        this.templateName = templateName;
        this.stringProvider = stringProvider;
    }

    public String getName() {

        return templateName;
    }

    public Map<String, Object> mapFieldValues(
        CaseDetails<AsylumCase> caseDetails
    ) {
        final AppealSubmissionDocFieldMapper fieldMapper =
            new AppealSubmissionDocFieldMapper(stringProvider);

        return fieldMapper.mapFieldValues(caseDetails);
    }

}
