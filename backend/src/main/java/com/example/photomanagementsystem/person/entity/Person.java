package com.example.photomanagementsystem.person.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Person entity for pm_person.
 */
@Data
public class Person {

    private Long id;

    private Long userId;

    private String name;

    private Long coverFaceId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
