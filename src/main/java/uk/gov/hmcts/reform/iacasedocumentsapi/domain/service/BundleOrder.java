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
            case END_APPEAL_AUTOMATICALLY:
                log.warn("END_APPEAL_AUTOMATICALLY tag should not be checked for hearing ordering, document desc: {}", document.getDescription());
                return 17;
            case HEARING_REQUIREMENTS:
                log.warn("HEARING_REQUIREMENTS tag should not be checked for hearing ordering, document desc: {}", document.getDescription());
                return 18;
            case CMA_REQUIREMENTS:
                log.warn("CMA_REQUIREMENTS tag should not be checked for cma ordering, document desc: {}", document.getDescription());
                return 19;
            case CMA_NOTICE:
                log.warn("CMA_NOTICE tag should not be checked for cma ordering, document desc: {}", document.getDescription());
                return 20;
            case HO_DECISION_LETTER:
                return 21;
            case FTPA_APPELLANT:
                return 22;
            case FTPA_RESPONDENT:
                return 23;
            case FTPA_DECISION_AND_REASONS:
                return 24;
            case RECORD_OUT_OF_TIME_DECISION_DOCUMENT:
                return 25;
            case UPPER_TRIBUNAL_BUNDLE:
                log.warn("UPPER_TRIBUNAL_BUNDLE tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                return 26;
            case APPEAL_REASONS:
                return 27;
            case CLARIFYING_QUESTIONS:
                return 28;
            case NONE:
                return 29;
            case ADA_SUITABILITY:
                return 30;
            case APPEAL_FORM:
                return 31;
            case NOTICE_OF_DECISION_UT_TRANSFER:
                return 32;
            case REQUEST_CASE_BUILDING:
                log.warn("REQUEST_CASE_BUILDING tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                return 33;
            case INTERNAL_ADA_SUITABILITY:
                log.warn("INTERNAL_ADA_SUITABILITY tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                return 34;
            case REQUEST_RESPONDENT_REVIEW:
                log.warn("REQUEST_RESPONDENT_REVIEW tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                return 35;
            case INTERNAL_DET_DECISION_AND_REASONS_LETTER:
                log.warn("INTERNAL_DET_DECISION_AND_REASONS_LETTER tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                return 36;
            case UPLOAD_THE_APPEAL_RESPONSE:
                log.warn("UPLOAD_THE_APPEAL_RESPONSE tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                return 37;
            case HEARING_BUNDLE_READY_LETTER:
                log.warn("HEARING BUNDLE READY_LETTER tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                return 38;
            case INTERNAL_APPEAL_SUBMISSION:
                log.warn("INTERNAL_APPEAL_SUBMISSION tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                return 39;
            default:
                throw new IllegalStateException("document has unknown tag: " + document.getTag() + ", description: " + document.getDescription());
        }
    }
}
