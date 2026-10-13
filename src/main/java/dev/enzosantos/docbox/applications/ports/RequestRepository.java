package dev.enzosantos.docbox.applications.ports;

import dev.enzosantos.docbox.domain.requests.AbstractRequest;

public interface RequestRepository {
  void create(AbstractRequest request);
}
