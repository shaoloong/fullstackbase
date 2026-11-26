/**
 * 产品表单请求体纯函数断言（零依赖，node 直接运行）。
 * 运行：node apps/frontend/src/features/product/formPayload.test.mjs
 */
import assert from 'node:assert/strict'
import { test } from 'node:test'
import { buildProductPayload } from './formPayload.js'

test('清空 category/description 提交空串而不是 null，避免后端按「不改」处理', () => {
  const body = buildProductPayload({
    name: '清空探测', code: 'RVPROBE', category: '', price: '8', stock: '2',
    description: '', status: 1
  })
  assert.equal(body.category, '')
  assert.equal(body.description, '')
  assert.ok(!Object.values(body).includes(null), '请求体不应含 null')
})

test('编辑回填后再清空：旧值被覆盖为空串', () => {
  const body = buildProductPayload({
    name: '清空探测', code: 'RVPROBE', category: '   ', price: '8', stock: '2',
    description: '   ', status: 0
  })
  assert.equal(body.category, '')
  assert.equal(body.description, '')
  assert.equal(body.status, 0)
})

test('正常字段 trim 后保留，未清空字段不被改动', () => {
  const body = buildProductPayload({
    name: '  名称  ', code: '  CODE1  ', category: ' 电子 ', price: '12.50', stock: '3',
    description: ' 描述 ', status: 1
  })
  assert.deepEqual(body, {
    name: '名称', code: 'CODE1', category: '电子', price: 12.5, stock: 3,
    description: '描述', status: 1
  })
})

test('null/undefined 可空字段归一为空串，不留 null', () => {
  const body = buildProductPayload({
    name: 'x', code: 'y', category: null, price: 1, stock: 0,
    description: undefined, status: 1
  })
  assert.equal(body.category, '')
  assert.equal(body.description, '')
})
