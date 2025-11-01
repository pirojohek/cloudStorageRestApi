package by.pirog.cloud_storage_RestAPI.service.defaultSerivces;

import by.pirog.cloud_storage_RestAPI.dto.ResponseFileDTO;
import by.pirog.cloud_storage_RestAPI.exception.customExceptions.BadRequestException;
import by.pirog.cloud_storage_RestAPI.exception.customExceptions.ResourceNotFoundException;
import by.pirog.cloud_storage_RestAPI.exception.customExceptions.UnknownException;
import by.pirog.cloud_storage_RestAPI.service.interfaces.MinioService;
import by.pirog.cloud_storage_RestAPI.service.interfaces.ResourceService;
import by.pirog.cloud_storage_RestAPI.utils.CustomUserDetails;
import io.minio.*;
import io.minio.messages.Item;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
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

    private final MinioService minioService;

    @Value("${minio.bucket}")
    private String bucket;

    String zipName = "data.zip";

    @Override
    public ResponseFileDTO getResourceInfo(String path) {
        String userResource = (path == null || path.isEmpty()) ?
                    getUserFolder() :
                getUserFolder() + URLDecoder
                        .decode(path.replaceFirst("^/+", ""), StandardCharsets.UTF_8);

        if (userResource.endsWith("/")) {
            boolean exists = this.minioService.isFolderExists(bucket, userResource);

            if (!exists) {
                throw new ResourceNotFoundException("Resource %s not found".formatted(path));
            }

            return ResponseFileDTO.builder()
                    .path(userResource.substring(getUserFolder().length()))
                    .name(path != null ? path.substring(path.lastIndexOf("/") + 1) : "/")
                    .size(this.minioService.getSizeFolder(bucket, userResource))
                    .type("DIRECTORY")
                    .build();
        }
        try {
            StatObjectResponse response = this.minioService.getStatObject(bucket, userResource);

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

    public void deleteResource(String path){
        String userResource = (path == null || path.isEmpty()) ?
                getUserFolder() :
                getUserFolder() + URLDecoder
                        .decode(path.replaceFirst("^/+", ""), StandardCharsets.UTF_8);

        Iterable<Result<Item>> results =
                this.minioService.getListObjectsByPrefix(bucket, userResource);

        this.minioService.deleteObjectsByResultItems(bucket, results);
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

                this.minioService.putObjectToFolder(bucket, objectName, inputStream, file.getSize(), contentType);

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

        Iterable<Result<Item>> resources =
                this.minioService.getListObjectsByPrefix(bucket, totalPath);

        if (!resources.iterator().hasNext()) {
            throw new ResourceNotFoundException("Resource not found");
        }

        if (!totalPath.endsWith("/")) {
            try (InputStream stream = this.minioService.getInputStreamObject(bucket, totalPath)) {

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

                try (InputStream inputStream = this.minioService.getInputStreamObject(bucket, objectName)) {
                    zipOut.putNextEntry(new ZipEntry(relativeName));
                    inputStream.transferTo(zipOut);
                    zipOut.closeEntry();
                }
            }
            zipOut.finish();
            response.flushBuffer();
        } catch (Exception ex) {
            throw new UnknownException("Failed to download resource");
        }
    }

    @Override
    public ResponseFileDTO renameOrReplaceResource(String from, String to) {
        return null;
    }


    @Override
    public List<ResponseFileDTO> searchResource(String path)  {
        if (path.equals("/") || path.isEmpty()) {
            throw new BadRequestException("Path is empty or null");
        }

        String userFolder = getUserFolder();

        Iterable<Result<Item>> items = this.minioService.getListObjectsByPrefix(bucket, userFolder);

        List<ResponseFileDTO> result = StreamSupport.stream(items.spliterator(), false).filter(item ->{
            try {
                String[] splitPath = item.get().objectName().split("/");

                return splitPath[splitPath.length - 1].equals("/") ?
                        splitPath[splitPath.length - 2].contains(path) : splitPath[splitPath.length - 1].equals(path);
            } catch (Exception e) {
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
                return null;
            }
        }).filter(Objects::nonNull)
                .toList();
        return result;
    }

    private String getUserFolder(){
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        return String.format("user-%d-files/", userDetails.getUser().getId());
    }
}
