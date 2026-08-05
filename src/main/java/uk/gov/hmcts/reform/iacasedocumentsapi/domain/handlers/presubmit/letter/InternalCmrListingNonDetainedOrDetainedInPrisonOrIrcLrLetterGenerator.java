package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentHandler;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DetentionFacility.IRC;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DetentionFacility.PRISON;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.*;

@Component
@Slf4j
public class InternalCmrListingNonDetainedOrDetainedInPrisonOrIrcLrLetterGenerator extends AbstractInternalCmrListingLetterGenerator {

    public InternalCmrListingNonDetainedOrDetainedInPrisonOrIrcLrLetterGenerator(
        @Qualifier("internalCmrListingLrLetter") DocumentCreator<AsylumCase> documentCreator,
        DocumentHandler documentHandler
    ) {
        super(documentCreator, documentHandler, DocumentTag.INTERNAL_CMR_LISTING_LR_LETTER);
    }

    @Override
    protected boolean isApplicable(AsylumCase asylumCase) {
        log.info("----------------InternalCmrListingNonDetainedOrDetainedInPrisonOrIrcLrLetterGenerator1");
        log.info("----------------!isDetainedAppeal(asylumCase) || isDetainedInOneOfFacilityTypes(asylumCase, IRC, PRISON): {}", !isDetainedAppeal(asylumCase) || isDetainedInOneOfFacilityTypes(asylumCase, IRC, PRISON));
        log.info("----------------hasBeenSubmittedAsLegalRepresentedInternalCase(asylumCase): {}", hasBeenSubmittedAsLegalRepresentedInternalCase(asylumCase));
        log.info("----------------InternalCmrListingNonDetainedOrDetainedInPrisonOrIrcLrLetterGenerator2");
        return (!isDetainedAppeal(asylumCase) || isDetainedInOneOfFacilityTypes(asylumCase, IRC, PRISON))
            && hasBeenSubmittedAsLegalRepresentedInternalCase(asylumCase);
    }
}
