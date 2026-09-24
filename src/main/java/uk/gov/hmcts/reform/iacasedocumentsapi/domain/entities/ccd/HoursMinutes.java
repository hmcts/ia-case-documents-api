package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class HoursMinutes {

    private int hours;
    private int minutes;

    public HoursMinutes() {
        // noop -- for deserializer
    }

    public HoursMinutes(Integer hours, Integer minutes) {
        int hrs = (hours != null) ? hours : 0;
        int mins = (minutes != null) ? minutes : 0;
        this.hours = hrs + (mins / 60);
        this.minutes = mins % 60;
    }

    public HoursMinutes(Integer minutes) {
        this(0, minutes);
    }

    public int getHours() {
        return hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public int convertToIntegerMinutes() {
        return (hours * 60) + minutes;
    }

    public String convertToPhrasalValue() {
        String stringHours = "";
        if (hours > 0) {
            stringHours = hours + " hour" + ((hours > 1) ? "s " : " ");
        }

        String stringMinutes = "";
        if (minutes > 0) {
            stringMinutes = minutes + " minute" + ((minutes > 1) ? "s " : " ");
        }

        return (stringHours + stringMinutes).trim();
    }
}
