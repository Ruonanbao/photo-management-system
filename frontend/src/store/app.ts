import { defineStore } from 'pinia'

export const useAppStore = defineStore('app', {
  state: () => ({
    uploadVisible: false,
    keyword: '',
    favoriteOnly: false,
    uploadRevision: 0,
    albumRevision: 0,
    lastUploadedAlbumId: null as number | null
  }),
  actions: {
    openUpload() {
      this.uploadVisible = true
    },
    closeUpload() {
      this.uploadVisible = false
    },
    setKeyword(keyword: string) {
      this.keyword = keyword
    },
    setFavoriteOnly(value: boolean) {
      this.favoriteOnly = value
    },
    markUploadCompleted(albumId?: number | null) {
      this.lastUploadedAlbumId = albumId || null
      this.uploadRevision += 1
      if (albumId) {
        this.albumRevision += 1
      }
    }
  }
})
