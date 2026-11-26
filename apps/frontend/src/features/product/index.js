/**
 * product 功能域出口：域外只允许从这里导入，禁止深路径引用域内文件。
 */
export { default as ProductPage } from './Page.vue'
export { useProductList } from './useProductList'
export {
  fetchProducts, getProduct, createProduct, updateProduct, deleteProduct
} from './api'
