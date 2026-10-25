package kubo.storage.controller;

import java.io.IOException;
import java.util.UUID;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import kubo.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StorageController {

    private final StorageService storageService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) throws IOException {
        String key = UUID.randomUUID().toString();
        storageService.uploadFile(key, file);
        return ResponseEntity.ok("file is added key:"+key);
    }

    @GetMapping("/download/{key}")
    public ResponseEntity<InputStreamResource> download(@PathVariable String key, @RequestParam(defaultValue = "download") String filename) {
        ResponseInputStream<GetObjectResponse> object = storageService.downloadFile(key);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(object.response().contentLength())
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(filename).build().toString())
                .body(new InputStreamResource(object));
    }

}
