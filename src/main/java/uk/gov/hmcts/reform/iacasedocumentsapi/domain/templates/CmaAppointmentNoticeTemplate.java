package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.HearingNoticeFieldMapper;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.StringProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DirectionFinder;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.HearingDetailsFinder;

@Component
public class CmaAppointmentNoticeTemplate implements DocumentTemplate<AsylumCase> {

    private final String templateName;
    private final StringProvider stringProvider;
    private final CustomerServicesProvider customerServicesProvider;
    private final HearingDetailsFinder hearingDetailsFinder;
    private final DirectionFinder directionFinder;
    private final String iaAipFrontendUrl;

    public CmaAppointmentNoticeTemplate(
        @Value("${cmaAppointmentNotice.templateName}") String templateName,
        @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
        StringProvider stringProvider,
        CustomerServicesProvider customerServicesProvider,
        HearingDetailsFinder hearingDetailsFinder,
        DirectionFinder directionFinder
    ) {
        this.templateName = templateName;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.stringProvider = stringProvider;
        this.customerServicesProvider = customerServicesProvider;
        this.hearingDetailsFinder = hearingDetailsFinder;
        this.directionFinder = directionFinder;
    }

    public String getName() {
        return templateName;
    }

    @Override
    public Map<String, Object> mapFieldValues(
        CaseDetails<AsylumCase> caseDetails
    ) {
        final AsylumCase asylumCase =
            caseDetails.getCaseData();

        final HearingNoticeFieldMapper fieldMapper = new HearingNoticeFieldMapper(stringProvider, customerServicesProvider);

        Map<String, Object> fieldValues = fieldMapper.mapFields(asylumCase);


        final HearingCentre listedHearingCentre =
            asylumCase
                .read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)
                .orElseThrow(() -> new IllegalStateException("listCaseHearingCentre is not present"));

        final Direction direction =
            directionFinder
                .findFirst(asylumCase, DirectionTag.REQUEST_CMA_REQUIREMENTS)
                .orElseThrow(() -> new IllegalStateException("direction '" + DirectionTag.REQUEST_CMA_REQUIREMENTS + "' is not present"));


        fieldValues.put("healthConditions", asylumCase.read(VULNERABILITIES_TRIBUNAL_RESPONSE, String.class)
            .orElse("No physical or mental health conditions"));
        fieldValues.put("pastExperiences", asylumCase.read(PAST_EXPERIENCES_TRIBUNAL_RESPONSE, String.class)
            .orElse("No special adjustments are being made to accommodate past experiences"));

        fieldValues.put("aipFrontendUrl", iaAipFrontendUrl);
        fieldValues.put("reasonForAppointment", direction.getExplanation());
        fieldValues.put("hearingCentreName", listedHearingCentre.toString());
        fieldValues.put("hearingCentreUrl", hearingDetailsFinder.getHearingCentreUrl(listedHearingCentre));

        return fieldValues;
    }
}

