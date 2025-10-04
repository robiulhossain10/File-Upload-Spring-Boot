package com.springupload.fileuploadspringboot.controller;

import com.springupload.fileuploadspringboot.dtos.FileInfo;
import com.springupload.fileuploadspringboot.dtos.ResponseMessage;
import com.springupload.fileuploadspringboot.entity.FileData;
import com.springupload.fileuploadspringboot.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/storage")
@RequiredArgsConstructor
public class StorageController {
    private final StorageService storageService;

    @PostMapping(
            value = "/fileupload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> uploadImageToSystem(@RequestParam("image") MultipartFile file) throws IOException {
        String message = "";
        try {
            String uploadImage = storageService.uploadImageToFileSystem(file);
            message = "Uploaded the file successfully: " + file.getOriginalFilename();
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
        } catch (Exception e) {
            message = "Could not upload the file: " + file.getOriginalFilename() + ". Error: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
        }
    }


    @GetMapping("/fileupload/{fileName}")
    public ResponseEntity<?>download(@PathVariable String fileName) throws IOException{
        byte[] imageData = storageService.downloadImageFromFileUplod(fileName);
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.valueOf("image/jpg"))
                .body(imageData);
    }

    @GetMapping()
    public ResponseEntity<?> getAllImage() throws IOException {
        List<FileInfo> fileInfoList = storageService.findAllImage();
        return ResponseEntity.status(HttpStatus.OK).body(fileInfoList);
    }
}
