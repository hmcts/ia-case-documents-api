package uk.gov.hmcts.reform.iacasenotificationsapi.domain.service;

import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;

@Service
public class DirectionFinder {

    public Optional<Direction> findFirst(
        AsylumCase asylumCase,
        DirectionTag directionTag
    ) {
        return
            asylumCase
                .getDirections()
                .orElseThrow(() -> new IllegalStateException("directions is not present"))
                .stream()
                .map(IdValue::getValue)
                .filter(direction -> direction.getTag() == directionTag)
                .findFirst();
    }
}
