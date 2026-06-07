<script setup lang="ts">
import { ref, nextTick, watch } from 'vue'
import { useUserStore } from '@/stores/user'
import { fetchChatHistory, sendChatMessage } from '@/api/cs'

const userStore = useUserStore()
const open = ref(false)
const messages = ref<{ role: 'user' | 'bot'; content: string; time: string }[]>([])
const input = ref('')
const sending = ref(false)
const chatBody = ref<HTMLElement>()

async function loadHistory() {
  if (!userStore.isLoggedIn) return
  try {
    const res: any = await fetchChatHistory()
    const items = res.data ?? []
    messages.value = items.flatMap((m: any) => {
      const result: { role: 'user' | 'bot'; content: string; time: string }[] = [
        { role: 'user', content: m.content, time: m.createdAt || '' },
      ]
      if (m.reply) {
        result.push({ role: 'bot', content: m.reply, time: m.createdAt || '' })
      }
      return result
    })
    scrollToBottom()
  } catch { /* ignore */ }
}

async function send() {
  const text = input.value.trim()
  if (!text || sending.value) return
  sending.value = true
  messages.value.push({ role: 'user', content: text, time: new Date().toLocaleTimeString() })
  input.value = ''
  scrollToBottom()
  try {
    const res: any = await sendChatMessage(text)
    const data = res.data
    if (data?.reply) {
      messages.value.push({ role: 'bot', content: data.reply, time: new Date().toLocaleTimeString() })
    }
  } catch {
    messages.value.push({ role: 'bot', content: '网络异常，请稍后重试', time: new Date().toLocaleTimeString() })
  } finally {
    sending.value = false
    scrollToBottom()
  }
}

function scrollToBottom() {
  nextTick(() => {
    if (chatBody.value) chatBody.value.scrollTop = chatBody.value.scrollHeight
  })
}

function toggle() {
  open.value = !open.value
  if (open.value && messages.value.length === 0) loadHistory()
}

watch(() => userStore.isLoggedIn, (v) => {
  if (v && open.value) loadHistory()
  if (!v) { messages.value = []; open.value = false }
})
</script>

<template>
  <div class="chat-widget" v-if="userStore.isLoggedIn">
    <!-- Chat bubble button -->
    <button class="chat-bubble" :class="{ active: open }" @click="toggle" aria-label="在线客服">
      <svg v-if="!open" xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
      </svg>
      <svg v-else xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
      </svg>
    </button>

    <!-- Chat panel -->
    <Transition name="chat-slide">
      <div v-if="open" class="chat-panel">
        <div class="chat-header">
          <span class="chat-header-dot"></span>
          <span>在线客服</span>
        </div>
        <div class="chat-body" ref="chatBody">
          <div v-if="messages.length === 0" class="chat-welcome">
            <p>您好！有什么可以帮您？</p>
            <p class="chat-hints">
              <span @click="input = '如何退款？'; send()">退款问题</span>
              <span @click="input = '什么时候发货？'; send()">发货时间</span>
              <span @click="input = '有优惠吗？'; send()">优惠活动</span>
            </p>
          </div>
          <div v-for="(msg, i) in messages" :key="i" class="chat-msg" :class="msg.role">
            <div class="chat-msg-avatar">{{ msg.role === 'bot' ? '客服' : '我' }}</div>
            <div class="chat-msg-bubble">{{ msg.content }}</div>
          </div>
          <div v-if="sending" class="chat-msg bot">
            <div class="chat-msg-avatar">客服</div>
            <div class="chat-msg-bubble typing">正在输入<span>...</span></div>
          </div>
        </div>
        <div class="chat-footer">
          <input
            v-model="input"
            placeholder="输入消息..."
            @keydown.enter="send"
            :disabled="sending"
          />
          <button @click="send" :disabled="sending || !input.trim()">发送</button>
        </div>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.chat-widget {
  position: fixed;
  bottom: 24px;
  right: 24px;
  z-index: 9999;
}

.chat-bubble {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff6b35, #f7931e);
  color: #fff;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 16px rgba(255, 107, 53, 0.4);
  transition: all 0.3s;
}
.chat-bubble:hover { transform: scale(1.08); }
.chat-bubble.active { background: #666; box-shadow: 0 2px 8px rgba(0,0,0,0.2); }

.chat-panel {
  position: absolute;
  bottom: 68px;
  right: 0;
  width: 380px;
  max-height: 520px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-header {
  padding: 14px 18px;
  background: linear-gradient(135deg, #ff6b35, #f7931e);
  color: #fff;
  font-weight: 600;
  font-size: 15px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.chat-header-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #4ade80;
}

.chat-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  min-height: 300px;
  max-height: 360px;
}

.chat-welcome {
  text-align: center;
  padding: 20px 0;
  color: #888;
  font-size: 14px;
}
.chat-hints {
  display: flex;
  gap: 8px;
  justify-content: center;
  margin-top: 12px;
  flex-wrap: wrap;
}
.chat-hints span {
  padding: 4px 12px;
  background: #fff5f0;
  color: #ff6b35;
  border-radius: 16px;
  font-size: 12px;
  cursor: pointer;
  transition: background 0.2s;
}
.chat-hints span:hover { background: #ffe8dd; }

.chat-msg {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
  align-items: flex-start;
}
.chat-msg.bot { flex-direction: row; }
.chat-msg.user { flex-direction: row-reverse; }

.chat-msg-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 600;
  flex-shrink: 0;
}
.chat-msg.bot .chat-msg-avatar { background: #fff5f0; color: #ff6b35; }
.chat-msg.user .chat-msg-avatar { background: #e8f5e9; color: #4caf50; }

.chat-msg-bubble {
  max-width: 240px;
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 13px;
  line-height: 1.5;
  word-break: break-word;
}
.chat-msg.bot .chat-msg-bubble { background: #f5f5f5; color: #333; border-top-left-radius: 4px; }
.chat-msg.user .chat-msg-bubble { background: #ff6b35; color: #fff; border-top-right-radius: 4px; }

.typing span {
  animation: typing-dot 1.4s infinite;
}
@keyframes typing-dot {
  0%, 20% { opacity: 0.2; }
  50% { opacity: 1; }
  80%, 100% { opacity: 0.2; }
}

.chat-footer {
  display: flex;
  gap: 8px;
  padding: 12px 16px;
  border-top: 1px solid #f0f0f0;
}
.chat-footer input {
  flex: 1;
  height: 38px;
  padding: 0 12px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 13px;
  outline: none;
  transition: border-color 0.2s;
}
.chat-footer input:focus { border-color: #ff6b35; }
.chat-footer button {
  padding: 0 18px;
  height: 38px;
  background: linear-gradient(135deg, #ff6b35, #f7931e);
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.2s;
}
.chat-footer button:disabled { opacity: 0.5; cursor: not-allowed; }

.chat-slide-enter-active,
.chat-slide-leave-active {
  transition: all 0.3s ease;
}
.chat-slide-enter-from,
.chat-slide-leave-to {
  opacity: 0;
  transform: translateY(20px) scale(0.95);
}

@media (max-width: 480px) {
  .chat-panel {
    width: calc(100vw - 32px);
    right: -8px;
    max-height: 70vh;
  }
  .chat-body { min-height: 200px; }
}
</style>
