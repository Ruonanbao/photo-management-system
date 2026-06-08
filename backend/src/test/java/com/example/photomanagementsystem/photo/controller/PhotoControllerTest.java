package com.example.photomanagementsystem.photo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static com.example.photomanagementsystem.testsupport.TestJwtSupport.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Photo controller integration tests.
 */
@SpringBootTest(properties = "photo.storage.path=target/test-uploads/photos")
@AutoConfigureMockMvc
class PhotoControllerTest {

    private static final long TEST_USER_ID = 1L;
    private static final long OTHER_USER_ID = 2L;

    private static final String TEST_PREFIX = "it_photo_";

    private static final byte[] PNG_BYTES = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAFgwJ/lh8rWAAAAABJRU5ErkJggg==");

    private final Path storageRoot = Paths.get("target/test-uploads/photos")
            .toAbsolutePath()
            .normalize();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws Exception {
        ensureTestUser();
        ensureOtherUser();
        cleanTestPhotos();
    }

    @AfterEach
    void tearDown() throws Exception {
        cleanTestPhotos();
    }

    @Test
    void listPhotosShouldReturnSuccess() throws Exception {
        mockMvc.perform(get("/api/v1/photos").with(jwt(TEST_USER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total", greaterThanOrEqualTo(0)));
    }

    @Test
    void uploadPhotoShouldReturnSuccess() throws Exception {
        MockMultipartFile file = pngFile(uniqueOriginalName());

        mockMvc.perform(multipart("/api/v1/photos/upload").file(file).with(jwt(TEST_USER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id", notNullValue()))
                .andExpect(jsonPath("$.data.originalName").value(file.getOriginalFilename()));
    }

    @Test
    void uploadJpegWithExifShouldPersistAndReturnMetadata() throws Exception {
        MockMultipartFile file = jpegWithExif(uniqueOriginalName());

        String response = mockMvc.perform(multipart("/api/v1/photos/upload").file(file).with(jwt(TEST_USER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.shotAt").value("2025-01-02T03:04:05"))
                .andExpect(jsonPath("$.data.cameraMake").value("Apple"))
                .andExpect(jsonPath("$.data.cameraModel").value("iPhone 15"))
                .andExpect(jsonPath("$.data.latitude").value(31.2346667))
                .andExpect(jsonPath("$.data.longitude").value(121.4686667))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long photoId = objectMapper.readTree(response).path("data").path("id").asLong();

        mockMvc.perform(get("/api/v1/photos/{id}", photoId).with(jwt(TEST_USER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.shotAt").value("2025-01-02T03:04:05"))
                .andExpect(jsonPath("$.data.cameraMake").value("Apple"))
                .andExpect(jsonPath("$.data.cameraModel").value("iPhone 15"))
                .andExpect(jsonPath("$.data.latitude").value(31.2346667))
                .andExpect(jsonPath("$.data.longitude").value(121.4686667));
    }

    @Test
    void uploadHeicPhotoWithOctetStreamShouldReturnSuccess() throws Exception {
        String originalName = uniqueOriginalName() + ".heic";
        MockMultipartFile file = new MockMultipartFile("file", originalName,
                MediaType.APPLICATION_OCTET_STREAM_VALUE, "heic image bytes".getBytes());

        mockMvc.perform(multipart("/api/v1/photos/upload").file(file).with(jwt(TEST_USER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.originalName").value(originalName))
                .andExpect(jsonPath("$.data.mimeType").value("image/heic"));
    }

    @Test
    void uploadPhotoToOwnAlbumShouldReturnSuccess() throws Exception {
        Long albumId = insertTestAlbum(TEST_USER_ID, "it_photo_album_" + UUID.randomUUID().toString().replace("-", ""));
        MockMultipartFile file = pngFile(uniqueOriginalName());

        mockMvc.perform(multipart("/api/v1/photos/upload")
                        .file(file)
                        .param("albumId", albumId.toString())
                        .with(jwt(TEST_USER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id", notNullValue()));
    }

    @Test
    void uploadPhotoWithInvalidMimeTypeShouldReturnFailure() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", uniqueOriginalName() + ".txt",
                MediaType.TEXT_PLAIN_VALUE, "not an image".getBytes());

        mockMvc.perform(multipart("/api/v1/photos/upload").file(file).with(jwt(TEST_USER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void uploadPhotoWithNotExistsAlbumShouldReturnBizFailure() throws Exception {
        MockMultipartFile file = pngFile(uniqueOriginalName());

        mockMvc.perform(multipart("/api/v1/photos/upload").file(file).param("albumId", "-1").with(jwt(TEST_USER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void getPhotoShouldReturnSuccess() throws Exception {
        Long photoId = insertTestPhoto(uniqueOriginalName(), true);

        mockMvc.perform(get("/api/v1/photos/{id}", photoId).with(jwt(TEST_USER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(photoId));
    }

    @Test
    void getPhotoWhenNotExistsShouldReturnBizFailure() throws Exception {
        mockMvc.perform(get("/api/v1/photos/{id}", -1L).with(jwt(TEST_USER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void getOtherUserPhotoShouldReturnBizFailure() throws Exception {
        Long photoId = insertTestPhoto(OTHER_USER_ID, uniqueOriginalName(), true);

        mockMvc.perform(get("/api/v1/photos/{id}", photoId).with(jwt(TEST_USER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void updateFavoriteShouldReturnSuccess() throws Exception {
        Long photoId = insertTestPhoto(uniqueOriginalName(), true);

        mockMvc.perform(put("/api/v1/photos/{id}/favorite", photoId)
                        .with(jwt(TEST_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(favoriteBody(Boolean.TRUE))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.favorite").value(true));
    }

    @Test
    void updateFavoriteWithoutFavoriteShouldReturnFailure() throws Exception {
        Long photoId = insertTestPhoto(uniqueOriginalName(), true);

        mockMvc.perform(put("/api/v1/photos/{id}/favorite", photoId)
                        .with(jwt(TEST_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new HashMap<>())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void updateFavoriteWhenPhotoNotExistsShouldReturnBizFailure() throws Exception {
        mockMvc.perform(put("/api/v1/photos/{id}/favorite", -1L)
                        .with(jwt(TEST_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(favoriteBody(Boolean.TRUE))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void downloadPhotoShouldReturnSuccess() throws Exception {
        Long photoId = insertTestPhoto(uniqueOriginalName(), true);

        mockMvc.perform(get("/api/v1/photos/{id}/download", photoId).with(jwt(TEST_USER_ID)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", MediaType.IMAGE_PNG_VALUE));
    }

    @Test
    void previewPhotoShouldReturnJpegPreview() throws Exception {
        MockMultipartFile file = generatedPngFile(uniqueOriginalName());
        String response = mockMvc.perform(multipart("/api/v1/photos/upload").file(file).with(jwt(TEST_USER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long photoId = objectMapper.readTree(response).path("data").path("id").asLong();

        mockMvc.perform(get("/api/v1/photos/{id}/preview", photoId).with(jwt(TEST_USER_ID)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", MediaType.IMAGE_JPEG_VALUE));
    }

    @Test
    void previewHeicPhotoShouldReturnUnsupportedPreviewMessage() throws Exception {
        String originalName = uniqueOriginalName() + ".heic";
        MockMultipartFile file = new MockMultipartFile("file", originalName,
                MediaType.APPLICATION_OCTET_STREAM_VALUE, "heic image bytes".getBytes());
        String response = mockMvc.perform(multipart("/api/v1/photos/upload").file(file).with(jwt(TEST_USER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long photoId = objectMapper.readTree(response).path("data").path("id").asLong();

        mockMvc.perform(get("/api/v1/photos/{id}/preview", photoId).with(jwt(TEST_USER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("HEIC 暂不支持预览，可下载原图查看"));
    }

    @Test
    void downloadHeicPhotoShouldReturnOriginalFile() throws Exception {
        String originalName = uniqueOriginalName() + ".heic";
        MockMultipartFile file = new MockMultipartFile("file", originalName,
                MediaType.APPLICATION_OCTET_STREAM_VALUE, "heic image bytes".getBytes());
        String response = mockMvc.perform(multipart("/api/v1/photos/upload").file(file).with(jwt(TEST_USER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long photoId = objectMapper.readTree(response).path("data").path("id").asLong();

        mockMvc.perform(get("/api/v1/photos/{id}/download", photoId).with(jwt(TEST_USER_ID)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/heic"));
    }

    @Test
    void downloadPhotoWhenNotExistsShouldReturnBizFailure() throws Exception {
        mockMvc.perform(get("/api/v1/photos/{id}/download", -1L).with(jwt(TEST_USER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void deletePhotoShouldReturnSuccess() throws Exception {
        Long photoId = insertTestPhoto(uniqueOriginalName(), false);

        mockMvc.perform(delete("/api/v1/photos/{id}", photoId).with(jwt(TEST_USER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM pm_photo WHERE id = ?", Integer.class, photoId);
        org.assertj.core.api.Assertions.assertThat(count).isZero();
    }

    @Test
    void deletePhotoWhenNotExistsShouldReturnBizFailure() throws Exception {
        mockMvc.perform(delete("/api/v1/photos/{id}", -1L).with(jwt(TEST_USER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void deleteOtherUserPhotoShouldReturnBizFailure() throws Exception {
        Long photoId = insertTestPhoto(OTHER_USER_ID, uniqueOriginalName(), true);

        mockMvc.perform(delete("/api/v1/photos/{id}", photoId).with(jwt(TEST_USER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM pm_photo WHERE id = ?", Integer.class, photoId);
        org.assertj.core.api.Assertions.assertThat(count).isOne();
    }

    @Test
    void timelineShouldReturnSuccess() throws Exception {
        mockMvc.perform(get("/api/v1/photos/timeline").with(jwt(TEST_USER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void locationsShouldReturnSuccess() throws Exception {
        mockMvc.perform(get("/api/v1/photos/locations").with(jwt(TEST_USER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void peopleShouldReturnSuccess() throws Exception {
        mockMvc.perform(get("/api/v1/photos/people").with(jwt(TEST_USER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    private void ensureTestUser() {
        ensureUser(TEST_USER_ID, "it_user_1");
    }

    private void ensureOtherUser() {
        ensureUser(OTHER_USER_ID, "it_user_2");
    }

    private void ensureUser(long userId, String username) {
        jdbcTemplate.update("""
                INSERT INTO sys_user (id, username, password, nickname, role, status, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (id) DO NOTHING
                """, userId, username, "test_password", "Integration Test User", "USER", 1,
                LocalDateTime.now(), LocalDateTime.now());
    }

    private Long insertTestPhoto(String originalName, boolean createFile) throws Exception {
        return insertTestPhoto(TEST_USER_ID, originalName, createFile);
    }

    private Long insertTestPhoto(long userId, String originalName, boolean createFile) throws Exception {
        Files.createDirectories(storageRoot);
        String filename = originalName + ".png";
        Path filePath = storageRoot.resolve(filename).normalize();
        if (createFile) {
            Files.write(filePath, PNG_BYTES);
        }
        LocalDateTime now = LocalDateTime.now();
        return jdbcTemplate.queryForObject("""
                INSERT INTO pm_photo
                    (user_id, filename, original_name, file_path, file_size, mime_type, width, height,
                     is_favorite, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, FALSE, ?, ?)
                RETURNING id
                """, Long.class, userId, filename, originalName + ".png", filePath.toString(),
                PNG_BYTES.length, MediaType.IMAGE_PNG_VALUE, 1, 1, now, now);
    }

    private Long insertTestAlbum(long userId, String name) {
        return jdbcTemplate.queryForObject("""
                INSERT INTO pm_album
                    (user_id, name, description, cover_photo_id, is_default, is_deleted, created_at, updated_at)
                VALUES (?, ?, ?, NULL, FALSE, FALSE, ?, ?)
                RETURNING id
                """, Long.class, userId, name, "integration test album", LocalDateTime.now(), LocalDateTime.now());
    }

    private void cleanTestPhotos() throws Exception {
        List<String> filePaths = jdbcTemplate.queryForList("""
                SELECT file_path FROM pm_photo
                WHERE user_id IN (?, ?) AND original_name LIKE ?
                """, String.class, TEST_USER_ID, OTHER_USER_ID, TEST_PREFIX + "%");
        jdbcTemplate.update("""
                DELETE FROM pm_album_photo
                WHERE photo_id IN (
                    SELECT id FROM pm_photo WHERE user_id IN (?, ?) AND original_name LIKE ?
                )
                """, TEST_USER_ID, OTHER_USER_ID, TEST_PREFIX + "%");
        jdbcTemplate.update("""
                DELETE FROM pm_face
                WHERE photo_id IN (
                    SELECT id FROM pm_photo WHERE user_id IN (?, ?) AND original_name LIKE ?
                )
                """, TEST_USER_ID, OTHER_USER_ID, TEST_PREFIX + "%");
        jdbcTemplate.update("DELETE FROM pm_photo WHERE user_id IN (?, ?) AND original_name LIKE ?",
                TEST_USER_ID, OTHER_USER_ID, TEST_PREFIX + "%");
        jdbcTemplate.update("DELETE FROM pm_album WHERE user_id IN (?, ?) AND name LIKE ?",
                TEST_USER_ID, OTHER_USER_ID, "it_photo_album_%");
        for (String filePath : filePaths) {
            deleteQuietly(Paths.get(filePath));
        }
        if (Files.exists(storageRoot)) {
            try (var stream = Files.walk(storageRoot)) {
                stream.sorted(Comparator.reverseOrder())
                        .filter(path -> !path.equals(storageRoot))
                        .forEach(this::deleteQuietly);
            }
        }
    }

    private MockMultipartFile pngFile(String originalName) {
        return new MockMultipartFile("file", originalName + ".png", MediaType.IMAGE_PNG_VALUE, PNG_BYTES);
    }

    private MockMultipartFile generatedPngFile(String originalName) throws Exception {
        BufferedImage image = new BufferedImage(4, 3, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, 0x336699);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", outputStream);
        return new MockMultipartFile("file", originalName + ".png", MediaType.IMAGE_PNG_VALUE,
                outputStream.toByteArray());
    }

    private MockMultipartFile jpegWithExif(String originalName) throws Exception {
        BufferedImage image = new BufferedImage(4, 3, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream jpeg = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", jpeg);
        byte[] imageBytes = jpeg.toByteArray();
        byte[] exifPayload = buildExifPayload();

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write(imageBytes, 0, 2);
        output.write(0xFF);
        output.write(0xE1);
        int segmentLength = exifPayload.length + 2;
        output.write((segmentLength >>> 8) & 0xFF);
        output.write(segmentLength & 0xFF);
        output.write(exifPayload);
        output.write(imageBytes, 2, imageBytes.length - 2);
        return new MockMultipartFile("file", originalName + ".jpg", MediaType.IMAGE_JPEG_VALUE, output.toByteArray());
    }

    private byte[] buildExifPayload() {
        byte[] payload = new byte[6 + 218];
        payload[0] = 'E';
        payload[1] = 'x';
        payload[2] = 'i';
        payload[3] = 'f';
        ByteBuffer tiff = ByteBuffer.wrap(payload, 6, 218).slice().order(ByteOrder.LITTLE_ENDIAN);
        tiff.put((byte) 'I').put((byte) 'I').putShort((short) 42).putInt(8);

        tiff.position(8);
        tiff.putShort((short) 4);
        putExifEntry(tiff, 0x010F, 2, 6, 62);
        putExifEntry(tiff, 0x0110, 2, 10, 68);
        putExifEntry(tiff, 0x8769, 4, 1, 78);
        putExifEntry(tiff, 0x8825, 4, 1, 116);
        tiff.putInt(0);

        putAscii(tiff, 62, "Apple");
        putAscii(tiff, 68, "iPhone 15");

        tiff.position(78);
        tiff.putShort((short) 1);
        putExifEntry(tiff, 0x9003, 2, 20, 96);
        tiff.putInt(0);
        putAscii(tiff, 96, "2025:01:02 03:04:05");

        tiff.position(116);
        tiff.putShort((short) 4);
        putExifEntry(tiff, 0x0001, 2, 2, 'N');
        putExifEntry(tiff, 0x0002, 5, 3, 170);
        putExifEntry(tiff, 0x0003, 2, 2, 'E');
        putExifEntry(tiff, 0x0004, 5, 3, 194);
        tiff.putInt(0);
        putRationals(tiff, 170, new int[][]{{31, 1}, {14, 1}, {48, 10}});
        putRationals(tiff, 194, new int[][]{{121, 1}, {28, 1}, {72, 10}});
        return payload;
    }

    private void putExifEntry(ByteBuffer buffer, int tag, int type, int count, int valueOrOffset) {
        buffer.putShort((short) tag).putShort((short) type).putInt(count).putInt(valueOrOffset);
    }

    private void putAscii(ByteBuffer buffer, int offset, String value) {
        buffer.position(offset);
        buffer.put(value.getBytes(java.nio.charset.StandardCharsets.US_ASCII)).put((byte) 0);
    }

    private void putRationals(ByteBuffer buffer, int offset, int[][] values) {
        buffer.position(offset);
        for (int[] value : values) {
            buffer.putInt(value[0]).putInt(value[1]);
        }
    }

    private Map<String, Object> favoriteBody(Boolean favorite) {
        Map<String, Object> body = new HashMap<>();
        body.put("favorite", favorite);
        return body;
    }

    private String uniqueOriginalName() {
        return TEST_PREFIX + UUID.randomUUID().toString().replace("-", "");
    }

    private String toJson(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (Exception ignored) {
            // Ignore cleanup failures for test files.
        }
    }
}
