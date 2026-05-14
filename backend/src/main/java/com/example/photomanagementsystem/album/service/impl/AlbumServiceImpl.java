package com.example.photomanagementsystem.album.service.impl;

import com.example.photomanagementsystem.album.dto.AlbumCreateDTO;
import com.example.photomanagementsystem.album.entity.Album;
import com.example.photomanagementsystem.album.mapper.AlbumMapper;
import com.example.photomanagementsystem.album.service.AlbumService;
import com.example.photomanagementsystem.album.vo.AlbumVO;
import com.example.photomanagementsystem.common.BizException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 相册业务实现。
 */
@Service
public class AlbumServiceImpl implements AlbumService {

    private static final int ALBUM_NAME_MAX_LENGTH = 50;

    private final AlbumMapper albumMapper;

    public AlbumServiceImpl(AlbumMapper albumMapper) {
        this.albumMapper = albumMapper;
    }

    @Override
    public AlbumVO createAlbum(AlbumCreateDTO createDTO) {
        String albumName = createDTO == null ? null : createDTO.getName();
        validateAlbumName(albumName);

        String trimmedName = albumName.trim();
        albumMapper.selectByName(trimmedName)
                .ifPresent(album -> {
                    throw new BizException(400, "相册名称不能重复");
                });

        LocalDateTime now = LocalDateTime.now();
        Album album = new Album();
        album.setName(trimmedName);
        album.setDefaultAlbum(Boolean.FALSE);//把这个相册设置为非默认相册
        album.setCreateTime(now);
        album.setUpdateTime(now);

        return convertToVO(albumMapper.insert(album));
    }

    @Override
    public List<AlbumVO> listAlbums() {
        return albumMapper.selectList().stream()
                .map(this::convertToVO)
                .toList();
    }

    @Override
    public void deleteAlbum(Long id) {
        Album album = albumMapper.selectById(id)
                .orElseThrow(() -> new BizException(404, "相册不存在"));

        if (Boolean.TRUE.equals(album.getDefaultAlbum())) {
            throw new BizException(400, "默认相册不能删除");
        }

        if (countPhotosByAlbumId(id) > 0) {
            throw new BizException(400, "非空相册不能删除");
        }

        albumMapper.deleteById(id);
    }

    private void validateAlbumName(String name) {
        if (!StringUtils.hasText(name)) {
            throw new BizException(400, "相册名称不能为空");
        }
        if (name.trim().length() > ALBUM_NAME_MAX_LENGTH) {
            throw new BizException(400, "相册名称长度不能超过50");
        }
    }

    private int countPhotosByAlbumId(Long albumId) {
        // TODO 照片模块完成后替换为真实照片数量查询。
        return 0;
    }

    private AlbumVO convertToVO(Album album) {
        AlbumVO albumVO = new AlbumVO();
        albumVO.setId(album.getId());
        albumVO.setName(album.getName());
        albumVO.setDefaultAlbum(album.getDefaultAlbum());
        albumVO.setPhotoCount(countPhotosByAlbumId(album.getId()));
        albumVO.setCreateTime(album.getCreateTime());
        return albumVO;
    }
}
