package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.CONTACT_PREFERENCE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.EMAIL;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.MOBILE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ContactPreference;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.Subscriber;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import org.springframework.stereotype.Service;

@Service
public class RecipientFinder {

  public Set<String> findAll(
      AsylumCase asylumCase,
      NotificationType notificationType) {
    requireNonNull(asylumCase, "asylumCase must not be null");

    final Optional<List<IdValue<Subscriber>>> maybeSubscribers = asylumCase.read(
        AsylumCaseDefinition.SUBSCRIPTIONS);

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

  public Set<String> findReppedAppellant(
      AsylumCase asylumCase,
      NotificationType notificationType) {
    requireNonNull(asylumCase, "asylumCase must not be null");

    final ContactPreference contactPreference =
        asylumCase.read(CONTACT_PREFERENCE, ContactPreference.class).orElse(null);

    switch (notificationType) {
      case SMS:
        if (contactPreference == ContactPreference.WANTS_SMS) {
          Optional<String> appellantSms = asylumCase.read(MOBILE_NUMBER, String.class);
          return appellantSms.map(Collections::singleton).orElse(Collections.emptySet());
        } else {
          return Collections.emptySet();
        }

      case EMAIL:
        if (contactPreference == ContactPreference.WANTS_EMAIL) {
          Optional<String> appellantEmail = asylumCase.read(EMAIL, String.class);
          return appellantEmail.map(Collections::singleton).orElse(Collections.emptySet());
        } else {
          return Collections.emptySet();
        }

      default:
        throw new IllegalArgumentException("Notification type must be one of 'SMS', 'EMAIL'");

    }
  }
}

