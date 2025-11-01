package by.pirog.cloud_storage_RestAPI.service.interfaces;

import by.pirog.cloud_storage_RestAPI.dto.ResponseFileDTO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ResourceService {

    ResponseFileDTO getResourceInfo(String path);

    void deleteResource(String path);

    List<ResponseFileDTO> uploadResource(String path, MultipartFile[] files);

    void downloadResource(String path, HttpServletResponse response);

    ResponseFileDTO renameOrReplaceResource(String from, String to);

    List<ResponseFileDTO> searchResource(String query);
}
