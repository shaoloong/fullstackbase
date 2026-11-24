import http from '../../api/http'

/**
 * 用户域接口层。
 *
 * 字段名必须与后端 UserQuery 一一对应；未来接契约生成后，
 * 这里改为从 @fullstack-base/api-client 导入生成的函数与类型（见 docs/extend-guide.md）。
 *
 * @param {Object} params page / size / keyword / status
 * @returns {Promise<{items: Array, total: number, page: number, size: number}>}
 *          响应拦截器已拆掉 R 外壳，这里拿到的直接是 data
 */
export function fetchUsers(params) {
  return http.get('/users', { params })
}
