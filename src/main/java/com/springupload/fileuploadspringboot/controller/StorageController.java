package com.springupload.fileuploadspringboot.controller;

import com.springupload.fileuploadspringboot.dtos.FileInfo;
import com.springupload.fileuploadspringboot.dtos.ResponseMessage;
import com.springupload.fileuploadspringboot.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@RestController
@RequestMapping("/api/storage")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class StorageController {

    private final StorageService storageService;

    // ✅ Logger setup
    private static final Logger logger = LoggerFactory.getLogger(StorageController.class);
    @PostMapping(
            value = "/fileupload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> uploadImagesToSystem(@RequestParam("images") List<MultipartFile> files) {
        logger.info("📤 Received {} file(s) for upload.", files.size());

        if (files.isEmpty()) {
            logger.warn("⚠️ No files received for upload.");
            return ResponseEntity.badRequest().body(new ResponseMessage("No files uploaded."));
        }

        List<String> uploadedFiles = new ArrayList<>();

        try {
            for (MultipartFile file : files) {
                logger.info("➡ Uploading file: {}", file.getOriginalFilename());
                String uploadedPath = storageService.uploadImageToFileSystem(file);
                uploadedFiles.add(file.getOriginalFilename());
                logger.info("✅ File '{}' uploaded successfully!", file.getOriginalFilename());
            }

            String message = "Uploaded " + uploadedFiles.size() + " file(s) successfully: " + uploadedFiles;
            return ResponseEntity.ok(new ResponseMessage(message));

        } catch (Exception e) {
            logger.error("❌ Failed to upload files. Error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseMessage("Could not upload files. Please check file path or permissions."));
        }
    }



    @GetMapping("/{fileName}")
    public ResponseEntity<?> download(@PathVariable String fileName) throws IOException {
        logger.info("⬇️ Download request received for file: {}", fileName);

        byte[] imageData = storageService.downloadImageFromFileUplod(fileName);
        logger.info("✅ File '{}' downloaded successfully ({} bytes)", fileName, imageData.length);

        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.valueOf("image/jpg"))
                .body(imageData);
    }


    @GetMapping()
    public ResponseEntity<?> getAllImage() throws IOException {
        logger.info("📦 Fetching all uploaded files...");

        List<FileInfo> fileInfoList = storageService.findAllImage();
        logger.info(" Total {} files found.", fileInfoList.size());

        return ResponseEntity.status(HttpStatus.OK).body(fileInfoList);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFile(@PathVariable Long id) {
        try {
            storageService.delete(id);
            return ResponseEntity.ok("File deleted successfully!");
        } catch (Exception e) {
            logger.error("Failed to delete file with id {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Could not delete file: " + e.getMessage());
        }
    }

}
