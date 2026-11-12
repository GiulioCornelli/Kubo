package kubo.storage.controller;

import java.time.Duration;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import kubo.storage.config.S3Properties;
import kubo.storage.dto.AbortMultipartRequest;
import kubo.storage.dto.CompleteMultipartRequest;
import kubo.storage.dto.InitiateMultipartRequest;
import kubo.storage.dto.InitiateMultipartResponse;
import kubo.storage.dto.MultipartPlanRequest;
import kubo.storage.dto.MultipartPlanResponse;
import kubo.storage.dto.ObjectInfoResponse;
import kubo.storage.dto.PartPresignRequest;
import kubo.storage.dto.PresignResponse;
import kubo.storage.service.StorageService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/multipart")
@RequiredArgsConstructor
public class MultipartController {

    private final S3Properties props;
    private final StorageService storageService;

    @PostMapping("/plan")
    public MultipartPlanResponse plan(@Valid @RequestBody MultipartPlanRequest request) {
        return storageService.planMultipart(request.size());
    }

    @PostMapping("/initiate")
    public InitiateMultipartResponse initiate(@Valid @RequestBody InitiateMultipartRequest request) {
        String uploadId = storageService.initiateMultipart(request.key(), request.contentType());
        return new InitiateMultipartResponse(request.key(), uploadId);
    }

    @PostMapping("/part-url")
    public PresignResponse partUrl(@Valid @RequestBody PartPresignRequest request) {
        Duration ttl = props.presignTtl();
        String url = storageService.presignPart(request.key(), request.uploadId(), request.partNumber(), ttl);
        return new PresignResponse(request.key(), url, ttl.toSeconds());
    }

    @PostMapping("/complete")
    public ObjectInfoResponse complete(@Valid @RequestBody CompleteMultipartRequest request) {
        return storageService.completeMultipart(request.key(), request.uploadId(), request.parts());
    }

    @PostMapping("/abort")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void abort(@Valid @RequestBody AbortMultipartRequest request) {
        storageService.abortMultipart(request.key(), request.uploadId());
    }
}
