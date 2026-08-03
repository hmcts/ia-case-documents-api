package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentHandler;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.hasBeenSubmittedAsLegalRepresentedInternalCase;

@Component
public class InternalCmrReListingAppellantLetterGenerator extends AbstractInternalCmrReListingLetterGenerator {

    public InternalCmrReListingAppellantLetterGenerator(
        @Qualifier("internalCmrReListingAppellantLetter") DocumentCreator<AsylumCase> documentCreator,
        DocumentHandler documentHandler
    ) {
        super(documentCreator, documentHandler, DocumentTag.INTERNAL_CMR_LISTING_APPELLANT_LETTER);
    }

    @Override
    protected boolean isApplicable(AsylumCase asylumCase) {
        return hasBeenSubmittedAsLegalRepresentedInternalCase(asylumCase);
    }
}
