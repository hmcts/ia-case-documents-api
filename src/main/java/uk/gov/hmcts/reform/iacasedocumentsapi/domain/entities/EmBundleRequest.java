package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import static java.util.Objects.requireNonNull;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;

@Getter
@EqualsAndHashCode
public class EmBundleRequest<T extends CaseData> {

    private String caseTypeId;
    private String jurisdictionId;
    private Callback<T> callback;

    private EmBundleRequest() {
    }

    public EmBundleRequest(Callback<T> callback) {
        requireNonNull(callback);

        this.caseTypeId = "Asylum";
        this.jurisdictionId = "IA";
        this.callback = callback;
    }

}
