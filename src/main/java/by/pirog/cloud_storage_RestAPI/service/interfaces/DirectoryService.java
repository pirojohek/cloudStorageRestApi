package by.pirog.cloud_storage_RestAPI.service.interfaces;

import by.pirog.cloud_storage_RestAPI.dto.ResponseFileDTO;

import java.util.List;

public interface DirectoryService {

    List<ResponseFileDTO> getInfoDirectory(String path);
}
