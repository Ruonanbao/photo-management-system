package com.example.photomanagementsystem.album.service.impl;

import com.example.photomanagementsystem.album.dto.AlbumCreateDTO;
import com.example.photomanagementsystem.album.dto.AlbumPhotoAddDTO;
import com.example.photomanagementsystem.album.dto.AlbumUpdateDTO;
import com.example.photomanagementsystem.album.entity.Album;
import com.example.photomanagementsystem.album.entity.AlbumPhoto;
import com.example.photomanagementsystem.album.entity.Photo;
import com.example.photomanagementsystem.album.mapper.AlbumMapper;
import com.example.photomanagementsystem.album.mapper.AlbumPhotoMapper;
import com.example.photomanagementsystem.album.mapper.PhotoMapper;
import com.example.photomanagementsystem.album.service.AlbumService;
import com.example.photomanagementsystem.album.vo.AlbumVO;
import com.example.photomanagementsystem.album.vo.PhotoVO;
import com.example.photomanagementsystem.common.BizException;
import com.example.photomanagementsystem.common.CurrentUserProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AlbumServiceImpl implements AlbumService {

    private static final int ALBUM_NAME_MAX_LENGTH = 50;

    private final AlbumMapper albumMapper;
    private final AlbumPhotoMapper albumPhotoMapper;
    private final PhotoMapper photoMapper;
    private final CurrentUserProvider currentUserProvider;

    public AlbumServiceImpl(AlbumMapper albumMapper, AlbumPhotoMapper albumPhotoMapper, PhotoMapper photoMapper,
            CurrentUserProvider currentUserProvider) {
        this.albumMapper = albumMapper;
        this.albumPhotoMapper = albumPhotoMapper;
        this.photoMapper = photoMapper;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    public List<AlbumVO> listAlbums() {
        Long userId = getCurrentUserId();
        return albumMapper.selectListByUserId(userId).stream()
                .map(this::convertToAlbumVO)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AlbumVO createAlbum(AlbumCreateDTO createDTO) {
        Long userId = getCurrentUserId();
        String albumName = createDTO == null ? null : createDTO.getName();
        validateAlbumName(albumName);

        String trimmedName = albumName.trim();
        validateAlbumNameNotExists(trimmedName, userId, null);

        LocalDateTime now = LocalDateTime.now();
        Album album = new Album();
        album.setUserId(userId);
        album.setName(trimmedName);
        album.setDescription(createDTO.getDescription());
        album.setCoverPhotoId(null);
        album.setDefaultAlbum(Boolean.FALSE);
        album.setDeleted(Boolean.FALSE);
        album.setCreateTime(now);
        album.setUpdateTime(now);
        return convertToAlbumVO(albumMapper.insert(album));
    }

    @Override
    public AlbumVO getAlbum(Long id) {
        Long userId = getCurrentUserId();
        return convertToAlbumVO(getAlbumEntity(id, userId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AlbumVO updateAlbum(Long id, AlbumUpdateDTO updateDTO) {
        Long userId = getCurrentUserId();
        Album album = getAlbumEntity(id, userId);
        String albumName = updateDTO == null ? null : updateDTO.getName();
        validateAlbumName(albumName);

        String trimmedName = albumName.trim();
        if (Boolean.TRUE.equals(album.getDefaultAlbum()) && !album.getName().equals(trimmedName)) {
            throw new BizException(400, "默认相册不能改名");
        }
        validateAlbumNameNotExists(trimmedName, userId, id);

        album.setName(trimmedName);
        album.setDescription(updateDTO.getDescription());
        album.setUpdateTime(LocalDateTime.now());
        return convertToAlbumVO(albumMapper.updateByIdAndUserId(album));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAlbum(Long id) {
        Long userId = getCurrentUserId();
        Album album = getAlbumEntity(id, userId);
        if (Boolean.TRUE.equals(album.getDefaultAlbum())) {
            throw new BizException(400, "默认相册不能删除");
        }
        albumPhotoMapper.deleteByAlbumIdAndUserId(id, userId);
        albumMapper.deleteByIdAndUserId(id, userId);
    }

    @Override
    public List<PhotoVO> listAlbumPhotos(Long id) {
        Long userId = getCurrentUserId();
        getAlbumEntity(id, userId);
        return photoMapper.selectListByAlbumIdAndUserId(id, userId).stream()
                .map(this::convertToPhotoVO)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addPhotoToAlbum(Long id, AlbumPhotoAddDTO addDTO) {
        Long userId = getCurrentUserId();
        getAlbumEntity(id, userId);

        Long photoId = addDTO == null ? null : addDTO.getPhotoId();
        if (photoId == null) {
            throw new BizException(400, "照片ID不能为空");
        }
        if (!photoMapper.existsByIdAndUserId(photoId, userId)) {
            throw new BizException(404, "照片不存在");
        }
        if (albumPhotoMapper.existsByAlbumIdAndPhotoIdAndUserId(id, photoId, userId)) {
            throw new BizException(400, "照片已在相册中");
        }

        AlbumPhoto albumPhoto = new AlbumPhoto();
        albumPhoto.setAlbumId(id);
        albumPhoto.setPhotoId(photoId);
        albumPhoto.setCreateTime(LocalDateTime.now());
        albumPhotoMapper.insert(albumPhoto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removePhotoFromAlbum(Long id, Long photoId) {
        Long userId = getCurrentUserId();
        getAlbumEntity(id, userId);
        if (photoId == null) {
            throw new BizException(400, "照片ID不能为空");
        }
        if (!albumPhotoMapper.existsByAlbumIdAndPhotoIdAndUserId(id, photoId, userId)) {
            throw new BizException(404, "照片不在相册中");
        }
        albumPhotoMapper.deleteByAlbumIdAndPhotoIdAndUserId(id, photoId, userId);
    }

    private Album getAlbumEntity(Long id, Long userId) {
        if (id == null) {
            throw new BizException(400, "相册ID不能为空");
        }
        return albumMapper.selectByIdAndUserId(id, userId)
                .orElseThrow(() -> new BizException(404, "相册不存在"));
    }

    private void validateAlbumName(String name) {
        if (!StringUtils.hasText(name)) {
            throw new BizException(400, "相册名称不能为空");
        }
        if (name.trim().length() > ALBUM_NAME_MAX_LENGTH) {
            throw new BizException(400, "相册名称长度不能超过50");
        }
    }

    private void validateAlbumNameNotExists(String name, Long userId, Long currentAlbumId) {
        albumMapper.selectByNameAndUserId(name, userId)
                .filter(album -> currentAlbumId == null || !album.getId().equals(currentAlbumId))
                .ifPresent(album -> {
                    throw new BizException(400, "相册名称不能重复");
                });
    }

    private Long getCurrentUserId() {
        return currentUserProvider.getCurrentUserId();
    }

    private AlbumVO convertToAlbumVO(Album album) {
        AlbumVO albumVO = new AlbumVO();
        albumVO.setId(album.getId());
        albumVO.setName(album.getName());
        albumVO.setDescription(album.getDescription());
        albumVO.setCoverPhotoId(album.getCoverPhotoId());
        albumVO.setDefaultAlbum(album.getDefaultAlbum());
        albumVO.setPhotoCount(albumPhotoMapper.countByAlbumIdAndUserId(album.getId(), album.getUserId()));
        albumVO.setCreateTime(album.getCreateTime());
        albumVO.setUpdateTime(album.getUpdateTime());
        return albumVO;
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
