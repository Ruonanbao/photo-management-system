package com.example.photomanagementsystem.album.service;

import com.example.photomanagementsystem.album.dto.AlbumCreateDTO;
import com.example.photomanagementsystem.album.vo.AlbumVO;

import java.util.List;

/**
 * 相册业务接口。
 */
public interface AlbumService {

    AlbumVO createAlbum(AlbumCreateDTO createDTO);

    List<AlbumVO> listAlbums();

    void deleteAlbum(Long id);
}
