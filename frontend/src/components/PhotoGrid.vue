<template>
  <div class="photo-grid">
    <button v-for="photo in photos" :key="photo.id" class="photo-tile" @click="$router.push(`/photos/${photo.id}`)">
      <img v-if="photo.id" :src="photoDownloadUrl(photo.id)" :alt="photo.originalName || photo.filename" />
      <div class="photo-meta">
        <span>{{ photo.originalName || photo.filename }}</span>
        <el-icon v-if="photo.favorite"><StarFilled /></el-icon>
      </div>
    </button>
  </div>
</template>

<script setup lang="ts">
import { StarFilled } from '@element-plus/icons-vue'

import type { Photo } from '@/api/photo'
import { photoDownloadUrl } from '@/utils/format'

defineProps<{ photos: Photo[] }>()
</script>
