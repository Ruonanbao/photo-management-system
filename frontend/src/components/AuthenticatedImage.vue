<template>
  <div v-if="isHeic" :class="['auth-image-placeholder', 'heic-placeholder', className]">
    <span>HEIC</span>
    <small>HEIC 暂不支持预览，可下载原图查看</small>
  </div>
  <img v-else-if="objectUrl" :src="objectUrl" :alt="alt" :class="className" />
  <div v-else :class="['auth-image-placeholder', className]"></div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue'

import { previewPhotoBlob } from '@/api/photo'

const props = defineProps<{
  photoId?: number | string | null
  mimeType?: string
  alt?: string
  className?: string
}>()

const objectUrl = ref('')
const isHeic = computed(() => /heic|heif/i.test(props.mimeType || ''))
let activeUrl = ''
let requestVersion = 0

function revokeActiveUrl() {
  if (activeUrl) {
    URL.revokeObjectURL(activeUrl)
    activeUrl = ''
  }
}

watch(
  () => [props.photoId, props.mimeType] as const,
  async ([photoId]) => {
    requestVersion += 1
    const version = requestVersion
    objectUrl.value = ''
    revokeActiveUrl()

    if (!photoId || isHeic.value) return
    try {
      const blob = await previewPhotoBlob(Number(photoId))
      if (version !== requestVersion) return

      activeUrl = URL.createObjectURL(blob)
      objectUrl.value = activeUrl
    } catch {
      if (version === requestVersion) objectUrl.value = ''
    }
  },
  { immediate: true }
)

onBeforeUnmount(revokeActiveUrl)
</script>
