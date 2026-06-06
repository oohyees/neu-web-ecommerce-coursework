<template>
  <ShopLayout>
    <div class="page-wrap pay-page" v-if="order">
      <div class="pay-card">
        <div class="pay-header">
          <el-icon :size="48" color="#16a34a">✓</el-icon>
          <h1>订单已生成，请完成支付</h1>
        </div>

        <div class="order-info">
          <div><span>订单号</span><strong>{{ orderData.orderNo }}</strong></div>
          <div><span>应付金额</span><b class="price">&yen;{{ orderData.payableAmount }}</b></div>
        </div>

        <el-divider>选择支付方式</el-divider>

        <div class="payment-methods">
          <label :class="['pay-method', { active: method === 'ALIPAY' }]">
            <input type="radio" v-model="method" value="ALIPAY" />
            <span class="pay-icon">💙</span>
            <div><strong>支付宝</strong><small>推荐安装支付宝客户端的用户使用</small></div>
          </label>
          <label :class="['pay-method', { active: method === 'WECHAT' }]">
            <input type="radio" v-model="method" value="WECHAT" />
            <span class="pay-icon">💚</span>
            <div><strong>微信支付</strong><small>推荐安装微信客户端的用户使用</small></div>
          </label>
          <label :class="['pay-method', { active: method === 'UNIONPAY' }]">
            <input type="radio" v-model="method" value="UNIONPAY" />
            <span class="pay-icon">💳</span>
            <div><strong>银联支付</strong><small>支持各大银行卡在线支付</small></div>
          </label>
        </div>

        <div class="pay-qrcode" v-if="paying">
          <div class="qrcode-box">
            <span class="qrcode-icon">📱</span>
            <strong>{{ method === 'ALIPAY' ? '支付宝' : method === 'WECHAT' ? '微信' : '银联' }}扫码支付</strong>
            <p>请使用{{ method === 'ALIPAY' ? '支付宝' : method === 'WECHAT' ? '微信' : '银联云闪付' }}扫描二维码完成支付</p>
            <p class="price">应付金额：&yen;{{ orderData.payableAmount }}</p>

            <div class="scan-area">
              <div class="qr-code">
                <!-- 模拟二维码图案 -->
                <svg viewBox="0 0 29 29" class="qr-svg">
                  <!-- 定位图案 -->
                  <rect x="0" y="0" width="9" height="1" fill="#111" /><rect x="0" y="0" width="1" height="9" fill="#111" />
                  <rect x="2" y="2" width="5" height="5" fill="#111" /><rect x="3" y="3" width="3" height="3" fill="#fff" />
                  <rect x="0" y="7" width="9" height="1" fill="#111" /><rect x="7" y="0" width="1" height="9" fill="#111" />
                  <rect x="20" y="0" width="9" height="1" fill="#111" /><rect x="20" y="0" width="1" height="9" fill="#111" />
                  <rect x="22" y="2" width="5" height="5" fill="#111" /><rect x="23" y="3" width="3" height="3" fill="#fff" />
                  <rect x="20" y="7" width="9" height="1" fill="#111" /><rect x="27" y="0" width="1" height="9" fill="#111" />
                  <rect x="0" y="20" width="9" height="1" fill="#111" /><rect x="0" y="20" width="1" height="9" fill="#111" />
                  <rect x="2" y="22" width="5" height="5" fill="#111" /><rect x="3" y="23" width="3" height="3" fill="#fff" />
                  <rect x="0" y="27" width="9" height="1" fill="#111" /><rect x="7" y="20" width="1" height="9" fill="#111" />
                  <!-- 随机数据区 -->
                  <rect x="10" y="0" width="1" height="1" fill="#111" /><rect x="12" y="0" width="1" height="1" fill="#111" />
                  <rect x="15" y="0" width="1" height="1" fill="#111" /><rect x="18" y="0" width="1" height="1" fill="#111" />
                  <rect x="10" y="2" width="1" height="1" fill="#111" /><rect x="14" y="2" width="1" height="1" fill="#111" />
                  <rect x="16" y="2" width="1" height="1" fill="#111" /><rect x="11" y="3" width="1" height="1" fill="#111" />
                  <rect x="13" y="3" width="1" height="1" fill="#111" /><rect x="17" y="3" width="1" height="1" fill="#111" />
                  <rect x="10" y="4" width="1" height="1" fill="#111" /><rect x="15" y="4" width="1" height="1" fill="#111" />
                  <rect x="12" y="5" width="1" height="1" fill="#111" /><rect x="18" y="5" width="1" height="1" fill="#111" />
                  <rect x="10" y="6" width="1" height="1" fill="#111" /><rect x="14" y="6" width="1" height="1" fill="#111" />
                  <rect x="16" y="6" width="1" height="1" fill="#111" /><rect x="11" y="7" width="1" height="1" fill="#111" />
                  <rect x="13" y="7" width="1" height="1" fill="#111" /><rect x="17" y="7" width="1" height="1" fill="#111" />
                  <rect x="10" y="9" width="1" height="1" fill="#111" /><rect x="13" y="9" width="1" height="1" fill="#111" />
                  <rect x="16" y="9" width="1" height="1" fill="#111" /><rect x="18" y="9" width="1" height="1" fill="#111" />
                  <rect x="11" y="10" width="1" height="1" fill="#111" /><rect x="14" y="10" width="1" height="1" fill="#111" />
                  <rect x="10" y="11" width="1" height="1" fill="#111" /><rect x="14" y="11" width="1" height="1" fill="#111" />
                  <rect x="18" y="11" width="1" height="1" fill="#111" /><rect x="12" y="12" width="1" height="1" fill="#111" />
                  <rect x="16" y="12" width="1" height="1" fill="#111" /><rect x="10" y="13" width="1" height="1" fill="#111" />
                  <rect x="15" y="13" width="1" height="1" fill="#111" /><rect x="11" y="14" width="1" height="1" fill="#111" />
                  <rect x="14" y="14" width="1" height="1" fill="#111" /><rect x="17" y="14" width="1" height="1" fill="#111" />
                  <rect x="10" y="15" width="1" height="1" fill="#111" /><rect x="16" y="15" width="1" height="1" fill="#111" />
                  <rect x="12" y="16" width="1" height="1" fill="#111" /><rect x="18" y="16" width="1" height="1" fill="#111" />
                  <rect x="14" y="17" width="1" height="1" fill="#111" /><rect x="10" y="18" width="1" height="1" fill="#111" />
                  <rect x="15" y="18" width="1" height="1" fill="#111" /><rect x="12" y="19" width="1" height="1" fill="#111" />
                  <rect x="16" y="19" width="1" height="1" fill="#111" /><rect x="18" y="19" width="1" height="1" fill="#111" />
                  <rect x="11" y="20" width="1" height="1" fill="#111" /><rect x="14" y="20" width="1" height="1" fill="#111" />
                  <rect x="17" y="20" width="1" height="1" fill="#111" /><rect x="10" y="21" width="1" height="1" fill="#111" />
                  <rect x="15" y="21" width="1" height="1" fill="#111" /><rect x="12" y="22" width="1" height="1" fill="#111" />
                  <rect x="18" y="22" width="1" height="1" fill="#111" /><rect x="11" y="23" width="1" height="1" fill="#111" />
                  <rect x="14" y="23" width="1" height="1" fill="#111" /><rect x="16" y="23" width="1" height="1" fill="#111" />
                  <rect x="10" y="24" width="1" height="1" fill="#111" /><rect x="17" y="24" width="1" height="1" fill="#111" />
                  <rect x="13" y="25" width="1" height="1" fill="#111" /><rect x="15" y="25" width="1" height="1" fill="#111" />
                  <rect x="12" y="26" width="1" height="1" fill="#111" /><rect x="18" y="26" width="1" height="1" fill="#111" />
                  <rect x="10" y="27" width="1" height="1" fill="#111" /><rect x="16" y="27" width="1" height="1" fill="#111" />
                  <rect x="11" y="28" width="1" height="1" fill="#111" /><rect x="14" y="28" width="1" height="1" fill="#111" />
                  <rect x="17" y="28" width="1" height="1" fill="#111" /><rect x="20" y="9" width="1" height="1" fill="#111" />
                  <rect x="22" y="9" width="1" height="1" fill="#111" /><rect x="25" y="9" width="1" height="1" fill="#111" />
                  <rect x="28" y="9" width="1" height="1" fill="#111" /><rect x="20" y="11" width="1" height="1" fill="#111" />
                  <rect x="24" y="11" width="1" height="1" fill="#111" /><rect x="27" y="11" width="1" height="1" fill="#111" />
                  <rect x="21" y="12" width="1" height="1" fill="#111" /><rect x="26" y="12" width="1" height="1" fill="#111" />
                  <rect x="23" y="13" width="1" height="1" fill="#111" /><rect x="25" y="13" width="1" height="1" fill="#111" />
                  <rect x="28" y="13" width="1" height="1" fill="#111" /><rect x="20" y="14" width="1" height="1" fill="#111" />
                  <rect x="22" y="14" width="1" height="1" fill="#111" /><rect x="24" y="15" width="1" height="1" fill="#111" />
                  <rect x="26" y="15" width="1" height="1" fill="#111" /><rect x="21" y="16" width="1" height="1" fill="#111" />
                  <rect x="27" y="16" width="1" height="1" fill="#111" /><rect x="23" y="17" width="1" height="1" fill="#111" />
                  <rect x="25" y="17" width="1" height="1" fill="#111" /><rect x="28" y="17" width="1" height="1" fill="#111" />
                  <rect x="20" y="18" width="1" height="1" fill="#111" /><rect x="22" y="19" width="1" height="1" fill="#111" />
                  <rect x="24" y="19" width="1" height="1" fill="#111" /><rect x="26" y="19" width="1" height="1" fill="#111" />
                  <rect x="21" y="20" width="1" height="1" fill="#111" /><rect x="23" y="20" width="1" height="1" fill="#111" />
                  <rect x="25" y="21" width="1" height="1" fill="#111" /><rect x="27" y="21" width="1" height="1" fill="#111" />
                  <rect x="20" y="22" width="1" height="1" fill="#111" /><rect x="22" y="23" width="1" height="1" fill="#111" />
                  <rect x="28" y="23" width="1" height="1" fill="#111" /><rect x="24" y="24" width="1" height="1" fill="#111" />
                  <rect x="26" y="24" width="1" height="1" fill="#111" /><rect x="21" y="25" width="1" height="1" fill="#111" />
                  <rect x="23" y="26" width="1" height="1" fill="#111" /><rect x="27" y="26" width="1" height="1" fill="#111" />
                  <rect x="25" y="27" width="1" height="1" fill="#111" /><rect x="28" y="27" width="1" height="1" fill="#111" />
                  <rect x="21" y="28" width="1" height="1" fill="#111" /><rect x="24" y="28" width="1" height="1" fill="#111" />
                  <!-- 中间 Logo 区域 -->
                  <rect x="11" y="11" width="7" height="7" fill="#fff" stroke="#e60023" stroke-width="0.5" />
                  <text x="14.5" y="16" text-anchor="middle" font-size="4" fill="#e60023" font-weight="bold">支</text>
                </svg>
                <!-- 扫描线动画 -->
                <div class="scan-line"></div>
              </div>
            </div>

            <div class="qr-timer">
              <span class="timer-dot"></span>
              请在 <b>{{ countdown }}</b> 秒内完成支付
            </div>

            <el-button type="danger" size="large" @click="confirmPay" :loading="confirming">确认支付</el-button>
            <p class="hint">课程模拟支付演示 · 点击按钮即完成支付</p>
          </div>
        </div>

        <el-button type="danger" size="large" class="pay-btn" @click="startPay" v-if="!paying" :disabled="!method">
          确认支付 &yen;{{ orderData.payableAmount }}
        </el-button>

        <div class="pay-footer">
          <el-button @click="$router.push('/orders')">查看订单</el-button>
          <el-button link @click="$router.push('/')">返回首页</el-button>
        </div>
      </div>
    </div>
    <div class="page-wrap" v-else-if="loading">
      <EmptyState title="加载中..." description="正在获取订单信息" />
    </div>
    <div class="page-wrap" v-else>
      <EmptyState title="订单不存在" description="该订单信息未找到，可能已被删除。">
        <el-button type="danger" @click="$router.push('/orders')">查看我的订单</el-button>
      </EmptyState>
    </div>
  </ShopLayout>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { api } from '@/api'
import ShopLayout from '../layouts/ShopLayout.vue'
import EmptyState from '../components/EmptyState.vue'

const route = useRoute(), router = useRouter()
const order = ref<any>(null), loading = ref(true)
const method = ref('ALIPAY')
const paying = ref(false), confirming = ref(false)
const countdown = ref(300)
let timer: any = null

const orderData = computed(() => {
  const o = order.value?.order
  return {
    orderNo: o?.orderNo || '',
    payableAmount: o?.totalAmount || '0.00'
  }
})

function startPay() {
  paying.value = true
  countdown.value = 300
  timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
      paying.value = false
      ElMessage.warning('支付超时，请重新选择支付方式')
    }
  }, 1000)
}

async function confirmPay() {
  confirming.value = true
  try {
    const orderId = route.params.id
    await api.put(`/orders/${orderId}/pay`)
    await api.put(`/orders/${orderId}/pay-gateway`, { method: method.value })
    clearInterval(timer)
    ElMessage.success('支付成功！')
    router.push(`/orders?status=PAID`)
  } catch {
    ElMessage.error('支付失败，请重试')
  } finally {
    confirming.value = false
  }
}

onMounted(async () => {
  try {
    order.value = (await api.get(`/orders/${route.params.id}`)).data.data
  } catch { /* order not found */ }
  loading.value = false
})

onBeforeUnmount(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.pay-page {
  display: flex;
  justify-content: center;
  padding-top: 20px;
}
.pay-card {
  width: min(620px, 100%);
  padding: 32px;
  background: #fff;
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
  text-align: center;
}
.pay-header h1 { font-size: 22px; margin: 12px 0 0; }
.order-info {
  display: grid; gap: 10px; margin: 20px 0;
  padding: 16px; background: #f8fafc; border-radius: var(--radius); text-align: left;
}
.order-info div { display: flex; justify-content: space-between; align-items: center; }
.order-info b { font-size: 26px; }

.payment-methods {
  display: grid; gap: 12px; margin: 16px 0; text-align: left;
}
.pay-method {
  display: flex; align-items: center; gap: 12px;
  padding: 14px 16px; cursor: pointer;
  border: 2px solid var(--line); border-radius: var(--radius);
  transition: border-color .2s;
}
.pay-method.active { border-color: var(--brand); background: var(--brand-light); }
.pay-method input { display: none; }
.pay-icon { font-size: 28px; }
.pay-method strong { display: block; font-size: 15px; }
.pay-method small { color: var(--muted); font-size: 12px; }

.pay-qrcode { margin: 20px 0; }
.qrcode-box { padding: 24px; background: #f8fafc; border-radius: var(--radius); }
.qrcode-icon { font-size: 48px; display: block; margin-bottom: 8px; }
.scan-area {
  width: 200px; height: 200px; margin: 18px auto 0;
  display: grid; place-items: center;
  background: #fff; border: 2px solid var(--line); border-radius: var(--radius);
  position: relative;
}

.qr-code {
  width: 170px; height: 170px; position: relative;
}
.qr-svg {
  width: 100%; height: 100%;
}
.scan-line {
  position: absolute; left: 8px; right: 8px; height: 2px;
  background: linear-gradient(90deg, transparent, #e60023, transparent);
  box-shadow: 0 0 8px rgba(230, 0, 35, .5);
  border-radius: 1px;
  animation: scanMove 2.5s ease-in-out infinite;
}
@keyframes scanMove {
  0% { top: 8px; }
  50% { top: calc(100% - 10px); }
  100% { top: 8px; }
}

.qr-timer {
  margin: 14px 0 8px;
  display: flex; align-items: center; justify-content: center; gap: 6px;
  font-size: 13px; color: var(--muted);
}
.qr-timer b {
  color: var(--brand); font-size: 16px; min-width: 24px; text-align: center;
}
.timer-dot {
  display: inline-block; width: 8px; height: 8px;
  background: var(--success); border-radius: 50%;
  animation: pulse 1s ease-in-out infinite;
}
@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: .3; }
}
.hint { color: var(--muted); font-size: 13px; margin-top: 12px; }

.pay-btn { width: 100%; height: 48px; font-size: 17px; font-weight: 700; margin-top: 8px; }
.pay-footer { display: flex; justify-content: center; gap: 12px; margin-top: 20px; }
</style>
