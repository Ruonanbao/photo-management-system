package com.example.photomanagementsystem.album.dto;

import lombok.Data;

/**
 * 更新相册请求参数。
 */
@Data
public class AlbumUpdateDTO {

    private String name;

    private String description;
}
