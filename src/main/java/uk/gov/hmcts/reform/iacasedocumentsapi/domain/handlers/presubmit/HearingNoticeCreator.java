package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentsAppender;

@Component
public class HearingNoticeCreator implements PreSubmitCallbackHandler<AsylumCase> {

    private final DocumentCreator<AsylumCase> hearingNoticeDocumentCreator;
    private final DocumentReceiver documentReceiver;
    private final DocumentsAppender documentsAppender;

    public HearingNoticeCreator(
        @Qualifier("hearingNotice") DocumentCreator<AsylumCase> hearingNoticeDocumentCreator,
        DocumentReceiver documentReceiver,
        DocumentsAppender documentsAppender
    ) {
        this.hearingNoticeDocumentCreator = hearingNoticeDocumentCreator;
        this.documentReceiver = documentReceiver;
        this.documentsAppender = documentsAppender;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.LIST_CASE;
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

        Document hearingNotice = hearingNoticeDocumentCreator.create(caseDetails);

        attachDocumentToCase(asylumCase, hearingNotice);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void attachDocumentToCase(
        AsylumCase asylumCase,
        Document hearingNotice
    ) {
        final List<IdValue<DocumentWithMetadata>> hearingDocuments =
            asylumCase
                .getHearingDocuments()
                .orElse(Collections.emptyList());

        DocumentWithMetadata documentWithMetadata =
            documentReceiver.receive(
                hearingNotice,
                "",
                DocumentTag.HEARING_NOTICE
            );

        List<IdValue<DocumentWithMetadata>> allHearingDocuments =
            documentsAppender.append(
                hearingDocuments,
                Collections.singletonList(documentWithMetadata),
                DocumentTag.HEARING_NOTICE
            );

        asylumCase.setHearingDocuments(allHearingDocuments);
    }
}
