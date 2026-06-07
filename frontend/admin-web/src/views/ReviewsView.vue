<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
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
const avgRating = computed(() => {
  if (!reviews.value.length) return '0.0'
  return (reviews.value.reduce((sum, r) => sum + Number(r.rating || 0), 0) / reviews.value.length).toFixed(1)
})
onMounted(load)
</script>
<template>
  <div class="admin-page reviews-page">
    <div class="tb-header">
      <div><h2>评价管理</h2><p class="page-subtitle">查看用户评价内容和评分，支撑商品详情页评价展示。</p></div>
    </div>
    <div class="admin-summary">
      <div class="summary-card"><div class="summary-card__label">当前页评价</div><div class="summary-card__value">{{ reviews.length }}</div><div class="summary-card__hint">评价列表记录</div></div>
      <div class="summary-card"><div class="summary-card__label">平均评分</div><div class="summary-card__value">{{ avgRating }}</div><div class="summary-card__hint">当前页统计</div></div>
      <div class="summary-card"><div class="summary-card__label">总记录数</div><div class="summary-card__value">{{ total }}</div><div class="summary-card__hint">分页结果</div></div>
      <div class="summary-card"><div class="summary-card__label">处理方式</div><div class="summary-card__value">删</div><div class="summary-card__hint">违规评价可删除</div></div>
    </div>
    <div class="table-panel">
      <el-table :data="reviews" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="userId" label="用户" width="90" />
        <el-table-column prop="productId" label="商品ID" width="100" />
        <el-table-column label="评分" width="140"><template #default="{row}"><span class="rating">{{'★'.repeat(row.rating)}}</span><span class="muted"> {{row.rating}}分</span></template></el-table-column>
        <el-table-column prop="content" label="内容" min-width="260" show-overflow-tooltip />
        <el-table-column label="操作" width="110" fixed="right"><template #default="{row}"><el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button></template></el-table-column>
      </el-table>
      <div class="table-panel__footer"><el-pagination v-model:current-page="page" :total="total" :page-size="10" layout="prev,pager,next" @change="load" /></div>
    </div>
  </div>
</template>
<style scoped>
.page-subtitle { margin-top: 8px; color: #6b7280; font-size: 13px; }
.rating { color: #f59e0b; letter-spacing: 1px; }
.muted { color: #8a94a6; font-size: 12px; }
</style>
