<script setup>
/**
 * 产品管理页面：列表 / 搜索 / 分页 + 新增 / 编辑 / 删除闭环。
 * 本层只负责交互与渲染，状态与请求逻辑都在 useProductList.js。
 */
import { useProductList } from './useProductList'

const {
  query, items, total, currentPage, pageSize, loading, errorMsg, actionError,
  totalPages, requestUrl,
  showForm, editingId, form, formError, saving, deletingId,
  load, handleSearch, handleReset, goPage, changeSize,
  openCreate, openEdit, closeForm, submitForm, remove
} = useProductList()
</script>

<template>
  <div class="page">
    <header class="hero">
      <h1>产品管理 <span class="tag">增删改查 · 统一响应体 · 逻辑删除</span></h1>
      <p>分页查询 · 名称/编码搜索 · 状态筛选 · 新增 / 编辑 / 删除</p>
    </header>

    <section class="card">
      <div class="filter-row">
        <div class="field grow">
          <label>关键词（名称 / 编码）</label>
          <input v-model="query.keyword" type="text" placeholder="输入后回车或点查询"
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
        <button class="primary" @click="openCreate">新增产品</button>
      </div>
    </section>

    <div v-if="actionError" class="state error">
      <p>{{ actionError }}</p>
      <button @click="actionError = ''">关闭</button>
    </div>

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
        <div v-if="loading && !items.length" class="state">正在加载产品数据…</div>
        <div v-else-if="!items.length" class="state">没有符合条件的产品，换个关键词试试</div>

        <div v-else class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>ID</th><th>名称</th><th>编码</th><th>分类</th>
                <th>售价</th><th>库存</th><th>状态</th><th>创建时间</th><th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in items" :key="row.id">
                <td class="mono">{{ row.id }}</td>
                <td>{{ row.name }}</td>
                <td class="mono">{{ row.code }}</td>
                <td>{{ row.category || '-' }}</td>
                <td class="mono">{{ Number(row.price).toFixed(2) }}</td>
                <td>{{ row.stock }}</td>
                <td>
                  <span class="badge" :class="row.status === 1 ? 'on' : 'off'">
                    {{ row.statusLabel }}
                  </span>
                </td>
                <td class="mono">{{ row.createdAt }}</td>
                <td>
                  <button :disabled="saving || deletingId === row.id" @click="openEdit(row)">编辑</button>
                  <button :disabled="saving || deletingId === row.id" @click="remove(row)">
                    {{ deletingId === row.id ? '删除中…' : '删除' }}
                  </button>
                </td>
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

    <div v-if="showForm" class="modal-mask" @click.self="closeForm">
      <div class="modal">
        <h2>{{ editingId ? '编辑产品' : '新增产品' }}</h2>
        <form @submit.prevent="submitForm">
          <div class="form-grid">
            <div class="field">
              <label>产品名称 *</label>
              <input v-model="form.name" type="text" maxlength="128" placeholder="至少 2 个字符" />
            </div>
            <div class="field">
              <label>产品编码 / SKU *</label>
              <input v-model="form.code" type="text" maxlength="64" placeholder="唯一编码，如 P001" />
            </div>
            <div class="field">
              <label>产品分类</label>
              <input v-model="form.category" type="text" maxlength="64" placeholder="选填" />
            </div>
            <div class="field">
              <label>售价 *（≥ 0.01）</label>
              <input v-model="form.price" type="number" step="0.01" min="0.01" placeholder="0.00" />
            </div>
            <div class="field">
              <label>库存 *（≥ 0）</label>
              <input v-model="form.stock" type="number" step="1" min="0" placeholder="0" />
            </div>
            <div class="field">
              <label>状态</label>
              <select v-model.number="form.status">
                <option :value="1">启用</option>
                <option :value="0">停用</option>
              </select>
            </div>
            <div class="field wide">
              <label>产品描述</label>
              <textarea v-model="form.description" maxlength="500" rows="3" placeholder="选填"></textarea>
            </div>
          </div>

          <p v-if="formError" class="form-error">{{ formError }}</p>

          <div class="modal-actions">
            <button type="button" :disabled="saving" @click="closeForm">取消</button>
            <button type="submit" class="primary" :disabled="saving">
              {{ saving ? '保存中…' : '保存' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<style scoped>
.modal-mask {
  position: fixed; inset: 0; z-index: 20;
  display: flex; align-items: center; justify-content: center;
  background: rgba(24, 32, 51, .45);
}
.modal {
  width: min(560px, calc(100vw - 32px));
  max-height: calc(100vh - 48px);
  overflow-y: auto;
  background: #fff; border-radius: 14px; padding: 24px;
  box-shadow: 0 18px 50px rgba(20, 30, 60, .2);
}
.modal h2 { margin: 0 0 18px; font-size: 18px; font-weight: 600; }
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }
.form-grid .wide { grid-column: 1 / -1; }
.modal input[type="number"], .modal textarea {
  border: 1px solid #d8deea; border-radius: 8px; padding: 8px 11px;
  font-size: 13px; font-family: inherit; outline: none;
}
.modal input[type="number"]:focus, .modal textarea:focus {
  border-color: #4f6ef7; box-shadow: 0 0 0 3px rgba(79, 110, 247, .12);
}
.form-error { margin: 14px 0 0; color: #d14343; font-size: 13px; }
.modal-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 20px; }
@media (max-width: 620px) { .form-grid { grid-template-columns: 1fr; } }
</style>
