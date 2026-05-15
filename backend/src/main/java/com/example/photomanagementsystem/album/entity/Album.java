package com.example.photomanagementsystem.album.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 相册实体，对应 pm_album 表。
 */
@Data
public class Album {

    private Long id;

    private Long userId;

    private String name;

    private String description;

    private Long coverPhotoId;

    private Boolean defaultAlbum;

    private Boolean deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
