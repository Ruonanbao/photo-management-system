<template>
  <main class="auth-page">
    <section class="auth-visual">
      <div>
        <p class="eyebrow">Photo Management</p>
        <h1>整理每一张值得留下的照片</h1>
        <p>本地部署的个人照片管理入口，支持照片库、相册、人物与地点聚合。</p>
      </div>
    </section>
    <section class="auth-panel">
      <el-tabs v-model="mode" stretch>
        <el-tab-pane label="登录" name="login">
          <el-form :model="loginForm" label-position="top" @submit.prevent>
            <el-form-item label="用户名">
              <el-input v-model="loginForm.username" autocomplete="username" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="loginForm.password" type="password" autocomplete="current-password" show-password />
            </el-form-item>
            <el-button class="full-button" type="primary" :loading="loading" @click="handleLogin">登录</el-button>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="注册" name="register">
          <el-form :model="registerForm" label-position="top" @submit.prevent>
            <el-form-item label="用户名">
              <el-input v-model="registerForm.username" autocomplete="username" />
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="registerForm.nickname" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="registerForm.email" autocomplete="email" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="registerForm.password" type="password" autocomplete="new-password" show-password />
            </el-form-item>
            <el-button class="full-button" type="primary" :loading="loading" @click="handleRegister">注册</el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </section>
  </main>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()
const mode = ref('login')
const loading = ref(false)

const loginForm = reactive({ username: '', password: '' })
const registerForm = reactive({ username: '', password: '', nickname: '', email: '' })

async function handleLogin() {
  loading.value = true
  try {
    await userStore.login(loginForm)
    router.push('/home')
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  loading.value = true
  try {
    await userStore.register(registerForm)
    ElMessage.success('注册成功，请登录')
    mode.value = 'login'
    loginForm.username = registerForm.username
  } finally {
    loading.value = false
  }
}
</script>
