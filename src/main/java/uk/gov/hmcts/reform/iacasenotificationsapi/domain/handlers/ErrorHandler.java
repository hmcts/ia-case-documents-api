package uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers;

import java.util.function.BiConsumer;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;

public interface ErrorHandler<T extends CaseData> extends BiConsumer<Callback<T>, Throwable> {
}
