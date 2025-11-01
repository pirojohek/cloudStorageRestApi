package by.pirog.cloud_storage_RestAPI.controller;

import by.pirog.cloud_storage_RestAPI.dto.ResponseFileDTO;

import by.pirog.cloud_storage_RestAPI.service.interfaces.DirectoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("directory")
public class DirectoryRestController {

    private final DirectoryService directoryService;

    @GetMapping
    public ResponseEntity<List<ResponseFileDTO>> searchDirectory(@RequestParam(name="path") String path) {
        List<ResponseFileDTO> info = directoryService.getInfoDirectory(path);
        return ResponseEntity.status(HttpStatus.OK).body(info);
    }
}
