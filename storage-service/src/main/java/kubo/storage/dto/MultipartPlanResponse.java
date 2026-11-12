package kubo.storage.dto;

// multipart = false: basta un PUT singolo (partSize e partCount valgono 0)
public record MultipartPlanResponse(boolean multipart, long partSize, int partCount) {
}
