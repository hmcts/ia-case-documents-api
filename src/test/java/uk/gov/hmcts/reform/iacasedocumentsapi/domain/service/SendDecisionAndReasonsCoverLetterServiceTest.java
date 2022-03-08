package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.DECISION_AND_REASONS_COVER_LETTER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.JOURNEY_TYPE;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.JourneyType;

@ExtendWith(MockitoExtension.class)
class SendDecisionAndReasonsCoverLetterServiceTest {

    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private DocumentCreator<AsylumCase> decisionAndReasonsCoverLetterDocumentCreator;
    @Mock
    private DocumentCreator<AsylumCase> aipDecisionAndReasonsCoverLetterDocumentCreator;
    @Mock
    private Document generatedCoverLetter;

    private SendDecisionAndReasonsCoverLetterService sendDecisionAndReasonsCoverLetterService;

    @BeforeEach
    public void setUp() {
        sendDecisionAndReasonsCoverLetterService =
            new SendDecisionAndReasonsCoverLetterService(decisionAndReasonsCoverLetterDocumentCreator, aipDecisionAndReasonsCoverLetterDocumentCreator);
    }

    @Test
    void saves_and_returns_cover_letter() {

        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(decisionAndReasonsCoverLetterDocumentCreator.create(caseDetails))
            .thenReturn(generatedCoverLetter);

        Document coverLetter = sendDecisionAndReasonsCoverLetterService.create(caseDetails);

        verify(asylumCase, times(1))
            .write(DECISION_AND_REASONS_COVER_LETTER, generatedCoverLetter);

        assertEquals((coverLetter), generatedCoverLetter);
    }

    @Test
    void saves_and_returns_cover_letter_for_aip_journey() {

        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getCaseData().read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        when(aipDecisionAndReasonsCoverLetterDocumentCreator.create(caseDetails))
            .thenReturn(generatedCoverLetter);

        Document coverLetter = sendDecisionAndReasonsCoverLetterService.create(caseDetails);

        verify(asylumCase, times(1))
            .write(DECISION_AND_REASONS_COVER_LETTER, generatedCoverLetter);

        assertEquals((coverLetter), generatedCoverLetter);
    }
}
