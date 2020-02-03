package uk.gov.hmcts.reform.iacasenotificationsapi.domain.service;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.SUBSCRIPTIONS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Subscriber;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;

@Service
public class RecipientsFinder {

    public Set<String> findAll(
        AsylumCase asylumCase,
        NotificationType notificationType) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        final Optional<List<IdValue<Subscriber>>> maybeSubscribers = asylumCase.read(SUBSCRIPTIONS);

        switch (notificationType) {
            case SMS:
                return maybeSubscribers
                    .orElse(Collections.emptyList()).stream()
                    .filter(subscriber -> YES.equals(subscriber.getValue().getWantsSms()))
                    .map(subscriber -> subscriber.getValue().getMobileNumber())
                    .collect(Collectors.toSet());

            case EMAIL:
                return maybeSubscribers
                    .orElse(Collections.emptyList()).stream()
                    .filter(subscriber -> YES.equals(subscriber.getValue().getWantsEmail()))
                    .map(subscriber -> subscriber.getValue().getEmail()).collect(Collectors.toSet());

            default:
                throw new IllegalArgumentException("Notification type must be one of 'SMS', 'EMAIL'");

        }
    }
}
