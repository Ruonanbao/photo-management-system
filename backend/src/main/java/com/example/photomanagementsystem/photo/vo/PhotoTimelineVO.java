package com.example.photomanagementsystem.photo.vo;

import lombok.Data;

import java.util.List;

/**
 * Photos grouped by year and month.
 */
@Data
public class PhotoTimelineVO {

    private String yearMonth;

    private List<PhotoVO> photos;
}
