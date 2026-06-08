<template>
  <div class="photo-grid">
    <article v-for="photo in photos" :key="photo.id" class="photo-tile" @click="$router.push(`/photos/${photo.id}`)">
      <AuthenticatedImage :photo-id="photo.id" :mime-type="photo.mimeType" :alt="photo.originalName || photo.filename" />
      <button class="favorite-float" type="button" @click.stop="emit('favorite', photo)">
        <el-icon><StarFilled v-if="photo.favorite" /><Star v-else /></el-icon>
      </button>
      <button v-if="removable" class="remove-float" type="button" @click.stop="emit('remove', photo)">
        <el-icon><Close /></el-icon>
      </button>
      <div class="photo-meta">
        <span>{{ photo.originalName || photo.filename }}</span>
        <el-icon v-if="photo.favorite"><StarFilled /></el-icon>
      </div>
    </article>
  </div>
</template>

<script setup lang="ts">
import { Close, Star, StarFilled } from '@element-plus/icons-vue'

import type { Photo } from '@/api/photo'
import AuthenticatedImage from '@/components/AuthenticatedImage.vue'

defineProps<{ photos: Photo[]; removable?: boolean }>()
const emit = defineEmits<{ favorite: [photo: Photo]; remove: [photo: Photo] }>()
</script>
