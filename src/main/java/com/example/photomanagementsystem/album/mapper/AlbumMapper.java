package com.example.photomanagementsystem.album.mapper;

import com.example.photomanagementsystem.album.entity.Album;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 相册数据访问。
 */
@Repository
public class AlbumMapper {

    private static final long DEFAULT_ALBUM_ID = 1L;

    private final AtomicLong idGenerator = new AtomicLong(DEFAULT_ALBUM_ID);

    private final Map<Long, Album> albumMap = new ConcurrentHashMap<>();

    public AlbumMapper() {
        Album defaultAlbum = new Album();
        LocalDateTime now = LocalDateTime.now();
        defaultAlbum.setId(DEFAULT_ALBUM_ID);
        defaultAlbum.setName("默认相册");
        defaultAlbum.setDefaultAlbum(Boolean.TRUE);
        defaultAlbum.setCreateTime(now);
        defaultAlbum.setUpdateTime(now);
        albumMap.put(DEFAULT_ALBUM_ID, defaultAlbum);
    }

    public Album insert(Album album) {
        Long id = idGenerator.incrementAndGet();
        album.setId(id);
        albumMap.put(id, album);
        return album;
    }

    public List<Album> selectList() {
        return new ArrayList<>(albumMap.values()).stream()
                .sorted(Comparator.comparing(Album::getCreateTime))
                .toList();
    }

    public Optional<Album> selectById(Long id) {
        return Optional.ofNullable(albumMap.get(id));
    }

    public Optional<Album> selectByName(String name) {
        return albumMap.values().stream()
                .filter(album -> Objects.equals(album.getName(), name))
                .findFirst();
    }

    public void deleteById(Long id) {
        albumMap.remove(id);
    }
}
