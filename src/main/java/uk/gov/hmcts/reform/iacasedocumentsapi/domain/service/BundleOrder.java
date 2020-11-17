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

        return Integer.compare(bundlePositionIndex(d1), bundlePositionIndex(d2));
    }

    private int bundlePositionIndex(DocumentWithMetadata document) {

        switch (document.getTag()) {
            case CASE_SUMMARY:
                return 1;
            case REHEARD_HEARING_NOTICE:
                return 2;                
            case HEARING_NOTICE:
                return 3;
            case APPEAL_SUBMISSION:
                return 4;
            case CASE_ARGUMENT:
                return 5;
            case ADDITIONAL_EVIDENCE:
                return 6;
            case APPEAL_RESPONSE:
                return 7;
            case RESPONDENT_EVIDENCE:
                return 8;
            case ADDENDUM_EVIDENCE:
                return 9;
            case HEARING_BUNDLE:
                log.warn("HEARING_BUNDLE tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                return 10;
            case REHEARD_DECISION_AND_REASONS_DRAFT:
                log.warn("REHEARD_DECISION_AND_REASONS_DRAFT DRAFT tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                return 11;
            case DECISION_AND_REASONS_DRAFT:
                log.warn("DECISION_AND_REASONS_DRAFT DRAFT tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                return 12;
            case DECISION_AND_REASONS_COVER_LETTER:
                log.warn("DECISION_AND_REASONS_COVER_LETTER tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                return 13;
            case FINAL_DECISION_AND_REASONS_PDF:
                log.warn("FINAL_DECISION_AND_REASONS_PDF tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                return 14;
            case APPEAL_SKELETON_BUNDLE:
                log.warn("APPEAL_SKELETON_BUNDLE tag should not be checked for hearing ordering, document desc: {}", document.getDescription());
                return 15;
            case END_APPEAL:
                log.warn("END_APPEAL tag should not be checked for hearing ordering, document desc: {}", document.getDescription());
                return 16;
            case HEARING_REQUIREMENTS:
                log.warn("HEARING_REQUIREMENTS tag should not be checked for hearing ordering, document desc: {}", document.getDescription());
                return 17;
            case CMA_REQUIREMENTS:
                log.warn("CMA_REQUIREMENTS tag should not be checked for cma ordering, document desc: {}", document.getDescription());
                return 18;
            case CMA_NOTICE:
                log.warn("CMA_NOTICE tag should not be checked for cma ordering, document desc: {}", document.getDescription());
                return 19;
            case HO_DECISION_LETTER:
                return 20;
            case NONE:
                return 21;
            default:
                throw new IllegalStateException("document has unknown tag: " + document.getTag() + ", description: " + document.getDescription());
        }
    }
}
