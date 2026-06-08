package com.example.photomanagementsystem.photo.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * EXIF metadata extracted from an uploaded photo.
 */
public record PhotoMetadata(
        LocalDateTime shotAt,
        String cameraMake,
        String cameraModel,
        BigDecimal latitude,
        BigDecimal longitude) {

    public static PhotoMetadata empty() {
        return new PhotoMetadata(null, null, null, null, null);
    }
}
