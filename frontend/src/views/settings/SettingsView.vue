<template>
  <div class="page-stack">
    <div class="page-header">
      <div>
        <p class="eyebrow">Settings</p>
        <h2>设置</h2>
      </div>
    </div>
    <section class="settings-panel">
      <el-avatar :size="56">{{ userInitial }}</el-avatar>
      <div>
        <h3>{{ userStore.profile?.nickname || userStore.profile?.username || '用户' }}</h3>
        <p>{{ userStore.profile?.email || '未设置邮箱' }}</p>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const userInitial = computed(() => (userStore.profile?.nickname || userStore.profile?.username || 'P').slice(0, 1).toUpperCase())
</script>
