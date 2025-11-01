package by.pirog.cloud_storage_RestAPI.service.defaultSerivces;

import by.pirog.cloud_storage_RestAPI.dto.ResponseFileDTO;
import by.pirog.cloud_storage_RestAPI.exception.customExceptions.BadRequestException;

import by.pirog.cloud_storage_RestAPI.exception.customExceptions.UnknownException;
import by.pirog.cloud_storage_RestAPI.service.interfaces.DirectoryService;
import by.pirog.cloud_storage_RestAPI.service.interfaces.MinioService;
import by.pirog.cloud_storage_RestAPI.utils.CustomUserDetails;

import io.minio.Result;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.StreamSupport;


@Service
@RequiredArgsConstructor
public class DefaultDirectoryService implements DirectoryService {

    private final MinioService minioService;

    @Value("minio.bucket")
    private String bucket;

    @Override
    public List<ResponseFileDTO> getInfoDirectory(String path) {
        if (!path.endsWith("/")) {
            throw new BadRequestException("path must end with '/'");
        }

        String totalPath = (path == null || path.isEmpty()) ?
                getUserFolder() : getUserFolder() + URLDecoder
                .decode(path.replaceFirst("^/+", ""), StandardCharsets.UTF_8);

        Iterable<Result<Item>> results = this.minioService.getListObjectsByPrefixNotRecursive(bucket, totalPath);

        List<ResponseFileDTO> response = StreamSupport.stream(results.spliterator(), false)
                .map(resultItem -> {
                    try{
                        Item item = resultItem.get();
                        String objectName = item.objectName();
                        String[] splitObjectName = objectName.split("/");

                        String type = objectName.endsWith("/") ? "DIRECTORY" : "FILE";

                        return ResponseFileDTO.builder()
                                .type(type)
                                .size(this.minioService.getSizeFolder(bucket,objectName))
                                .name(splitObjectName[splitObjectName.length - 1])
                                .path(objectName.substring(objectName.indexOf("/") + 1))
                                .build();

                    } catch (Exception e) {
                        throw new UnknownException("Failed to get directory info");
                    }
                }).toList();

        return response;
    }



    private String getUserFolder(){
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        return String.format("user-%d-files/", userDetails.getUser().getId());
    }
}
