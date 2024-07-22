package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.*;

import uk.gov.hmcts.reform.iacasenotificationsapi.domain.BailCaseUtils;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;

public interface BailEmailNotificationPersonalisation extends BaseNotificationPersonalisation<BailCase> {

    default String decision(BailCase bailCase) {

        Boolean bailGranted = BailCaseUtils.isBailGranted(bailCase);

        if (bailGranted == null) {
            return "";
        }

        return bailGranted
            ? "Granted"
            : "Refused";
    }

    default boolean isLegallyRepresented(BailCase bailCase) {
        return (bailCase.read(IS_LEGALLY_REPRESENTED_FOR_FLAG, YesOrNo.class).orElse(YesOrNo.NO)) == YesOrNo.YES;
    }
}
