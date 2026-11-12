package kubo.storage.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record PartInfo(
        @Min(1) @Max(10000) int partNumber,
        @NotBlank String eTag) {
}
