<template>
  <div class="page-stack">
    <div class="page-header">
      <div>
        <p class="eyebrow">Favorites</p>
        <h2>收藏</h2>
      </div>
    </div>

    <el-skeleton v-if="loading" :rows="7" animated />
    <el-empty v-else-if="!photos.length" description="暂无收藏照片" />
    <PhotoGrid v-else :photos="photos" @favorite="toggleFavorite" />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'

import { listPhotos, updateFavorite, type Photo } from '@/api/photo'
import PhotoGrid from '@/components/PhotoGrid.vue'
import { useAppStore } from '@/store/app'

const appStore = useAppStore()
const photos = ref<Photo[]>([])
const loading = ref(false)

async function loadPhotos() {
  loading.value = true
  try {
    const page = await listPhotos({ favorite: true, keyword: appStore.keyword || undefined, page: 1, size: 100 })
    photos.value = page.records || []
  } finally {
    loading.value = false
  }
}

async function toggleFavorite(photo: Photo) {
  const updated = await updateFavorite(photo.id, !photo.favorite)
  if (!updated.favorite) {
    photos.value = photos.value.filter((item) => item.id !== photo.id)
  } else {
    Object.assign(photo, updated)
  }
}

watch(() => appStore.keyword, loadPhotos)
watch(() => appStore.uploadRevision, loadPhotos)
onMounted(loadPhotos)
</script>
