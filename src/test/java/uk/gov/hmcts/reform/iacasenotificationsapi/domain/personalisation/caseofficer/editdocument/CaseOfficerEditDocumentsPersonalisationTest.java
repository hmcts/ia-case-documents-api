package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer.editdocument;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.CaseNote;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.AppealService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;

@RunWith(JUnitParamsRunner.class)
public class CaseOfficerEditDocumentsPersonalisationTest {

    public static final String DOC_ID = "d209e64c-b8fe-4ffa-8f8b-c7ae922c6b65";
    public static final String DOC_ID2 = "ba21d046-6edf-42c4-9b17-1511488a57da";
    public static final String DOC_ID3 = "9e04bf7e-2d99-4fa1-8fa9-741727b48991";
    @Rule
    public MockitoRule rule = MockitoJUnit.rule().strictness(Strictness.LENIENT);
    @Mock
    private EmailAddressFinder emailAddressFinder;
    @Mock
    private EditDocumentService editDocumentService;
    @Mock
    private AppealService appealService;
    @Mock
    private AsylumCase asylumCase;

    private CaseOfficerEditDocumentsPersonalisation personalisation;

    private final String beforeListingTemplateId = "beforeListingTemplateId";
    private final String afterListingTemplateId = "afterListingTemplateId";

    @Captor
    private ArgumentCaptor<List<String>> argCaptor;

    @Before
    public void setUp() {
        personalisation = new CaseOfficerEditDocumentsPersonalisation(
            beforeListingTemplateId,
            afterListingTemplateId,
            emailAddressFinder,
            editDocumentService,
            "http://localhost",
            appealService);
    }

    @Test
    public void getReferenceId() {
        assertEquals("1234_APPEAL_DOCUMENT_DELETED_CASE_OFFICER", personalisation.getReferenceId(1234L));
    }

    @Test
    public void getTemplateId() {
        when(appealService.isAppealListed(asylumCase)).thenReturn(false);
        assertEquals(beforeListingTemplateId, personalisation.getTemplateId(asylumCase));

        when(appealService.isAppealListed(asylumCase)).thenReturn(true);
        assertEquals(afterListingTemplateId, personalisation.getTemplateId(asylumCase));
    }

    @Test
    public void getRecipientsList() {
        given(emailAddressFinder.getHearingCentreEmailAddress(any(AsylumCase.class))).willReturn("hearingCentre@email.com");

        assertTrue(personalisation.getRecipientsList(new AsylumCase()).contains("hearingCentre@email.com"));
    }

    @Test
    @Parameters(method = "generateDifferentCaseNotesScenarios")
    public void getPersonalisation(Callback<AsylumCase> callback, String expectedReason) {
        FormattedDocument formattedDocument = new FormattedDocument("some file name", "some desc");
        given(editDocumentService.getFormattedDocumentsGivenCaseAndDocIds(any(), any()))
            .willReturn(new FormattedDocumentList(Collections.singletonList(formattedDocument)));

        Map<String, String> actualPersonalisation = personalisation.getPersonalisation(callback);

        assertEquals("RP/50001/2020", actualPersonalisation.get("appealReferenceNumber"));
        assertEquals("Lacy Dawson", actualPersonalisation.get("appellantGivenNames"));
        assertEquals("Venus Blevins", actualPersonalisation.get("appellantFamilyName"));
        assertEquals("http://localhost", actualPersonalisation.get("linkToOnlineService"));
        assertEquals(expectedReason, actualPersonalisation.get("reasonForEditingOrDeletingDocuments"));

        then(editDocumentService).should(times(1))
            .getFormattedDocumentsGivenCaseAndDocIds(any(AsylumCase.class), argCaptor.capture());

        List<String> actualDocsIds = argCaptor.getValue();
        assertThat(actualDocsIds).containsOnly(DOC_ID, DOC_ID2);
    }

    private Object[] generateDifferentCaseNotesScenarios() {
        String multiLineReason = "line 1 reason" + System.lineSeparator() + "line 2 reason";
        String singleLine = "line 1 reason";
        return new Object[] {
            new Object[] {generateSingleCaseNoteWithMultiLineReason(), multiLineReason},
            new Object[] {generateSingleCaseNoteWithSingleLineReason(), singleLine},
            new Object[] {generateTwoCaseNotesWithMultiLineReasons(), multiLineReason},
        };
    }

    private Callback<AsylumCase> generateTwoCaseNotesWithMultiLineReasons() {
        String multiLineReason = "line 1 reason" + System.lineSeparator() + "line 2 reason";
        IdValue<CaseNote> idCaseNote1 = buildCaseNote(multiLineReason);

        String singleLine = "line 1 reason";
        IdValue<CaseNote> idCaseNote2 = buildCaseNote(singleLine);
        AsylumCase asylumCase = new AsylumCase();
        writeCaseNote(asylumCase, Arrays.asList(idCaseNote1, idCaseNote2));
        return buildTestCallback(asylumCase);
    }

    private Callback<AsylumCase> generateSingleCaseNoteWithMultiLineReason() {
        String multiLineReason = "line 1 reason" + System.lineSeparator() + "line 2 reason";
        IdValue<CaseNote> idCaseNote = buildCaseNote(multiLineReason);
        AsylumCase asylumCase = new AsylumCase();
        writeCaseNote(asylumCase, Collections.singletonList(idCaseNote));
        return buildTestCallback(asylumCase);
    }

    private Callback<AsylumCase> generateSingleCaseNoteWithSingleLineReason() {
        String singleLine = "line 1 reason";
        IdValue<CaseNote> idCaseNote = buildCaseNote(singleLine);
        AsylumCase asylumCase = new AsylumCase();
        writeCaseNote(asylumCase, Collections.singletonList(idCaseNote));
        return buildTestCallback(asylumCase);
    }

    private Callback<AsylumCase> buildTestCallback(AsylumCase asylumCase) {
        CaseDetails<AsylumCase> caseDetails = new CaseDetails<>(1L, "IA", State.APPEAL_SUBMITTED,
            asylumCase, LocalDateTime.now());
        CaseDetails<AsylumCase> caseDetailsBefore = new CaseDetails<>(1L, "IA", State.APPEAL_SUBMITTED,
            new AsylumCase(), LocalDateTime.now());
        return new Callback<>(caseDetails, Optional.of(caseDetailsBefore), Event.EDIT_DOCUMENTS);
    }

    private void writeCaseNote(AsylumCase asylumCase, List<IdValue<CaseNote>> caseNoteList) {
        asylumCase.write(CASE_NOTES, caseNoteList);
        asylumCase.write(APPEAL_REFERENCE_NUMBER, "RP/50001/2020");
        asylumCase.write(APPELLANT_GIVEN_NAMES, "Lacy Dawson");
        asylumCase.write(APPELLANT_FAMILY_NAME, "Venus Blevins");
    }

    private IdValue<CaseNote> buildCaseNote(String reason) {
        CaseNote caseNote = new CaseNote("subject", buildCaseNoteDescription(reason), "user",
            LocalDate.now().toString());
        return new IdValue<>("1", caseNote);
    }

    private String buildCaseNoteDescription(String reason) {
        return String.format("documentIds: %s" + System.lineSeparator() + "reason: %s" + System.lineSeparator(),
            Arrays.asList(DOC_ID, DOC_ID2), reason);
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {
        assertThatThrownBy(() -> personalisation.getPersonalisation((Callback<AsylumCase>) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");
    }
}
