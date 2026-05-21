package com.example.photomanagementsystem.photo.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Photo entity for pm_photo.
 */
@Data
public class Photo {

    private Long id;

    private Long userId;

    private String filename;

    private String originalName;

    private String filePath;

    private String thumbnailPath;

    private Long fileSize;

    private String mimeType;

    private Integer width;

    private Integer height;

    private LocalDateTime shotAt;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private String locationName;

    private String cameraMake;

    private String cameraModel;

    private Boolean favorite;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
