package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentWithMetadata;
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
            .filter(tag -> !tag.equals(DocumentTag.BAIL_SUBMISSION))
            .map(tag -> new DocumentWithMetadata(document, "someDescription", "01-01-2019", tag,"test"))
            .sorted(bundleOrder)
            .map(DocumentWithMetadata::getTag)
            .collect(Collectors.toList());

        assertEquals(30, sortedTags.size());

        List<DocumentTag> documentTagList = Arrays.asList(
            DocumentTag.CASE_SUMMARY,
            DocumentTag.REHEARD_HEARING_NOTICE,
            DocumentTag.HEARING_NOTICE,
            DocumentTag.APPEAL_SUBMISSION,
            DocumentTag.APPEAL_SUBMISSION,
            DocumentTag.CASE_ARGUMENT,
            DocumentTag.CASE_ARGUMENT,
            DocumentTag.ADDITIONAL_EVIDENCE,
            DocumentTag.APPEAL_RESPONSE,
            DocumentTag.RESPONDENT_EVIDENCE,
            DocumentTag.ADDENDUM_EVIDENCE,
            DocumentTag.HEARING_BUNDLE,
            DocumentTag.REHEARD_DECISION_AND_REASONS_DRAFT,
            DocumentTag.DECISION_AND_REASONS_DRAFT,
            DocumentTag.DECISION_AND_REASONS_COVER_LETTER,
            DocumentTag.FINAL_DECISION_AND_REASONS_PDF,
            DocumentTag.APPEAL_SKELETON_BUNDLE,
            DocumentTag.END_APPEAL,
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
            DocumentTag.NONE
        );

        int index = 0;
        for (DocumentTag documentTag : documentTagList) {
            assertEquals(documentTag, sortedTags.get(index));
            index++;
        }
    }
}
