package kubo.storage.dto;

import kubo.storage.validation.ValidKey;

public record PresignRequest(@ValidKey String key) {

}
