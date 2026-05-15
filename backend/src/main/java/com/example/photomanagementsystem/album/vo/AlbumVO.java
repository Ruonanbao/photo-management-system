package com.example.photomanagementsystem.album.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 相册展示对象。
 */
@Data
public class AlbumVO {

    private Long id;

    private String name;

    private String description;

    private Boolean defaultAlbum;

    private Integer photoCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
