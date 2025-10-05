package com.springupload.fileuploadspringboot.service;

import com.springupload.fileuploadspringboot.dtos.FileInfo;
import com.springupload.fileuploadspringboot.entity.FileData;
import com.springupload.fileuploadspringboot.repository.FileDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StorageService {
    private final FileDataRepository fileDataRepository;

    private final String FOLDER_PATH = "H:\\SpringBoot\\FileUpload\\";


    public String uploadImageToFileSystem(MultipartFile file) throws IOException {
            String originalFilename = file.getOriginalFilename();
        String extension = "";

        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String uniqueFileName = UUID.randomUUID().toString() + extension;
        String filePath = FOLDER_PATH  + uniqueFileName;
//    String filePath = FOLDER_PATH+file.getOriginalFilename();
    FileData fileData = fileDataRepository.save(
            FileData.builder()
                    .name(uniqueFileName)
                    .type(file.getContentType())
                    .filePath(filePath).build());

            FileData saved = fileDataRepository.save(fileData);

            //save the actual file in the folder
        file.transferTo(new File(filePath));

        return "File uploaded successfully: " + saved.getFilePath();


}

public byte[] downloadImageFromFileUplod(String fileName) throws IOException{
    FileData fileData = fileDataRepository.
            findByName(fileName);

    if (fileData == null){
        throw new RuntimeException("File Not Found: "+ fileName);
    }

    return Files.readAllBytes(new File(fileData.getFilePath()).toPath());
}

    public List<FileInfo> findAllImage() {
        List<FileInfo> infos = new ArrayList<>();

        List<FileData> files = fileDataRepository.findAll();

        for (FileData fileData : files) {
            Path path = Path.of(fileData.getFilePath());
            if (Files.exists(path)) {
                try {
                    byte[] image = Files.readAllBytes(path);
                    infos.add(new FileInfo(fileData.getId() ,fileData.getName(), fileData.getFilePath(), image));
                } catch (IOException e) {
                    System.err.println("Failed to read file: " + fileData.getFilePath() + " -> " + e.getMessage());
                }
            } else {
                System.err.println("File not found: " + fileData.getFilePath());
            }
        }

        return infos;
    }

    public void delete(Long id) {
        FileData fileData = fileDataRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found with id: " + id));

        Path path = Path.of(fileData.getFilePath());

        try {
            if (Files.exists(path)) {
                Files.delete(path);
                System.out.println("File deleted from folder: " + path);
            } else {
                System.err.println("⚠File not found in folder: " + path);
            }
        } catch (IOException e) {
            System.err.println("⚠Failed to delete file from folder: " + e.getMessage());
            throw new RuntimeException("Failed to delete file from system: " + e.getMessage());
        }
        try {
            fileDataRepository.deleteById(id);
            System.out.println("File record deleted from database: " + id);
        } catch (Exception e) {
            System.err.println("⚠Failed to delete database record: " + e.getMessage());
            throw new RuntimeException("Failed to delete database record for file id " + id);
        }
    }


}
