package uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;

import java.util.Optional;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.STF_24W_CURRENT_STATUS_AUTO_GENERATED;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event.COMPLETE_CASE_REVIEW;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isAppellantInUk;

@Slf4j
public class Stf24WeeksUtils {
    public static final String STF_24_WEEKS_REVIEW_DOCUMENT_CREATOR = "stf24WeeksReviewDocumentCreator";

    private Stf24WeeksUtils() {

    }

    public static boolean isCaseReviewFor24WeeksCase(Event event, AsylumCase asylumCase) {
        boolean inCountryAppeal = isAppellantInUk(asylumCase);
        boolean hasStf24W = hasStf24WeeksStatus(asylumCase);
        return event == COMPLETE_CASE_REVIEW
                && inCountryAppeal
                && hasStf24W;
    }

    public static boolean hasStf24WeeksStatus(AsylumCase asylumCase) {
        Optional<YesOrNo> read = asylumCase.read(STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.class);
        return read.map(value -> value.equals(YES)).orElse(false);
    }
}
