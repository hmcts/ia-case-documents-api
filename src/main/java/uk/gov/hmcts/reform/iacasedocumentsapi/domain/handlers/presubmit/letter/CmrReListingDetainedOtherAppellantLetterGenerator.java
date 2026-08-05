package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentHandler;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DetentionFacility.OTHER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isDetainedInFacilityType;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isInternalCase;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isRepJourney;

@Component
public class CmrReListingDetainedOtherAppellantLetterGenerator extends AbstractInternalCmrReListingLetterGenerator {

    public CmrReListingDetainedOtherAppellantLetterGenerator(
        @Qualifier("internalCmrReListingAppellantLetter") DocumentCreator<AsylumCase> documentCreator,
        DocumentHandler documentHandler
    ) {
        super(documentCreator, documentHandler, DocumentTag.INTERNAL_CMR_LISTING_LETTER);
    }

    @Override
    protected boolean isApplicable(AsylumCase asylumCase) {
        return isRepJourney(asylumCase)
               && !isInternalCase(asylumCase)
               && isDetainedInFacilityType(asylumCase, OTHER);
    }
}
