package kubo.storage.dto;

public record PresignResponse(String key, String url, long expiresInSeconds) {

}
