package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.Stf24WeeksCaseReviewDocFieldMapper;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.StringProvider;

import java.util.Map;

@Component
public class Stf24WeeksCaseReviewTemplate implements DocumentTemplate<AsylumCase> {

    private final String templateName;
    private final StringProvider stringProvider;

    public Stf24WeeksCaseReviewTemplate(
            @Value("${stf24WeeksCaseReview.templateName}") String templateName,
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

        final Stf24WeeksCaseReviewDocFieldMapper fieldMapper =
                new Stf24WeeksCaseReviewDocFieldMapper(stringProvider);

        return fieldMapper.mapFieldValues(caseDetails);
    }
}
