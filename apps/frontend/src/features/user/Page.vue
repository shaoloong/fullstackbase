<script setup>
/**
 * 用户功能域页面：分页查询 / 关键词搜索 / 状态筛选。
 *
 * 这一层只负责「交互 + 渲染」，业务状态与请求逻辑都在 useUserList.js。
 * 状态字典（statusLabel）、时间格式（createdAt）由后端 VO 算好返回，
 * 后端换实现时前端一行都不用动。
 */
import { useUserList } from './useUserList'

const {
  query, items, total, currentPage, pageSize, loading, errorMsg,
  totalPages, requestUrl,
  load, handleSearch, handleReset, goPage, changeSize
} = useUserList()
</script>

<template>
  <div class="page">
    <header class="hero">
      <h1>用户列表 <span class="tag">Vue 3 + Spring Boot 3.5</span></h1>
      <p>分页查询 · 关键词搜索 · 状态筛选 · 统一响应体 · 逻辑删除，全链路可运行</p>
    </header>

    <section class="card">
      <div class="filter-row">
        <div class="field grow">
          <label>关键词（姓名 / 手机号 / 邮箱）</label>
          <input v-model="query.keyword" type="text"
                 placeholder="输入后回车或点查询，例如：张 / 1380 / example.com"
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

        <div class="field">
          <label>每页条数</label>
          <select :value="query.size" @change="changeSize">
            <option :value="10">10 条/页</option>
            <option :value="20">20 条/页</option>
            <option :value="50">50 条/页</option>
          </select>
        </div>

        <button class="primary" :disabled="loading" @click="handleSearch">查询</button>
        <button :disabled="loading" @click="handleReset">重置</button>
      </div>
    </section>

    <section class="card">
      <div class="table-meta">
        <span>
          共 <b>{{ total }}</b> 条
          <template v-if="loading"><span class="loading-tip"> · 加载中…</span></template>
        </span>
        <code class="api">{{ requestUrl }}</code>
      </div>

      <div v-if="errorMsg" class="state error">
        <p>{{ errorMsg }}</p>
        <button @click="load">重新加载</button>
      </div>

      <template v-else>
        <div v-if="loading && !items.length" class="state">正在加载用户数据…</div>
        <div v-else-if="!items.length" class="state">没有符合条件的数据，换个关键词试试</div>

        <div v-else class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>ID</th><th>姓名</th><th>手机号</th>
                <th>邮箱</th><th>状态</th><th>创建时间</th>
              </tr>
            </thead>
            <tbody>
              <!-- id 是后端刻意转成 String 的，避免 Long 精度丢失 -->
              <tr v-for="u in items" :key="u.id">
                <td class="mono">{{ u.id }}</td>
                <td>{{ u.name }}</td>
                <td class="mono">{{ u.phone }}</td>
                <td class="mono">{{ u.email }}</td>
                <td>
                  <span class="badge" :class="u.status === 1 ? 'on' : 'off'">{{ u.statusLabel }}</span>
                </td>
                <td class="mono">{{ u.createdAt }}</td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="pager">
          <span>第 {{ currentPage }} / {{ totalPages }} 页</span>
          <button :disabled="currentPage <= 1 || loading" @click="goPage(currentPage - 1)">上一页</button>
          <button v-for="p in totalPages" :key="p" :class="{ active: p === currentPage }"
                  :disabled="loading" @click="goPage(p)">{{ p }}</button>
          <button :disabled="currentPage >= totalPages || loading" @click="goPage(currentPage + 1)">下一页</button>
        </div>
      </template>
    </section>
  </div>
</template>
