<template>
  <div class="page-stack">
    <div class="page-header">
      <div>
        <p class="eyebrow">Person</p>
        <h2>{{ displayName }}</h2>
      </div>
      <el-button :icon="Edit" @click="editVisible = true">重命名</el-button>
    </div>

    <el-skeleton v-if="loading" :rows="7" animated />
    <el-empty v-else-if="!photos.length" description="暂无人物照片" />
    <PhotoGrid v-else :photos="photos" @favorite="toggleFavorite" />

    <el-dialog v-model="editVisible" title="更新人物名称" width="380px">
      <el-input v-model="name" placeholder="人物名称" />
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleUpdate">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { Edit } from '@element-plus/icons-vue'

import { getPerson, listPersonPhotos, updatePerson, type Person } from '@/api/person'
import { updateFavorite, type Photo } from '@/api/photo'
import PhotoGrid from '@/components/PhotoGrid.vue'

const props = defineProps<{ id: number }>()
const person = ref<Person | null>(null)
const photos = ref<Photo[]>([])
const name = ref('')
const loading = ref(false)
const saving = ref(false)
const editVisible = ref(false)

const displayName = computed(() => person.value?.name || `人物 ${props.id}`)

async function loadPerson() {
  loading.value = true
  try {
    const [personResult, photoResult] = await Promise.all([getPerson(props.id), listPersonPhotos(props.id)])
    person.value = personResult
    name.value = personResult.name || ''
    photos.value = photoResult as Photo[]
  } finally {
    loading.value = false
  }
}

async function handleUpdate() {
  saving.value = true
  try {
    person.value = await updatePerson(props.id, name.value)
    editVisible.value = false
  } finally {
    saving.value = false
  }
}

async function toggleFavorite(photo: Photo) {
  const updated = await updateFavorite(photo.id, !photo.favorite)
  Object.assign(photo, updated)
}

onMounted(loadPerson)
</script>
