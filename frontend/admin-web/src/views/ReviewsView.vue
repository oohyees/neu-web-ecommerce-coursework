<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchAllReviews, deleteReview } from '@/api/product'

const reviews = ref<any[]>([]); const total = ref(0); const page = ref(1); const loading = ref(false)

async function load() {
  loading.value = true
  try { const res: any = await fetchAllReviews({ page: page.value, size: 10 }); reviews.value = res.data?.items ?? []; total.value = res.data?.total ?? 0 } catch { reviews.value = [] }
  finally { loading.value = false }
}
async function handleDelete(r: any) {
  try { await ElMessageBox.confirm('确认删除该评价？','提示',{type:'warning'}); await deleteReview(r.id); ElMessage.success('已删除'); load() } catch { /* cancelled */ }
}
onMounted(load)
</script>
<template>
  <div>
    <div class="tb-header"><h2>评价管理</h2></div>
    <el-table :data="reviews" stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="userId" label="用户" width="70" />
      <el-table-column prop="productId" label="商品ID" width="80" />
      <el-table-column label="评分" width="100"><template #default="{row}">{{'★'.repeat(row.rating)}}{{row.rating}}分</template></el-table-column>
      <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip />
      <el-table-column label="操作" width="100"><template #default="{row}"><el-button text size="small" type="danger" @click="handleDelete(row)">删除</el-button></template></el-table-column>
    </el-table>
    <el-pagination v-model:current-page="page" :total="total" :page-size="10" layout="prev,pager,next" style="margin-top:16px;justify-content:flex-end" @change="load" />
  </div>
</template>
<style scoped>.tb-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; } .tb-header h2 { font-size: 20px; font-weight: 700; }</style>
