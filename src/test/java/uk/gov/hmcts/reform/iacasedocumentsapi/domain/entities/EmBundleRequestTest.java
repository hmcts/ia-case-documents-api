package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;

@ExtendWith(MockitoExtension.class)
class EmBundleRequestTest {

    @Mock private Callback<AsylumCase> callback;

    private EmBundleRequest emBundleRequest;

    @Test
    void should_hold_onto_values() {

        emBundleRequest = new EmBundleRequest(callback);

        assertThat(emBundleRequest.getCaseTypeId()).isEqualTo("Asylum");
        assertThat(emBundleRequest.getJurisdictionId()).isEqualTo("IA");
        assertThat(emBundleRequest.getCallback()).isEqualTo(callback);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> new EmBundleRequest(null))
                .isExactlyInstanceOf(NullPointerException.class);
    }
}
