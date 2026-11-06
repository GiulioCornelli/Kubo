package kubo.storage.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kubo.storage.dto.ObjectInfoResponse;
import kubo.storage.service.StorageService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/objects")
@RequiredArgsConstructor
public class ObjectController {

    private final StorageService storageService;

    @GetMapping("/{key}")
    public ObjectInfoResponse info(@PathVariable String key) {
        return storageService.head(key);
    }
}
