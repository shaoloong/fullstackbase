import { computed, onMounted, reactive, ref } from 'vue'
import { fetchUsers } from './api'

/**
 * 用户列表的组合式函数：把「状态 + 请求 + 交互」从组件里抽出来。
 *
 * 拆开的好处：Page.vue 只管渲染，逻辑可以单测、可以被别的页面复用；
 * 新增功能域时直接复制本文件改 api 与字段名即可（见 templates/frontend-feature）。
 */
export function useUserList() {
  // 查询条件：字段名必须与后端 UserQuery 完全一致
  const query = reactive({ page: 1, size: 10, keyword: '', status: null })

  const items = ref([])          // 当前页数据
  const total = ref(0)           // 符合条件的总条数
  const currentPage = ref(1)     // 后端返回的实际页码
  const pageSize = ref(10)       // 后端返回的实际每页条数
  const loading = ref(false)
  const errorMsg = ref('')

  // 竞态保护：快速连点分页时旧请求可能更晚回来，用自增序号丢弃过期响应
  let requestSeq = 0

  // 总页数至少 1：total=0 时分页器会整个消失
  const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize.value)))

  // 把真实请求串显示出来，方便对照接口文档与 Network 面板
  const requestUrl = computed(() => {
    const p = new URLSearchParams()
    p.set('page', String(query.page))
    p.set('size', String(query.size))
    if (query.keyword.trim()) p.set('keyword', query.keyword.trim())
    if (query.status !== null && query.status !== '') p.set('status', String(query.status))
    return `GET /api/users?${p.toString()}`
  })

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
      const data = await fetchUsers(params)
      if (seq !== requestSeq) return       // 已有更新的请求发出，丢弃本次结果

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

  // 条件变化后必须回到第 1 页，否则「在第 3 页搜关键词」会得到空结果
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

  onMounted(load)   // 页面挂载即拉第一页

  return {
    query, items, total, currentPage, pageSize, loading, errorMsg,
    totalPages, requestUrl,
    load, handleSearch, handleReset, goPage, changeSize
  }
}
