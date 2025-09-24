package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCase;

@ExtendWith(MockitoExtension.class)
class StartEventDetailsTest {

    private Event eventId;
    private String token = "eventToken";
    private long caseId = 1234;
    private String jurisdiction = "IA";

    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private CaseDetails<AsylumCase> expectedCaseDetails;
    @Mock private AsylumCase asylumCase;

    private StartEventDetails startEventDetails;

    @Test
    void should_test_equals_contract() {

        EqualsVerifier
            .forClass(StartEventDetails.class)
            .usingGetClass()
            .withPrefabValues(CaseDetails.class, caseDetails, expectedCaseDetails)
            .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS, Warning.STRICT_HASHCODE)
            .verify();
    }

    @Test
    void should_hold_onto_values() {

        eventId = Event.UPDATE_PAYMENT_STATUS;

        when(caseDetails.getId()).thenReturn(caseId);
        when(caseDetails.getState()).thenReturn(State.APPEAL_SUBMITTED);
        when(caseDetails.getJurisdiction()).thenReturn(jurisdiction);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        startEventDetails = new StartEventDetails(eventId, token, caseDetails);

        assertEquals(Event.UPDATE_PAYMENT_STATUS, startEventDetails.getEventId());
        assertEquals(token, startEventDetails.getToken());
        assertEquals(caseId, startEventDetails.getCaseDetails().getId());
        assertEquals(jurisdiction, startEventDetails.getCaseDetails().getJurisdiction());
        assertEquals(State.APPEAL_SUBMITTED, startEventDetails.getCaseDetails().getState());
        assertEquals(asylumCase, startEventDetails.getCaseDetails().getCaseData());
    }
}
