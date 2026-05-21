package com.example.photomanagementsystem.photo.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * Photo upload request.
 */
@Data
public class PhotoUploadDTO {

    private MultipartFile file;

    private Long albumId;
}
