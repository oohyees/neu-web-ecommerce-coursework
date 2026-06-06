<template>
  <ShopLayout>
    <div class="page-wrap feedback-layout">
      <!-- 提交反馈卡片 -->
      <section class="page-card">
        <h2 class="card-title">提交反馈</h2>
        <p class="card-desc">欢迎提出宝贵意见，我们将尽快处理并回复。</p>
        <el-form :model="form" label-width="80px" class="feedback-form">
          <el-form-item label="反馈类型">
            <el-select v-model="form.type" placeholder="请选择反馈类型" style="width: 100%">
              <el-option label="商品问题" value="product" />
              <el-option label="订单问题" value="order" />
              <el-option label="物流问题" value="logistics" />
              <el-option label="售后问题" value="aftersale" />
              <el-option label="账户问题" value="account" />
              <el-option label="其他建议" value="other" />
            </el-select>
          </el-form-item>
          <el-form-item label="反馈内容">
            <el-input v-model="form.content" type="textarea" :rows="5" placeholder="请详细描述你的反馈内容..." />
          </el-form-item>
          <el-form-item label="联系方式">
            <el-input v-model="form.contact" placeholder="选填，方便我们联系你" />
          </el-form-item>
          <el-form-item>
            <el-button type="danger" @click="submit" :loading="submitting">提交反馈</el-button>
          </el-form-item>
        </el-form>
      </section>

      <!-- 我的反馈记录卡片 -->
      <section class="page-card">
        <h2 class="card-title">我的反馈记录</h2>
        <el-table v-if="items.length" :data="items" class="feedback-table">
          <el-table-column prop="content" label="反馈内容" min-width="200" show-overflow-tooltip />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <StatusTag :value="row.status" kind="order" />
            </template>
          </el-table-column>
          <el-table-column prop="reply" label="回复" min-width="150" show-overflow-tooltip>
            <template #default="{ row }">
              <span :class="{ muted: !row.reply }">{{ row.reply || '暂无回复' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="提交时间" width="170" />
        </el-table>
        <EmptyState v-else title="暂无反馈记录" description="提交反馈后可在此查看处理和回复状态。" />
      </section>
    </div>
  </ShopLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '@/api'
import { useUserStore, useCartStore, useFavoriteStore } from '@/stores'
import ShopLayout from '../layouts/ShopLayout.vue'
import EmptyState from '../components/EmptyState.vue'
import StatusTag from '../components/StatusTag.vue'

const userStore = useUserStore()
const items = ref<any[]>([])
const submitting = ref(false)
const form = ref({ type: '', content: '', contact: '' })

async function load() {
  items.value = (await api.get('/feedback', { params: { userId: userStore.userId } })).data.data || []
}

async function submit() {
  if (!form.value.content) return ElMessage.warning('请输入反馈内容')
  submitting.value = true
  try {
    await api.post('/feedback', { userId: userStore.userId, type: form.value.type, content: form.value.content, contact: form.value.contact })
    ElMessage.success('反馈提交成功')
    form.value = { type: '', content: '', contact: '' }
    load()
  } catch {
    ElMessage.error('提交失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.feedback-layout {
  display: grid;
  gap: 20px;
  max-width: 900px;
}

.card-title {
  margin: 0 0 6px;
  font-size: 18px;
  font-weight: 700;
}

.card-desc {
  margin: 0 0 20px;
  color: var(--muted);
  font-size: 14px;
}

.feedback-form {
  max-width: 560px;
}

.feedback-form :deep(.el-textarea__inner) {
  min-height: 120px;
}

.feedback-table {
  width: 100%;
}
</style>
