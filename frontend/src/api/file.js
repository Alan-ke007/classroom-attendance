import request from '@/utils/request'

/**
 * 上传文件
 */
export function uploadFile(formData) {
  return request({
    url: '/file/upload',
    method: 'post',
    headers: { 'Content-Type': 'multipart/form-data' },
    data: formData
  })
}

/**
 * 下载文件
 */
export function downloadFile(id) {
  return request({
    url: `/file/download/${id}`,
    method: 'get',
    responseType: 'blob'
  })
}

/**
 * 获取文件详情
 */
export function getFile(id) {
  return request({
    url: `/file/${id}`,
    method: 'get'
  })
}

/**
 * 分页查询文件列表
 */
export function getFileList(params) {
  return request({
    url: '/file/list',
    method: 'get',
    params
  })
}

/**
 * 删除文件
 */
export function deleteFile(id) {
  return request({
    url: `/file/${id}`,
    method: 'delete'
  })
}
