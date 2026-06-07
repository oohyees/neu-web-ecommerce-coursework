<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { fetchUsers, setUserEnabled } from '@/api/user'

const users = ref<any[]>([]); const total = ref(0); const page = ref(1); const keyword = ref(''); const loading = ref(false)

async function load() {
  loading.value = true
  try { const res: any = await fetchUsers({ page: page.value, size: 10, keyword: keyword.value || undefined }); users.value = res.data?.items ?? []; total.value = res.data?.total ?? 0 } catch { users.value = [] }
  finally { loading.value = false }
}
async function toggleEnabled(u: any) {
  try { await setUserEnabled(u.id, !u.enabled); ElMessage.success(u.enabled ? '已禁用' : '已启用'); load() } catch { /* handled */ }
}
function applySearch() { page.value = 1; load() }
const enabledCount = computed(() => users.value.filter((u) => u.enabled).length)
const disabledCount = computed(() => users.value.filter((u) => !u.enabled).length)
const emailCount = computed(() => users.value.filter((u) => u.email).length)
const phoneCount = computed(() => users.value.filter((u) => u.phone).length)
function initials(u: any) {
  return String(u.nickname || u.username || 'U').slice(0, 1).toUpperCase()
}
onMounted(load)
</script>
<template>
  <div class="admin-page users-page">
    <div class="tb-header">
      <div>
        <h2>用户管理</h2>
        <p class="page-subtitle">查看普通用户资料、联系方式和账号状态，支持课堂演示禁用/启用。</p>
      </div>
      <div class="tb-actions">
        <el-input v-model="keyword" :prefix-icon="Search" placeholder="搜索账号/手机/昵称" size="default" style="width:260px" clearable @keyup.enter="applySearch" @clear="applySearch" />
      </div>
    </div>

    <div class="admin-summary">
      <div class="summary-card"><div class="summary-card__label">当前页正常</div><div class="summary-card__value">{{ enabledCount }}</div><div class="summary-card__hint">可登录购物</div></div>
      <div class="summary-card"><div class="summary-card__label">当前页禁用</div><div class="summary-card__value">{{ disabledCount }}</div><div class="summary-card__hint">后台风控证据</div></div>
      <div class="summary-card"><div class="summary-card__label">绑定邮箱</div><div class="summary-card__value">{{ emailCount }}</div><div class="summary-card__hint">找回密码/验证码</div></div>
      <div class="summary-card"><div class="summary-card__label">绑定手机</div><div class="summary-card__value">{{ phoneCount }}</div><div class="summary-card__hint">用户资料完整度</div></div>
    </div>

    <div class="table-panel">
      <el-table :data="users" stripe v-loading="loading">
        <el-table-column label="用户" min-width="220">
          <template #default="{row}">
            <div class="user-cell">
              <div class="avatar">{{ initials(row) }}</div>
              <div class="user-meta">
                <strong>{{ row.nickname || row.username }}</strong>
                <span>@{{ row.username }} · ID {{ row.id }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
        <el-table-column prop="phone" label="手机" width="140" />
        <el-table-column label="状态" width="110"><template #default="{row}"><span :class="['status-pill', row.enabled ? 'status-pill--success' : 'status-pill--danger']">{{row.enabled?'正常':'已禁用'}}</span></template></el-table-column>
        <el-table-column label="操作" width="120" fixed="right"><template #default="{row}"><el-button size="small" :type="row.enabled?'danger':'success'" @click="toggleEnabled(row)">{{row.enabled?'禁用':'启用'}}</el-button></template></el-table-column>
      </el-table>
      <div class="table-panel__footer">
        <el-pagination v-model:current-page="page" :total="total" :page-size="10" layout="prev,pager,next" @change="load" />
      </div>
    </div>
  </div>
</template>
<style scoped>
.page-subtitle { margin-top: 8px; color: #6b7280; font-size: 13px; }
.user-cell { display: flex; align-items: center; gap: 12px; }
.avatar { display: inline-flex; align-items: center; justify-content: center; width: 42px; height: 42px; border-radius: 8px; background: linear-gradient(135deg, #ff6b35, #f59e0b); color: #fff; font-weight: 800; }
.user-meta { display: grid; gap: 4px; min-width: 0; }
.user-meta strong { color: #1f2937; font-size: 14px; }
.user-meta span { color: #8a94a6; font-size: 12px; }
</style>
