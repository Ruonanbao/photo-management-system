package com.example.photomanagementsystem.album.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Photo {

    private Long id;

    private String filename;

    private String originalName;

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
