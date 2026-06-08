package com.example.photomanagementsystem.photo.service;

import com.example.photomanagementsystem.photo.dto.PhotoFavoriteDTO;
import com.example.photomanagementsystem.photo.dto.PhotoListQueryDTO;
import com.example.photomanagementsystem.photo.dto.PhotoUploadDTO;
import com.example.photomanagementsystem.photo.vo.PhotoDownloadVO;
import com.example.photomanagementsystem.photo.vo.PhotoLocationVO;
import com.example.photomanagementsystem.photo.vo.PhotoPageVO;
import com.example.photomanagementsystem.photo.vo.PhotoPeopleVO;
import com.example.photomanagementsystem.photo.vo.PhotoTimelineVO;
import com.example.photomanagementsystem.photo.vo.PhotoVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Photo service.
 */
public interface PhotoService {

    PhotoPageVO<PhotoVO> listPhotos(PhotoListQueryDTO queryDTO);

    PhotoVO uploadPhoto(PhotoUploadDTO uploadDTO);

    List<PhotoVO> uploadPhotos(List<MultipartFile> files, Long albumId);

    PhotoVO getPhoto(Long id);

    void deletePhoto(Long id);

    PhotoVO updateFavorite(Long id, PhotoFavoriteDTO favoriteDTO);

    PhotoDownloadVO getDownloadFile(Long id);

    PhotoDownloadVO getPreviewFile(Long id);

    List<PhotoTimelineVO> listTimeline();

    List<PhotoLocationVO> listLocations();

    List<PhotoPeopleVO> listPeople();
}
