package com.example.photomanagementsystem.photo.vo;

import lombok.Data;

import java.nio.file.Path;

/**
 * Local file metadata for download.
 */
@Data
public class PhotoDownloadVO {

    private Path path;

    private String filename;

    private String mimeType;
}
