package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

import lombok.*;

@EqualsAndHashCode
@ToString
public class Message {
    private String messageHeader;
    private String messageBody;

    public Message() {

    }

    public Message(String messageHeader, String messageBody) {
        this.messageHeader = messageHeader;
        this.messageBody = messageBody;
    }

    public String getMessageHeader() {
        return messageHeader;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageHeader(String messageHeader) {
        this.messageHeader = messageHeader;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }
}
