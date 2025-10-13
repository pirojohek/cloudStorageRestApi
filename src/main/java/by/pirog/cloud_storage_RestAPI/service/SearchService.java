package by.pirog.cloud_storage_RestAPI.service;

import by.pirog.cloud_storage_RestAPI.dto.ResponseFileDTO;
import org.apache.coyote.BadRequestException;

import java.util.List;

public interface SearchService {

    List<ResponseFileDTO> searchResource(String query);
}
