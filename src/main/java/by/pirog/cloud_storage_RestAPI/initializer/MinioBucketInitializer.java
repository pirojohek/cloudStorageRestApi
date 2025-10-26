package by.pirog.cloud_storage_RestAPI.initializer;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Log4j2
@RequiredArgsConstructor
public class MinioBucketInitializer {

    @Value("${minio.bucket}")
    private String bucketName;

    private final MinioClient minioClient;

    @EventListener(ApplicationReadyEvent.class)
    public void initializeBucketMinio() {
        try{

            log.info("Creating bucket after startup");
            boolean isBucketExists = this.minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName).build());
            if (!isBucketExists){
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
