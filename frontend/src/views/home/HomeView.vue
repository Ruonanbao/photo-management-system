<template>
  <div class="library-layout">
    <div class="page-stack">
      <div class="page-header">
        <div>
          <p class="eyebrow">Library</p>
          <h2>照片库</h2>
        </div>
      </div>

      <el-skeleton v-if="loading" :rows="8" animated />
      <el-empty v-else-if="!photos.length" description="暂无照片">
        <el-button type="primary" @click="appStore.openUpload()">上传照片</el-button>
      </el-empty>
      <section v-else v-for="group in dayGroups" :id="group.anchor" :key="group.date" class="timeline-group">
        <h3>{{ group.date }}</h3>
        <PhotoGrid :photos="group.photos" @favorite="toggleFavorite" />
      </section>
    </div>

    <aside v-if="yearGroups.length" class="year-rail">
      <a v-for="group in yearGroups" :key="group.year" :href="`#${group.anchor}`">{{ group.year }}</a>
    </aside>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'

import { listPhotos, updateFavorite, type Photo } from '@/api/photo'
import PhotoGrid from '@/components/PhotoGrid.vue'
import { groupByDay } from '@/utils/format'
import { useAppStore } from '@/store/app'

const appStore = useAppStore()
const photos = ref<Photo[]>([])
const loading = ref(false)

const dayGroups = computed(() =>
  Object.entries(groupByDay(photos.value)).map(([date, items], index) => ({
    date,
    photos: items,
    anchor: `day-${index}`
  }))
)

const yearGroups = computed(() => {
  const years = new Map<string, string>()
  dayGroups.value.forEach((group) => {
    const year = group.date.match(/\d{4}/)?.[0] || '未知'
    if (!years.has(year)) years.set(year, group.anchor)
  })
  return Array.from(years, ([year, anchor]) => ({ year, anchor }))
})

async function loadPhotos() {
  loading.value = true
  try {
    const page = await listPhotos({
      keyword: appStore.keyword || undefined,
      page: 1,
      size: 100
    })
    photos.value = page.records || []
  } finally {
    loading.value = false
  }
}

async function toggleFavorite(photo: Photo) {
  const updated = await updateFavorite(photo.id, !photo.favorite)
  Object.assign(photo, updated)
}

watch(() => appStore.keyword, loadPhotos)
watch(() => appStore.uploadRevision, loadPhotos)
onMounted(loadPhotos)
</script>
