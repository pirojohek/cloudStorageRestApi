package by.pirog.cloud_storage_RestAPI.service;

import by.pirog.cloud_storage_RestAPI.dto.ResponseFileDTO;
import by.pirog.cloud_storage_RestAPI.exception.ResourceNotFoundException;
import by.pirog.cloud_storage_RestAPI.exception.UnknownException;
import by.pirog.cloud_storage_RestAPI.utils.CustomUserDetails;
import io.minio.*;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import java.util.ArrayList;

import java.util.List;
import java.util.Objects;

import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Log4j2
@Service
@RequiredArgsConstructor
public class DefaultResourceService implements ResourceService {
    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    String zipName = "data.zip";

    @Override
    public ResponseFileDTO getResourceInfo(String path) throws Exception {
        String userResource = (path == null || path.isEmpty()) ?
                    getUserFolder() :
                getUserFolder() + URLDecoder
                        .decode(path.replaceFirst("^/+", ""), StandardCharsets.UTF_8);

        if (userResource.endsWith("/")) {
            boolean exists = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucket)
                            .prefix(userResource)
                            .recursive(false)
                            .build()
            ).iterator().hasNext();

            if (!exists) {
                throw new ResourceNotFoundException("Resource %s not found".formatted(path));
            }

            return ResponseFileDTO.builder()
                    .path(userResource.substring(getUserFolder().length()))
                    .name(path != null ? path.substring(path.lastIndexOf("/") + 1) : "/")
                    .size(getSizeFolder(userResource))
                    .type("DIRECTORY")
                    .build();
        }
        try {
            StatObjectResponse response = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucket)
                            .object(userResource)
                            .build()
            );

            return ResponseFileDTO.builder()
                    .path(userResource)
                    .name(userResource.substring(userResource.lastIndexOf("/")))
                    .size(response.size())
                    .type("FILE")
                    .build();

        } catch (Exception ex) {
            throw new UnknownException("Failed to stat object");
        }
    }

    public void deleteResource(String path) throws BadRequestException {
        String userResource = (path == null || path.isEmpty()) ?
                getUserFolder() :
                getUserFolder() + URLDecoder
                        .decode(path.replaceFirst("^/+", ""), StandardCharsets.UTF_8);

        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucket)
                        .prefix(userResource)
                        .recursive(true)
                        .build()
        );

        List<String> objectNames = StreamSupport.stream(results.spliterator(), false)
                .map(r -> {
                    try {
                        return r.get().objectName();
                    } catch (Exception e) {
                        throw new UnknownException("Failed to delete object");
                    }
                })
                .toList();

        if (objectNames.isEmpty()) {
            throw new ResourceNotFoundException("Folder or file does not exist");
        }

        for (String objectName : objectNames) {
            try {
                log.info("Deleting object: {}", objectName);
                minioClient.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(bucket)
                                .object(objectName)
                                .build()
                );
                log.info("Successfully deleted: {}", objectName);
            } catch (Exception e) {
                throw new UnknownException("Failed to delete object");
            }
        }
    }

    @Override
    public List<ResponseFileDTO> uploadResource(String path, MultipartFile[] files) {

        String pathToUpload = getUserFolder();
        List<ResponseFileDTO> responseFileDTOList = new ArrayList<>();

        for (MultipartFile file : files) {
            try(InputStream inputStream = file.getInputStream()) {
                String objectName = pathToUpload + file.getOriginalFilename();

                String contentType = file.getContentType() != null
                        ? file.getContentType() : "application/octet-stream";

                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucket)
                                .object(objectName)
                                .stream(inputStream, file.getSize(), -1)
                                .contentType(contentType)
                                .build()
                );

                responseFileDTOList.add(ResponseFileDTO.builder()
                        .name(file.getName())
                        .path(objectName)
                        .size(file.getSize())
                        .type(contentType)
                        .build());
            } catch (Exception e) {
                throw new UnknownException("Failed to upload resource");
            }
        }
        return responseFileDTOList;
    }

    @Override
    public void downloadResource(String path, HttpServletResponse response)  {

        String totalPath = (path == null || path.isEmpty()) ?
                getUserFolder() : getUserFolder() + URLDecoder
                .decode(path.replaceFirst("^/+", ""), StandardCharsets.UTF_8);

        var resources = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucket)
                        .prefix(totalPath)
                        .recursive(true)
                        .build()
        );

        if (!resources.iterator().hasNext()) {
            throw new ResourceNotFoundException("Resource not found");
        }

        if (!totalPath.endsWith("/")) {
            try (InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(totalPath)
                            .build())) {

                response.setContentType("application/octet-stream");
                response.setStatus(HttpServletResponse.SC_OK);
                String fileName = totalPath.substring(totalPath.lastIndexOf("/") + 1);
                response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
                stream.transferTo(response.getOutputStream());
                response.flushBuffer();
                return;
            }  catch (Exception e) {
                throw new UnknownException("Failed to download resource");
            }
        }

        response.setContentType("application/zip");
        response.setStatus(HttpServletResponse.SC_OK);
        response.setHeader("Content-Disposition", "attachment; filename=\"" + zipName + "\"");
        try (ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream())) {
            for (var resource : resources) {
                String objectName = resource.get().objectName();
                String relativeName = objectName.substring(getUserFolder().length());

                if (objectName.endsWith("/")) {
                    zipOut.putNextEntry(new ZipEntry(relativeName));
                    zipOut.closeEntry();
                    continue;
                }

                try (InputStream inputStream = minioClient.getObject(
                        GetObjectArgs.builder().bucket(bucket).object(objectName).build())) {
                    zipOut.putNextEntry(new ZipEntry(relativeName));
                    inputStream.transferTo(zipOut);
                    zipOut.closeEntry();
                }
            }
            zipOut.finish();
            response.flushBuffer();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public ResponseFileDTO renameOrReplaceResource(String from, String to) {
        return null;
    }


    @Override
    public List<ResponseFileDTO> searchResource(String query) throws Exception {
        if (query.equals("/") || query.isEmpty()) {
            throw new BadRequestException("inValid Path");
        }

        String userFolder = getUserFolder();

        Iterable<Result<Item>> items = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucket)
                        .prefix(userFolder)
                        .recursive(true)
                        .build()
        );

        List<ResponseFileDTO> result = StreamSupport.stream(items.spliterator(), false).filter(item ->{
            try {
                String[] splitPath = item.get().objectName().split("/");

                return splitPath[splitPath.length - 1].equals("/") ?
                        splitPath[splitPath.length - 2].contains(query) : splitPath[splitPath.length - 1].equals(query);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

        }).map(itemResult -> {
            try{
                Item item = itemResult.get();
                String[] splitPath = item.objectName().split("/");

                return ResponseFileDTO.builder()
                        .path(item.objectName().substring(getUserFolder().length()))
                        .name(splitPath[splitPath.length - 1].equals("/") ?
                                splitPath[splitPath.length - 2] : splitPath[splitPath.length - 1])
                        .size(item.size())
                        .type(splitPath[splitPath.length - 1].equals("/") ? "DIRECTORY" : "FILE")
                        .build();
            } catch (Exception e){
                e.printStackTrace();
                return null;
            }
        }).filter(Objects::nonNull)
                .toList();
        return result;
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
        return String.format("user-%d-files/", userDetails.getUser().getId());
    }
}
