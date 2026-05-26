<template>
  <AdminLayout>
    <AdminPageHeader title="评价管理" eyebrow="Review" subtitle="查看和管理用户对商品的评价。">
      <el-button type="danger" @click="load">刷新</el-button>
    </AdminPageHeader>

    <section class="admin-card">
      <el-table v-if="items.length" :data="items">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="productId" label="商品ID" width="90" />
        <el-table-column prop="userId" label="用户ID" width="90" />
        <el-table-column prop="rating" label="评分" width="80">
          <template #default="{ row }"><span class="price">{{ row.rating }} 分</span></template>
        </el-table-column>
        <el-table-column prop="content" label="评价内容" min-width="220" show-overflow-tooltip />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button link type="danger" size="small" @click="remove(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <EmptyState v-else title="暂无评价数据" description="当前没有商品评价记录。" />
      <div v-if="total > size" class="pager">
        <el-pagination layout="prev, pager, next, total" :total="total" :page-size="size" :current-page="page" @current-change="changePage" />
      </div>
    </section>
  </AdminLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '../api'
import AdminLayout from '../layouts/AdminLayout.vue'
import AdminPageHeader from '../components/AdminPageHeader.vue'
import EmptyState from '../components/EmptyState.vue'

const items = ref([]), page = ref(1), size = 10, total = ref(0)
async function load() {
  const result = (await api.get('/reviews/admin/all', { params: { page: page.value, size } })).data.data
  items.value = result.items || []; total.value = result.total || 0
}
async function remove(id) {
  try {
    await ElMessageBox.confirm('确定要删除该评价吗？', '确认删除', { confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning' })
    await api.delete(`/reviews/admin/${id}`)
    ElMessage.success('评价已删除')
    load()
  } catch { /* user cancelled */ }
}
function changePage(v) { page.value = v; load() }
onMounted(load)
</script>

<style scoped>
.pager { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
