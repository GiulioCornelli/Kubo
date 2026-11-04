package kubo.storage.dto;

public record ObjectInfoResponse(
        String key,
        long size,
        String contentType) {
}
