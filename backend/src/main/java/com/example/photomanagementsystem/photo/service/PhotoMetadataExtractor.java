package com.example.photomanagementsystem.photo.service;

import com.drew.imaging.ImageMetadataReader;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.TimeZone;

/**
 * Reads available EXIF metadata without making upload success depend on it.
 */
@Component
public class PhotoMetadataExtractor {

    private static final Logger log = LoggerFactory.getLogger(PhotoMetadataExtractor.class);

    public PhotoMetadata extract(Path photoPath) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(photoPath.toFile());
            ExifSubIFDDirectory exif = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            ExifIFD0Directory ifd0 = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            GpsDirectory gps = metadata.getFirstDirectoryOfType(GpsDirectory.class);

            LocalDateTime shotAt = toLocalDateTime(exif == null
                    ? null
                    : exif.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL, TimeZone.getDefault()));
            String cameraMake = normalized(ifd0 == null ? null : ifd0.getString(ExifIFD0Directory.TAG_MAKE));
            String cameraModel = normalized(ifd0 == null ? null : ifd0.getString(ExifIFD0Directory.TAG_MODEL));
            GeoLocation location = gps == null ? null : gps.getGeoLocation();
            BigDecimal latitude = coordinate(location == null ? null : location.getLatitude());
            BigDecimal longitude = coordinate(location == null ? null : location.getLongitude());

            return new PhotoMetadata(shotAt, cameraMake, cameraModel, latitude, longitude);
        } catch (Exception exception) {
            log.warn("Unable to read EXIF metadata from {}: {}", photoPath.getFileName(), exception.getMessage());
            return PhotoMetadata.empty();
        }
    }

    private LocalDateTime toLocalDateTime(Date date) {
        return date == null ? null : LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    private BigDecimal coordinate(Double value) {
        return value == null || !Double.isFinite(value)
                ? null
                : BigDecimal.valueOf(value).setScale(7, RoundingMode.HALF_UP);
    }

    private String normalized(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
