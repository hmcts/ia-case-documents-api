package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentHandler;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DetentionFacility.OTHER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.hasBeenSubmittedAsLegalRepresentedInternalCase;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isAppellantInDetention;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isDetainedInFacilityType;

@Component
public class InternalCmrReListingLrLetterGenerator extends AbstractInternalCmrReListingLetterGenerator {

    public InternalCmrReListingLrLetterGenerator(
        @Qualifier("internalCmrReListingLrLetter") DocumentCreator<AsylumCase> documentCreator,
        DocumentHandler documentHandler
    ) {
        super(documentCreator, documentHandler, DocumentTag.INTERNAL_CMR_RE_LISTING_LR_LETTER);
    }

    @Override
    protected boolean isApplicable(AsylumCase asylumCase) {
        return hasBeenSubmittedAsLegalRepresentedInternalCase(asylumCase)
               && (!isAppellantInDetention(asylumCase) || isDetainedInFacilityType(asylumCase, OTHER));
    }
}
