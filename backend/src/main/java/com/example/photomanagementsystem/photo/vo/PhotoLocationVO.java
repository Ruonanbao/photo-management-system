package com.example.photomanagementsystem.photo.vo;

import lombok.Data;

import java.util.List;

/**
 * Photos grouped by location.
 */
@Data
public class PhotoLocationVO {

    private String locationName;

    private List<PhotoVO> photos;
}
