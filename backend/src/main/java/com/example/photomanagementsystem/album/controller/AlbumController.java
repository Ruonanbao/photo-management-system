package com.example.photomanagementsystem.album.controller;

import com.example.photomanagementsystem.album.dto.AlbumCreateDTO;
import com.example.photomanagementsystem.album.dto.AlbumPhotoAddDTO;
import com.example.photomanagementsystem.album.dto.AlbumUpdateDTO;
import com.example.photomanagementsystem.album.service.AlbumService;
import com.example.photomanagementsystem.album.vo.AlbumVO;
import com.example.photomanagementsystem.album.vo.PhotoVO;
import com.example.photomanagementsystem.common.Result;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 相册接口。
 */
@RestController
@RequestMapping("/api/v1/albums")
public class AlbumController {

    private final AlbumService albumService;

    public AlbumController(AlbumService albumService) {
        this.albumService = albumService;
    }

    @GetMapping
    public Result<List<AlbumVO>> listAlbums() {
        return Result.success(albumService.listAlbums());
    }

    @PostMapping
    public Result<AlbumVO> createAlbum(@RequestBody AlbumCreateDTO createDTO) {
        return Result.success(albumService.createAlbum(createDTO));
    }

    @GetMapping("/{id}")
    public Result<AlbumVO> getAlbum(@PathVariable Long id) {
        return Result.success(albumService.getAlbum(id));
    }

    @PutMapping("/{id}")
    public Result<AlbumVO> updateAlbum(@PathVariable Long id, @RequestBody AlbumUpdateDTO updateDTO) {
        return Result.success(albumService.updateAlbum(id, updateDTO));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteAlbum(@PathVariable Long id) {
        albumService.deleteAlbum(id);
        return Result.success();
    }

    @GetMapping("/{id}/photos")
    public Result<List<PhotoVO>> listAlbumPhotos(@PathVariable Long id) {
        return Result.success(albumService.listAlbumPhotos(id));
    }

    @PostMapping("/{id}/photos")
    public Result<Void> addPhotoToAlbum(@PathVariable Long id, @RequestBody AlbumPhotoAddDTO addDTO) {
        albumService.addPhotoToAlbum(id, addDTO);
        return Result.success();
    }

    @DeleteMapping("/{id}/photos/{photoId}")
    public Result<Void> removePhotoFromAlbum(@PathVariable Long id, @PathVariable Long photoId) {
        albumService.removePhotoFromAlbum(id, photoId);
        return Result.success();
    }
}
