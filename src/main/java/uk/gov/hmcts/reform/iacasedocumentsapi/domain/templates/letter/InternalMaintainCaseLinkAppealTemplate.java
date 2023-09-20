package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.CASE_LINKS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getAppellantPersonalisation;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.DateUtils.formatDateForNotificationAttachmentDocument;

import java.time.LocalDate;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.caselinking.CaseLink;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.DocumentTemplate;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@Component
public class InternalMaintainCaseLinkAppealTemplate implements DocumentTemplate<AsylumCase> {

    private final String templateName;
    private final CustomerServicesProvider customerServicesProvider;
    private static final String reasonKey = "reason";
    private static final Map<String, String> reasons = Map.ofEntries(
            Map.entry("CLRC001", "Related appeal"),
            Map.entry("CLRC002", "Related proceedings"),
            Map.entry("CLRC003", "Same Party"),
            Map.entry("CLRC004", "Same child/ren"),
            Map.entry("CLRC005", "Familial"),
            Map.entry("CLRC006", "Guardian"),
            Map.entry("CLRC007", "Referred to the same judge"),
            Map.entry("CLRC008", "Shared evidence"),
            Map.entry("CLRC009", "Common circumstance"),
            Map.entry("CLRC010", "Bail"),
            Map.entry("CLRC011", "Findings of fact"),
            Map.entry("CLRC012", "First Tier Agency (FTA) Request"),
            Map.entry("CLRC013", "Point of law"),
            Map.entry("CLRC014", "Other"),
            Map.entry("CLRC015", "Case consolidated"),
            Map.entry("CLRC016", "Progressed as part of this lead case"),
            Map.entry("CLRC017", "Linked for a hearing")
    );

    public InternalMaintainCaseLinkAppealTemplate(
            @Value("${internalDetainedMaintainCaseLinkAppeal.templateName}") String templateName,
            CustomerServicesProvider customerServicesProvider) {
        this.templateName = templateName;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getName() {
        return templateName;
    }

    public Map<String, Object> mapFieldValues(
            CaseDetails<AsylumCase> caseDetails
    ) {
        final AsylumCase asylumCase = caseDetails.getCaseData();

        final Map<String, Object> fieldValues = new HashMap<>();

        fieldValues.putAll(getAppellantPersonalisation(asylumCase));
        fieldValues.put("customerServicesTelephone", customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase));
        fieldValues.put("customerServicesEmail", customerServicesProvider.getInternalCustomerServicesEmail(asylumCase));
        fieldValues.put("dateLetterSent", formatDateForNotificationAttachmentDocument(LocalDate.now()));
        fieldValues.put(reasonKey, resolveReasons(asylumCase));
        return fieldValues;
    }

    private List<AbstractMap.SimpleEntry<String, String>> resolveReasons(AsylumCase asylumCase) {
        return retrieveLatestCaseLink(asylumCase)
                .getReasonsForLink()
                .stream()
                .map(IdValue::getValue)
                .map(reasonForLink -> new AbstractMap.SimpleEntry<>(reasonKey, reasons.get(reasonForLink.getReason())))
                .toList();
    }

    private CaseLink retrieveLatestCaseLink(AsylumCase asylumCase) {
        Optional<List<IdValue<CaseLink>>> maybeCaseLinks = asylumCase.read(CASE_LINKS);

        return maybeCaseLinks
                .orElseThrow(() -> new IllegalStateException("caseLinks are not present"))
                .stream()
                .map(IdValue::getValue)
                .max(Comparator.comparing(CaseLink::getCreatedDateTime))
                .orElseThrow(() -> new IllegalStateException("caseLink is not present"));
    }

}
