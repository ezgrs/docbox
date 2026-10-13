package dev.enzosantos.docbox.domain.requests;

public interface AbstractRejectedRequest extends AbstractEvaluatedRequest {
  String getRejectionReason();
}
