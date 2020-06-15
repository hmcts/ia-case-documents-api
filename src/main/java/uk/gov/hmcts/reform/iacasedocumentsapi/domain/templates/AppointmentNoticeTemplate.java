package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_REQUIREMENTS_OTHER;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DirectionFinder;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.HearingNoticeFieldMapper;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.StringProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.HearingDetailsFinder;

@Component
public class AppointmentNoticeTemplate implements DocumentTemplate<AsylumCase> {

    private final String templateName;
    private final String iaAipFrontendUrl;
    private final StringProvider stringProvider;
    private final DirectionFinder directionFinder;
    private final HearingDetailsFinder hearingDetailsFinder;


    public AppointmentNoticeTemplate(
        @Value("${aipAppointmentNotice.templateName}") String templateName,
        @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
        DirectionFinder directionFinder,
        StringProvider stringProvider,
        HearingDetailsFinder hearingDetailsFinder

    ) {
        this.templateName = templateName;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.stringProvider = stringProvider;
        this.directionFinder = directionFinder;
        this.hearingDetailsFinder = hearingDetailsFinder;
    }

    public String getName() {
        return templateName;
    }

    @Override
    public Map<String, Object> mapFieldValues(
        CaseDetails<AsylumCase> caseDetails
    ) {
        final AsylumCase asylumCase = caseDetails.getCaseData();

        final HearingNoticeFieldMapper fieldMapper = new HearingNoticeFieldMapper(stringProvider);

        //TODO: get hearing centre name
        final String hearingCentre = hearingDetailsFinder.getHearingCentreName(asylumCase);
        //TODO: get hearing centre url
        final String hearingCentreUrl = "";
        //TODO: find reason for appointment using direction finder;
        final String reasonForAppointment = "";

        final Map<String, Object> personalisation = fieldMapper.mapFields(asylumCase);
        personalisation.put("healthConditions", asylumCase.read(LIST_CASE_REQUIREMENTS_OTHER, String.class).orElse("No health conditions reported"));
        personalisation.put("pastExperiences", asylumCase.read(LIST_CASE_REQUIREMENTS_OTHER, String.class).orElse("No past experiences reported"));
        personalisation.put("reasonForAppointment", reasonForAppointment);
        personalisation.put("hearingCentreName", hearingCentre);
        personalisation.put("hearingCentreUrl", iaAipFrontendUrl);
        personalisation.put("aipFrontendUrl", iaAipFrontendUrl);

        return personalisation;
    }
}
