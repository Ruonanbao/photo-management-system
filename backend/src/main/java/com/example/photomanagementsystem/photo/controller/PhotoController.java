package com.example.photomanagementsystem.photo.controller;

import com.example.photomanagementsystem.common.Result;
import com.example.photomanagementsystem.photo.dto.PhotoFavoriteDTO;
import com.example.photomanagementsystem.photo.dto.PhotoListQueryDTO;
import com.example.photomanagementsystem.photo.dto.PhotoUploadDTO;
import com.example.photomanagementsystem.photo.service.PhotoService;
import com.example.photomanagementsystem.photo.vo.PhotoDownloadVO;
import com.example.photomanagementsystem.photo.vo.PhotoLocationVO;
import com.example.photomanagementsystem.photo.vo.PhotoPageVO;
import com.example.photomanagementsystem.photo.vo.PhotoPeopleVO;
import com.example.photomanagementsystem.photo.vo.PhotoTimelineVO;
import com.example.photomanagementsystem.photo.vo.PhotoVO;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Photo API.
 */
@RestController
@RequestMapping("/api/v1/photos")
public class PhotoController {

    private final PhotoService photoService;

    public PhotoController(PhotoService photoService) {
        this.photoService = photoService;
    }

    @GetMapping
    public Result<PhotoPageVO<PhotoVO>> listPhotos(@RequestParam(required = false) Boolean favorite,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        PhotoListQueryDTO queryDTO = new PhotoListQueryDTO();
        queryDTO.setFavorite(favorite);
        queryDTO.setKeyword(keyword);
        queryDTO.setPage(page);
        queryDTO.setSize(size);
        return Result.success(photoService.listPhotos(queryDTO));
    }

    @PostMapping("/upload")
    public Result<PhotoVO> uploadPhoto(@RequestParam("file") MultipartFile file,
            @RequestParam(required = false) Long albumId) {
        PhotoUploadDTO uploadDTO = new PhotoUploadDTO();
        uploadDTO.setFile(file);
        uploadDTO.setAlbumId(albumId);
        return Result.success(photoService.uploadPhoto(uploadDTO));
    }

    @GetMapping("/{id}")
    public Result<PhotoVO> getPhoto(@PathVariable Long id) {
        return Result.success(photoService.getPhoto(id));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deletePhoto(@PathVariable Long id) {
        photoService.deletePhoto(id);
        return Result.success();
    }

    @PutMapping("/{id}/favorite")
    public Result<PhotoVO> updateFavorite(@PathVariable Long id, @RequestBody PhotoFavoriteDTO favoriteDTO) {
        return Result.success(photoService.updateFavorite(id, favoriteDTO));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadPhoto(@PathVariable Long id) {
        PhotoDownloadVO downloadVO = photoService.getDownloadFile(id);
        Resource resource = new FileSystemResource(downloadVO.getPath());
        String encodedName = UriUtils.encode(downloadVO.getFilename(), StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(downloadVO.getMimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedName)
                .body(resource);
    }

    @GetMapping("/timeline")
    public Result<List<PhotoTimelineVO>> listTimeline() {
        return Result.success(photoService.listTimeline());
    }

    @GetMapping("/locations")
    public Result<List<PhotoLocationVO>> listLocations() {
        return Result.success(photoService.listLocations());
    }

    @GetMapping("/people")
    public Result<List<PhotoPeopleVO>> listPeople() {
        return Result.success(photoService.listPeople());
    }
}
