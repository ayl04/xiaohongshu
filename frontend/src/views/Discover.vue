<template>
  <div class="min-h-screen flex flex-col items-center justify-center py-16">
    <div class="text-center space-y-8 max-w-md w-full px-6">
      <div class="space-y-2">
        <h1 class="text-4xl font-bold text-[#ff2442]">小鼠书</h1>
        <p class="text-gray-400 text-sm">登录后即可发布笔记、管理个人资料</p>
      </div>

      <div class="space-y-4">
        <button
          class="w-full bg-[#ff2442] text-white font-bold rounded-full py-3 cursor-pointer hover:opacity-90"
          @click="handlePublish"
        >
          发布笔记
        </button>
        <button
          class="w-full border border-gray-200 text-gray-700 font-bold rounded-full py-3 cursor-pointer hover:bg-gray-100"
          @click="handleProfile"
        >
          我的主页
        </button>
      </div>
    </div>

    <!-- 发布笔记模态框 -->
    <PublishModal v-model:visible="showPublishModal" />
  </div>
</template>

<script setup>
import { ref, computed, inject } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import PublishModal from '@/components/note/PublishModal.vue'

const router = useRouter()
const userStore = useUserStore()
const showLoginModal = inject('showLoginModal')

const showPublishModal = ref(false)
const isLoggedIn = computed(() => !!userStore.token)

const handlePublish = () => {
  if (!isLoggedIn.value) {
    showLoginModal.value = true
    return
  }
  showPublishModal.value = true
}

const handleProfile = () => {
  if (!isLoggedIn.value) {
    showLoginModal.value = true
    return
  }
  router.push(`/user/profile/${userStore.profile.userId}`)
}
</script>