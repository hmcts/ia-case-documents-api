package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MessageTest {

    private final String header = "success";
    private final String body = "success body";


    Message message =
        new Message(
            header,
            body
        );

    @Test
    void should_hold_onto_values() {
        assertEquals(header, message.getMessageHeader());
        assertEquals(body, message.getMessageBody());
    }
}
