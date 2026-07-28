<script setup lang="ts">
import { onMounted } from 'vue'
import { useDictStore } from '@/stores/dict'

const dictStore = useDictStore()

onMounted(() => {
  dictStore.loadDict()
})
</script>

<template>
  <router-view />
</template>
