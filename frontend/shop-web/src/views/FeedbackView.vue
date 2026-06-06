<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchMyFeedback, submitFeedback } from '@/api/feedback'

const items = ref<any[]>([])
const form = ref({ type: '建议', content: '', contact: '' })
const submitting = ref(false)

async function load() {
  try {
    const res: any = await fetchMyFeedback()
    items.value = res.data ?? []
  } catch { items.value = [] }
}

async function handleSubmit() {
  if (!form.value.content.trim()) { ElMessage.warning('请输入反馈内容'); return }
  submitting.value = true
  try {
    await submitFeedback(form.value)
    ElMessage.success('提交成功')
    form.value.content = ''
    load()
  } catch { /* handled */ }
  finally { submitting.value = false }
}

onMounted(load)
</script>

<template>
  <div class="page-container feedback-page">
    <h2 class="page-title">意见反馈</h2>

    <div class="form-card">
      <h3>提交反馈</h3>
      <el-form>
        <el-form-item label="类型">
          <el-select v-model="form.type"><el-option value="建议" label="建议" /><el-option value="问题" label="问题" /><el-option value="其他" label="其他" /></el-select>
        </el-form-item>
        <el-form-item label="内容"><el-input v-model="form.content" type="textarea" :rows="4" placeholder="请描述你的问题或建议..." /></el-form-item>
        <el-form-item label="联系方式"><el-input v-model="form.contact" placeholder="邮箱/手机（选填）" /></el-form-item>
        <el-form-item><el-button type="primary" :loading="submitting" @click="handleSubmit">提交</el-button></el-form-item>
      </el-form>
    </div>

    <div v-if="items.length" class="history">
      <h3>我的反馈记录</h3>
      <div v-for="item in items" :key="item.id" class="item">
        <div class="item-header"><span class="item-type">{{ item.type }}</span><span :class="'item-status status-'+item.status?.toLowerCase()">{{ item.status }}</span></div>
        <p>{{ item.content }}</p>
        <p v-if="item.reply" class="reply">回复：{{ item.reply }}</p>
      </div>
    </div>
  </div>
</template>
<style scoped>
.feedback-page { max-width: 600px; }
.page-title { font-size: 22px; font-weight: 700; margin-bottom: 20px; }
.form-card { background: #fff; border-radius: 12px; padding: 24px; margin-bottom: 24px; box-shadow: 0 1px 4px rgba(0,0,0,0.04); }
.form-card h3, .history h3 { font-size: 16px; font-weight: 600; margin-bottom: 16px; }
.item { background: #fff; border-radius: 8px; padding: 14px; margin-bottom: 8px; }
.item-header { display: flex; justify-content: space-between; margin-bottom: 6px; }
.item-type { font-size: 13px; color: var(--color-primary); }
.item-status { font-size: 12px; padding: 1px 6px; border-radius: 3px; }
.status-pending { color: #f59e0b; }
.status-replied { color: #22c55e; }
.reply { color: #666; font-size: 13px; margin-top: 6px; padding-top: 6px; border-top: 1px solid #f0f0f0; }
</style>
