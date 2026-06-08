package com.example.photomanagementsystem.photo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Generates browser-compatible JPEG previews while preserving original files.
 */
@Component
public class PhotoPreviewGenerator {

    private final int maxSize;

    public PhotoPreviewGenerator(@Value("${photo.preview.max-size:1600}") int maxSize) {
        this.maxSize = Math.max(320, maxSize);
    }

    public Path generatePreview(Path originalPath, String mimeType) throws IOException {
        BufferedImage source = ImageIO.read(originalPath.toFile());
        if (source == null) {
            return null;
        }

        BufferedImage preview = scaleToJpeg(source);
        Path previewPath = buildPreviewPath(originalPath);
        if (!ImageIO.write(preview, "jpg", previewPath.toFile())) {
            throw new IOException("No JPEG writer available");
        }
        return previewPath;
    }

    private BufferedImage scaleToJpeg(BufferedImage source) {
        double scale = Math.min(1.0, (double) maxSize / Math.max(source.getWidth(), source.getHeight()));
        int width = Math.max(1, (int) Math.round(source.getWidth() * scale));
        int height = Math.max(1, (int) Math.round(source.getHeight() * scale));
        BufferedImage preview = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = preview.createGraphics();
        try {
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, width, height);
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.drawImage(source, 0, 0, width, height, null);
        } finally {
            graphics.dispose();
        }
        return preview;
    }

    private Path buildPreviewPath(Path originalPath) throws IOException {
        Path parent = originalPath.getParent();
        Path previewDirectory = parent.resolve("previews");
        Files.createDirectories(previewDirectory);
        String filename = originalPath.getFileName().toString();
        int dotIndex = filename.lastIndexOf('.');
        String baseName = dotIndex > 0 ? filename.substring(0, dotIndex) : filename;
        return previewDirectory.resolve(baseName + ".jpg").normalize();
    }
}
