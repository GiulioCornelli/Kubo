package kubo.storage.service;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import kubo.storage.dto.MultipartPlanResponse;
import kubo.storage.dto.ObjectInfoResponse;
import kubo.storage.dto.PartInfo;
import kubo.storage.dto.UploadPresignResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import kubo.storage.config.S3Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.DeleteObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.UploadPartPresignRequest;

@Service 
@Slf4j
@RequiredArgsConstructor 
public class StorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final S3Properties props;

    // Limiti del multipart S3: parti >= 5 MiB (tranne l'ultima), al massimo 10.000 parti.
    // Fino a questa dimensione basta un PUT singolo (S3 arriva a 5 GiB, ma sopra ~100 MiB il multipart permette retry e parallelismo).
    private static final long SINGLE_PUT_MAX = 100L * 1024 * 1024;
    private static final long PREFERRED_PART_SIZE = 16L * 1024 * 1024;
    private static final int MAX_PARTS = 10_000;


    // ----------------- Normal Operation -----------------
    public void uploadFile(String key, MultipartFile file) throws IOException{
        s3Client.putObject(PutObjectRequest.builder()
                .bucket(props.bucket())
                .key(key)
                .build(),
            RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
    }

    public ResponseInputStream<GetObjectResponse> downloadFile(String key){
        return s3Client.getObject(GetObjectRequest.builder()
                .bucket(props.bucket())
                .key(key)
                .build());
    }

    public void deleteFile(String key){
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(props.bucket())
                .key(key)
                .build());
    }


    // ----------------- Presigner URL -----------------
    public UploadPresignResponse presignUpload(String key, long size, String contentType, Duration ttl){
        PutObjectRequest putRequest = PutObjectRequest.builder()
            .bucket(props.bucket())
            .key(key)
            .contentLength(size)
            .contentType(contentType)
            .build();
        
        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(ttl)
                .putObjectRequest(putRequest)
                .build();

        PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(presignRequest);

        // header firmati da inviare nel PUT (host lo imposta il client HTTP)
        Map<String, String> headers = new LinkedHashMap<>();
        presigned.signedHeaders().forEach((name, values) -> {
            if (!name.equalsIgnoreCase("host")) {
                headers.put(name, String.join(",", values));
            }
        });

        return new UploadPresignResponse(key, presigned.url().toString(), ttl.toSeconds(), headers);
    }

    public String presignDownload(String key, Duration ttl){
        GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(props.bucket())
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(ttl)
                .getObjectRequest(getRequest)
                .build();

        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    public String presignDelete(String key, Duration ttl){
        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(props.bucket())
                .key(key)
                .build();

        DeleteObjectPresignRequest presignRequest = DeleteObjectPresignRequest.builder()
                .signatureDuration(ttl)
                .deleteObjectRequest(deleteRequest)
                .build();

        return s3Presigner.presignDeleteObject(presignRequest).url().toString();
    }

    public MultipartPlanResponse planMultipart(long size){
        if (size <= SINGLE_PUT_MAX) {
            return new MultipartPlanResponse(false, 0, 0);
        }
        long partSize = Math.max(PREFERRED_PART_SIZE, ceilDiv(size, MAX_PARTS));
        int partCount = (int) ceilDiv(size, partSize);
        return new MultipartPlanResponse(true, partSize, partCount);
    }

    private static long ceilDiv(long a, long b){
        return (a + b - 1) / b;
    }

    public String initiateMultipart(String key,String contentType){
        CreateMultipartUploadResponse resp = s3Client.createMultipartUpload(
                CreateMultipartUploadRequest.builder()
                        .bucket(props.bucket())
                        .key(key)
                        .contentType(contentType)
                        .build()
        );

        return resp.uploadId();
    }

    public String presignPart(String key, String uploadId, int partNumber, Duration ttl){
        UploadPartRequest partRequest = UploadPartRequest.builder()
                .bucket(props.bucket())
                .key(key)
                .uploadId(uploadId)
                .partNumber(partNumber)
                .build();

        UploadPartPresignRequest presignRequest = UploadPartPresignRequest.builder()
                .signatureDuration(ttl)
                .uploadPartRequest(partRequest)
                .build();

        return s3Presigner.presignUploadPart(presignRequest).url().toString();
    }

    public ObjectInfoResponse completeMultipart(String key, String uploadId, List<PartInfo> parts){
        List<CompletedPart> completedParts = parts.stream()
                .map(p -> CompletedPart.builder()
                        .partNumber(p.partNumber())
                        .eTag(p.eTag())
                        .build())
                .toList();

        s3Client.completeMultipartUpload(CompleteMultipartUploadRequest.builder()
                .bucket(props.bucket())
                .key(key)
                .uploadId(uploadId)
                .multipartUpload(CompletedMultipartUpload.builder()
                        .parts(completedParts)
                        .build())
                .build());

        return head(key);
    }

    public void abortMultipart(String key, String uploadId){
        s3Client.abortMultipartUpload(AbortMultipartUploadRequest.builder()
                .bucket(props.bucket())
                .key(key)
                .uploadId(uploadId)
                .build());
    }

    public int abortStaleMultipartUploads(Duration maxAge){
        Instant limit = Instant.now().minus(maxAge);
        int aborted = 0;

        ListMultipartUploadsRequest request = ListMultipartUploadsRequest.builder()
                .bucket(props.bucket())
                .build();

        for (MultipartUpload upload : s3Client.listMultipartUploadsPaginator(request).uploads()) {
            if (upload.initiated().isBefore(limit)) {
                try {
                    abortMultipart(upload.key(), upload.uploadId());
                    aborted++;
                } catch (S3Exception e) {
                    log.warn("Cannot abort multipart upload {} ({}): {}", upload.key(), upload.uploadId(), e.getMessage());
                }
            }
        }
        return aborted;
    }


    // ----------------- Check -----------------
    public ObjectInfoResponse head(String key){
        HeadObjectResponse resp = s3Client.headObject(HeadObjectRequest.builder()
                .bucket(props.bucket())
                .key(key)
                .build());

        return new ObjectInfoResponse(key, resp.contentLength(), resp.contentType());
    }
}
