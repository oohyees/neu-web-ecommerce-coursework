<script setup lang="ts">
import { ref, nextTick, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchMyConsultations, submitConsultation, fetchChatHistory, sendChatMessage } from '@/api/cs'

// Mode toggle: 'form' = original consultation form, 'chat' = live chat dialog
const mode = ref<'form' | 'chat'>('chat')

// ─── Form mode ───
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

// ─── Chat mode ───
const chatMessages = ref<{ role: 'user' | 'bot'; content: string; time: string }[]>([])
const chatInput = ref('')
const chatSending = ref(false)
const chatBody = ref<HTMLElement>()

async function loadChatHistory() {
  try {
    const res: any = await fetchChatHistory()
    const data = res.data ?? []
    chatMessages.value = data.flatMap((m: any) => {
      const result: { role: 'user' | 'bot'; content: string; time: string }[] = [
        { role: 'user', content: m.content, time: m.createdAt || '' },
      ]
      if (m.reply) {
        result.push({ role: 'bot', content: m.reply, time: m.createdAt || '' })
      }
      return result
    })
    scrollChatBottom()
  } catch { /* ignore */ }
}

async function sendChat() {
  const text = chatInput.value.trim()
  if (!text || chatSending.value) return
  chatSending.value = true
  chatMessages.value.push({ role: 'user', content: text, time: new Date().toLocaleTimeString() })
  chatInput.value = ''
  scrollChatBottom()
  try {
    const res: any = await sendChatMessage(text)
    const data = res.data
    if (data?.reply) {
      chatMessages.value.push({ role: 'bot', content: data.reply, time: new Date().toLocaleTimeString() })
    }
  } catch {
    chatMessages.value.push({ role: 'bot', content: '网络异常，请稍后重试', time: new Date().toLocaleTimeString() })
  } finally {
    chatSending.value = false
    scrollChatBottom()
  }
}

function scrollChatBottom() {
  nextTick(() => {
    if (chatBody.value) chatBody.value.scrollTop = chatBody.value.scrollHeight
  })
}

function quickAsk(text: string) {
  chatInput.value = text
  sendChat()
}

onMounted(() => {
  load()
  loadChatHistory()
})
</script>

<template>
  <div class="page-container cs-page">
    <div class="page-intro">
      <div>
        <h2 class="page-title">在线客服</h2>
        <p>选择对话模式即时沟通，或切换表单模式提交咨询，后台客服会尽快回复。</p>
      </div>
    </div>

    <!-- Mode toggle -->
    <div class="mode-toggle">
      <button :class="{ active: mode === 'chat' }" @click="mode = 'chat'">
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
        对话模式
      </button>
      <button :class="{ active: mode === 'form' }" @click="mode = 'form'">
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg>
        表单模式
      </button>
    </div>

    <!-- ═══ Chat Mode ═══ -->
    <div v-if="mode === 'chat'" class="chat-container">
      <div class="chat-panel">
        <div class="chat-header">
          <span class="chat-header-dot"></span>
          <span>在线客服</span>
        </div>
        <div class="chat-body" ref="chatBody">
          <div v-if="chatMessages.length === 0" class="chat-welcome">
            <p>您好！有什么可以帮您？</p>
            <p class="chat-hints">
              <span @click="quickAsk('如何退款？')">退款问题</span>
              <span @click="quickAsk('什么时候发货？')">发货时间</span>
              <span @click="quickAsk('有优惠吗？')">优惠活动</span>
              <span @click="quickAsk('忘记密码怎么办？')">找回密码</span>
            </p>
          </div>
          <div v-for="(msg, i) in chatMessages" :key="i" class="chat-msg" :class="msg.role">
            <div class="chat-msg-avatar">{{ msg.role === 'bot' ? '客服' : '我' }}</div>
            <div class="chat-msg-bubble">{{ msg.content }}</div>
          </div>
          <div v-if="chatSending" class="chat-msg bot">
            <div class="chat-msg-avatar">客服</div>
            <div class="chat-msg-bubble typing">正在输入<span>...</span></div>
          </div>
        </div>
        <div class="chat-footer">
          <input v-model="chatInput" placeholder="输入消息..." @keydown.enter="sendChat" :disabled="chatSending" />
          <button @click="sendChat" :disabled="chatSending || !chatInput.trim()">发送</button>
        </div>
      </div>
    </div>

    <!-- ═══ Form Mode ═══ -->
    <template v-if="mode === 'form'">
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
    </template>
  </div>
</template>

<style scoped>
.cs-page { max-width: 700px; }
.page-title { font-size: 22px; font-weight: 700; margin-bottom: 20px; }

/* Mode toggle */
.mode-toggle {
  display: flex; gap: 0; margin-bottom: 20px;
  background: #f5f5f5; border-radius: 10px; padding: 4px; width: fit-content;
}
.mode-toggle button {
  display: flex; align-items: center; gap: 6px;
  padding: 8px 20px; border: none; border-radius: 8px;
  font-size: 14px; font-weight: 500; cursor: pointer;
  background: transparent; color: #888; transition: all 0.2s;
}
.mode-toggle button.active {
  background: #fff; color: var(--color-primary, #ff6b35);
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
}
.mode-toggle button:hover:not(.active) { color: #555; }

/* Chat mode */
.chat-container { margin-bottom: 24px; }
.chat-panel {
  background: #fff; border-radius: 16px; overflow: hidden;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}
.chat-header {
  padding: 14px 18px;
  background: linear-gradient(135deg, #ff6b35, #f7931e);
  color: #fff; font-weight: 600; font-size: 15px;
  display: flex; align-items: center; gap: 8px;
}
.chat-header-dot {
  width: 8px; height: 8px; border-radius: 50%; background: #4ade80;
}
.chat-body {
  height: 400px; overflow-y: auto; padding: 16px;
}
.chat-welcome {
  text-align: center; padding: 40px 0; color: #888; font-size: 14px;
}
.chat-hints {
  display: flex; gap: 8px; justify-content: center; margin-top: 16px; flex-wrap: wrap;
}
.chat-hints span {
  padding: 6px 14px; background: #fff5f0; color: #ff6b35;
  border-radius: 16px; font-size: 12px; cursor: pointer; transition: background 0.2s;
}
.chat-hints span:hover { background: #ffe8dd; }

.chat-msg {
  display: flex; gap: 8px; margin-bottom: 12px; align-items: flex-start;
}
.chat-msg.user { flex-direction: row-reverse; }
.chat-msg-avatar {
  width: 32px; height: 32px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 11px; font-weight: 600; flex-shrink: 0;
}
.chat-msg.bot .chat-msg-avatar { background: #fff5f0; color: #ff6b35; }
.chat-msg.user .chat-msg-avatar { background: #e8f5e9; color: #4caf50; }
.chat-msg-bubble {
  max-width: 300px; padding: 10px 14px; border-radius: 12px;
  font-size: 13px; line-height: 1.5; word-break: break-word;
}
.chat-msg.bot .chat-msg-bubble { background: #f5f5f5; color: #333; border-top-left-radius: 4px; }
.chat-msg.user .chat-msg-bubble { background: #ff6b35; color: #fff; border-top-right-radius: 4px; }
.typing span { animation: typing-dot 1.4s infinite; }
@keyframes typing-dot { 0%, 20% { opacity: 0.2; } 50% { opacity: 1; } 80%, 100% { opacity: 0.2; } }

.chat-footer {
  display: flex; gap: 8px; padding: 12px 16px; border-top: 1px solid #f0f0f0;
}
.chat-footer input {
  flex: 1; height: 40px; padding: 0 12px;
  border: 1px solid #e0e0e0; border-radius: 8px; font-size: 13px; outline: none;
  transition: border-color 0.2s;
}
.chat-footer input:focus { border-color: #ff6b35; }
.chat-footer button {
  padding: 0 20px; height: 40px;
  background: linear-gradient(135deg, #ff6b35, #f7931e);
  color: #fff; border: none; border-radius: 8px;
  font-size: 13px; font-weight: 600; cursor: pointer; transition: opacity 0.2s;
}
.chat-footer button:disabled { opacity: 0.5; cursor: not-allowed; }

/* Form mode */
.form-card {
  background: #fff; border-radius: 12px; padding: 24px;
  margin-bottom: 24px; box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}
.form-card h3, .history h3 { font-size: 16px; font-weight: 600; margin-bottom: 16px; }
.item { background: #fff; border-radius: 8px; padding: 14px; margin-bottom: 8px; }
.item-subject { font-weight: 600; font-size: 14px; margin-bottom: 6px; }
.item-content { font-size: 13px; color: #666; }
.item-reply { color: #22c55e; font-size: 13px; margin-top: 6px; padding-top: 6px; border-top: 1px solid #f0f0f0; }
.pending { font-size: 12px; color: #f59e0b; }

@media (max-width: 768px) {
  .chat-body { height: 320px; }
  .chat-msg-bubble { max-width: 220px; }
}
</style>
