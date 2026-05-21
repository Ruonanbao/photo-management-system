package com.example.photomanagementsystem.photo.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Photo response object.
 */
@Data
public class PhotoVO {

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
