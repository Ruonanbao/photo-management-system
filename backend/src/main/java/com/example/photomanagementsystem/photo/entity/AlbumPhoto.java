package com.example.photomanagementsystem.photo.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Relation entity for pm_album_photo.
 */
@Data
public class AlbumPhoto {

    private Long id;

    private Long albumId;

    private Long photoId;

    private LocalDateTime createTime;
}
