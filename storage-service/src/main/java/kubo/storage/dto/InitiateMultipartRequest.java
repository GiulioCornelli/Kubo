package kubo.storage.dto;

import jakarta.validation.constraints.NotBlank;
import kubo.storage.validation.ValidKey;

public record InitiateMultipartRequest(
        @ValidKey String key,
        @NotBlank String contentType) {
}
