package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.enties.em;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;

class BundleCaseDataTest {

    private List<IdValue<Bundle>> bundles = Collections.emptyList();
    private BundleCaseData bundleCaseData = new BundleCaseData(bundles);

    @Test
    void should_hold_onto_values() {
        assertEquals(bundles, bundleCaseData.getCaseBundles());
    }
}
