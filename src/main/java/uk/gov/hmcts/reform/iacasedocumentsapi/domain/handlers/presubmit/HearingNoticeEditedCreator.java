package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.HearingDetailsFinder;

@Component
public class HearingNoticeEditedCreator implements PreSubmitCallbackHandler<AsylumCase> {

    private final DocumentCreator<AsylumCase> hearingNoticeUpdatedRequirementsDocumentCreator;
    private final DocumentCreator<AsylumCase> hearingNoticeUpdatedDetailsDocumentCreator;
    private final DocumentCreator<AsylumCase> remoteHearingNoticeUpdatedDetailsDocumentCreator;
    private final DocumentHandler documentHandler;
    private final HearingDetailsFinder hearingDetailsFinder;

    public HearingNoticeEditedCreator(
        @Qualifier("hearingNoticeUpdatedRequirements") DocumentCreator<AsylumCase> hearingNoticeUpdatedRequirementsDocumentCreator,
        @Qualifier("hearingNoticeUpdatedDetails") DocumentCreator<AsylumCase> hearingNoticeUpdatedDetailsDocumentCreator,
        @Qualifier("remoteHearingNoticeUpdatedDetails") DocumentCreator<AsylumCase> remoteHearingNoticeUpdatedDetailsDocumentCreator,
        DocumentHandler documentHandler,
        HearingDetailsFinder hearingDetailsFinder
    ) {
        this.hearingNoticeUpdatedRequirementsDocumentCreator = hearingNoticeUpdatedRequirementsDocumentCreator;
        this.hearingNoticeUpdatedDetailsDocumentCreator = hearingNoticeUpdatedDetailsDocumentCreator;
        this.remoteHearingNoticeUpdatedDetailsDocumentCreator = remoteHearingNoticeUpdatedDetailsDocumentCreator;
        this.documentHandler = documentHandler;
        this.hearingDetailsFinder = hearingDetailsFinder;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && asList(Event.EDIT_CASE_LISTING).contains(callback.getEvent());
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final CaseDetails<AsylumCase> caseDetails = callback.getCaseDetails();
        final AsylumCase asylumCase = caseDetails.getCaseData();
        final Optional<CaseDetails<AsylumCase>> caseDetailsBefore = callback.getCaseDetailsBefore();
        final String listCaseHearingCentre = hearingDetailsFinder.getHearingCentreName(caseDetails.getCaseData());
        final String hearingDate = hearingDetailsFinder.getHearingDateTime(caseDetails.getCaseData());


        if (caseDetailsBefore.isPresent()) {

            final String hearingCentreNameBefore =
                hearingDetailsFinder.getHearingCentreName(caseDetailsBefore.get().getCaseData());

            final String oldHearingDate =
                hearingDetailsFinder.getHearingDateTime(caseDetailsBefore.get().getCaseData());

            boolean isCaseUsingLocationRefData = asylumCase.read(IS_CASE_USING_LOCATION_REF_DATA, YesOrNo.class)
                    .orElse(YesOrNo.NO).equals(YesOrNo.YES);

            //prevent the existing case with previous selected remote hearing when the ref data feature is on with different hearing centre
            //IS_REMOTE_HEARING is used for the case ref data
            if ((!isCaseUsingLocationRefData && asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class).equals(Optional.of(HearingCentre.REMOTE_HEARING)))
                    || (isCaseUsingLocationRefData && asylumCase.read(IS_REMOTE_HEARING, YesOrNo.class).orElse(YesOrNo.NO).equals(YesOrNo.YES))) {
                generateDocument(caseDetails, asylumCase, caseDetailsBefore, remoteHearingNoticeUpdatedDetailsDocumentCreator);
            } else if (hearingCentreNameBefore.equals(listCaseHearingCentre) && oldHearingDate.equals(hearingDate)) {
                generateDocument(caseDetails, asylumCase, caseDetailsBefore, hearingNoticeUpdatedRequirementsDocumentCreator);
            } else {
                generateDocument(caseDetails, asylumCase, caseDetailsBefore, hearingNoticeUpdatedDetailsDocumentCreator);
            }
        } else {
            throw new IllegalStateException("previous case data is not present");
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void generateDocument(CaseDetails<AsylumCase> caseDetails,
                                  AsylumCase asylumCase,
                                  Optional<CaseDetails<AsylumCase>> caseDetailsBefore,
                                  DocumentCreator<AsylumCase> hearingNoticeEditedDocumentCreator) {

        Document hearingNoticeEdited =
            hearingNoticeEditedDocumentCreator.create(
                caseDetails,
                caseDetailsBefore
                    .orElseThrow(() -> new IllegalStateException("previous case data is not present")));

        if ((asylumCase.read(AsylumCaseDefinition.IS_REHEARD_APPEAL_ENABLED, YesOrNo.class).equals(Optional.of(YesOrNo.YES))
             && (asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class).map(flag -> flag.equals(YesOrNo.YES)).orElse(false)))) {
            documentHandler.addWithMetadataWithoutReplacingExistingDocuments(
                asylumCase,
                hearingNoticeEdited,
                REHEARD_HEARING_DOCUMENTS,
                DocumentTag.REHEARD_HEARING_NOTICE
            );
        } else {
            documentHandler.addWithMetadataWithoutReplacingExistingDocuments(
                asylumCase,
                hearingNoticeEdited,
                HEARING_DOCUMENTS,
                DocumentTag.HEARING_NOTICE
            );
        }
    }
}
