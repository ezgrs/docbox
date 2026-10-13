package dev.enzosantos.docbox.applications.ports;

import java.time.Instant;

public interface TimeProvider {
  Instant provideInstant();
}
