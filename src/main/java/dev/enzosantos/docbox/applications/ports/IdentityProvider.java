package dev.enzosantos.docbox.applications.ports;

import java.util.UUID;

public interface IdentityProvider {
  UUID provideUuid();
}
