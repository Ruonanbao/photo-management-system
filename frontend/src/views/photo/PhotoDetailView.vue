<template>
  <div class="detail-layout">
    <section class="photo-stage">
      <AuthenticatedImage
        v-if="photo"
        :photo-id="photo.id"
        :mime-type="photo.mimeType"
        :alt="photo.originalName || photo.filename"
      />
      <el-skeleton v-else :rows="8" animated />
    </section>
    <aside class="info-panel">
      <div class="panel-header">
        <el-button :icon="ArrowLeft" circle @click="router.back()" />
        <h2>{{ photo?.originalName || photo?.filename || '照片详情' }}</h2>
      </div>
      <div class="detail-actions">
        <el-button v-if="photo" :icon="photo.favorite ? StarFilled : Star" @click="toggleFavorite">
          {{ photo.favorite ? '取消收藏' : '收藏' }}
        </el-button>
        <el-button v-if="photo" :icon="Download" @click="triggerPhotoDownload(photo.id)">下载</el-button>
        <el-button v-if="photo" :icon="Delete" type="danger" plain @click="handleDelete">删除</el-button>
      </div>
      <el-descriptions v-if="photo" :column="1" border>
        <el-descriptions-item label="文件名">{{ photo.filename }}</el-descriptions-item>
        <el-descriptions-item label="大小">{{ formatFileSize(photo.fileSize) }}</el-descriptions-item>
        <el-descriptions-item label="尺寸">{{ photo.width || '-' }} x {{ photo.height || '-' }}</el-descriptions-item>
        <el-descriptions-item label="拍摄时间">{{ formatDateTime(photo.shotAt) }}</el-descriptions-item>
        <el-descriptions-item label="地点">{{ photo.locationName || '未记录' }}</el-descriptions-item>
        <el-descriptions-item label="GPS 坐标">{{ coordinateText }}</el-descriptions-item>
        <el-descriptions-item label="相机型号">{{ cameraName }}</el-descriptions-item>
        <el-descriptions-item label="收藏状态">{{ photo.favorite ? '已收藏' : '未收藏' }}</el-descriptions-item>
      </el-descriptions>
    </aside>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, Delete, Download, Star, StarFilled } from '@element-plus/icons-vue'

import { deletePhoto, getPhoto, updateFavorite, type Photo } from '@/api/photo'
import AuthenticatedImage from '@/components/AuthenticatedImage.vue'
import { formatDateTime, formatFileSize, triggerPhotoDownload } from '@/utils/format'

const props = defineProps<{ id: number }>()
const router = useRouter()
const photo = ref<Photo | null>(null)

const cameraName = computed(() => {
  if (!photo.value?.cameraMake && !photo.value?.cameraModel) return '未记录'
  return [photo.value.cameraMake, photo.value.cameraModel].filter(Boolean).join(' ')
})

const coordinateText = computed(() => {
  if (photo.value?.latitude == null || photo.value?.longitude == null) return '未记录'
  return `${photo.value.latitude}, ${photo.value.longitude}`
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
