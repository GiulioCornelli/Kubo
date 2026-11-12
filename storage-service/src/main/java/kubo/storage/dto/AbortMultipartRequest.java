package kubo.storage.dto;

import jakarta.validation.constraints.NotBlank;
import kubo.storage.validation.ValidKey;

public record AbortMultipartRequest(
        @ValidKey String key,
        @NotBlank String uploadId) {
}
