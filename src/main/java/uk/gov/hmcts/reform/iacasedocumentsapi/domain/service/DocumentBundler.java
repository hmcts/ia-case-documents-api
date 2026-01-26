package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import java.util.List;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;

public interface DocumentBundler {

    Document bundle(
        List<DocumentWithMetadata> documents,
        String bundleTitle,
        String bundleFilename
    );

    Document bundleWithoutContentsOrCoverSheets(
        List<DocumentWithMetadata> documents,
        String bundleTitle,
        String bundleFilename
    );

    Document asyncBundleWithoutContentsOrCoverSheetsForEvent(
        List<DocumentWithMetadata> documents,
        String bundleTitle,
        String bundleFilename,
        Event event,
        Long caseId
    );
}
