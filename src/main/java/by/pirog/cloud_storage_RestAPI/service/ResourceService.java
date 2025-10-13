package by.pirog.cloud_storage_RestAPI.service;

import by.pirog.cloud_storage_RestAPI.dto.ResponseFileDTO;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.coyote.BadRequestException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ResourceService {

    ResponseFileDTO getResourceInfo(String path) throws Exception;

    void deleteResource(String path) throws Exception;

    List<ResponseFileDTO> uploadResource(String path, MultipartFile[] files);

    void downloadResource(String path, HttpServletResponse response) throws IOException;

    ResponseFileDTO renameOrReplaceResource(String from, String to);

    List<ResponseFileDTO> searchResource(String query) throws BadRequestException, Exception;
}
