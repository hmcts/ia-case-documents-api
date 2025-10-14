package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import static java.util.Objects.requireNonNull;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;

@EqualsAndHashCode
@ToString
public class BailDirection {

    private String sendDirectionDescription;
    private String sendDirectionList;
    private String dateOfCompliance;
    private String dateSent;
    private String dateTimeDirectionCreated;
    private String dateTimeDirectionModified;
    private List<IdValue<uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.PreviousDates>> previousDates;


    private BailDirection() {
    }

    public BailDirection(
        String sendDirectionDescription,
        String sendDirectionList,
        String dateOfCompliance,
        String dateSent,
        String dateTimeDirectionCreated,
        String dateTimeDirectionModified,
        List<IdValue<uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.PreviousDates>> previousDates
    ) {
        this.sendDirectionDescription = requireNonNull(sendDirectionDescription);
        this.sendDirectionList = requireNonNull(sendDirectionList);
        this.dateOfCompliance = requireNonNull(dateOfCompliance);
        this.dateSent = requireNonNull(dateSent);
        this.dateTimeDirectionCreated = requireNonNull(dateTimeDirectionCreated);
        this.dateTimeDirectionModified = dateTimeDirectionModified;
        this.previousDates = previousDates;
    }


    public String getSendDirectionDescription() {
        return requireNonNull(sendDirectionDescription);
    }

    public String getSendDirectionList() {
        return requireNonNull(sendDirectionList);
    }

    public String getDateOfCompliance() {
        return requireNonNull(dateOfCompliance);
    }

    public String getDateSent() {
        return requireNonNull(dateSent);
    }

    public String getDateTimeDirectionCreated() {
        return dateTimeDirectionCreated;
    }

    public String getDateTimeDirectionModified() {
        return dateTimeDirectionModified;
    }

    public List<IdValue<PreviousDates>> getPreviousDates() {
        return previousDates;
    }
}
