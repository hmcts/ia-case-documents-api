package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;


import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_IN_DETENTION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.IS_ADMIN;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.NOTIFICATION_ATTACHMENT_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.SEND_DIRECTION_PARTIES;


import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.Parties;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.DirectionFinder;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
@MockitoSettings(strictness = Strictness.LENIENT)
public class InternalNonStandardDirectionLetterGeneratorTest {

    @Mock
    private DocumentCreator<AsylumCase> internalNonStandardDirectionLetterCreator;
    @Mock
    private DocumentHandler documentHandler;
    @Mock
    private DirectionFinder directionFinder;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private Document uploadedDocument;
    private InternalNonStandardDirectionLetterGenerator internalNonStandardDirectionLetterGenerator;
    private final String directionExplanation = "some explanation";
    private final Parties directionParties = Parties.APPELLANT;
    private final String directionDateDue = "2023-06-16";
    private final String directionDateSent = "2023-06-02";
    private final String directionUniqueId = "95e90870-2429-4660-b9c2-4111aff37304";
    private final String directionType = "someDirectionType";

    private final Direction directionOne = new Direction(
        directionExplanation,
        directionParties,
        directionDateDue,
        directionDateSent,
        DirectionTag.NONE,
        Collections.emptyList(),
        Collections.emptyList(),
        directionUniqueId,
        directionType
    );
    private final Direction directionTwo = new Direction(
        directionExplanation,
        Parties.LEGAL_REPRESENTATIVE,
        directionDateDue,
        directionDateSent,
        DirectionTag.NONE,
        Collections.emptyList(),
        Collections.emptyList(),
        directionUniqueId,
        directionType
    );

    private final Direction directionThree = new Direction(
        directionExplanation,
        directionParties,
        directionDateDue,
        directionDateSent,
        DirectionTag.CASE_EDIT,
        Collections.emptyList(),
        Collections.emptyList(),
        directionUniqueId,
        directionType
    );


    @BeforeEach
    public void setUp() {
        internalNonStandardDirectionLetterGenerator =
            new InternalNonStandardDirectionLetterGenerator(
                internalNonStandardDirectionLetterCreator,
                documentHandler,
                directionFinder);
    }

    @Test
    public void should_create_internal_non_standard_direction_Nonada_letter_and_append_to_notifications_documents() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getCaseDetails().getCaseData().read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(
            Optional.of(YesOrNo.YES));
        when(callback.getCaseDetails().getCaseData().read(IS_ADMIN, YesOrNo.class)).thenReturn(
            Optional.of(YesOrNo.YES));
        when(directionFinder.findFirst(asylumCase, DirectionTag.NONE)).thenReturn(Optional.of(directionOne));
        when(asylumCase.read(SEND_DIRECTION_PARTIES)).thenReturn(Optional.of(Parties.APPELLANT));

        when(internalNonStandardDirectionLetterCreator.create(caseDetails)).thenReturn(uploadedDocument);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            internalNonStandardDirectionLetterGenerator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(documentHandler, times(1)).addWithMetadata(
            asylumCase, uploadedDocument,
            NOTIFICATION_ATTACHMENT_DOCUMENTS,
            DocumentTag.INTERNAL_NON_STANDARD_DIRECTION_TO_APPELLANT_LETTER
        );
    }

    @Test
    public void should_create_internal_non_standard_direction_ada_letter_and_append_to_notifications_documents() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getCaseDetails().getCaseData().read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(
            Optional.of(YesOrNo.YES));
        when(callback.getCaseDetails().getCaseData().read(IS_ADMIN, YesOrNo.class)).thenReturn(
            Optional.of(YesOrNo.YES));
        when(callback.getCaseDetails().getCaseData().read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(
            Optional.of(YesOrNo.YES));
        when(directionFinder.findFirst(asylumCase, DirectionTag.NONE)).thenReturn(Optional.of(directionOne));
        when(asylumCase.read(SEND_DIRECTION_PARTIES)).thenReturn(Optional.of(Parties.APPELLANT));

        when(internalNonStandardDirectionLetterCreator.create(caseDetails)).thenReturn(uploadedDocument);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            internalNonStandardDirectionLetterGenerator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(documentHandler, times(1)).addWithMetadata(
            asylumCase, uploadedDocument,
            NOTIFICATION_ATTACHMENT_DOCUMENTS,
            DocumentTag.INTERNAL_NON_STANDARD_DIRECTION_TO_APPELLANT_LETTER


        );
    }

    @Test
    public void incorrect_parties_test() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getCaseDetails().getCaseData().read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(
            Optional.of(YesOrNo.YES));
        when(callback.getCaseDetails().getCaseData().read(IS_ADMIN, YesOrNo.class)).thenReturn(
            Optional.of(YesOrNo.YES));
        when(internalNonStandardDirectionLetterCreator.create(caseDetails)).thenReturn(uploadedDocument);
        when(directionFinder.findFirst(asylumCase, DirectionTag.NONE)).thenReturn(Optional.of(directionTwo));

        assertThatThrownBy(
            () -> internalNonStandardDirectionLetterGenerator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void incorrect_direction_test() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getCaseDetails().getCaseData().read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(
            Optional.of(YesOrNo.YES));
        when(callback.getCaseDetails().getCaseData().read(IS_ADMIN, YesOrNo.class)).thenReturn(
            Optional.of(YesOrNo.YES));
        when(internalNonStandardDirectionLetterCreator.create(caseDetails)).thenReturn(uploadedDocument);
        when(directionFinder.findFirst(asylumCase, DirectionTag.NONE)).thenReturn(Optional.of(directionThree));

        assertThatThrownBy(
            () -> internalNonStandardDirectionLetterGenerator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }


    @Test
    public void it_cannot_handle_callback_if_is_admin_is_missing() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);
            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(asylumCase);
            when(callback.getCaseDetails().getCaseData().read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(
                Optional.of(YesOrNo.YES));

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = internalNonStandardDirectionLetterGenerator.canHandle(callbackStage, callback);
                Assertions.assertFalse(canHandle);
            }
            Mockito.reset(callback);
        }
    }

    @Test
    public void it_cannot_handle_callback_if_is_detained_is_missing() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);
            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(asylumCase);
            when(callback.getCaseDetails().getCaseData().read(IS_ADMIN, YesOrNo.class)).thenReturn(
                Optional.of(YesOrNo.YES));

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = internalNonStandardDirectionLetterGenerator.canHandle(callbackStage, callback);
                Assertions.assertFalse(canHandle);
            }
            Mockito.reset(callback);
        }
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> internalNonStandardDirectionLetterGenerator.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
            () -> internalNonStandardDirectionLetterGenerator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
                null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> internalNonStandardDirectionLetterGenerator.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
            () -> internalNonStandardDirectionLetterGenerator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
                null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}