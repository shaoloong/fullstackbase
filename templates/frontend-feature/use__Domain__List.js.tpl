import { computed, onMounted, reactive, ref } from 'vue'
import { fetch__Domain__s } from './api'

/**
 * __title__列表组合式函数：状态 + 请求 + 交互，页面只管渲染。
 * 生成后必改：字段名对齐后端 __Domain__Query / __Domain__VO。
 */
export function use__Domain__List() {
  const query = reactive({ page: 1, size: 10, keyword: '', status: null })
  const items = ref([])
  const total = ref(0)
  const currentPage = ref(1)
  const pageSize = ref(10)
  const loading = ref(false)
  const errorMsg = ref('')

  // 竞态保护：连点分页时丢弃过期响应
  let requestSeq = 0

  const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize.value)))
  const requestUrl = computed(() => `GET __api__?page=${query.page}&size=${query.size}`)

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
      const data = await fetch__Domain__s(params)
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

  // 条件变化必须回到第 1 页，否则「在第 3 页搜关键词」会得到空结果
  function handleSearch() { query.page = 1; load() }
  function handleReset() {
    query.keyword = ''; query.status = null; query.size = 10; query.page = 1; load()
  }
  function goPage(p) {
    if (p < 1 || p > totalPages.value || p === currentPage.value || loading.value) return
    query.page = p; load()
  }
  function changeSize(e) {
    query.size = Number(e.target.value); query.page = 1; load()
  }

  onMounted(load)

  return {
    query, items, total, currentPage, pageSize, loading, errorMsg,
    totalPages, requestUrl,
    load, handleSearch, handleReset, goPage, changeSize
  }
}
