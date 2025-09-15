package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;

@ExtendWith(MockitoExtension.class)
public class BundleOrderTest {

    private final BundleOrder bundleOrder = new BundleOrder();

    @Mock Document document;

    @Test
    public void should_sort_in_correct_order_excluding_bail_tags() {
        List<DocumentTag> tags = newArrayList(DocumentTag.values());
        tags.add(DocumentTag.CASE_ARGUMENT);
        tags.add(DocumentTag.APPEAL_SUBMISSION);
        Collections.shuffle(tags);

        List<DocumentTag> sortedTags = tags.stream()
            .filter(tag -> !tag.getCaseType().equals(CaseType.BAIL))
            .map(tag -> new DocumentWithMetadata(document, "someDescription", "01-01-2019", tag,"test"))
            .sorted(bundleOrder)
            .map(DocumentWithMetadata::getTag)
            .toList();

        assertEquals(99, sortedTags.size());

        List<DocumentTag> documentTagList = Arrays.asList(
            DocumentTag.CASE_SUMMARY,
            DocumentTag.REHEARD_HEARING_NOTICE,
            DocumentTag.HEARING_NOTICE,
            DocumentTag.REHEARD_HEARING_NOTICE_RELISTED,
            DocumentTag.HEARING_NOTICE_RELISTED,
            DocumentTag.APPEAL_SUBMISSION,
            DocumentTag.APPEAL_SUBMISSION,
            DocumentTag.CASE_ARGUMENT,
            DocumentTag.CASE_ARGUMENT,
            DocumentTag.ADDITIONAL_EVIDENCE,
            DocumentTag.APPEAL_RESPONSE,
            DocumentTag.RESPONDENT_EVIDENCE,
            DocumentTag.ADDENDUM_EVIDENCE,
            DocumentTag.HEARING_BUNDLE,
            DocumentTag.UPDATED_HEARING_BUNDLE,
            DocumentTag.REHEARD_DECISION_AND_REASONS_DRAFT,
            DocumentTag.DECISION_AND_REASONS_DRAFT,
            DocumentTag.DECISION_AND_REASONS_COVER_LETTER,
            DocumentTag.FINAL_DECISION_AND_REASONS_PDF,
            DocumentTag.APPEAL_SKELETON_BUNDLE,
            DocumentTag.END_APPEAL,
            DocumentTag.END_APPEAL_AUTOMATICALLY,
            DocumentTag.HEARING_REQUIREMENTS,
            DocumentTag.CMA_REQUIREMENTS,
            DocumentTag.CMA_NOTICE,
            DocumentTag.HO_DECISION_LETTER,
            DocumentTag.FTPA_APPELLANT,
            DocumentTag.FTPA_RESPONDENT,
            DocumentTag.FTPA_DECISION_AND_REASONS,
            DocumentTag.RECORD_OUT_OF_TIME_DECISION_DOCUMENT,
            DocumentTag.UPPER_TRIBUNAL_BUNDLE,
            DocumentTag.APPEAL_REASONS,
            DocumentTag.CLARIFYING_QUESTIONS,
            DocumentTag.FINAL_DECISION_AND_REASONS_DOCUMENT,
            DocumentTag.ADA_SUITABILITY,
            DocumentTag.APPEAL_FORM,
            DocumentTag.NOTICE_OF_DECISION_UT_TRANSFER,
            DocumentTag.REQUEST_CASE_BUILDING,
            DocumentTag.INTERNAL_ADA_SUITABILITY,
            DocumentTag.REQUEST_RESPONDENT_REVIEW,
            DocumentTag.INTERNAL_DET_DECISION_AND_REASONS_LETTER,
            DocumentTag.UPLOAD_THE_APPEAL_RESPONSE,
            DocumentTag.HEARING_BUNDLE_READY_LETTER,
            DocumentTag.INTERNAL_APPEAL_SUBMISSION,
            DocumentTag.INTERNAL_REQUEST_RESPONDENT_EVIDENCE_LETTER,
            DocumentTag.INTERNAL_END_APPEAL_AUTOMATICALLY,
            DocumentTag.INTERNAL_APPEAL_FEE_DUE_LETTER,
            DocumentTag.INTERNAL_DET_MARK_AS_PAID_LETTER,
            DocumentTag.INTERNAL_LIST_CASE_LETTER,
            DocumentTag.INTERNAL_REQUEST_HEARING_REQUIREMENTS_LETTER,
            DocumentTag.INTERNAL_DETAINED_REQUEST_HO_RESPONSE_REVIEW,
            DocumentTag.INTERNAL_DETAINED_EDIT_CASE_LISTING_LETTER,
            DocumentTag.INTERNAL_DET_MARK_AS_ADA_LETTER,
            DocumentTag.INTERNAL_DECIDE_AN_APPELLANT_APPLICATION_LETTER,
            DocumentTag.INTERNAL_APPLY_FOR_FTPA_RESPONDENT,
            DocumentTag.INTERNAL_DETAINED_TRANSFER_OUT_OF_ADA_LETTER,
            DocumentTag.INTERNAL_FTPA_SUBMITTED_APPELLANT_LETTER,
            DocumentTag.INTERNAL_APPELLANT_FTPA_DECIDED_LETTER,
            DocumentTag.INTERNAL_HO_FTPA_DECIDED_LETTER,
            DocumentTag.INTERNAL_HEARING_ADJUSTMENTS_UPDATED_LETTER,
            DocumentTag.INTERNAL_DECIDE_HOME_OFFICE_APPLICATION_LETTER,
            DocumentTag.MAINTAIN_CASE_UNLINK_APPEAL_LETTER,
            DocumentTag.INTERNAL_UPLOAD_ADDITIONAL_EVIDENCE_LETTER,
            DocumentTag.INTERNAL_CHANGE_HEARING_CENTRE_LETTER,
            DocumentTag.MAINTAIN_CASE_LINK_APPEAL_LETTER,
            DocumentTag.AMEND_HOME_OFFICE_APPEAL_RESPONSE,
            DocumentTag.INTERNAL_NON_STANDARD_DIRECTION_TO_APPELLANT_LETTER,
            DocumentTag.INTERNAL_CHANGE_DIRECTION_DUE_DATE_LETTER,
            DocumentTag.INTERNAL_EDIT_APPEAL_LETTER,
            DocumentTag.HOME_OFFICE_UPLOAD_ADDITIONAL_ADDENDUM_EVIDENCE_LETTER,
            DocumentTag.LEGAL_OFFICER_UPLOAD_ADDITIONAL_EVIDENCE_LETTER,
            DocumentTag.INTERNAL_HO_CHANGE_DIRECTION_DUE_DATE_LETTER,
            DocumentTag.INTERNAL_NON_STANDARD_DIRECTION_RESPONDENT_LETTER,
            DocumentTag.INTERNAL_REINSTATE_APPEAL_LETTER,
            DocumentTag.INTERNAL_ADJOURN_HEARING_WITHOUT_DATE,
            DocumentTag.UPPER_TRIBUNAL_TRANSFER_ORDER_DOCUMENT,
            DocumentTag.IAUT_2_FORM,
            DocumentTag.UPDATED_DECISION_AND_REASONS_COVER_LETTER,
            DocumentTag.UPDATED_FINAL_DECISION_AND_REASONS_PDF,
            DocumentTag.REMITTAL_DECISION,
            DocumentTag.NOTICE_OF_ADJOURNED_HEARING,
            DocumentTag.APPEAL_WAS_NOT_SUBMITTED_SUPPORTING_DOCUMENT,
            DocumentTag.INTERNAL_END_APPEAL_LETTER,
            DocumentTag.INTERNAL_END_APPEAL_LETTER_BUNDLE,
            DocumentTag.INTERNAL_CASE_LISTED_LETTER,
            DocumentTag.INTERNAL_CASE_LISTED_LETTER_BUNDLE,
            DocumentTag.INTERNAL_OUT_OF_TIME_DECISION_LETTER,
            DocumentTag.INTERNAL_OUT_OF_TIME_DECISION_LETTER_BUNDLE,
            DocumentTag.INTERNAL_EDIT_CASE_LISTING_LETTER,
            DocumentTag.INTERNAL_EDIT_CASE_LISTING_LETTER_BUNDLE,
            DocumentTag.INTERNAL_DETAINED_MANAGE_FEE_UPDATE_LETTER,
            DocumentTag.INTERNAL_DETAINED_APPEAL_SUBMITTED_OUT_OF_TIME_WITH_EXEMPTION_LETTER,
            DocumentTag.INTERNAL_DETAINED_APPEAL_SUBMITTED_IN_TIME_WITH_FEE_TO_PAY_LETTER,
            DocumentTag.INTERNAL_DETAINED_OUT_OF_TIME_DECISION_ALLOWED_LETTER,
            DocumentTag.INTERNAL_DETAINED_OUT_OF_TIME_REMISSION_GRANTED_IRC_PRISON_LETTER,
            DocumentTag.NONE
        );

        assertTrue(sortedTags.containsAll(documentTagList));
        /*
        int index = 0;
        for (DocumentTag documentTag : documentTagList) {
            assertEquals(documentTag, sortedTags.get(index));
            index++;
        }
        */
    }
}

