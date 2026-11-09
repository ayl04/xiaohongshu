<template>
  <div class="max-w-4xl mx-auto px-6 py-12">
    <!-- 用户信息卡片 -->
    <div class="flex items-start gap-8">
      <img
        v-if="profile.avatar"
        :src="profile.avatar"
        class="w-24 h-24 rounded-full object-cover"
      />
      <img v-else src="@/assets/avatar.png" class="w-24 h-24 rounded-full object-cover" />

      <div class="flex-1 space-y-4">
        <div class="flex items-center gap-4">
          <h1 class="text-2xl font-bold text-gray-900">{{ profile.nickName || profile.nickname || '用户' }}</h1>
          <button
            v-if="isSelf"
            class="px-5 py-2 border border-gray-200 rounded-full text-sm font-medium text-gray-700 hover:bg-gray-100 cursor-pointer"
            @click="showEditModal = true"
          >
            编辑资料
          </button>
          <button
            v-else
            class="px-5 py-2 bg-[#ff2442] text-white rounded-full text-sm font-medium cursor-pointer hover:opacity-90"
            @click="handleFollow"
          >
            关注
          </button>
        </div>

        <div class="text-sm text-gray-500">小红书号：{{ profile.id || profile.xiaohashuId || '-' }}</div>
        <div v-if="profile.introduction" class="text-sm text-gray-700">{{ profile.introduction }}</div>
      </div>
    </div>

    <!-- 编辑资料模态框 -->
    <EditProfileModal v-model:visible="showEditModal" @update-success="loadProfile" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getUserProfile } from '@/api/user'
import { followUser } from '@/api/relation'
import EditProfileModal from '@/components/profile/EditProfileModal.vue'
import { message } from '@/utils/message'

const route = useRoute()
const userStore = useUserStore()

const userId = computed(() => {
  const id = route.params.userId
  return id === 'undefined' ? null : id
})

const profile = ref({})
const showEditModal = ref(false)

const isSelf = computed(() => {
  const ownId = userStore.profile && (userStore.profile.userId || userStore.profile.id)
  return ownId !== null && ownId !== undefined && String(ownId) === String(userId.value)
})

const loadProfile = async () => {
  if (!userId.value) return
  try {
    const res = await getUserProfile(userId.value)
    if (res.success) {
      profile.value = res.data || {}
    }
  } catch (e) {
    console.error(e)
  }
}

const handleFollow = async () => {
  if (!userId.value) return
  try {
    const res = await followUser(userId.value)
    if (res.success) {
      message.show('关注成功')
    } else {
      message.show(res.message || '关注失败')
    }
  } catch (e) {
    message.show('关注失败')
  }
}

onMounted(loadProfile)
</script>