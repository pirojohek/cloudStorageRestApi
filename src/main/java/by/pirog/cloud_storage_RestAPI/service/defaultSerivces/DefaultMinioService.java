package by.pirog.cloud_storage_RestAPI.service.defaultSerivces;

import by.pirog.cloud_storage_RestAPI.exception.customExceptions.ResourceNotFoundException;
import by.pirog.cloud_storage_RestAPI.exception.customExceptions.UnknownException;
import by.pirog.cloud_storage_RestAPI.service.interfaces.MinioService;
import io.minio.*;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;
import java.util.stream.StreamSupport;


@Service
@RequiredArgsConstructor
public class DefaultMinioService implements MinioService {

    private final MinioClient minioClient;

    @Override
    public Iterable<Result<Item>> getListObjectsByPrefix(String bucket, String prefix){
        return minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucket)
                        .prefix(prefix)
                        .recursive(true)
                        .build()
        );
    }

    @Override
    public Long getSizeFolder(String bucket, String path) {
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder().bucket(bucket)
                            .prefix(path).recursive(true).build()
            );
            long totalSize = 0;
            for (Result<Item> result : results) {
                Item item = result.get();
                totalSize+= item.size();
            }
            return totalSize;
        } catch (Exception e) {
            throw new UnknownException("Failed to get size folder");
        }
    }

    @Override
    public void putObjectToFolder(String bucket, String objectName,
                                  InputStream inputStream, long fileSize, String contentType) throws Exception{
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectName)
                        .stream(inputStream, fileSize, -1)
                        .contentType(contentType)
                        .build()
        );
    }

    @Override
    public InputStream getInputStreamObject(String bucket, String objectName) throws Exception {
        return minioClient.getObject(GetObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectName)
                        .build());
    }

    @Override
    public Iterable<Result<Item>> getListObjectsByPrefixNotRecursive(String bucket, String prefix) {
        return minioClient.listObjects(
                ListObjectsArgs.builder().bucket(bucket).prefix(prefix).build());
    }

    @Override
    public void deleteObjectsByResultItems(String bucket, Iterable<Result<Item>> objects) {
        List<DeleteObject> objectsToDelete = StreamSupport.stream(objects.spliterator(), false)
                .map(r -> {
                    try {
                        return new DeleteObject(r.get().objectName());
                    } catch (Exception e) {
                        throw new UnknownException("Failed to get object name");
                    }
                })
                .toList();

        if (objectsToDelete.isEmpty()) {
            throw new ResourceNotFoundException("Folder or file does not exist");
        }

        minioClient.removeObjects(
                RemoveObjectsArgs.builder()
                        .bucket(bucket)
                        .objects(objectsToDelete)
                        .build()
        );
    }


    @Override
    public boolean isFolderExists(String bucket, String prefix) {
        return minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucket)
                        .prefix(prefix)
                        .recursive(false)
                        .build()
        ).iterator().hasNext();
    }

    @Override
    public StatObjectResponse getStatObject(String bucket, String resource) throws Exception{
        return minioClient.statObject(
                StatObjectArgs.builder()
                        .bucket(bucket)
                        .object(resource)
                        .build()
        );
    }

}
