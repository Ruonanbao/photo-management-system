<template>
  <el-dialog v-model="visible" title="上传照片" width="560px" align-center>
    <div class="upload-options">
      <span>选择相册</span>
      <el-select v-model="selectedAlbumId" :loading="albumLoading" clearable placeholder="可不选，仅进入照片库">
        <el-option v-for="album in albums" :key="album.id" :label="album.name" :value="album.id" />
      </el-select>
    </div>

    <el-upload
      class="upload-box"
      drag
      multiple
      :auto-upload="false"
      :file-list="fileList"
      accept="image/jpeg,image/png,image/webp,image/heic,image/heif"
      @change="handleChange"
      @remove="handleRemove"
    >
      <el-icon class="upload-icon"><UploadFilled /></el-icon>
      <div class="el-upload__text">拖拽照片到这里，或点击选择</div>
      <template #tip>
        <div class="el-upload__tip">支持 JPG、PNG、WebP、HEIC，多文件上传。</div>
      </template>
    </el-upload>

    <template #footer>
      <el-button @click="appStore.closeUpload()">取消</el-button>
      <el-button type="primary" :loading="uploading" :disabled="!fileList.length" @click="submit">
        开始上传
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { ElMessage, type UploadFile, type UploadFiles } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'

import { listAlbums, type Album } from '@/api/album'
import { uploadPhotos } from '@/api/photo'
import { useAppStore } from '@/store/app'

const appStore = useAppStore()
const fileList = ref<UploadFile[]>([])
const albums = ref<Album[]>([])
const selectedAlbumId = ref<number | null>(null)
const albumLoading = ref(false)
const uploading = ref(false)

const visible = computed({
  get: () => appStore.uploadVisible,
  set: (value: boolean) => {
    if (!value) appStore.closeUpload()
  }
})

function handleChange(_: UploadFile, files: UploadFiles) {
  fileList.value = files
}

function handleRemove(_: UploadFile, files: UploadFiles) {
  fileList.value = files
}

async function loadAlbums() {
  albumLoading.value = true
  try {
    albums.value = await listAlbums()
  } finally {
    albumLoading.value = false
  }
}

async function submit() {
  const files = fileList.value.map((item) => item.raw).filter(Boolean) as File[]
  if (!files.length) return
  uploading.value = true
  try {
    await uploadPhotos(files, selectedAlbumId.value || undefined)
    appStore.markUploadCompleted(selectedAlbumId.value)
    ElMessage.success('上传完成')
    fileList.value = []
    selectedAlbumId.value = null
    appStore.closeUpload()
  } finally {
    uploading.value = false
  }
}

watch(visible, (value) => {
  if (value) loadAlbums()
})
</script>
