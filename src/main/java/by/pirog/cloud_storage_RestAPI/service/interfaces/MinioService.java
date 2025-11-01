package by.pirog.cloud_storage_RestAPI.service.interfaces;

import io.minio.MinioClient;
import io.minio.Result;
import io.minio.StatObjectResponse;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

public interface MinioService {

    Iterable<Result<Item>> getListObjectsByPrefix(String bucket, String prefix);

    void deleteObjectsByResultItems(String bucket, Iterable<Result<Item>> objects);

    boolean isFolderExists(String bucket, String prefix);

    StatObjectResponse getStatObject(String bucket, String resource) throws Exception;

    Long getSizeFolder(String bucket, String path);

    void putObjectToFolder(String bucket, String objectName,
                           InputStream inputStream, long fileSize, String contentType) throws Exception;

    InputStream getInputStreamObject(String bucket, String objectName) throws Exception;
}