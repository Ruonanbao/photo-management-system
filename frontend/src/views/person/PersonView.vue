<template>
  <div class="page-stack">
    <div class="page-header">
      <div>
        <p class="eyebrow">People</p>
        <h2>人物</h2>
      </div>
    </div>

    <el-skeleton v-if="loading" :rows="6" animated />
    <el-empty v-else-if="!persons.length" description="暂无人物分组" />
    <div v-else class="person-grid">
      <article v-for="person in persons" :key="person.id" class="person-card" @click="$router.push(`/persons/${person.id}`)">
        <el-avatar :size="84">{{ displayName(person).slice(0, 1) }}</el-avatar>
        <h3>{{ displayName(person) }}</h3>
        <span>{{ person.photoCount || 0 }} 张照片</span>
      </article>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'

import { listPersons, type Person } from '@/api/person'

const persons = ref<Person[]>([])
const loading = ref(false)

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

onMounted(loadPersons)
</script>
