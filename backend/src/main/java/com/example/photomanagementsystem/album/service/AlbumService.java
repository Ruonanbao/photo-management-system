package com.example.photomanagementsystem.album.service;

import com.example.photomanagementsystem.album.dto.AlbumCreateDTO;
import com.example.photomanagementsystem.album.dto.AlbumPhotoAddDTO;
import com.example.photomanagementsystem.album.dto.AlbumUpdateDTO;
import com.example.photomanagementsystem.album.vo.AlbumVO;
import com.example.photomanagementsystem.album.vo.PhotoVO;

import java.util.List;

/**
 * 相册业务接口。
 */
public interface AlbumService {

    List<AlbumVO> listAlbums();

    AlbumVO createAlbum(AlbumCreateDTO createDTO);

    AlbumVO getAlbum(Long id);

    AlbumVO updateAlbum(Long id, AlbumUpdateDTO updateDTO);

    void deleteAlbum(Long id);

    List<PhotoVO> listAlbumPhotos(Long id);

    void addPhotoToAlbum(Long id, AlbumPhotoAddDTO addDTO);

    void removePhotoFromAlbum(Long id, Long photoId);
}
