package com.example.photomanagementsystem.album.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 相册实体。
 */
@Data
public class Album {

    private Long id;

    private String name;

    private Boolean defaultAlbum;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
