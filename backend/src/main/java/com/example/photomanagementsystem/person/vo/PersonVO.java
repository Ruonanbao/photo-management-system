package com.example.photomanagementsystem.person.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Person response object.
 */
@Data
public class PersonVO {

    private Long id;

    private String name;

    private Long coverFaceId;

    private Long photoCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
