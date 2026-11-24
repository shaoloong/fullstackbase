<script setup>
/**
 * __title__功能域页面（模板生成的骨架）。
 * 只负责渲染，状态与请求在 use__Domain__List.js。
 * 生成后必改：表格列对齐后端 __Domain__VO。
 */
import { use__Domain__List } from './use__Domain__List'

const {
  query, items, total, currentPage, pageSize, loading, errorMsg,
  totalPages, requestUrl,
  load, handleSearch, handleReset, goPage, changeSize
} = use__Domain__List()
</script>

<template>
  <div class="page">
    <header class="hero">
      <h1>__title__列表 <span class="tag">由模板生成</span></h1>
      <p>分页查询 · 关键词搜索 · 状态筛选；表格列请按业务字段调整</p>
    </header>

    <section class="card">
      <div class="filter-row">
        <div class="field grow">
          <label>关键词</label>
          <input v-model="query.keyword" type="text" placeholder="输入关键词"
                 @keyup.enter="handleSearch" />
        </div>
        <div class="field">
          <label>状态</label>
          <select v-model="query.status">
            <option :value="null">全部状态</option>
            <option :value="1">启用</option>
            <option :value="0">停用</option>
          </select>
        </div>
        <button class="primary" :disabled="loading" @click="handleSearch">查询</button>
        <button :disabled="loading" @click="handleReset">重置</button>
      </div>
    </section>

    <section class="card">
      <div class="table-meta">
        <span>共 <b>{{ total }}</b> 条</span>
        <code class="api">{{ requestUrl }}</code>
      </div>

      <div v-if="errorMsg" class="state error">
        <p>{{ errorMsg }}</p><button @click="load">重新加载</button>
      </div>
      <template v-else>
        <div v-if="loading && !items.length" class="state">正在加载…</div>
        <div v-else-if="!items.length" class="state">没有符合条件的数据</div>
        <div v-else class="table-wrap">
          <table>
            <thead><tr><th>ID</th><th>名称</th><th>状态</th><th>创建时间</th></tr></thead>
            <tbody>
              <tr v-for="row in items" :key="row.id">
                <td class="mono">{{ row.id }}</td>
                <td>{{ row.name }}</td>
                <td>
                  <span class="badge" :class="row.status === 1 ? 'on' : 'off'">
                    {{ row.statusLabel }}
                  </span>
                </td>
                <td class="mono">{{ row.createdAt }}</td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="pager">
          <span>第 {{ currentPage }} / {{ totalPages }} 页</span>
          <button :disabled="currentPage <= 1 || loading" @click="goPage(currentPage - 1)">上一页</button>
          <button :disabled="currentPage >= totalPages || loading" @click="goPage(currentPage + 1)">下一页</button>
        </div>
      </template>
    </section>
  </div>
</template>
