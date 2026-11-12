package kubo.storage.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import kubo.storage.validation.ValidKey;

// size: massimo 5 GiB, il limite di S3 per un PUT singolo
public record UploadPresignRequest(
        @ValidKey String key,
        @Positive @Max(5_368_709_120L) long size,
        @NotBlank @Pattern(regexp = "^[\\w.+-]+/[\\w.+-]+$", message = "contentType must be a MIME type") String contentType) {
}
