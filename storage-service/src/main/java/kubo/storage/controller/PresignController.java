package kubo.storage.controller;

import java.time.Duration;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kubo.storage.config.S3Properties;
import kubo.storage.dto.PresignRequest;
import kubo.storage.dto.PresignResponse;
import kubo.storage.dto.UploadPresignRequest;
import kubo.storage.dto.UploadPresignResponse;
import kubo.storage.service.StorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController 
@RequestMapping("/api/presign")
@RequiredArgsConstructor
public class PresignController {

    private final S3Properties props;
    private final StorageService storageService;

    @PostMapping("/upload")
    public UploadPresignResponse upload(@Valid @RequestBody UploadPresignRequest request) {
        return storageService.presignUpload(request.key(), request.size(), request.contentType(), props.presignTtl());
    }

    @PostMapping("/download")
    public PresignResponse download(@Valid @RequestBody PresignRequest request) {
        Duration ttl = props.presignTtl();
        String url = storageService.presignDownload(request.key(), ttl);
        return new PresignResponse(request.key(), url, ttl.toSeconds());
    }

    @PostMapping("/delete")
    public PresignResponse delete(@Valid @RequestBody PresignRequest request) {
        Duration ttl = props.presignTtl();
        String url = storageService.presignDelete(request.key(), ttl);
        return new PresignResponse(request.key(), url, ttl.toSeconds());
    }
    

}
