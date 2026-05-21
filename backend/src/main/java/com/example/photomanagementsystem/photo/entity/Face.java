package com.example.photomanagementsystem.photo.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Face entity for pm_face. Feature vector is intentionally not modeled here.
 */
@Data
public class Face {

    private Long id;

    private Long photoId;

    private Float bboxX;

    private Float bboxY;

    private Float bboxWidth;

    private Float bboxHeight;

    private Long personId;

    private LocalDateTime createTime;
}
