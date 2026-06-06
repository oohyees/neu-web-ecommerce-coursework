<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
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
onMounted(load)
</script>
<template>
  <div>
    <div class="tb-header"><h2>用户管理</h2><el-input v-model="keyword" placeholder="搜索账号/手机/昵称" size="default" style="width:240px" clearable @change="load" /></div>
    <el-table :data="users" stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="nickname" label="昵称" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column prop="phone" label="手机" />
      <el-table-column label="状态" width="80"><template #default="{row}"><el-tag :type="row.enabled?'success':'danger'" size="small">{{row.enabled?'正常':'已禁用'}}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="100"><template #default="{row}"><el-button text size="small" :type="row.enabled?'danger':'success'" @click="toggleEnabled(row)">{{row.enabled?'禁用':'启用'}}</el-button></template></el-table-column>
    </el-table>
    <el-pagination v-model:current-page="page" :total="total" :page-size="10" layout="prev,pager,next" style="margin-top:16px;justify-content:flex-end" @change="load" />
  </div>
</template>
<style scoped>
.tb-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.tb-header h2 { font-size: 20px; font-weight: 700; }
</style>
