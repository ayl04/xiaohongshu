import axios from '@/axios'

// 接口前缀
// 注意：后端 oss 服务存在 /file/upload 接口，但网关当前未配置 file 路由，
// 此接口保留但前端目前可能无法走通。
const API_PREFIX = '/oss/file'

/**
 * 上传文件
 */
export function uploadFile(formData) {
  return axios.post(`${API_PREFIX}/upload`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}


