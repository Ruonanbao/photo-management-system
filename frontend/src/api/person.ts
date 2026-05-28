import request from '@/utils/request'
import type { Photo } from '@/api/photo'

export interface Person {
  id: number
  name?: string
  coverFaceId?: number
  photoCount?: number
  createTime?: string
  updateTime?: string
}

export function listPersons() {
  return request.get<unknown, Person[]>('/api/v1/persons')
}

export function getPerson(id: number) {
  return request.get<unknown, Person>(`/api/v1/persons/${id}`)
}

export function updatePerson(id: number, name: string) {
  return request.put<unknown, Person>(`/api/v1/persons/${id}`, { name })
}

export function deletePerson(id: number) {
  return request.delete<unknown, void>(`/api/v1/persons/${id}`)
}

export function listPersonPhotos(id: number) {
  return request.get<unknown, Photo[]>(`/api/v1/persons/${id}/photos`)
}
