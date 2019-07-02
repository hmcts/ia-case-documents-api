package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.DECISION_AND_REASONS_COVER_LETTER;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;

@RunWith(MockitoJUnitRunner.class)
public class SendDecisionAndReasonsCoverLetterServiceTest {

    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private DocumentCreator<AsylumCase> decisionAndReasonsCoverLetterDocumentCreator;
    @Mock private Document generatedCoverLetter;

    private SendDecisionAndReasonsCoverLetterService sendDecisionAndReasonsCoverLetterService;

    @Before
    public void setUp() {

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        sendDecisionAndReasonsCoverLetterService =
            new SendDecisionAndReasonsCoverLetterService(decisionAndReasonsCoverLetterDocumentCreator);
    }

    @Test
    public void saves_and_returns_cover_letter() {

        when(decisionAndReasonsCoverLetterDocumentCreator.create(caseDetails))
            .thenReturn(generatedCoverLetter);

        Document coverLetter = sendDecisionAndReasonsCoverLetterService.create(caseDetails);

        verify(asylumCase, times(1))
            .write(DECISION_AND_REASONS_COVER_LETTER, generatedCoverLetter);

        assertThat(coverLetter).isEqualTo(generatedCoverLetter);
    }
}