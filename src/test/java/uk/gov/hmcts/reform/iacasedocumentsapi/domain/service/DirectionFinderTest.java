package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.DIRECTIONS;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.DirectionFinder;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class DirectionFinderTest {

    private final DirectionFinder directionFinder = new DirectionFinder();
    @Mock private AsylumCase asylumCase;
    @Mock private IdValue<Direction> existingDirectionById1;
    @Mock private IdValue<Direction> existingDirectionById2;
    @Mock private Direction existingDirection1 = mock(Direction.class);
    @Mock private Direction existingDirection2 = mock(Direction.class);

    @Test
    public void should_find_first_tagged_direction() {

        List<IdValue<Direction>> directions =
            Arrays.asList(
                existingDirectionById1,
                existingDirectionById2
            );

        when(existingDirectionById1.getValue()).thenReturn(existingDirection1);
        when(existingDirectionById2.getValue()).thenReturn(existingDirection2);

        when(existingDirection1.getTag()).thenReturn(DirectionTag.LEGAL_REPRESENTATIVE_REVIEW);
        when(existingDirection2.getTag()).thenReturn(DirectionTag.BUILD_CASE);

        when(asylumCase.read(DIRECTIONS)).thenReturn(Optional.of(directions));

        final Optional<Direction> firstFoundDirection =
            directionFinder.findFirst(asylumCase, DirectionTag.BUILD_CASE);

        assertNotNull(firstFoundDirection);
        assertTrue(firstFoundDirection.isPresent());
        assertEquals(existingDirection2, firstFoundDirection.get());
    }

    @Test
    public void should_return_empty_optional_if_not_found() {

        List<IdValue<Direction>> directions =
            Arrays.asList(
                existingDirectionById1
            );

        when(existingDirectionById1.getValue()).thenReturn(existingDirection1);

        when(existingDirection1.getTag()).thenReturn(DirectionTag.LEGAL_REPRESENTATIVE_REVIEW);

        when(asylumCase.read(DIRECTIONS)).thenReturn(Optional.of(directions));

        final Optional<Direction> firstFoundDirection =
            directionFinder.findFirst(asylumCase, DirectionTag.RESPONDENT_REVIEW);

        assertNotNull(firstFoundDirection);
        assertFalse(firstFoundDirection.isPresent());
    }
}
