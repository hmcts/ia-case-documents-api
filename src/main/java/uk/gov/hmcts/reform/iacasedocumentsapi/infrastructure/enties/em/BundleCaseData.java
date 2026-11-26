package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.enties.em;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;

public class BundleCaseData implements CaseData {

    private List<IdValue<Bundle>> caseBundles;

    @JsonCreator
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
