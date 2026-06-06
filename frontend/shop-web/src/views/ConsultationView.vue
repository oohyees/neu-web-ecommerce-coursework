<template>
  <ShopLayout>
    <div class="page-wrap">
      <!-- 在线聊天 -->
      <section class="page-card">
        <div class="chat-header">
          <h2>在线聊天</h2>
          <el-tag :type="connected ? 'success' : 'info'" size="small">{{ connected ? '已连接' : '未连接' }}</el-tag>
        </div>
        <div class="chat-box" ref="chatBox">
          <div v-if="!chatMessages.length" class="chat-empty">发送消息开始与客服实时对话</div>
          <div v-for="(m, i) in chatMessages" :key="i" :class="['chat-msg', m.from === userId ? 'self' : 'other']">
            <strong>{{ m.fromName || m.from }}</strong>
            <p>{{ m.content }}</p>
            <small>{{ m.time?.substring(11, 19) }}</small>
          </div>
        </div>
        <div class="chat-input">
          <el-input v-model="chatText" placeholder="输入消息..." @keyup.enter="sendMessage">
            <template #append><el-button type="danger" @click="sendMessage" :disabled="!connected">发送</el-button></template>
          </el-input>
        </div>
      </section>

      <!-- 留言表单 -->
      <section class="page-card">
        <h2>提交留言</h2>
        <p class="card-desc">提交问题后客服将尽快回复处理。</p>
        <el-form :model="form" class="form">
          <el-form-item label="主题">
            <el-input v-model="form.subject" placeholder="请输入咨询主题" />
          </el-form-item>
          <el-form-item label="内容">
            <el-input v-model="form.content" type="textarea" :rows="4" placeholder="请详细描述你的问题..." />
          </el-form-item>
          <el-form-item>
            <el-button type="danger" @click="submit" :loading="submitting">提交留言</el-button>
          </el-form-item>
        </el-form>
      </section>

      <!-- 历史记录 -->
      <section class="page-card">
        <h2>我的咨询记录</h2>
        <el-table v-if="items.length" :data="items">
          <el-table-column prop="subject" label="主题" min-width="160" show-overflow-tooltip />
          <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip />
          <el-table-column label="状态" width="100">
            <template #default="{ row }"><StatusTag :value="row.status" kind="order" /></template>
          </el-table-column>
          <el-table-column prop="reply" label="回复" min-width="150" show-overflow-tooltip>
            <template #default="{ row }"><span :class="{ muted: !row.reply }">{{ row.reply || '暂无回复' }}</span></template>
          </el-table-column>
        </el-table>
        <EmptyState v-else title="暂无咨询记录" description="提交咨询后可在此查看处理状态和回复。" />
      </section>
    </div>
  </ShopLayout>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../api'
import { useSessionStore } from '../store'
import ShopLayout from '../layouts/ShopLayout.vue'
import EmptyState from '../components/EmptyState.vue'
import StatusTag from '../components/StatusTag.vue'

const session = useSessionStore()
const items = ref([]), form = ref({ subject: '', content: '' }), submitting = ref(false)
const userId = String(session.userId || 2)

// WebSocket 聊天
const chatText = ref(''), chatMessages = ref([]), connected = ref(false), chatBox = ref(null)
let ws = null

function connectWebSocket() {
  const protocol = location.protocol === 'https:' ? 'wss:' : 'ws:'
  const host = location.host
  ws = new WebSocket(`${protocol}//${host}/ws/chat/${userId}`)
  ws.onopen = () => { connected.value = true }
  ws.onmessage = (e) => {
    try {
      chatMessages.value.push(JSON.parse(e.data))
      nextTick(() => { if (chatBox.value) chatBox.value.scrollTop = chatBox.value.scrollHeight })
    } catch { /* ignore */ }
  }
  ws.onclose = () => { connected.value = false; setTimeout(connectWebSocket, 3000) }
  ws.onerror = () => { connected.value = false }
}

function sendMessage() {
  if (!chatText.value.trim() || !ws || ws.readyState !== WebSocket.OPEN) return
  ws.send(JSON.stringify({ content: chatText.value, to: 'admin' }))
  chatText.value = ''
}

onMounted(() => { connectWebSocket(); load() })
onUnmounted(() => { if (ws) ws.close() })

async function load() { items.value = (await api.get('/consultations', { params: { userId: session.userId } })).data.data || [] }
async function submit() {
  if (!form.value.subject || !form.value.content) return ElMessage.warning('请填写主题和内容')
  submitting.value = true
  try {
    await api.post('/consultations', { userId: session.userId, ...form.value })
    ElMessage.success('留言已提交')
    form.value = { subject: '', content: '' }
    load()
  } catch { ElMessage.error('提交失败') }
  finally { submitting.value = false }
}
</script>

<style scoped>
.page-card { margin-bottom: 20px; }
.page-card h2 { margin: 0 0 6px; font-size: 18px; font-weight: 700; }
.card-desc { margin: 0 0 18px; color: var(--muted); font-size: 14px; }
.form { max-width: 560px; }

.chat-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.chat-header h2 { margin: 0; }
.chat-box {
  height: 280px; overflow-y: auto; padding: 12px;
  background: #f8fafc; border: 1px solid var(--line); border-radius: var(--radius);
  margin-bottom: 12px;
}
.chat-empty { text-align: center; color: var(--muted); padding-top: 120px; font-size: 14px; }
.chat-msg { margin-bottom: 10px; padding: 8px 12px; background: #fff; border-radius: var(--radius-sm); max-width: 80%; }
.chat-msg.self { margin-left: auto; background: var(--brand-light); border: 1px solid #fecdd3; }
.chat-msg strong { display: block; font-size: 12px; color: var(--brand); margin-bottom: 4px; }
.chat-msg p { margin: 0; font-size: 14px; line-height: 1.5; }
.chat-msg small { color: var(--muted); font-size: 11px; }
.chat-input { max-width: 100%; }
</style>
