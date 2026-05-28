import request from '@/utils/request'

export interface Photo {
  id: number
  filename: string
  originalName: string
  fileSize?: number
  mimeType?: string
  width?: number
  height?: number
  shotAt?: string
  latitude?: number
  longitude?: number
  locationName?: string
  cameraMake?: string
  cameraModel?: string
  favorite?: boolean
  createTime?: string
  updateTime?: string
}

export interface PhotoPage<T> {
  records: T[]
  total: number
  page: number
  size: number
}

export interface PhotoQuery {
  favorite?: boolean
  keyword?: string
  startTime?: string
  endTime?: string
  locationName?: string
  personId?: number
  albumId?: number
  page?: number
  size?: number
}

export interface PhotoTimeline {
  yearMonth: string
  photos: Photo[]
}

export interface PhotoLocation {
  locationName: string
  photos: Photo[]
}

export interface PhotoPeople {
  personId: number
  personName?: string
  coverFaceId?: number
  photos: Photo[]
}

export function listPhotos(params: PhotoQuery = {}) {
  return request.get<unknown, PhotoPage<Photo>>('/api/v1/photos', { params })
}

export function uploadPhoto(file: File, albumId?: number) {
  const formData = new FormData()
  formData.append('file', file)
  if (albumId) formData.append('albumId', String(albumId))
  return request.post<unknown, Photo>('/api/v1/photos/upload', formData)
}

export function uploadPhotos(files: File[], albumId?: number) {
  const formData = new FormData()
  files.forEach((file) => formData.append('files', file))
  if (albumId) formData.append('albumId', String(albumId))
  return request.post<unknown, Photo[]>('/api/v1/photos/upload', formData)
}

export function getPhoto(id: number) {
  return request.get<unknown, Photo>(`/api/v1/photos/${id}`)
}

export function deletePhoto(id: number) {
  return request.delete<unknown, void>(`/api/v1/photos/${id}`)
}

export function updateFavorite(id: number, favorite: boolean) {
  return request.put<unknown, Photo>(`/api/v1/photos/${id}/favorite`, { favorite })
}

export function listTimeline() {
  return request.get<unknown, PhotoTimeline[]>('/api/v1/photos/timeline')
}

export function listLocations() {
  return request.get<unknown, PhotoLocation[]>('/api/v1/photos/locations')
}

export function listPeople() {
  return request.get<unknown, PhotoPeople[]>('/api/v1/photos/people')
}
