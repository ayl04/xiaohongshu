import axios from "@/axios";

// 接口前缀
const API_PREFIX = '/relation/relation'

// 关注用户
export function followUser(followUserId) {
    return axios.post(`${API_PREFIX}/follow`, {followUserId})
}


