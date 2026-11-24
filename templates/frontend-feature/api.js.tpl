import http from '../../api/http'

/**
 * __title__域接口层。
 *
 * 生成后必改：路径与字段名对齐后端 __Domain__Query。
 * 接契约生成后，改为从 @fullstack-base/api-client 导入生成的函数（见 docs/extend-guide.md）。
 */
export function fetch__Domain__s(params) {
  return http.get('__relapi__', { params })
}
