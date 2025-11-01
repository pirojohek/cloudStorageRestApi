package by.pirog.cloud_storage_RestAPI.service.defaultSerivces;

import by.pirog.cloud_storage_RestAPI.dto.ResponseFileDTO;
import by.pirog.cloud_storage_RestAPI.exception.customExceptions.UnknownException;
import by.pirog.cloud_storage_RestAPI.service.interfaces.SearchService;
import by.pirog.cloud_storage_RestAPI.utils.CustomUserDetails;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
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
public class DefaultSearchService implements SearchService {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    @Override
    public List<ResponseFileDTO> searchResource(String path) {
        String userResource = (path == null || path.isEmpty()) ?
                getUserFolder() :
                getUserFolder() + URLDecoder
                        .decode(path.replaceFirst("^/+", ""), StandardCharsets.UTF_8);

        Iterable<Result<Item>> items = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucket)
                        .prefix(userResource)
                        .recursive(true)
                        .build()
        );

        return StreamSupport.stream(items.spliterator(), false)
                .map(itemResult -> {
                    try{
                        Item item = itemResult.get();
                        String[] listPath = item.objectName().split("/");

                        return new ResponseFileDTO().builder()
                                .path(item.objectName().substring(getUserFolder().length()))
                                .name(item.objectName().endsWith("/") ?
                                        listPath[listPath.length - 2] :
                                        listPath[listPath.length - 1])
                                .build();
                    } catch (Exception e) {
                        throw new UnknownException("Something went wrong on server. We work on in");
                    }
                }).toList();
    }

    private String getUserFolder(){
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        return String.format("user-%d-files/", userDetails.getUser().getId());
    }
}
