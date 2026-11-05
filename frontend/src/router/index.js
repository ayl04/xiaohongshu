import {createRouter, createWebHistory} from 'vue-router'
import BasicLayout from '@/layouts/BasicLayout.vue'

const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/',
            component: BasicLayout,
            redirect: '/discover',
            children: [
                {
                    path: '/discover',
                    name: 'Discover',
                    component: () => import('@/views/Discover.vue')
                },
                {
                    path: '/user/profile/:userId',
                    name: 'Profile',
                    component: () => import('@/views/Profile.vue')
                }
            ]
        }
    ]
})

export default router
