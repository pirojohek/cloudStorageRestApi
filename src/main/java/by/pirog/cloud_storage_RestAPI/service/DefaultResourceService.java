package by.pirog.cloud_storage_RestAPI.service;

import by.pirog.cloud_storage_RestAPI.dto.ResponseFileDTO;
import by.pirog.cloud_storage_RestAPI.utils.CustomUserDetails;
import io.minio.*;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.StreamSupport;

@Log4j2
@Service
@RequiredArgsConstructor
public class DefaultResourceService implements ResourceService {
    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    @Override
    public ResponseFileDTO getFileInfo(String path) throws Exception {
        if (path == null || path.isEmpty()) {
            throw new BadRequestException("Path is null or empty");
        }

        String userFolder = getUserFolder();

        if (path.endsWith("/")) {
            boolean exists = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucket)
                            .prefix(userFolder + path)
                            .recursive(false)
                            .build()
            ).iterator().hasNext();

            if (!exists) {
                throw new BadRequestException("Path does not exist");
            }

            return ResponseFileDTO.builder()
                    .path(userFolder + path)
                    .name(path.substring(path.lastIndexOf("/") + 1))
                    .size(getSizeFolder(path))
                    .type("DIRECTORY")
                    .build();
        }
        try {
            StatObjectResponse response = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucket)
                            .object(userFolder + path)
                            .build()
            );

            return ResponseFileDTO.builder()
                    .path(userFolder + path.substring(0, path.lastIndexOf("/")))
                    .name(path.substring(path.lastIndexOf("/") + 1))
                    .size(response.size())
                    .type("FILE")
                    .build();

        } catch (Exception ex) {
            throw new Exception("Stat Object not found");
        }
    }

    public void deleteResource(String path) throws BadRequestException {
        if (path == null || path.isEmpty()) {
            throw new BadRequestException("Invalid path provided");
        }

        String userFolder = getUserFolder();

        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucket)
                        .prefix(userFolder + path)
                        .recursive(true)
                        .build()
        );

        List<DeleteObject> objectsToDelete = StreamSupport.stream(results.spliterator(), false)
                .map(r -> {
                    try {
                        return new DeleteObject(r.get().objectName());
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to read object info", e);
                    }
                })
                .toList();

        if (objectsToDelete.isEmpty()) {
            throw new BadRequestException("Folder or file does not exist");
        }

        minioClient.removeObjects(
                RemoveObjectsArgs.builder()
                        .bucket(bucket)
                        .objects(objectsToDelete)
                        .build()
        );
    }


    @Override
    public List<ResponseFileDTO> uploadFiles(String path, MultipartFile[] files) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String pathToUpload = getUserFolder();

        List<ResponseFileDTO> responseFileDTOList = new ArrayList<>();
        for (MultipartFile file : files) {
            try(InputStream inputStream = file.getInputStream()) {

                String objectName = pathToUpload + file.getOriginalFilename();
                log.info("Uploading file {} to bucket {}", objectName, bucket);
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucket)
                                .object(objectName)
                                .stream(inputStream, file.getSize(), -1)
                                .contentType(file.getContentType())
                                .build()
                );

                responseFileDTOList.add(ResponseFileDTO.builder()
                        .name(file.getName())
                        .path(objectName)
                        .size(file.getSize())
                        .type(file.getContentType())
                        .build());


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return responseFileDTOList;
    }


    private Long getSizeFolder(String path) throws Exception{
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
    }

    private String getUserFolder(){
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        return String.format("user-%d-folder", userDetails.getUser().getId());
    }
}
