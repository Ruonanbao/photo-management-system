package com.example.photomanagementsystem.photo.dto;

import lombok.Data;

/**
 * Photo list query parameters.
 */
@Data
public class PhotoListQueryDTO {

    private Boolean favorite;

    private String keyword;

    private Integer page;

    private Integer size;
}
