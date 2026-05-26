import request from '@/utils/request'

export interface Person {
  id: number
  name?: string
  coverFaceId?: number
  photoCount?: number
  createTime?: string
  updateTime?: string
}

export interface PersonPhoto {
  id: number
  photoId?: number
  filename?: string
  originalName?: string
  shotAt?: string
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
  return request.get<unknown, PersonPhoto[]>(`/api/v1/persons/${id}/photos`)
}
