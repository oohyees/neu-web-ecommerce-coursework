<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchMyConsultations, submitConsultation } from '@/api/cs'

const items = ref<any[]>([])
const form = ref({ subject: '', content: '' })
const submitting = ref(false)

async function load() {
  try {
    const res: any = await fetchMyConsultations()
    items.value = res.data ?? []
  } catch { items.value = [] }
}

async function handleSubmit() {
  if (!form.value.subject || !form.value.content) { ElMessage.warning('请填写完整'); return }
  submitting.value = true
  try {
    await submitConsultation(form.value)
    ElMessage.success('留言已发送，客服会尽快回复')
    form.value = { subject: '', content: '' }
    load()
  } catch { /* handled */ }
  finally { submitting.value = false }
}

onMounted(load)
</script>

<template>
  <div class="page-container cs-page">
    <div class="page-intro">
      <div>
        <h2 class="page-title">在线客服</h2>
        <p>提交咨询主题和内容，后台客服咨询页面可回复，形成服务闭环。</p>
      </div>
    </div>
    <div class="page-metrics">
      <div class="metric-card"><span>咨询记录</span><strong>{{ items.length }}</strong><small>历史咨询</small></div>
      <div class="metric-card"><span>客服回复</span><strong>支持</strong><small>后台处理</small></div>
      <div class="metric-card"><span>状态</span><strong>可追踪</strong><small>等待/已回复</small></div>
    </div>
    <div class="form-card">
      <h3>发起咨询</h3>
      <el-form>
        <el-form-item label="主题"><el-input v-model="form.subject" placeholder="咨询主题" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="form.content" type="textarea" :rows="4" placeholder="描述你的问题..." /></el-form-item>
        <el-form-item><el-button type="primary" :loading="submitting" @click="handleSubmit">发送</el-button></el-form-item>
      </el-form>
    </div>
    <div v-if="items.length" class="history">
      <h3>咨询记录</h3>
      <div v-for="item in items" :key="item.id" class="item">
        <div class="item-subject">{{ item.subject }}</div>
        <p class="item-content">{{ item.content }}</p>
        <p v-if="item.reply" class="item-reply">客服回复：{{ item.reply }}</p>
        <span v-else class="pending">等待回复...</span>
      </div>
    </div>
  </div>
</template>
<style scoped>
.cs-page { max-width: 600px; }
.page-title { font-size: 22px; font-weight: 700; margin-bottom: 20px; }
.form-card { background: #fff; border-radius: 12px; padding: 24px; margin-bottom: 24px; box-shadow: 0 1px 4px rgba(0,0,0,0.04); }
.form-card h3, .history h3 { font-size: 16px; font-weight: 600; margin-bottom: 16px; }
.item { background: #fff; border-radius: 8px; padding: 14px; margin-bottom: 8px; }
.item-subject { font-weight: 600; font-size: 14px; margin-bottom: 6px; }
.item-content { font-size: 13px; color: #666; }
.item-reply { color: #22c55e; font-size: 13px; margin-top: 6px; padding-top: 6px; border-top: 1px solid #f0f0f0; }
.pending { font-size: 12px; color: #f59e0b; }
</style>
