package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import java.util.Comparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentWithMetadata;

@Component
public class BundleOrder implements Comparator<DocumentWithMetadata> {

    private static final Logger log = LoggerFactory.getLogger(BundleOrder.class);

    @Override
    public int compare(DocumentWithMetadata d1, DocumentWithMetadata d2) {

        return bundlePositionIndex(d1) == bundlePositionIndex(d2) ? 0 :
            bundlePositionIndex(d1) > bundlePositionIndex(d2) ? 1 : -1;
    }

    private int bundlePositionIndex(DocumentWithMetadata document) {

        switch (document.getTag()) {
            case CASE_SUMMARY:
                return 1;
            case HEARING_NOTICE:
                return 2;
            case APPEAL_SUBMISSION:
                return 3;
            case CASE_ARGUMENT:
                return 4;
            case ADDITIONAL_EVIDENCE:
                return 5;
            case APPEAL_RESPONSE:
                return 6;
            case RESPONDENT_EVIDENCE:
                return 7;
            case HEARING_BUNDLE:
                log.warn("HEARING_BUNDLE tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                return 8;
            case DECISION_AND_REASONS_DRAFT:
                log.warn("DECISION_AND_REASONS_DRAFT DRAFT tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                return 9;
            case DECISION_AND_REASONS_COVER_LETTER:
                log.warn("DECISION_AND_REASONS_COVER_LETTER tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                return 10;
            case FINAL_DECISION_AND_REASONS_PDF:
                log.warn("FINAL_DECISION_AND_REASONS_PDF tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                return 11;
            case APPEAL_SKELETON_BUNDLE:
                log.warn("APPEAL_SKELETON_BUNDLE tag should not be checked for hearing ordering, document desc: {}", document.getDescription());
                return 12;
            case END_APPEAL:
                log.warn("END_APPEAL tag should not be checked for hearing ordering, document desc: {}", document.getDescription());
                return 13;
            case NONE:
                return 14;
            default:
                throw new IllegalStateException("document has unknown tag: " + document.getTag() + ", description: " + document.getDescription());
        }
    }
}
