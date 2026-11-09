<template>
  <aside class="fixed top-17 bottom-0 bg-white">
    <div class="w-[320px]">
      <nav class="py-5">
        <ul class="space-y-2">
          <li class="px-5">
            <router-link
                to="/discover"
                class="font-bold flex items-center px-5 py-3 rounded-full left-nav"
                active-class="left-nav-active"
            >
              <svg t="1739537093272" class="icon w-[20px] h-[20px] mr-3" viewBox="0 0 1024 1024" version="1.1"
                   xmlns="http://www.w3.org/2000/svg" p-id="31408" width="200" height="200">
                <path
                    d="M883.84 310.4L579.84 92.16a117.12 117.12 0 0 0-136.32 0L140.8 310.4a159.36 159.36 0 0 0-64 128v352a158.72 158.72 0 0 0 158.08 158.08h199.68v-210.56a80 80 0 1 1 160 0v209.92h199.68a158.72 158.72 0 0 0 158.08-158.08v-352a159.36 159.36 0 0 0-68.48-127.36zM511.36 570.24a168.96 168.96 0 0 0-168.96 168.96v121.6H231.04a69.76 69.76 0 0 1-69.76-69.76v-352a70.4 70.4 0 0 1 29.44-56.96l304-218.24a28.16 28.16 0 0 1 32.64 0l304 218.24a70.4 70.4 0 0 1 29.44 56.96v352a69.76 69.76 0 0 1-69.76 69.76h-111.36v-121.6A168.96 168.96 0 0 0 512 570.24z"
                    fill="#515151" p-id="31409"></path>
              </svg>
              发现
            </router-link>
          </li>
          <li class="px-5">
            <button
                class="font-bold flex items-center px-5 py-3 rounded-full left-nav w-full text-left"
                @click="handlePublishNote"
            >
              <svg t="1739586087440" class="w-[18px] h-[18px] mr-3" viewBox="0 0 1024 1024" version="1.1"
                   xmlns="http://www.w3.org/2000/svg" p-id="1462" width="200" height="200">
                <path
                    d="M1020.787451 304.38902C1012.35451 116.053333 907.946667 11.64549 719.209412 2.81098 665.800784 0 648.131765 0 512 0S358.199216 0 304.38902 2.81098C116.053333 11.64549 11.64549 116.053333 2.81098 304.38902 0 358.600784 0 376.269804 0 512s0 153.399216 2.81098 207.209412c8.83451 188.737255 112.840784 293.145098 301.57804 301.578039 54.211765 2.409412 71.880784 3.212549 207.61098 3.212549s153.399216 0 207.209412-3.212549c188.737255-8.432941 293.145098-112.840784 301.578039-301.578039 2.409412-53.810196 3.212549-71.479216 3.212549-207.209412s-0.803137-153.399216-3.212549-207.61098z m-110.431373 410.001568c-6.023529 128.903529-67.061961 190.343529-195.96549 195.96549-52.60549 2.409412-68.266667 3.212549-200.784313 3.212549s-150.186667 0-200.784314-3.212549C179.902745 904.734118 120.470588 843.294118 113.242353 714.390588c-2.409412-53.007059-2.81098-68.668235-2.81098-200.784313s0-148.981961 2.81098-200.784314C120.470588 179.902745 179.902745 120.470588 309.609412 113.242353c52.60549-2.409412 68.668235-2.81098 200.784313-2.81098s149.383529 0 200.784314 2.81098C843.294118 120.470588 904.734118 179.902745 910.356078 309.609412c2.409412 51.400784 2.81098 67.061961 2.810981 200.784313s-0.401569 151.391373-2.810981 203.996863z"
                    p-id="1463" fill="#515151"></path>
                <path
                    d="M686.280784 456.985098h-131.714509V325.270588a42.566275 42.566275 0 0 0-85.13255 0v131.71451H337.719216a42.566275 42.566275 0 0 0 0 85.132549h131.714509v131.71451a42.566275 42.566275 0 0 0 85.13255 0V542.117647h131.714509a42.566275 42.566275 0 0 0 0-85.132549z"
                    p-id="1464" fill="#515151"></path>
              </svg>
              发布
            </button>
          </li>
          <li v-if="!isLoggedIn" class="px-5 space-y-3">
            <button
                v-if="!isLoggedIn"
                class="w-full bg-[#ff2442] text-white font-bold rounded-full py-3 mt-4 cursor-pointer"
                @click="handleShowLogin"
            >
              登录
            </button>

            <div class="rounded-xl px-5 py-4 border border-gray-200">
              <h3 class="text-sm mb-2">马上登录即可</h3>
              <div class="space-y-2 text-[#999] text-sm">
                <div class="flex items-center">
                  <svg class="w-4 h-4 mr-2" viewBox="0 0 24 24" fill="none">
                    <path d="M5 13l4 4L19 7" stroke="currentColor" stroke-width="2"/>
                  </svg>
                  刷到更懂你的优质内容
                </div>
                <div class="flex items-center">
                  <svg class="w-4 h-4 mr-2" viewBox="0 0 24 24" fill="none">
                    <path
                        d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"
                        stroke="currentColor" stroke-width="2"/>
                  </svg>
                  搜索最新种草、拔草信息
                </div>
                <div class="flex items-center">
                  <svg class="w-4 h-4 mr-2" viewBox="0 0 24 24" fill="none">
                    <path
                        d="M12 20l-1.45-1.32C5.4 13.93 2 10.88 2 7.5 2 4.42 4.42 2 7.5 2c1.74 0 3.41.81 4.5 2.09C13.09 2.81 14.76 2 16.5 2 19.58 2 22 4.42 22 7.5c0 3.38-3.4 6.43-8.55 11.18L12 20z"
                        stroke="currentColor" stroke-width="2"/>
                  </svg>
                  查看收藏、点赞的笔记
                </div>
                <div class="flex items-center">
                  <svg class="w-4 h-4 mr-2" viewBox="0 0 24 24" fill="none">
                    <path
                        d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8zM23 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75"
                        stroke="currentColor" stroke-width="2"/>
                  </svg>
                  与他人更好地互动、交流
                </div>
              </div>
            </div>
          </li>
          <li v-if="isLoggedIn" class="px-5">
            <router-link
                :to="`/user/profile/${userStore.profile.userId}`"
                class="font-bold flex items-center px-5 py-3 rounded-full left-nav"
                active-class="left-nav-active"
            >
              <img v-if="userStore.profile.avatar"
                   :src="userStore.profile.avatar"
                   class="w-[22px] h-[22px] rounded-full mr-3"
              />
              <img v-else src="@/assets/avatar.png" class="w-[22px] h-[22px] rounded-full mr-3"/>
              我
            </router-link>
          </li>
        </ul>
      </nav>
      <div class="absolute bottom-8 left-0 w-full">
        <div class="px-5 pl-10 relative">
          <button
              class="font-bold flex items-center px-5 py-3 rounded-full hover:bg-gray-100 w-full"
              @click="toggleMoreMenu"
          >
            <svg class="w-[24px] h-[24px] mr-3" viewBox="0 0 24 24" fill="none">
              <path d="M4 6h16M4 12h16M4 18h16" stroke="currentColor" stroke-width="2"/>
            </svg>
            更多
          </button>

          <!-- 更多菜单 -->
          <MoreMenu v-model:visible="showMoreMenu"/>
        </div>
      </div>
    </div>
  </aside>

  <!-- 添加发布模态框 -->
  <PublishModal v-model:visible="showPublishModal"/>
</template>

<script setup>
import {ref, inject, computed} from 'vue'
import MoreMenu from './MoreMenu.vue'
import PublishModal from '@/components/note/PublishModal.vue'
import {useUserStore} from '@/stores/user'

const userStore = useUserStore()

// 使用 store 中的计算属性判断登录状态
const isLoggedIn = computed(() => !!userStore.token)

const showLoginModal = inject('showLoginModal')
const showMoreMenu = ref(false)
const showPublishModal = ref(false)

const toggleMoreMenu = () => {
  showMoreMenu.value = !showMoreMenu.value
}

const handleShowLogin = () => {
  showLoginModal.value = true
}

const handlePublishNote = () => {
  if (!isLoggedIn.value) {
    showLoginModal.value = true
    return
  }
  showPublishModal.value = true
}
</script>

<style scoped>
.left-nav:hover {
  background-color: var(--color-active-background);
}

.left-nav-active {
  background-color: var(--color-active-background);
}
</style>