package dev.enzosantos.docbox.domain.payloads;

public interface AbstractRequestRejectedPayload extends AbstractRequestEvaluatedPayload {
  String getRejectionReason();
}
