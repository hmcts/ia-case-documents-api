package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.formatDateTimeForRendering;

import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.HearingNoticeFieldMapper;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.StringProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@Component
public class HearingNoticeUpdatedTemplateProvider {
    private static final DateTimeFormatter DOCUMENT_DATE_FORMAT = DateTimeFormatter.ofPattern("ddMMyyyy");
    private final StringProvider stringProvider;
    private final CustomerServicesProvider customerServicesProvider;

    public HearingNoticeUpdatedTemplateProvider(
        StringProvider stringProvider,
        CustomerServicesProvider customerServicesProvider
    ) {
        this.stringProvider = stringProvider;
        this.customerServicesProvider = customerServicesProvider;
    }

    public Map<String, Object> mapFieldValues(
        CaseDetails<AsylumCase> caseDetails,
        CaseDetails<AsylumCase> caseDetailsBefore
    ) {
        final AsylumCase asylumCase =
            caseDetails.getCaseData();

        final AsylumCase asylumCaseBefore =
            caseDetailsBefore.getCaseData();

        final HearingNoticeFieldMapper fieldMapper
            = new HearingNoticeFieldMapper(stringProvider, customerServicesProvider);

        final Map<String, Object> fieldValues =
            fieldMapper.mapFields(asylumCase);

        final HearingCentre listedHearingCentreBefore =
            asylumCaseBefore
                .read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)
                .orElseThrow(() -> new IllegalStateException("listCaseHearingCentre (before) is not present"));

        final String hearingCentreNameBefore =
            stringProvider
                .get("hearingCentreName", listedHearingCentreBefore.toString())
                .orElseThrow(() -> new IllegalStateException("listCaseHearingCentre (before) is not present"));

        fieldValues.put("oldHearingCentre", hearingCentreNameBefore);
        fieldValues.put("oldHearingDate", formatDateTimeForRendering(asylumCaseBefore.read(LIST_CASE_HEARING_DATE, String.class).orElse(""), DOCUMENT_DATE_FORMAT));

        if (asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class).equals(Optional.of(HearingCentre.REMOTE_HEARING))) {

            final String remoteVideoCallTribunalResponse = asylumCase
                .read(REMOTE_VIDEO_CALL_TRIBUNAL_RESPONSE, String.class)
                .orElse("");

            fieldValues.put("remoteHearing", "Remote hearing");
            fieldValues.put("remoteVideoCallTribunalResponse", remoteVideoCallTribunalResponse);
        }

        if (asylumCase.read(IS_VIRTUAL_HEARING, YesOrNo.class).orElse(YesOrNo.NO).equals(YesOrNo.YES)) {
            final String remoteVideoCallTribunalResponse = asylumCase
                .read(REMOTE_VIDEO_CALL_TRIBUNAL_RESPONSE, String.class)
                .orElse("");

            fieldValues.put("remoteVideoCallTribunalResponse", remoteVideoCallTribunalResponse);
        }

        return fieldValues;
    }
}
