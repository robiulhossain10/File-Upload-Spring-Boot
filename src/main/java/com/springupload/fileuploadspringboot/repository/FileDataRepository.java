package com.springupload.fileuploadspringboot.repository;

import com.springupload.fileuploadspringboot.entity.FileData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileDataRepository extends JpaRepository<FileData, Long> {
    FileData findByName(String fileName);
}