<template>
  <div class="page-stack">
    <div class="page-header">
      <div>
        <p class="eyebrow">People</p>
        <h2>人物</h2>
      </div>
    </div>

    <el-skeleton v-if="loading" :rows="6" animated />
    <el-empty v-else-if="!persons.length" description="暂无人物识别结果" />
    <div v-else class="person-grid">
      <article v-for="person in persons" :key="person.id" class="person-card" @click="selectPerson(person)">
        <el-avatar :size="84">{{ displayName(person).slice(0, 1) }}</el-avatar>
        <h3>{{ displayName(person) }}</h3>
        <span>{{ person.photoCount || 0 }} 张照片</span>
      </article>
    </div>

    <el-drawer v-model="drawerVisible" :title="selected ? displayName(selected) : '人物照片'" size="420px">
      <el-skeleton v-if="photoLoading" :rows="5" animated />
      <div v-else class="person-photo-list">
        <article v-for="item in personPhotos" :key="item.id" class="person-photo-row">
          <div class="row-thumb">{{ (item.originalName || item.filename || 'P').slice(0, 1) }}</div>
          <div>
            <strong>{{ item.originalName || item.filename || '未命名照片' }}</strong>
            <span>{{ formatDateTime(item.shotAt) }}</span>
          </div>
        </article>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'

import { listPersonPhotos, listPersons, type Person, type PersonPhoto } from '@/api/person'
import { formatDateTime } from '@/utils/format'

const persons = ref<Person[]>([])
const personPhotos = ref<PersonPhoto[]>([])
const selected = ref<Person | null>(null)
const loading = ref(false)
const photoLoading = ref(false)
const drawerVisible = ref(false)

function displayName(person: Person) {
  return person.name || `人物 ${person.id}`
}

async function loadPersons() {
  loading.value = true
  try {
    persons.value = await listPersons()
  } finally {
    loading.value = false
  }
}

async function selectPerson(person: Person) {
  selected.value = person
  drawerVisible.value = true
  photoLoading.value = true
  try {
    personPhotos.value = await listPersonPhotos(person.id)
  } finally {
    photoLoading.value = false
  }
}

onMounted(loadPersons)
</script>
