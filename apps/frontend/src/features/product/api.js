import http from '../../api/http'

/**
 * 产品域接口层：路径与字段名对齐后端 ProductController。
 * 响应拦截器已拆掉 R 外壳，这里拿到的直接是 data。
 */
export function fetchProducts(params) {
  return http.get('/products', { params })
}

export function getProduct(id) {
  return http.get(`/products/${id}`)
}

export function createProduct(body) {
  return http.post('/products', body)
}

export function updateProduct(id, body) {
  return http.put(`/products/${id}`, body)
}

export function deleteProduct(id) {
  return http.delete(`/products/${id}`)
}
