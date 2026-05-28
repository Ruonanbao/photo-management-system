<template>
  <div class="page-stack">
    <div class="page-header">
      <div>
        <p class="eyebrow">Explore</p>
        <h2>探索/地点</h2>
      </div>
    </div>

    <el-skeleton v-if="loading" :rows="6" animated />
    <el-empty v-else-if="!locations.length" description="暂无地点分组" />
    <div v-else class="explore-grid">
      <article v-for="location in locations" :key="location.locationName" class="explore-card">
        <h3>{{ location.locationName || '未知地点' }}</h3>
        <span>{{ location.photos.length }} 张照片</span>
        <PhotoGrid :photos="location.photos.slice(0, 6)" @favorite="toggleFavorite" />
      </article>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'

import { listLocations, updateFavorite, type Photo, type PhotoLocation } from '@/api/photo'
import PhotoGrid from '@/components/PhotoGrid.vue'

const locations = ref<PhotoLocation[]>([])
const loading = ref(false)

async function loadExplore() {
  loading.value = true
  try {
    locations.value = await listLocations()
  } finally {
    loading.value = false
  }
}

async function toggleFavorite(photo: Photo) {
  const updated = await updateFavorite(photo.id, !photo.favorite)
  Object.assign(photo, updated)
}

onMounted(loadExplore)
</script>
