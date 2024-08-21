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

        return switch (document.getTag()) {
            case CASE_SUMMARY -> 1;
            case REHEARD_HEARING_NOTICE -> 2;
            case HEARING_NOTICE -> 3;
            case APPEAL_SUBMISSION -> 4;
            case CASE_ARGUMENT -> 5;
            case ADDITIONAL_EVIDENCE -> 6;
            case APPEAL_RESPONSE -> 7;
            case RESPONDENT_EVIDENCE -> 8;
            case ADDENDUM_EVIDENCE -> 9;
            case HEARING_BUNDLE -> {
                log.warn("HEARING_BUNDLE tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                yield 10;
            }
            case UPDATED_HEARING_BUNDLE -> {
                log.warn("HEARING_BUNDLE tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                yield 11;
            }
            case REHEARD_DECISION_AND_REASONS_DRAFT -> {
                log.warn("REHEARD_DECISION_AND_REASONS_DRAFT DRAFT tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                yield 12;
            }
            case DECISION_AND_REASONS_DRAFT -> {
                log.warn("DECISION_AND_REASONS_DRAFT DRAFT tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                yield 13;
            }
            case DECISION_AND_REASONS_COVER_LETTER -> {
                log.warn("DECISION_AND_REASONS_COVER_LETTER tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                yield 14;
            }
            case FINAL_DECISION_AND_REASONS_PDF -> {
                log.warn("FINAL_DECISION_AND_REASONS_PDF tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                yield 15;
            }
            case APPEAL_SKELETON_BUNDLE -> {
                log.warn("APPEAL_SKELETON_BUNDLE tag should not be checked for hearing ordering, document desc: {}", document.getDescription());
                yield 16;
            }
            case END_APPEAL -> {
                log.warn("END_APPEAL tag should not be checked for hearing ordering, document desc: {}", document.getDescription());
                yield 17;
            }
            case END_APPEAL_AUTOMATICALLY -> {
                log.warn("END_APPEAL_AUTOMATICALLY tag should not be checked for hearing ordering, document desc: {}", document.getDescription());
                yield 18;
            }
            case HEARING_REQUIREMENTS -> {
                log.warn("HEARING_REQUIREMENTS tag should not be checked for hearing ordering, document desc: {}", document.getDescription());
                yield 19;
            }
            case CMA_REQUIREMENTS -> {
                log.warn("CMA_REQUIREMENTS tag should not be checked for cma ordering, document desc: {}", document.getDescription());
                yield 20;
            }
            case CMA_NOTICE -> {
                log.warn("CMA_NOTICE tag should not be checked for cma ordering, document desc: {}", document.getDescription());
                yield 21;
            }
            case HO_DECISION_LETTER -> 22;
            case FTPA_APPELLANT -> 23;
            case FTPA_RESPONDENT -> 24;
            case FTPA_DECISION_AND_REASONS -> 25;
            case RECORD_OUT_OF_TIME_DECISION_DOCUMENT -> 26;
            case UPPER_TRIBUNAL_BUNDLE -> {
                log.warn("UPPER_TRIBUNAL_BUNDLE tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                yield 27;
            }
            case APPEAL_REASONS -> 28;
            case CLARIFYING_QUESTIONS -> 29;
            case FINAL_DECISION_AND_REASONS_DOCUMENT -> {
                log.warn("FINAL_DECISION_AND_REASONS_DOCUMENT tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                yield 30;
            }
            case ADA_SUITABILITY -> 31;
            case APPEAL_FORM -> 32;
            case NOTICE_OF_DECISION_UT_TRANSFER -> 33;
            case REQUEST_CASE_BUILDING -> {
                log.warn("REQUEST_CASE_BUILDING tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                yield 34;
            }
            case INTERNAL_ADA_SUITABILITY -> {
                log.warn("INTERNAL_ADA_SUITABILITY tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                yield 35;
            }
            case REQUEST_RESPONDENT_REVIEW -> {
                log.warn("REQUEST_RESPONDENT_REVIEW tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                yield 36;
            }
            case INTERNAL_DET_DECISION_AND_REASONS_LETTER -> {
                log.warn("INTERNAL_DET_DECISION_AND_REASONS_LETTER tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                yield 37;
            }
            case UPLOAD_THE_APPEAL_RESPONSE -> {
                log.warn("UPLOAD_THE_APPEAL_RESPONSE tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                yield 38;
            }
            case HEARING_BUNDLE_READY_LETTER -> {
                log.warn("HEARING BUNDLE READY_LETTER tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                yield 39;
            }
            case INTERNAL_APPEAL_SUBMISSION -> {
                log.warn("INTERNAL_APPEAL_SUBMISSION tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                yield 40;
            }
            case INTERNAL_REQUEST_RESPONDENT_EVIDENCE_LETTER -> {
                log.warn("INTERNAL_REQUEST_RESPONDENT_EVIDENCE_LETTER tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                yield 41;
            }
            case INTERNAL_END_APPEAL_AUTOMATICALLY -> {
                log.warn("INTERNAL_END_APPEAL_AUTOMATICALLY tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                yield 42;
            }
            case INTERNAL_APPEAL_FEE_DUE_LETTER -> {
                log.warn("INTERNAL_APPEAL_FEE_DUE_LETTER tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                yield 43;
            }
            case INTERNAL_DET_MARK_AS_PAID_LETTER -> {
                log.warn("INTERNAL_DET_MARK_AS_PAID_LETTER tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                yield 44;
            }
            case INTERNAL_LIST_CASE_LETTER -> {
                log.warn("INTERNAL_LIST_CASE_LETTER tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                yield 45;
            }
            case INTERNAL_REQUEST_HEARING_REQUIREMENTS_LETTER -> {
                log.warn("INTERNAL_REQUEST_HEARING_REQUIREMENTS_LETTER tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                yield 46;
            }
            case INTERNAL_DETAINED_REQUEST_HO_RESPONSE_REVIEW -> {
                log.warn("INTERNAL_DETAINED_REQUEST_HO_RESPONSE_REVIEW tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                yield 47;
            }
            case INTERNAL_DETAINED_EDIT_CASE_LISTING_LETTER -> {
                log.warn("INTERNAL_DETAINED_EDIT_CASE_LISTING_LETTER tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                yield 48;
            }
            case INTERNAL_DET_MARK_AS_ADA_LETTER -> {
                log.warn("INTERNAL_DET_MARK_AS_ADA_LETTER tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                yield 49;
            }
            case INTERNAL_DECIDE_AN_APPELLANT_APPLICATION_LETTER -> {
                log.warn("INTERNAL_DECIDE_AN_APPELLANT_APPLICATION_LETTER tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                yield 50;
            }
            case INTERNAL_APPLY_FOR_FTPA_RESPONDENT -> {
                log.warn("INTERNAL_APPLY_FOR_FTPA_RESPONDENT_LETTER tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                yield 51;
            }
            case INTERNAL_DETAINED_TRANSFER_OUT_OF_ADA_LETTER -> {
                log.warn("INTERNAL_DETAINED_TRANSFER_OUT_OF_ADA_LETTER tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                yield 52;
            }
            case INTERNAL_FTPA_SUBMITTED_APPELLANT_LETTER -> {
                log.warn("INTERNAL_FTPA_SUBMITTED_APPELLANT_LETTER tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                yield 53;
            }
            case INTERNAL_APPELLANT_FTPA_DECIDED_LETTER -> {
                log.warn("INTERNAL_FTPA_DECIDED_LETTER tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                yield 54;
            }
            case INTERNAL_HO_FTPA_DECIDED_LETTER -> {
                log.warn("INTERNAL_HO_FTPA_DECIDED_LETTER tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                yield 55;
            }
            case INTERNAL_HEARING_ADJUSTMENTS_UPDATED_LETTER -> {
                log.warn("INTERNAL_HEARING_ADJUSTMENTS_UPDATED_LETTER tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                yield 56;
            }
            case INTERNAL_DECIDE_HOME_OFFICE_APPLICATION_LETTER -> {
                log.warn("INTERNAL_DECIDE_HOME_OFFICE_APPLICATION_LETTER tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                yield 57;
            }
            case MAINTAIN_CASE_UNLINK_APPEAL_LETTER -> {
                log.warn("MAINTAIN_CASE_UNLINK_APPEAL_LETTER tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                yield 58;
            }
            case INTERNAL_UPLOAD_ADDITIONAL_EVIDENCE_LETTER -> {
                log.warn("INTERNAL_UPLOAD_ADDITIONAL_EVIDENCE_LETTER tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                yield 59;
            }
            case INTERNAL_CHANGE_HEARING_CENTRE_LETTER -> {
                log.warn("INTERNAL_CHANGE_HEARING_CENTRE_LETTER tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                yield 60;
            }
            case MAINTAIN_CASE_LINK_APPEAL_LETTER -> {
                log.warn("MAINTAIN_CASE_LINK_APPEAL_LETTER tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                yield 61;
            }
            case AMEND_HOME_OFFICE_APPEAL_RESPONSE -> {
                log.warn("AMEND_HOME_OFFICE_APPEAL_RESPONSE tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                yield 62;
            }
            case INTERNAL_NON_STANDARD_DIRECTION_TO_APPELLANT_LETTER -> {
                log.warn("INTERNAL_NON_STANDARD_DIRECTION_TO_APPELLANT_LETTER tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                yield 63;
            }
            case INTERNAL_CHANGE_DIRECTION_DUE_DATE_LETTER -> {
                log.warn("INTERNAL_CHANGE_DIRECTION_DUE_DATE_LETTER tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                yield 64;
            }
            case INTERNAL_EDIT_APPEAL_LETTER -> {
                log.warn("INTERNAL_EDIT_APPEAL_LETTER tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                yield 65;
            }
            case HOME_OFFICE_UPLOAD_ADDITIONAL_ADDENDUM_EVIDENCE_LETTER -> {
                log.warn("HOME_OFFICE_UPLOAD_ADDITIONAL_ADDENDUM_EVIDENCE_LETTER tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                yield 66;
            }
            case LEGAL_OFFICER_UPLOAD_ADDITIONAL_EVIDENCE_LETTER -> {
                log.warn("LEGAL_OFFICER_UPLOAD_ADDITIONAL_EVIDENCE_LETTER tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                yield 67;
            }
            case INTERNAL_HO_CHANGE_DIRECTION_DUE_DATE_LETTER -> {
                log.warn("INTERNAL_HO_CHANGE_DIRECTION_DUE_DATE_LETTER tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                yield 68;
            }
            case INTERNAL_NON_STANDARD_DIRECTION_RESPONDENT_LETTER -> {
                log.warn("INTERNAL_NON_STANDARD_DIRECTION_RESPONDENT_LETTER tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                yield 69;
            }
            case INTERNAL_REINSTATE_APPEAL_LETTER -> {
                log.warn("INTERNAL_REINSTATE_APPEAL_LETTER tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                yield 70;
            }
            case INTERNAL_ADJOURN_HEARING_WITHOUT_DATE -> {
                log.warn("INTERNAL_ADJOURN_HEARING_WITHOUT_DATE tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                yield 71;
            }
            case UPPER_TRIBUNAL_TRANSFER_ORDER_DOCUMENT -> {
                log.warn("UPPER_TRIBUNAL_TRANSFER_ORDER_DOCUMENT tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                yield 72;
            }
            case IAUT_2_FORM -> {
                log.warn("IAUT_2_FORM tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                yield 73;
            }
            case UPDATED_DECISION_AND_REASONS_COVER_LETTER -> 74;
            case UPDATED_FINAL_DECISION_AND_REASONS_PDF -> {
                log.warn("UPDATED_FINAL_DECISION_AND_REASONS_PDF tag should not be checked for bundle ordering, document desc: {}", document.getDescription());
                yield 75;
            }
            case REMITTAL_DECISION -> 76;
            case NOTICE_OF_ADJOURNED_HEARING -> 77;
            case NONE -> 78;
            default ->
                throw new IllegalStateException("document has unknown tag: " + document.getTag() + ", description: " + document.getDescription());
        };
    }
}
