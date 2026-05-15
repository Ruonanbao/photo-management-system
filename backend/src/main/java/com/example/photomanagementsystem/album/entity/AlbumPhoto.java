package com.example.photomanagementsystem.album.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 相册照片关联实体，对应 pm_album_photo 表。
 */
@Data
public class AlbumPhoto {

    private Long id;

    private Long albumId;

    private Long photoId;

    private LocalDateTime createTime;
}
