<template>
  <img v-if="objectUrl" :src="objectUrl" :alt="alt" :class="className" />
  <div v-else :class="['auth-image-placeholder', className]"></div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, ref, watch } from 'vue'

import { downloadPhotoBlob } from '@/api/photo'

const props = defineProps<{
  photoId?: number | string | null
  alt?: string
  className?: string
}>()

const objectUrl = ref('')
let activeUrl = ''
let requestVersion = 0

function revokeActiveUrl() {
  if (activeUrl) {
    URL.revokeObjectURL(activeUrl)
    activeUrl = ''
  }
}

watch(
  () => props.photoId,
  async (photoId) => {
    requestVersion += 1
    const version = requestVersion
    objectUrl.value = ''
    revokeActiveUrl()

    if (!photoId) return
    const blob = await downloadPhotoBlob(Number(photoId))
    if (version !== requestVersion) return

    activeUrl = URL.createObjectURL(blob)
    objectUrl.value = activeUrl
  },
  { immediate: true }
)

onBeforeUnmount(revokeActiveUrl)
</script>
