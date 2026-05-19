<template>
  <ShopLayout>
    <div class="page-wrap pay-page" v-if="order">
      <div class="pay-card">
        <div class="pay-header">
          <el-icon :size="48" color="#16a34a">✓</el-icon>
          <h1>订单已生成，请完成支付</h1>
        </div>

        <div class="order-info">
          <div><span>订单号</span><strong>{{ order.order.orderNo }}</strong></div>
          <div><span>应付金额</span><b class="price">&yen;{{ order.order.totalAmount }}</b></div>
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
            <p class="price">应付金额：&yen;{{ order.order.totalAmount }}</p>
            <div class="scan-area">
              <div class="qr-placeholder">
                <span>扫码支付模拟</span>
                <small>课程演示用途</small>
              </div>
            </div>
            <el-button type="danger" size="large" @click="confirmPay" :loading="confirming">确认支付</el-button>
            <p class="hint">此为模拟支付，点击"确认支付"即可完成订单</p>
          </div>
        </div>

        <el-button type="danger" size="large" class="pay-btn" @click="startPay" v-if="!paying" :disabled="!method">
          确认支付 &yen;{{ order.order.totalAmount }}
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

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { api } from '../api'
import ShopLayout from '../layouts/ShopLayout.vue'
import EmptyState from '../components/EmptyState.vue'

const route = useRoute(), router = useRouter()
const order = ref(null), loading = ref(true)
const method = ref('ALIPAY')
const paying = ref(false), confirming = ref(false)

function startPay() { paying.value = true }

async function confirmPay() {
  confirming.value = true
  try {
    const orderId = route.params.id
    await api.put(`/orders/${orderId}/pay`)
    // 记录支付方式
    await api.put(`/orders/${orderId}/pay-gateway`, { method: method.value })
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
  width: 180px; height: 180px; margin: 16px auto;
  display: grid; place-items: center;
  background: #fff; border: 2px dashed var(--line); border-radius: var(--radius);
}
.qr-placeholder { text-align: center; color: var(--muted); }
.qr-placeholder small { display: block; margin-top: 6px; font-size: 12px; }
.hint { color: var(--muted); font-size: 13px; margin-top: 12px; }

.pay-btn { width: 100%; height: 48px; font-size: 17px; font-weight: 700; margin-top: 8px; }
.pay-footer { display: flex; justify-content: center; gap: 12px; margin-top: 20px; }
</style>
