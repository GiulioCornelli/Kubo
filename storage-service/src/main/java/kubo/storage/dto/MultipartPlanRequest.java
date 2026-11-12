package kubo.storage.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;

// 5 TiB: dimensione massima di un oggetto S3
public record MultipartPlanRequest(@Positive @Max(5_497_558_138_880L) long size) {
}
