package by.pirog.cloud_storage_RestAPI.controller;


import by.pirog.cloud_storage_RestAPI.dto.ResponseFileDTO;
import by.pirog.cloud_storage_RestAPI.service.DefaultResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("resource")
public class ResourceRestController {

    private final DefaultResourceService fileService;

    @GetMapping
    public ResponseEntity<ResponseFileDTO> getResourceInfo(@RequestParam(name="path") String path) throws Exception {
        ResponseFileDTO response = fileService.getFileInfo(path);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public void deleteResource(@RequestParam("path") String path) throws Exception {

    }

    @GetMapping("/download")
    public void downloadResource(@RequestParam(name="path", required=false) String path) {
        try{

        }catch (FileNotFoundException exception)
    }

    @PostMapping
    public ResponseEntity<List<ResponseFileDTO>> uploadResource(@RequestParam(name = "path", required = false) String path,
                                                             @RequestParam("files") MultipartFile[] files) {
        log.info("Received request to upload files for path {}", path);

        List<ResponseFileDTO> response = fileService.uploadFiles(path, files);

        return ResponseEntity.ok().body(response);
    }

}
