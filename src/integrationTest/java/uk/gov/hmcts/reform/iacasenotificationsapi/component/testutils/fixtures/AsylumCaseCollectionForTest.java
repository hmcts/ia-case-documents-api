package uk.gov.hmcts.reform.iacasenotificationsapi.component.testutils.fixtures;

import java.util.ArrayList;
import java.util.List;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;

public class AsylumCaseCollectionForTest<T> implements Builder<List<IdValue<T>>> {

    private List<IdValue<T>> values = new ArrayList<>();

    public static <T> AsylumCaseCollectionForTest someListOf(Class<T> clazz) {
        return new AsylumCaseCollectionForTest<T>();
    }

    public AsylumCaseCollectionForTest<T> with(T value) {

        IdValue<T> idValue = new IdValue<>(
            String.valueOf(values.size()),
            value);

        values.add(idValue);

        return this;
    }

    @Override
    public List<IdValue<T>> build() {
        return values;
    }
}
