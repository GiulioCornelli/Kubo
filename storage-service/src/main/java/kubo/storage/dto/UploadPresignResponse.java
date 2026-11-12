package kubo.storage.dto;

import java.util.Map;

// headers: quelli che il client deve inviare nel PUT, altrimenti la firma non corrisponde
public record UploadPresignResponse(String key, String url, long expiresInSeconds, Map<String, String> headers) {
}
