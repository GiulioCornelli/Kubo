package kubo.storage.service;

import java.io.IOException;
import java.time.Duration;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import kubo.storage.config.S3Properties;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service 
@RequiredArgsConstructor 
public class StorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final S3Properties props;


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


    // ----------------- Presigner URL -----------------
    public String presingUpload(String key, Duration ttl){
        PutObjectRequest putRequest = PutObjectRequest.builder()
            .bucket(props.bucket())
            .key(key)
            .build();
        
        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(ttl)
                .putObjectRequest(putRequest)
                .build();
        
        return s3Presigner.presignPutObject(presignRequest).url().toString();
    }
}
