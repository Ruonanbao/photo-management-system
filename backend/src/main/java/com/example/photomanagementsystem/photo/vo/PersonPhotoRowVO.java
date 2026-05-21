package com.example.photomanagementsystem.photo.vo;

import lombok.Data;

/**
 * Flat row for person and photo grouping query.
 */
@Data
public class PersonPhotoRowVO {

    private Long personId;

    private String personName;

    private Long coverFaceId;

    private PhotoVO photo;
}
