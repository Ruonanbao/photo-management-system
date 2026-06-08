package com.example.photomanagementsystem.photo.service.impl;

import com.example.photomanagementsystem.common.BizException;
import com.example.photomanagementsystem.common.CurrentUserProvider;
import com.example.photomanagementsystem.photo.dto.PhotoFavoriteDTO;
import com.example.photomanagementsystem.photo.dto.PhotoListQueryDTO;
import com.example.photomanagementsystem.photo.dto.PhotoUploadDTO;
import com.example.photomanagementsystem.photo.entity.AlbumPhoto;
import com.example.photomanagementsystem.photo.entity.Photo;
import com.example.photomanagementsystem.photo.mapper.PhotoAlbumMapper;
import com.example.photomanagementsystem.photo.mapper.PhotoMapper;
import com.example.photomanagementsystem.photo.service.PhotoMetadata;
import com.example.photomanagementsystem.photo.service.PhotoMetadataExtractor;
import com.example.photomanagementsystem.photo.service.PhotoPreviewGenerator;
import com.example.photomanagementsystem.photo.service.PhotoService;
import com.example.photomanagementsystem.photo.vo.PersonPhotoRowVO;
import com.example.photomanagementsystem.photo.vo.PhotoDownloadVO;
import com.example.photomanagementsystem.photo.vo.PhotoLocationVO;
import com.example.photomanagementsystem.photo.vo.PhotoPageVO;
import com.example.photomanagementsystem.photo.vo.PhotoPeopleVO;
import com.example.photomanagementsystem.photo.vo.PhotoTimelineVO;
import com.example.photomanagementsystem.photo.vo.PhotoVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class PhotoServiceImpl implements PhotoService {

    private static final Logger log = LoggerFactory.getLogger(PhotoServiceImpl.class);
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;
    private static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final String OCTET_STREAM_MIME_TYPE = "application/octet-stream";
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".webp", ".heic", ".heif");
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/heic", "image/heif",
            "image/heic-sequence", "image/heif-sequence", "image/x-heic", "image/x-heif");

    private final PhotoMapper photoMapper;
    private final PhotoAlbumMapper photoAlbumMapper;
    private final CurrentUserProvider currentUserProvider;
    private final PhotoMetadataExtractor photoMetadataExtractor;
    private final PhotoPreviewGenerator photoPreviewGenerator;
    private final String storagePath;
    private final long maxFileSizeBytes;

    public PhotoServiceImpl(PhotoMapper photoMapper, PhotoAlbumMapper photoAlbumMapper,
            CurrentUserProvider currentUserProvider, PhotoMetadataExtractor photoMetadataExtractor,
            PhotoPreviewGenerator photoPreviewGenerator,
            @Value("${photo.storage.path:uploads/photos}") String storagePath,
            @Value("${spring.servlet.multipart.max-file-size:50MB}") DataSize maxFileSize) {
        this.photoMapper = photoMapper;
        this.photoAlbumMapper = photoAlbumMapper;
        this.currentUserProvider = currentUserProvider;
        this.photoMetadataExtractor = photoMetadataExtractor;
        this.photoPreviewGenerator = photoPreviewGenerator;
        this.storagePath = storagePath;
        this.maxFileSizeBytes = maxFileSize.toBytes();
    }

    @Override
    public PhotoPageVO<PhotoVO> listPhotos(PhotoListQueryDTO queryDTO) {
        Long userId = getCurrentUserId();
        int page = normalizePage(queryDTO == null ? null : queryDTO.getPage());
        int size = normalizeSize(queryDTO == null ? null : queryDTO.getSize());
        int offset = (page - 1) * size;

        PhotoPageVO<PhotoVO> pageVO = new PhotoPageVO<>();
        pageVO.setRecords(photoMapper.selectPageByUserId(userId, queryDTO, offset, size).stream()
                .map(this::convertToPhotoVO)
                .toList());
        pageVO.setTotal(photoMapper.countByUserId(userId, queryDTO));
        pageVO.setPage(page);
        pageVO.setSize(size);
        return pageVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PhotoVO uploadPhoto(PhotoUploadDTO uploadDTO) {
        Long userId = getCurrentUserId();
        MultipartFile file = uploadDTO == null ? null : uploadDTO.getFile();
        validateUploadFile(file);

        Long albumId = uploadDTO.getAlbumId();
        if (albumId != null && !photoAlbumMapper.existsAlbumByIdAndUserId(albumId, userId)) {
            throw new BizException(404, "相册不存在");
        }

        Path savedPath = saveFile(file);
        Path previewPath = null;
        try {
            LocalDateTime now = LocalDateTime.now();
            String mimeType = resolveMimeType(file);
            previewPath = generatePreview(savedPath, mimeType);
            Dimension dimension = readImageDimension(savedPath, mimeType);
            PhotoMetadata metadata = photoMetadataExtractor.extract(savedPath);
            Photo photo = new Photo();
            photo.setUserId(userId);
            photo.setFilename(savedPath.getFileName().toString());
            photo.setOriginalName(file.getOriginalFilename());
            photo.setFilePath(savedPath.toString());
            photo.setThumbnailPath(previewPath == null ? null : previewPath.toString());
            photo.setFileSize(file.getSize());
            photo.setMimeType(mimeType);
            photo.setWidth(dimension == null ? null : dimension.width);
            photo.setHeight(dimension == null ? null : dimension.height);
            photo.setShotAt(metadata.shotAt());
            photo.setLatitude(metadata.latitude());
            photo.setLongitude(metadata.longitude());
            photo.setCameraMake(metadata.cameraMake());
            photo.setCameraModel(metadata.cameraModel());
            photo.setFavorite(Boolean.FALSE);
            photo.setCreateTime(now);
            photo.setUpdateTime(now);
            Photo savedPhoto = photoMapper.insert(photo);

            if (albumId != null) {
                AlbumPhoto albumPhoto = new AlbumPhoto();
                albumPhoto.setAlbumId(albumId);
                albumPhoto.setPhotoId(savedPhoto.getId());
                albumPhoto.setCreateTime(now);
                photoAlbumMapper.insertRelation(albumPhoto);
            }
            return convertToPhotoVO(savedPhoto);
        } catch (RuntimeException exception) {
            deleteLocalFileQuietly(previewPath);
            deleteLocalFileQuietly(savedPath);
            throw exception;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<PhotoVO> uploadPhotos(List<MultipartFile> files, Long albumId) {
        if (files == null || files.isEmpty()) {
            throw new BizException(400, "上传文件不能为空");
        }
        List<PhotoVO> uploadedPhotos = new ArrayList<>();
        for (MultipartFile file : files) {
            PhotoUploadDTO uploadDTO = new PhotoUploadDTO();
            uploadDTO.setFile(file);
            uploadDTO.setAlbumId(albumId);
            uploadedPhotos.add(uploadPhoto(uploadDTO));
        }
        return uploadedPhotos;
    }

    @Override
    public PhotoVO getPhoto(Long id) {
        Long userId = getCurrentUserId();
        return convertToPhotoVO(getPhotoEntity(id, userId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePhoto(Long id) {
        Long userId = getCurrentUserId();
        Photo photo = getPhotoEntity(id, userId);
        photoAlbumMapper.deleteByPhotoIdAndUserId(id, userId);
        photoMapper.deleteByIdAndUserId(id, userId);
        deleteLocalFile(StringUtils.hasText(photo.getThumbnailPath()) ? Paths.get(photo.getThumbnailPath()) : null);
        deleteLocalFile(Paths.get(photo.getFilePath()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PhotoVO updateFavorite(Long id, PhotoFavoriteDTO favoriteDTO) {
        Long userId = getCurrentUserId();
        Photo photo = getPhotoEntity(id, userId);
        Boolean favorite = favoriteDTO == null ? null : favoriteDTO.getFavorite();
        if (favorite == null) {
            throw new BizException(400, "收藏状态不能为空");
        }
        LocalDateTime now = LocalDateTime.now();
        photoMapper.updateFavorite(id, userId, favorite, now);
        photo.setFavorite(favorite);
        photo.setUpdateTime(now);
        return convertToPhotoVO(photo);
    }

    @Override
    public PhotoDownloadVO getDownloadFile(Long id) {
        Long userId = getCurrentUserId();
        Photo photo = getPhotoEntity(id, userId);
        Path path = Paths.get(photo.getFilePath());
        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            throw new BizException(404, "照片文件不存在");
        }

        PhotoDownloadVO downloadVO = new PhotoDownloadVO();
        downloadVO.setPath(path);
        downloadVO.setFilename(StringUtils.hasText(photo.getOriginalName()) ? photo.getOriginalName() : photo.getFilename());
        downloadVO.setMimeType(photo.getMimeType());
        return downloadVO;
    }

    @Override
    public PhotoDownloadVO getPreviewFile(Long id) {
        Long userId = getCurrentUserId();
        Photo photo = getPhotoEntity(id, userId);
        Path previewPath = StringUtils.hasText(photo.getThumbnailPath()) ? Paths.get(photo.getThumbnailPath()) : null;
        if (previewPath != null && Files.exists(previewPath) && Files.isRegularFile(previewPath)) {
            PhotoDownloadVO previewVO = new PhotoDownloadVO();
            previewVO.setPath(previewPath);
            previewVO.setFilename(previewPath.getFileName().toString());
            previewVO.setMimeType(MediaType.IMAGE_JPEG_VALUE);
            return previewVO;
        }
        if (isHeicMimeType(photo.getMimeType())) {
            throw new BizException(404, "HEIC 暂不支持预览，可下载原图查看");
        }
        return getDownloadFile(id);
    }

    @Override
    public List<PhotoTimelineVO> listTimeline() {
        Long userId = getCurrentUserId();
        Map<String, List<PhotoVO>> groupMap = new LinkedHashMap<>();
        photoMapper.selectTimelineByUserId(userId).forEach(photo -> {
            LocalDateTime groupTime = photo.getShotAt() == null ? photo.getCreateTime() : photo.getShotAt();
            String yearMonth = YEAR_MONTH_FORMATTER.format(groupTime);
            groupMap.computeIfAbsent(yearMonth, key -> new ArrayList<>()).add(convertToPhotoVO(photo));
        });

        return groupMap.entrySet().stream()
                .map(entry -> {
                    PhotoTimelineVO timelineVO = new PhotoTimelineVO();
                    timelineVO.setYearMonth(entry.getKey());
                    timelineVO.setPhotos(entry.getValue());
                    return timelineVO;
                })
                .toList();
    }

    @Override
    public List<PhotoLocationVO> listLocations() {
        Long userId = getCurrentUserId();
        Map<String, List<PhotoVO>> groupMap = new LinkedHashMap<>();
        photoMapper.selectLocationsByUserId(userId).forEach(photo -> {
            String locationName = StringUtils.hasText(photo.getLocationName()) ? photo.getLocationName() : "未知地点";
            groupMap.computeIfAbsent(locationName, key -> new ArrayList<>()).add(convertToPhotoVO(photo));
        });

        return groupMap.entrySet().stream()
                .map(entry -> {
                    PhotoLocationVO locationVO = new PhotoLocationVO();
                    locationVO.setLocationName(entry.getKey());
                    locationVO.setPhotos(entry.getValue());
                    return locationVO;
                })
                .toList();
    }

    @Override
    public List<PhotoPeopleVO> listPeople() {
        Long userId = getCurrentUserId();
        Map<Long, PhotoPeopleVO> peopleMap = new LinkedHashMap<>();
        Map<Long, Set<Long>> personPhotoIdMap = new LinkedHashMap<>();

        for (PersonPhotoRowVO row : photoMapper.selectPeoplePhotosByUserId(userId)) {
            PhotoPeopleVO peopleVO = peopleMap.computeIfAbsent(row.getPersonId(), personId -> {
                PhotoPeopleVO newPeopleVO = new PhotoPeopleVO();
                newPeopleVO.setPersonId(row.getPersonId());
                newPeopleVO.setPersonName(row.getPersonName());
                newPeopleVO.setCoverFaceId(row.getCoverFaceId());
                newPeopleVO.setPhotos(new ArrayList<>());
                return newPeopleVO;
            });
            Set<Long> photoIds = personPhotoIdMap.computeIfAbsent(row.getPersonId(), personId -> new HashSet<>());
            if (photoIds.add(row.getPhoto().getId())) {
                peopleVO.getPhotos().add(row.getPhoto());
            }
        }
        return new ArrayList<>(peopleMap.values());
    }

    private void validateUploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException(400, "上传文件不能为空");
        }
        if (file.getSize() > maxFileSizeBytes) {
            throw new BizException(413, "上传文件过大");
        }
        if (!isAllowedImageFile(file)) {
            throw new BizException(400, "只允许上传 JPG、PNG、WEBP、HEIC 图片");
        }
    }

    private Path saveFile(MultipartFile file) {
        try {
            Path storageRoot = Paths.get(storagePath).toAbsolutePath().normalize();
            Files.createDirectories(storageRoot);
            Path savedPath = storageRoot.resolve(buildUniqueFilename(file.getOriginalFilename(), resolveMimeType(file)))
                    .normalize();
            if (!savedPath.startsWith(storageRoot)) {
                throw new BizException(400, "文件名不合法");
            }
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, savedPath, StandardCopyOption.REPLACE_EXISTING);
            }
            return savedPath;
        } catch (IOException exception) {
            throw new BizException(500, "保存照片文件失败");
        }
    }

    private Path generatePreview(Path savedPath, String mimeType) {
        if (isHeicMimeType(mimeType)) {
            log.info("Skipping HEIC preview generation for {}", savedPath.getFileName());
            return null;
        }
        try {
            return photoPreviewGenerator.generatePreview(savedPath, mimeType);
        } catch (IOException | RuntimeException exception) {
            log.warn("Failed to generate preview for {}", savedPath.getFileName(), exception);
            return null;
        }
    }

    private String buildUniqueFilename(String originalFilename, String mimeType) {
        String extension = getExtension(originalFilename);
        if (!StringUtils.hasText(extension)) {
            extension = switch (mimeType) {
                case "image/jpeg" -> ".jpg";
                case "image/png" -> ".png";
                case "image/webp" -> ".webp";
                case "image/heic", "image/heic-sequence", "image/x-heic" -> ".heic";
                case "image/heif", "image/heif-sequence", "image/x-heif" -> ".heif";
                default -> "";
            };
        }
        return UUID.randomUUID().toString().replace("-", "") + extension.toLowerCase(Locale.ROOT);
    }

    private boolean isAllowedImageFile(MultipartFile file) {
        String extension = getExtension(file.getOriginalFilename()).toLowerCase(Locale.ROOT);
        String contentType = normalizeMimeType(file.getContentType());
        if (!StringUtils.hasText(extension)) {
            return ALLOWED_MIME_TYPES.contains(contentType);
        }
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            return false;
        }
        return !StringUtils.hasText(contentType)
                || OCTET_STREAM_MIME_TYPE.equals(contentType)
                || ALLOWED_MIME_TYPES.contains(contentType);
    }

    private String resolveMimeType(MultipartFile file) {
        String contentType = normalizeMimeType(file.getContentType());
        if (StringUtils.hasText(contentType) && !OCTET_STREAM_MIME_TYPE.equals(contentType)) {
            return contentType;
        }
        return switch (getExtension(file.getOriginalFilename()).toLowerCase(Locale.ROOT)) {
            case ".jpg", ".jpeg" -> "image/jpeg";
            case ".png" -> "image/png";
            case ".webp" -> "image/webp";
            case ".heic" -> "image/heic";
            case ".heif" -> "image/heif";
            default -> contentType;
        };
    }

    private String normalizeMimeType(String contentType) {
        return StringUtils.hasText(contentType) ? contentType.toLowerCase(Locale.ROOT) : "";
    }

    private boolean isHeicMimeType(String mimeType) {
        return StringUtils.hasText(mimeType) && (mimeType.contains("heic") || mimeType.contains("heif"));
    }

    private String getExtension(String originalFilename) {
        if (!StringUtils.hasText(originalFilename)) {
            return "";
        }
        String cleanName = Paths.get(originalFilename).getFileName().toString();
        int dotIndex = cleanName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == cleanName.length() - 1) {
            return "";
        }
        return cleanName.substring(dotIndex);
    }

    private Dimension readImageDimension(Path path, String mimeType) {
        if (isHeicMimeType(mimeType)) {
            return null;
        }
        if ("image/webp".equals(mimeType)) {
            return readWebpDimension(path);
        }
        try {
            BufferedImage image = ImageIO.read(path.toFile());
            if (image != null) {
                return new Dimension(image.getWidth(), image.getHeight());
            }
            try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(path.toFile())) {
                ImageReader reader = ImageIO.getImageReaders(imageInputStream).next();
                reader.setInput(imageInputStream);
                return new Dimension(reader.getWidth(0), reader.getHeight(0));
            }
        } catch (Exception exception) {
            return null;
        }
    }

    private Dimension readWebpDimension(Path path) {
        try {
            byte[] bytes = Files.readAllBytes(path);
            if (bytes.length < 30 || !"RIFF".equals(new String(bytes, 0, 4))
                    || !"WEBP".equals(new String(bytes, 8, 4))) {
                return null;
            }
            String chunkType = new String(bytes, 12, 4);
            if ("VP8X".equals(chunkType) && bytes.length >= 30) {
                int width = 1 + readLittleEndian24(bytes, 24);
                int height = 1 + readLittleEndian24(bytes, 27);
                return new Dimension(width, height);
            }
            if ("VP8 ".equals(chunkType) && bytes.length >= 30) {
                int width = ByteBuffer.wrap(bytes, 26, 2).order(ByteOrder.LITTLE_ENDIAN).getShort() & 0x3FFF;
                int height = ByteBuffer.wrap(bytes, 28, 2).order(ByteOrder.LITTLE_ENDIAN).getShort() & 0x3FFF;
                return new Dimension(width, height);
            }
            if ("VP8L".equals(chunkType) && bytes.length >= 25) {
                int b0 = bytes[21] & 0xFF;
                int b1 = bytes[22] & 0xFF;
                int b2 = bytes[23] & 0xFF;
                int b3 = bytes[24] & 0xFF;
                int width = 1 + (((b1 & 0x3F) << 8) | b0);
                int height = 1 + ((b3 << 6) | (b2 >> 2) | ((b1 & 0xC0) << 6));
                return new Dimension(width, height);
            }
            return null;
        } catch (IOException exception) {
            return null;
        }
    }

    private int readLittleEndian24(byte[] bytes, int offset) {
        return (bytes[offset] & 0xFF) | ((bytes[offset + 1] & 0xFF) << 8) | ((bytes[offset + 2] & 0xFF) << 16);
    }

    private void deleteLocalFile(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException exception) {
            throw new BizException(500, "删除照片文件失败");
        }
    }

    private void deleteLocalFileQuietly(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // Ignore cleanup failures after upload errors.
        }
    }

    private Photo getPhotoEntity(Long id, Long userId) {
        if (id == null) {
            throw new BizException(400, "照片ID不能为空");
        }
        return photoMapper.selectByIdAndUserId(id, userId)
                .orElseThrow(() -> new BizException(404, "照片不存在"));
    }

    private int normalizePage(Integer page) {
        return page == null || page < 1 ? DEFAULT_PAGE : page;
    }

    private int normalizeSize(Integer size) {
        if (size == null || size < 1) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
    }

    private Long getCurrentUserId() {
        return currentUserProvider.getCurrentUserId();
    }

    private PhotoVO convertToPhotoVO(Photo photo) {
        PhotoVO photoVO = new PhotoVO();
        photoVO.setId(photo.getId());
        photoVO.setFilename(photo.getFilename());
        photoVO.setOriginalName(photo.getOriginalName());
        photoVO.setFileSize(photo.getFileSize());
        photoVO.setMimeType(photo.getMimeType());
        photoVO.setWidth(photo.getWidth());
        photoVO.setHeight(photo.getHeight());
        photoVO.setShotAt(photo.getShotAt());
        photoVO.setLatitude(photo.getLatitude());
        photoVO.setLongitude(photo.getLongitude());
        photoVO.setLocationName(photo.getLocationName());
        photoVO.setCameraMake(photo.getCameraMake());
        photoVO.setCameraModel(photo.getCameraModel());
        photoVO.setFavorite(photo.getFavorite());
        photoVO.setCreateTime(photo.getCreateTime());
        photoVO.setUpdateTime(photo.getUpdateTime());
        return photoVO;
    }
}
