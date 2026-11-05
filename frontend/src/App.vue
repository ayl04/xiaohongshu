<template>
  <router-view></router-view>
  
  <!-- 登录模态框 -->
  <Teleport to="body">
    <LoginModal v-model:visible="showLoginModal" />
  </Teleport>
</template>

<script setup>
import { ref, provide } from 'vue'
import LoginModal from '@/components/auth/LoginModal.vue'

const showLoginModal = ref(false)
provide('showLoginModal', showLoginModal)
</script>

<style scoped>
</style>
