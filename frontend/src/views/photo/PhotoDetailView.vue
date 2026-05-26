<template>
  <div class="detail-layout">
    <section class="photo-stage">
      <el-image v-if="photo" :src="photoDownloadUrl(photo.id)" fit="contain" />
      <el-skeleton v-else :rows="8" animated />
    </section>
    <aside class="info-panel">
      <div class="panel-header">
        <h2>{{ photo?.originalName || photo?.filename || '照片详情' }}</h2>
        <el-button v-if="photo" :icon="photo.favorite ? StarFilled : Star" circle @click="toggleFavorite" />
      </div>
      <el-descriptions v-if="photo" :column="1" border>
        <el-descriptions-item label="文件名">{{ photo.filename }}</el-descriptions-item>
        <el-descriptions-item label="大小">{{ formatFileSize(photo.fileSize) }}</el-descriptions-item>
        <el-descriptions-item label="尺寸">{{ photo.width || '-' }} x {{ photo.height || '-' }}</el-descriptions-item>
        <el-descriptions-item label="拍摄时间">{{ formatDateTime(photo.shotAt) }}</el-descriptions-item>
        <el-descriptions-item label="地点">{{ photo.locationName || '未记录' }}</el-descriptions-item>
        <el-descriptions-item label="相机">{{ cameraName }}</el-descriptions-item>
      </el-descriptions>
      <el-button class="full-button danger-button" :icon="Delete" @click="handleDelete">删除照片</el-button>
    </aside>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Delete, Star, StarFilled } from '@element-plus/icons-vue'

import { deletePhoto, getPhoto, updateFavorite, type Photo } from '@/api/photo'
import { formatDateTime, formatFileSize, photoDownloadUrl } from '@/utils/format'

const props = defineProps<{ id: number }>()
const router = useRouter()
const photo = ref<Photo | null>(null)

const cameraName = computed(() => {
  if (!photo.value?.cameraMake && !photo.value?.cameraModel) return '未记录'
  return [photo.value.cameraMake, photo.value.cameraModel].filter(Boolean).join(' ')
})

async function loadPhoto() {
  photo.value = await getPhoto(props.id)
}

async function toggleFavorite() {
  if (!photo.value) return
  photo.value = await updateFavorite(photo.value.id, !photo.value.favorite)
}

async function handleDelete() {
  if (!photo.value) return
  await deletePhoto(photo.value.id)
  router.push('/home')
}

onMounted(loadPhoto)
</script>
