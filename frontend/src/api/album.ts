import request from '@/utils/request'
import type { Photo } from '@/api/photo'

export interface Album {
  id: number
  name: string
  description?: string
  coverPhotoId?: number
  defaultAlbum?: boolean
  photoCount?: number
  createTime?: string
  updateTime?: string
}

export interface AlbumParams {
  name: string
  description?: string
}

export function listAlbums() {
  return request.get<unknown, Album[]>('/api/v1/albums')
}

export function createAlbum(data: AlbumParams) {
  return request.post<unknown, Album>('/api/v1/albums', data)
}

export function getAlbum(id: number) {
  return request.get<unknown, Album>(`/api/v1/albums/${id}`)
}

export function updateAlbum(id: number, data: AlbumParams) {
  return request.put<unknown, Album>(`/api/v1/albums/${id}`, data)
}

export function deleteAlbum(id: number) {
  return request.delete<unknown, void>(`/api/v1/albums/${id}`)
}

export function listAlbumPhotos(id: number) {
  return request.get<unknown, Photo[]>(`/api/v1/albums/${id}/photos`)
}

export function addPhotoToAlbum(id: number, photoId: number) {
  return request.post<unknown, void>(`/api/v1/albums/${id}/photos`, { photoId })
}

export function removePhotoFromAlbum(id: number, photoId: number) {
  return request.delete<unknown, void>(`/api/v1/albums/${id}/photos/${photoId}`)
}
