package uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers;

import java.util.function.BiConsumer;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;

public interface ErrorHandler extends BiConsumer<Callback<AsylumCase>, Throwable> {
}
