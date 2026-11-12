package kubo.storage.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import kubo.storage.validation.ValidKey;

public record CompleteMultipartRequest(
        @ValidKey String key,
        @NotBlank String uploadId,
        @NotEmpty @Valid List<PartInfo> parts) {
}
