package kubo.storage.job;


import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import kubo.storage.config.S3Properties;
import kubo.storage.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.exception.SdkException;

@Slf4j
@Component
public class MultipartCleanupJob {

    private final StorageService storageService;
    private final S3Properties props;

    public MultipartCleanupJob(StorageService storageService, S3Properties props) {
        this.storageService = storageService;
        this.props = props;
    }

    @Scheduled(fixedDelayString = "${kubo.s3.cleanup-interval:PT1H}")
    public void cleanup(){
        try {
            int aborted = storageService.abortStaleMultipartUploads(props.multipartMaxAge());
            if (aborted > 0) {
                log.info("Aborted {} stale multipart uploads", aborted);
            }
        } catch (SdkException e) {
            log.error("Multipart cleanup failed", e);
        }
    }
}
