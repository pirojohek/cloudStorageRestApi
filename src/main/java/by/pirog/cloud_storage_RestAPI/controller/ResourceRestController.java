package by.pirog.cloud_storage_RestAPI.controller;


import by.pirog.cloud_storage_RestAPI.dto.ResponseFileDTO;
import by.pirog.cloud_storage_RestAPI.service.DefaultResourceService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("resource")
public class ResourceRestController {

    private final DefaultResourceService fileService;

    @GetMapping
    public ResponseEntity<ResponseFileDTO> getResourceInfo(@RequestParam(name="path", required=false) String path) throws Exception {
        ResponseFileDTO response = fileService.getResourceInfo(path);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteResource(@RequestParam(name="path", required=false) String path) throws Exception {
        fileService.deleteResource(path);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/download")
    public void downloadResource(@RequestParam(name="path", required=false) String path,
                                 HttpServletResponse response) {
        fileService.downloadResource(path, response);
    }


    @GetMapping("/search")
    public ResponseEntity<List<ResponseFileDTO>> searchResource(@RequestParam(name="query") String query) throws Exception {
        List<ResponseFileDTO> response = fileService.searchResource(query);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    @PostMapping
    public ResponseEntity<List<ResponseFileDTO>> uploadResource(@RequestParam(name = "path", required = false) String path,
                                                             @RequestParam("files") MultipartFile[] files) {
        List<ResponseFileDTO> response = fileService.uploadResource(path, files);
        return ResponseEntity.ok().body(response);
    }

}
