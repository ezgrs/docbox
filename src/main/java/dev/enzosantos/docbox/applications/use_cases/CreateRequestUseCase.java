package dev.enzosantos.docbox.applications.use_cases;

import dev.enzosantos.docbox.applications.ports.IdentityProvider;
import dev.enzosantos.docbox.applications.ports.RequestRepository;
import dev.enzosantos.docbox.applications.ports.TimeProvider;
import dev.enzosantos.docbox.domain.requests.Request;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class CreateRequestUseCase {
  private final RequestRepository requestRepository;
  private final IdentityProvider identityProvider;
  private final TimeProvider timeProvider;

  public CreateRequestUseCase(
      RequestRepository requestRepository,
      IdentityProvider identityProvider,
      TimeProvider timeProvider) {
    this.requestRepository = requestRepository;
    this.identityProvider = identityProvider;
    this.timeProvider = timeProvider;
  }

  public void execute(String name, Optional<String> description, UUID userId) {
    final UUID id = identityProvider.provideUuid();
    final Instant createdAt = timeProvider.provideInstant();
    this.requestRepository.create(
        new Request() {
          @Override
          public UUID getId() {
            return id;
          }

          @Override
          public String getName() {
            return name;
          }

          @Override
          public Optional<String> getDescription() {
            return description;
          }

          @Override
          public Instant getCreatedAt() {
            return createdAt;
          }

          @Override
          public Instant getUpdatedAt() {
            return createdAt;
          }

          @Override
          public UUID getCreatorId() {
            return userId;
          }
        });
  }
}
