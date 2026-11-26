import { computed, onMounted, reactive, ref } from 'vue'
import { createProduct, deleteProduct, fetchProducts, updateProduct } from './api'
import { buildProductPayload } from './formPayload'

/**
 * 产品列表组合式函数：列表状态 + 请求 + 增删改交互，页面只管渲染。
 * 竞态保护沿用 useUserList：过期响应直接丢弃；写操作用 saving / deletingId 防重复提交。
 */
export function useProductList() {
  const query = reactive({ page: 1, size: 10, keyword: '', status: null })
  const items = ref([])
  const total = ref(0)
  const currentPage = ref(1)
  const pageSize = ref(10)
  const loading = ref(false)
  const errorMsg = ref('')
  const actionError = ref('')

  // 新增 / 编辑表单状态
  const showForm = ref(false)
  const editingId = ref(null)
  const form = reactive(emptyForm())
  const formError = ref('')
  const saving = ref(false)
  const deletingId = ref(null)

  let requestSeq = 0

  const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize.value)))
  const requestUrl = computed(() => {
    const p = new URLSearchParams()
    p.set('page', String(query.page))
    p.set('size', String(query.size))
    if (query.keyword.trim()) p.set('keyword', query.keyword.trim())
    if (query.status !== null && query.status !== '') p.set('status', String(query.status))
    return `GET /api/products?${p.toString()}`
  })

  function emptyForm() {
    return { name: '', code: '', category: '', price: '', stock: '', description: '', status: 1 }
  }

  async function load() {
    const seq = ++requestSeq
    loading.value = true
    errorMsg.value = ''
    try {
      const params = {
        page: query.page,
        size: query.size,
        keyword: query.keyword.trim() || undefined,
        status: query.status === null || query.status === '' ? undefined : Number(query.status)
      }
      const data = await fetchProducts(params)
      if (seq !== requestSeq) return
      items.value = data.items || []
      total.value = data.total || 0
      currentPage.value = data.page
      pageSize.value = data.size
    } catch (e) {
      if (seq !== requestSeq) return
      errorMsg.value = e.message || '请求失败'
      items.value = []
      total.value = 0
    } finally {
      if (seq === requestSeq) loading.value = false
    }
  }

  function handleSearch() { query.page = 1; load() }

  function handleReset() {
    query.keyword = ''
    query.status = null
    query.size = 10
    query.page = 1
    load()
  }

  function goPage(p) {
    if (p < 1 || p > totalPages.value || p === currentPage.value || loading.value) return
    query.page = p
    load()
  }

  function changeSize(e) {
    query.size = Number(e.target.value)
    query.page = 1
    load()
  }

  function openCreate() {
    Object.assign(form, emptyForm())
    editingId.value = null
    formError.value = ''
    actionError.value = ''
    showForm.value = true
  }

  function openEdit(row) {
    // VO 可空字段被 Jackson 省略，用 || '' 兜底回填，避免 undefined 进表单
    Object.assign(form, {
      name: row.name || '',
      code: row.code || '',
      category: row.category || '',
      price: row.price == null ? '' : String(row.price),
      stock: row.stock == null ? '' : String(row.stock),
      description: row.description || '',
      status: row.status == null ? 1 : Number(row.status)
    })
    editingId.value = row.id
    formError.value = ''
    actionError.value = ''
    showForm.value = true
  }

  function closeForm() {
    if (saving.value) return
    showForm.value = false
    formError.value = ''
  }

  function validateForm() {
    if (!String(form.name).trim() || String(form.name).trim().length < 2) return '产品名称至少 2 个字符'
    if (!String(form.code).trim()) return '产品编码不能为空'
    if (form.price === '' || form.price === null || Number(form.price) < 0.01) return '售价不能小于 0.01'
    if (form.stock === '' || form.stock === null || Number(form.stock) < 0) return '库存不能小于 0'
    return ''
  }

  async function submitForm() {
    const invalid = validateForm()
    if (invalid) { formError.value = invalid; return }
    saving.value = true
    formError.value = ''
    const body = buildProductPayload(form)
    try {
      if (editingId.value) {
        await updateProduct(editingId.value, body)
      } else {
        await createProduct(body)
      }
      showForm.value = false
      await load()
    } catch (e) {
      formError.value = e.message || '保存失败'
    } finally {
      saving.value = false
    }
  }

  async function remove(row) {
    if (saving.value || deletingId.value) return
    if (!window.confirm(`确认删除产品「${row.name}」？删除后将从列表消失。`)) return
    deletingId.value = row.id
    actionError.value = ''
    try {
      await deleteProduct(row.id)
      // 删除后若当前页变空，回到上一页再刷新
      if (items.value.length === 1 && query.page > 1) query.page -= 1
      await load()
    } catch (e) {
      actionError.value = e.message || '删除失败'
    } finally {
      deletingId.value = null
    }
  }

  onMounted(load)

  return {
    query, items, total, currentPage, pageSize, loading, errorMsg, actionError,
    totalPages, requestUrl,
    showForm, editingId, form, formError, saving, deletingId,
    load, handleSearch, handleReset, goPage, changeSize,
    openCreate, openEdit, closeForm, submitForm, remove
  }
}
