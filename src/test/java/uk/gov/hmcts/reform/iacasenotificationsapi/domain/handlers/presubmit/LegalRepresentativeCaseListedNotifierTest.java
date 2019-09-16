package uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REPRESENTATIVE_EMAIL_ADDRESS;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.LegalRepresentativePersonalisationFactory;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class LegalRepresentativeCaseListedNotifierTest {

    private final String legalRepresentativeEmailAddress = "ia-law-firm@example.com";

    @Mock private LegalRepresentativePersonalisationFactory legalRepresentativePersonalisationFactory;
    @Mock private AsylumCase asylumCase;

    private LegalRepresentativeCaseListedNotifier legalRepresentativeCaseListedNotifier;

    @Before
    public void setUp() {

        legalRepresentativeCaseListedNotifier =
            new LegalRepresentativeCaseListedNotifier(
                legalRepresentativePersonalisationFactory
            );
    }

    @Test
    public void should_throw_when_legal_representative_email_address_not_present() {

        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> legalRepresentativeCaseListedNotifier.getEmailAddress(asylumCase))
            .hasMessage("legalRepresentativeEmailAddress is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_return_legal_representative_email_address() {

        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class)).thenReturn(Optional.of(legalRepresentativeEmailAddress));

        final String actualEmailAddress = legalRepresentativeCaseListedNotifier.getEmailAddress(asylumCase);

        assertEquals(legalRepresentativeEmailAddress, actualEmailAddress);
    }

    @Test
    public void should_return_personalisation_for_legal_representative() {

        legalRepresentativeCaseListedNotifier.getPersonalisation(asylumCase);

        verify(legalRepresentativePersonalisationFactory, times(1)).createListedCase(asylumCase);
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> new LegalRepresentativeCaseListedNotifier(null))
            .hasMessage("legalRepresentativePersonalisationFactory must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
