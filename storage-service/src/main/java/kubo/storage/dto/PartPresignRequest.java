package kubo.storage.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import kubo.storage.validation.ValidKey;

public record PartPresignRequest(
        @ValidKey String key,
        @NotBlank String uploadId,
        @Min(1) @Max(10000) int partNumber) {
}
