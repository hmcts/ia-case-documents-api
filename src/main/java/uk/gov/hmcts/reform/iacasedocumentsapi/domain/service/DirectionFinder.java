package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;

@Service
public class DirectionFinder {

    public Optional<Direction> findFirst(
        AsylumCase asylumCase,
        DirectionTag directionTag
    ) {
        Optional<List<IdValue<Direction>>> maybeDirections = asylumCase
                .read(AsylumCaseDefinition.DIRECTIONS);
        return
            maybeDirections
                .orElse(Collections.emptyList())
                .stream()
                .map(IdValue::getValue)
                .filter(direction -> direction.getTag() == directionTag)
                .findFirst();
    }
}
