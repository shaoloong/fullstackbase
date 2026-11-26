/**
 * 产品表单 → 接口请求体的纯函数（无 Vue / HTTP 依赖，便于用 node 直接断言）。
 *
 * 关键约定：可空字段 category / description 清空时必须提交空串 ''，
 * 不能转成 null —— 后端 ProductServiceImpl.update() 把 null 解释为「不改」，
 * 只有非 null（含空串）才会写库，否则用户无法清空已填内容。
 */
export function buildProductPayload(form) {
  return {
    name: String(form.name ?? '').trim(),
    code: String(form.code ?? '').trim(),
    category: String(form.category ?? '').trim(),
    price: Number(form.price),
    stock: Number(form.stock),
    description: String(form.description ?? '').trim(),
    status: Number(form.status)
  }
}
