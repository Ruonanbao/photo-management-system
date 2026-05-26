<template>
  <div class="page-stack">
    <div class="page-header">
      <div>
        <p class="eyebrow">Library</p>
        <h2>照片流</h2>
      </div>
      <el-switch v-model="favoriteOnly" active-text="仅收藏" @change="loadPhotos" />
    </div>

    <el-skeleton v-if="loading" :rows="8" animated />
    <el-empty v-else-if="!photos.length" description="暂无照片">
      <el-button type="primary" @click="appStore.openUpload()">上传照片</el-button>
    </el-empty>
    <PhotoGrid v-else :photos="photos" />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'

import { listPhotos, type Photo } from '@/api/photo'
import PhotoGrid from '@/components/PhotoGrid.vue'
import { useAppStore } from '@/store/app'

const appStore = useAppStore()
const photos = ref<Photo[]>([])
const loading = ref(false)
const favoriteOnly = ref(appStore.favoriteOnly)

async function loadPhotos() {
  loading.value = true
  try {
    appStore.setFavoriteOnly(favoriteOnly.value)
    const page = await listPhotos({
      favorite: favoriteOnly.value || undefined,
      keyword: appStore.keyword || undefined,
      page: 1,
      size: 60
    })
    photos.value = page.records || []
  } finally {
    loading.value = false
  }
}

watch(() => appStore.keyword, loadPhotos)
watch(() => appStore.uploadRevision, loadPhotos)
onMounted(loadPhotos)
</script>
