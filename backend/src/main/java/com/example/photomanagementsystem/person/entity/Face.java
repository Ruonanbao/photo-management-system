package com.example.photomanagementsystem.person.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Face entity for pm_face.
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
