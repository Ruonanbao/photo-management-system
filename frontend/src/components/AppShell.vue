<template>
  <div class="app-shell">
    <aside class="sidebar">
      <div class="brand">
        <div class="brand-mark">P</div>
        <span>Photo</span>
      </div>
      <nav class="nav-list">
        <RouterLink to="/home" class="nav-item">
          <Picture />
          <span>照片</span>
        </RouterLink>
        <RouterLink to="/albums" class="nav-item">
          <Collection />
          <span>相册</span>
        </RouterLink>
        <RouterLink to="/persons" class="nav-item">
          <User />
          <span>人物</span>
        </RouterLink>
      </nav>
      <div class="sidebar-footer">
        <el-avatar :size="34">{{ userInitial }}</el-avatar>
        <div class="account">
          <strong>{{ userStore.profile?.nickname || userStore.profile?.username || '用户' }}</strong>
          <span>{{ userStore.profile?.email || '本地照片库' }}</span>
        </div>
      </div>
    </aside>

    <main class="workspace">
      <header class="topbar">
        <el-input
          v-model="keyword"
          class="search"
          placeholder="搜索照片、地点或文件名"
          clearable
          @input="appStore.setKeyword(keyword)"
        >
          <template #prefix>
            <Search />
          </template>
        </el-input>
        <el-button :icon="Upload" type="primary" @click="appStore.openUpload()">上传</el-button>
        <el-button :icon="SwitchButton" circle @click="handleLogout" />
      </header>
      <section class="content">
        <RouterView />
      </section>
    </main>

    <UploadDialog />
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Collection, Picture, Search, SwitchButton, Upload, User } from '@element-plus/icons-vue'

import UploadDialog from '@/components/UploadDialog.vue'
import { useAppStore } from '@/store/app'
import { useUserStore } from '@/store/user'

const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()
const keyword = ref(appStore.keyword)

const userInitial = computed(() => {
  const name = userStore.profile?.nickname || userStore.profile?.username || 'P'
  return name.slice(0, 1).toUpperCase()
})

async function handleLogout() {
  await userStore.logout()
  router.push('/auth')
}
</script>
