package kubo.storage.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kubo.s3")
public record S3Properties(
        String endpoint,
        String publicEndpoint,
        String region,
        String bucket,
        String accessKey,
        String secretKey,
        Duration presignTtl,
        Duration multipartMaxAge) {

    public S3Properties{
        if (publicEndpoint == null || publicEndpoint.isBlank())publicEndpoint = endpoint;
    }

}

