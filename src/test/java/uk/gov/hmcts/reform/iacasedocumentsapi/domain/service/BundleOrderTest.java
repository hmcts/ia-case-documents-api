package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;

@RunWith(MockitoJUnitRunner.class)
public class BundleOrderTest {

    private final BundleOrder bundleOrder = new BundleOrder();

    @Mock Document document;

    @Test
    public void should_sort_in_correct_order() {
        List<DocumentTag> tags = newArrayList(DocumentTag.values());
        tags.add(DocumentTag.CASE_ARGUMENT);
        tags.add(DocumentTag.APPEAL_SUBMISSION);
        Collections.shuffle(tags);

        List<DocumentTag> sortedTags = tags.stream()
            .map(tag -> new DocumentWithMetadata(document, "someDescription", "01-01-2019", tag))
            .sorted(bundleOrder)
            .map(DocumentWithMetadata::getTag)
            .collect(Collectors.toList());

        assertEquals(19, sortedTags.size());
        assertEquals(DocumentTag.CASE_SUMMARY, sortedTags.get(0));
        assertEquals(DocumentTag.HEARING_NOTICE, sortedTags.get(1));
        assertEquals(DocumentTag.APPEAL_SUBMISSION, sortedTags.get(2));
        assertEquals(DocumentTag.APPEAL_SUBMISSION, sortedTags.get(3));
        assertEquals(DocumentTag.CASE_ARGUMENT, sortedTags.get(4));
        assertEquals(DocumentTag.CASE_ARGUMENT, sortedTags.get(5));
        assertEquals(DocumentTag.ADDITIONAL_EVIDENCE, sortedTags.get(6));
        assertEquals(DocumentTag.APPEAL_RESPONSE, sortedTags.get(7));
        assertEquals(DocumentTag.RESPONDENT_EVIDENCE, sortedTags.get(8));
        assertEquals(DocumentTag.ADDENDUM_EVIDENCE, sortedTags.get(9));
        assertEquals(DocumentTag.HEARING_BUNDLE, sortedTags.get(10));
        assertEquals(DocumentTag.DECISION_AND_REASONS_DRAFT, sortedTags.get(11));
        assertEquals(DocumentTag.DECISION_AND_REASONS_COVER_LETTER, sortedTags.get(12));
        assertEquals(DocumentTag.FINAL_DECISION_AND_REASONS_PDF, sortedTags.get(13));
        assertEquals(DocumentTag.APPEAL_SKELETON_BUNDLE, sortedTags.get(14));
        assertEquals(DocumentTag.END_APPEAL, sortedTags.get(15));
        assertEquals(DocumentTag.HEARING_REQUIREMENTS, sortedTags.get(16));
        assertEquals(DocumentTag.CMA_REQUIREMENTS, sortedTags.get(17));
        assertEquals(DocumentTag.NONE, sortedTags.get(18));
    }
}
