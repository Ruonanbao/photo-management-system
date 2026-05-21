package com.example.photomanagementsystem.photo.vo;

import lombok.Data;

import java.util.List;

/**
 * Photos grouped by person.
 */
@Data
public class PhotoPeopleVO {

    private Long personId;

    private String personName;

    private Long coverFaceId;

    private List<PhotoVO> photos;
}
