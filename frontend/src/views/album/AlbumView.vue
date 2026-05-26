<template>
  <div class="page-stack">
    <div class="page-header">
      <div>
        <p class="eyebrow">Albums</p>
        <h2>相册</h2>
      </div>
      <el-button type="primary" :icon="Plus" @click="createVisible = true">新建相册</el-button>
    </div>

    <el-skeleton v-if="loading" :rows="6" animated />
    <div v-else class="album-grid">
      <article v-for="album in albums" :key="album.id" class="album-card">
        <div class="album-cover">
          <Collection />
        </div>
        <h3>{{ album.name }}</h3>
        <p>{{ album.description || '无描述' }}</p>
        <span>{{ album.photoCount || 0 }} 张照片</span>
      </article>
    </div>

    <el-dialog v-model="createVisible" title="新建相册" width="420px">
      <el-form :model="form" label-position="top">
        <el-form-item label="名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleCreate">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { Collection, Plus } from '@element-plus/icons-vue'

import { createAlbum, listAlbums, type Album } from '@/api/album'
import { useAppStore } from '@/store/app'

const appStore = useAppStore()
const albums = ref<Album[]>([])
const loading = ref(false)
const saving = ref(false)
const createVisible = ref(false)
const form = reactive({
  name: '',
  description: ''
})

async function loadAlbums() {
  loading.value = true
  try {
    albums.value = await listAlbums()
  } finally {
    loading.value = false
  }
}

async function handleCreate() {
  saving.value = true
  try {
    await createAlbum(form)
    form.name = ''
    form.description = ''
    createVisible.value = false
    await loadAlbums()
  } finally {
    saving.value = false
  }
}

onMounted(loadAlbums)
watch(() => appStore.albumRevision, loadAlbums)
</script>
