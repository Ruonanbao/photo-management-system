package com.example.photomanagementsystem.photo.vo;

import lombok.Data;

import java.util.List;

/**
 * Page response object.
 *
 * @param <T> item type
 */
@Data
public class PhotoPageVO<T> {

    private List<T> records;

    private long total;

    private int page;

    private int size;
}
