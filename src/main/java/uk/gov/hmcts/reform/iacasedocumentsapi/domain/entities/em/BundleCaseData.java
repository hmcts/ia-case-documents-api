package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.em;

import java.util.List;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;

public class BundleCaseData implements CaseData {

    private List<IdValue<Bundle>> caseBundles;

    private BundleCaseData() {
        // noop -- for deserializer
    }

    public BundleCaseData(
        List<IdValue<Bundle>> caseBundles
    ) {
        this.caseBundles = caseBundles;
    }

    public List<IdValue<Bundle>> getCaseBundles() {
        return caseBundles;
    }
}
