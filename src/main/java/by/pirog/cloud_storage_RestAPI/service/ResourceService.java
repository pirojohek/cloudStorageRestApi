package by.pirog.cloud_storage_RestAPI.service;

import by.pirog.cloud_storage_RestAPI.dto.ResponseFileDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ResourceService {

    ResponseFileDTO getFileInfo(String path) throws Exception;

    void deleteResource(String path) throws Exception;

    List<ResponseFileDTO> uploadFiles(String path, MultipartFile[] files);
}
