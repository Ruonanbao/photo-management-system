<template>
  <div class="page-stack">
    <div class="page-header">
      <div>
        <p class="eyebrow">Album</p>
        <h2>{{ album?.name || '相册详情' }}</h2>
        <p class="subtle">{{ album?.description || '无描述' }}</p>
      </div>
      <div class="action-row">
        <el-button :icon="Plus" @click="addVisible = true">添加照片</el-button>
        <el-button :icon="Edit" @click="openEdit">编辑</el-button>
        <el-button :icon="Delete" type="danger" plain @click="handleDelete">删除</el-button>
      </div>
    </div>

    <el-skeleton v-if="loading" :rows="7" animated />
    <el-empty v-else-if="!photos.length" description="相册中暂无照片" />
    <PhotoGrid v-else :photos="photos" removable @favorite="toggleFavorite" @remove="handleRemovePhoto" />

    <el-dialog v-model="addVisible" title="添加照片到相册" width="520px">
      <el-select v-model="selectedPhotoIds" multiple filterable class="full-button" placeholder="选择照片">
        <el-option
          v-for="photo in libraryPhotos"
          :key="photo.id"
          :label="photo.originalName || photo.filename"
          :value="photo.id"
        />
      </el-select>
      <template #footer>
        <el-button @click="addVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleAddPhotos">添加</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="editVisible" title="编辑相册" width="420px">
      <el-form :model="form" label-position="top">
        <el-form-item label="名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleUpdate">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Delete, Edit, Plus } from '@element-plus/icons-vue'

import { addPhotoToAlbum, deleteAlbum, getAlbum, listAlbumPhotos, removePhotoFromAlbum, updateAlbum, type Album } from '@/api/album'
import { listPhotos, updateFavorite, type Photo } from '@/api/photo'
import PhotoGrid from '@/components/PhotoGrid.vue'

const props = defineProps<{ id: number }>()
const router = useRouter()
const album = ref<Album | null>(null)
const photos = ref<Photo[]>([])
const libraryPhotos = ref<Photo[]>([])
const selectedPhotoIds = ref<number[]>([])
const loading = ref(false)
const saving = ref(false)
const addVisible = ref(false)
const editVisible = ref(false)
const form = reactive({ name: '', description: '' })

async function loadAlbum() {
  loading.value = true
  try {
    const [albumResult, photoResult] = await Promise.all([getAlbum(props.id), listAlbumPhotos(props.id)])
    album.value = albumResult
    photos.value = photoResult
  } finally {
    loading.value = false
  }
}

async function loadLibraryPhotos() {
  const page = await listPhotos({ page: 1, size: 100 })
  libraryPhotos.value = page.records || []
}

function openEdit() {
  form.name = album.value?.name || ''
  form.description = album.value?.description || ''
  editVisible.value = true
}

async function handleUpdate() {
  saving.value = true
  try {
    album.value = await updateAlbum(props.id, form)
    editVisible.value = false
  } finally {
    saving.value = false
  }
}

async function handleAddPhotos() {
  saving.value = true
  try {
    await Promise.all(selectedPhotoIds.value.map((photoId) => addPhotoToAlbum(props.id, photoId).catch(() => undefined)))
    selectedPhotoIds.value = []
    addVisible.value = false
    await loadAlbum()
  } finally {
    saving.value = false
  }
}

async function handleDelete() {
  await deleteAlbum(props.id)
  router.push('/albums')
}

async function handleRemovePhoto(photo: Photo) {
  await removePhotoFromAlbum(props.id, photo.id)
  photos.value = photos.value.filter((item) => item.id !== photo.id)
}

async function toggleFavorite(photo: Photo) {
  const updated = await updateFavorite(photo.id, !photo.favorite)
  Object.assign(photo, updated)
}

watch(addVisible, (value) => {
  if (value) loadLibraryPhotos()
})
onMounted(loadAlbum)
</script>
