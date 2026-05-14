package com.example.photomanagementsystem.album.controller;

import com.example.photomanagementsystem.album.dto.AlbumCreateDTO;
import com.example.photomanagementsystem.album.service.AlbumService;
import com.example.photomanagementsystem.album.vo.AlbumVO;
import com.example.photomanagementsystem.common.Result;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 相册接口。
 */
@RestController
@RequestMapping("/albums")
public class AlbumController {

    private final AlbumService albumService;

    public AlbumController(AlbumService albumService) {
        this.albumService = albumService;
    }

    @PostMapping
    public Result<AlbumVO> createAlbum(@RequestBody AlbumCreateDTO createDTO) {
        return Result.success(albumService.createAlbum(createDTO));
    }

    @GetMapping
    public Result<List<AlbumVO>> listAlbums() {
        return Result.success(albumService.listAlbums());
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteAlbum(@PathVariable Long id) {
        albumService.deleteAlbum(id);
        return Result.success();
    }
}
