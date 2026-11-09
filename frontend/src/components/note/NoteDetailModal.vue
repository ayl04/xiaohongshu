<template>
  <Teleport to="body">
    <div v-if="visible" class="fixed inset-0 z-50 flex items-center justify-center">
      <div class="absolute inset-0 bg-black/50" @click="onClose"></div>
      <div class="relative bg-white rounded-2xl w-[760px] max-h-[90vh] overflow-y-auto">
        <div class="p-8">
          <div class="font-bold text-xl mb-4">{{ detail.title || '暂无标题' }}</div>

          <img v-for="(uri, i) in detail.imgUris" :key="i" :src="uri" class="w-full rounded-lg mb-4" />
          <video v-if="detail.videoUri" :src="detail.videoUri" controls class="w-full rounded-lg mb-4"></video>

          <div class="text-gray-700 whitespace-pre-wrap mb-6">{{ detail.content }}</div>

          <div class="flex items-center gap-3 pt-4 border-t border-gray-100">
            <img :src="detail.avatar" class="w-10 h-10 rounded-full object-cover" />
            <span class="font-medium">{{ detail.creatorName }}</span>
            <button
              class="ml-auto px-4 py-1.5 bg-[#ff2442] text-white rounded-full text-sm cursor-pointer hover:opacity-90"
              @click="handleFollow"
            >
              关注
            </button>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref, watch } from 'vue'
import { getNoteDetail } from '@/api/note'
import { followUser } from '@/api/relation'
import { message } from '@/utils/message'

const props = defineProps({
  visible: { type: Boolean, default: false },
  note: { type: Object, default: null }
})
const emit = defineEmits(['update:visible'])

const detail = ref({})

watch(() => props.visible, (v) => {
  if (v && props.note && props.note.id) {
    getNoteDetail(props.note.id).then(res => {
      if (res.success) {
        detail.value = res.data || {}
      }
    })
  }
})

const onClose = () => emit('update:visible', false)

const handleFollow = () => {
  if (!detail.value.creatorId) return
  followUser(detail.value.creatorId).then(res => {
    if (res.success) {
      message.show('关注成功')
    }
  })
}
</script>