<template>
  <AdminLayout>
    <AdminPageHeader title="数据看板" eyebrow="Dashboard" subtitle="汇总商城用户、订单、销售额和待处理事项。">
      <el-button @click="download" :loading="downloading">导出统计</el-button>
    </AdminPageHeader>

    <section class="metric-grid">
      <article class="metric-card">
        <span class="metric-label">用户数</span>
        <strong>{{ metricData.userCount ?? '-' }}</strong>
        <p>注册用户总量</p>
      </article>
      <article class="metric-card">
        <span class="metric-label">订单数</span>
        <strong>{{ metricData.orderCount ?? '-' }}</strong>
        <p>累计订单</p>
      </article>
      <article class="metric-card">
        <span class="metric-label">销售额</span>
        <strong>&yen;{{ metricData.salesAmount ?? '-' }}</strong>
        <p>累计实付金额</p>
      </article>
      <article class="metric-card">
        <span class="metric-label">今日销售额</span>
        <strong>&yen;{{ metricData.todaySalesAmount ?? '-' }}</strong>
        <p>今日交易表现</p>
      </article>
    </section>

    <section class="quick-card">
      <div class="quick-head">
        <h3>验收快捷操作</h3>
        <span>商品、订单、用户、内容营销和导入导出入口集中展示</span>
      </div>
      <div class="quick-grid">
        <button v-for="item in quickActions" :key="item.path" type="button" @click="goQuick(item)">
          <span>{{ item.icon }}</span>
          <strong>{{ item.label }}</strong>
          <em>{{ item.desc }}</em>
        </button>
      </div>
    </section>

    <section class="workbench">
      <div class="chart-card wide">
        <h3>近7日销售趋势</h3>
        <div ref="trendEl" class="chart" v-show="hasTrendData"></div>
        <div v-if="!hasTrendData" class="chart-placeholder">{{ dataReady ? '暂无销售数据，创建订单后将自动生成趋势图' : '数据加载中...' }}</div>
      </div>
      <div class="chart-card">
        <h3>订单状态分布</h3>
        <div ref="statusEl" class="chart" v-show="hasStatusData"></div>
        <div v-if="!hasStatusData" class="chart-placeholder">{{ dataReady ? '暂无订单数据，有订单后将自动展示分布' : '数据加载中...' }}</div>
      </div>
      <div class="chart-card">
        <h3>热销商品排行</h3>
        <div ref="hotEl" class="chart" v-show="hasHotData"></div>
        <div v-if="!hasHotData" class="chart-placeholder">{{ dataReady ? '暂无商品排行数据' : '数据加载中...' }}</div>
      </div>
      <aside class="todo-card">
        <h3>待处理事项</h3>
        <div><span>待发货</span><strong>{{ pending.ship }}</strong></div>
        <div><span>待退款</span><strong>{{ pending.refund }}</strong></div>
        <div><span>待回复反馈</span><strong>{{ pending.reply }}</strong></div>
        <div><span>待处理咨询</span><strong>{{ pending.consultation }}</strong></div>
      </aside>
    </section>
  </AdminLayout>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { api } from '@/api'
import { useAdminStore } from '@/stores/admin'
import AdminLayout from '../layouts/AdminLayout.vue'
import AdminPageHeader from '../components/AdminPageHeader.vue'

const data = ref<any>({})
const dataReady = ref(false)
const downloading = ref(false)
const trendEl = ref()
const hotEl = ref()
const statusEl = ref()
const router = useRouter()
const adminStore = useAdminStore()

const quickActions = [
  { label: '商品管理', desc: '新增编辑、库存价格', icon: '品', path: '/admin/products' },
  { label: '订单管理', desc: '发货退款、状态筛选', icon: '单', path: '/admin/orders' },
  { label: '用户管理', desc: '启用禁用、用户详情', icon: '用', path: '/admin/users', superOnly: true },
  { label: '轮播管理', desc: '首页广告和排序', icon: '播', path: '/admin/banners' },
  { label: '公告管理', desc: '系统公告发布', icon: '告', path: '/admin/announcements', superOnly: true },
  { label: '客服咨询', desc: '咨询处理和回复', icon: '客', path: '/admin/consultations' },
  { label: '反馈管理', desc: '用户建议处理', icon: '馈', path: '/admin/feedback' },
  { label: '导入导出', desc: '商品批量和统计证据', icon: '导', path: '/admin/import-export' },
]

const metricData = computed(() => ({
  userCount: data.value.userCount,
  orderCount: data.value.orderCount,
  salesAmount: data.value.salesAmount,
  todaySalesAmount: data.value.todaySalesAmount
}))

const hasTrendData = computed(() => data.value.salesTrend && data.value.salesTrend.length > 0)
const hasStatusData = computed(() => data.value.orderStatus && data.value.orderStatus.length > 0)
const hasHotData = computed(() => data.value.hotProducts && data.value.hotProducts.length > 0)

const pending = computed(() => {
  const orderStatus = data.value.orderStatus || []
  return {
    ship: orderStatus.find(i => i.status === 'PAID' || i.status === '待发货')?.value || 0,
    refund: data.value.refundRequests || 0,
    reply: data.value.pendingReplies || 0,
    consultation: data.value.pendingConsultations || 0
  }
})

async function download() {
  try {
    downloading.value = true
    const response = await api.get('/admin/dashboard/export', { responseType: 'blob' })
    const url = URL.createObjectURL(response.data)
    const a = document.createElement('a')
    a.href = url; a.download = 'dashboard.xlsx'; a.click()
    URL.revokeObjectURL(url)
  } catch {
    ElMessage.error('导出失败，请稍后重试')
  } finally {
    downloading.value = false
  }
}

function goQuick(item: any) {
  if (item.superOnly && adminStore.role !== 'SUPER_ADMIN') {
    ElMessage.warning('仅超级管理员可访问该页面')
    return
  }
  router.push(item.path)
}

const STATUS_NAMES = { CREATED: '待处理', PAID: '已支付', SHIPPED: '已发货', COMPLETED: '已完成', CANCELLED: '已取消' }
const STATUS_COLORS = { CREATED: '#d97706', PAID: '#2563eb', SHIPPED: '#6366f1', COMPLETED: '#16a34a', CANCELLED: '#6b7280' }

// 跟踪所有 ECharts 实例以便清理
const chartInstances: echarts.EChartsType[] = []

function getOrCreateChart(domRef) {
  const el = domRef.value
  if (!el) return null
  const existing = echarts.getInstanceByDom(el)
  if (existing) { existing.clear(); return existing }
  const instance = echarts.init(el)
  chartInstances.push(instance)
  return instance
}

function renderCharts() {
  if (!dataReady.value) return

  // 销售趋势
  const trendData = data.value.salesTrend
  if (trendData && trendData.length) {
    const trendChart = getOrCreateChart(trendEl)
    if (trendChart) {
      trendChart.setOption({
        tooltip: { trigger: 'axis' },
        grid: { left: '3%', right: '4%', bottom: '8%', top: '10%', containLabel: true },
        xAxis: { type: 'category', data: trendData.map(i => i.day), axisLine: { lineStyle: { color: '#e5e7eb' } } },
        yAxis: { type: 'value', splitLine: { lineStyle: { color: '#f1f5f9' } } },
        series: [{
          type: 'line', smooth: true, data: trendData.map(i => i.amount),
          areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(230,0,35,.25)' }, { offset: 1, color: 'rgba(230,0,35,.02)' }
          ])},
          lineStyle: { color: '#e60023', width: 2 },
          itemStyle: { color: '#e60023' }
        }]
      })
    }
  }

  // 订单状态分布
  const statusData = data.value.orderStatus
  if (statusData && statusData.length) {
    const statusChart = getOrCreateChart(statusEl)
    if (statusChart) {
      statusChart.setOption({
        tooltip: { trigger: 'item' },
        series: [{
          type: 'pie', radius: ['48%', '75%'], center: ['50%', '55%'],
          data: statusData.map(i => {
            const name = STATUS_NAMES[i.status] || i.status
            const color = STATUS_COLORS[i.status] || '#9ca3af'
            return { name, value: i.value, itemStyle: { color } }
          }),
          label: { fontSize: 12 }, emphasis: { label: { fontWeight: 'bold' } }
        }]
      })
    }
  }

  // 热销商品排行
  const hotData = data.value.hotProducts
  if (hotData && hotData.length) {
    const hotChart = getOrCreateChart(hotEl)
    if (hotChart) {
      hotChart.setOption({
        tooltip: { trigger: 'axis' },
        grid: { left: '3%', right: '10%', bottom: '8%', top: '10%', containLabel: true },
        xAxis: {
          type: 'category',
          data: hotData.map(i => i.name.length > 6 ? i.name.slice(0, 6) + '…' : i.name),
          axisLabel: { fontSize: 11 }
        },
        yAxis: { type: 'value', splitLine: { lineStyle: { color: '#f1f5f9' } } },
        series: [{
          type: 'bar', data: hotData.map(i => i.sales),
          itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#ff5c72' }, { offset: 1, color: '#e60023' }
          ]), borderRadius: [4, 4, 0, 0] },
          barWidth: '50%'
        }]
      })
    }
  }
}

function handleResize() {
  chartInstances.forEach(c => { try { c.resize() } catch { /* disposed */ } })
}

onMounted(async () => {
  try {
    const res = await api.get('/admin/dashboard')
    data.value = res.data.data || {}
    dataReady.value = true
  } catch {
    dataReady.value = false
  }
  await nextTick()
  renderCharts()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chartInstances.forEach(c => { try { c.dispose() } catch { /* already disposed */ } })
  chartInstances.length = 0
})
</script>

<style scoped>
.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.metric-card {
  padding: 20px;
  background: #fff;
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
}

.metric-card .metric-label,
.metric-card p {
  color: var(--muted);
  font-size: 13px;
  margin: 0;
}

.metric-card strong {
  display: block;
  margin: 8px 0 4px;
  font-size: 30px;
  font-weight: 800;
  color: var(--ink);
}

.metric-card p {
  margin-top: 2px;
}

.quick-card {
  padding: 18px;
  margin-bottom: 20px;
  background: #fff;
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
}

.quick-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 14px;
  margin-bottom: 14px;
}

.quick-head h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
}

.quick-head span {
  color: var(--muted);
  font-size: 13px;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.quick-grid button {
  display: grid;
  grid-template-columns: 38px minmax(0, 1fr);
  gap: 3px 10px;
  align-items: center;
  min-height: 76px;
  padding: 13px;
  text-align: left;
  cursor: pointer;
  background: #f8fafc;
  border: 1px solid #e5e7eb;
  border-radius: var(--radius);
  transition: border-color .18s ease, box-shadow .18s ease, transform .18s ease;
}

.quick-grid button:hover {
  border-color: #f2b8c2;
  box-shadow: var(--shadow-sm);
  transform: translateY(-1px);
}

.quick-grid button span {
  grid-row: span 2;
  display: grid;
  place-items: center;
  width: 38px;
  height: 38px;
  color: var(--brand);
  background: var(--brand-light);
  border-radius: 12px;
  font-weight: 800;
}

.quick-grid strong {
  min-width: 0;
  font-size: 14px;
  line-height: 1.2;
}

.quick-grid em {
  min-width: 0;
  color: var(--muted);
  font-size: 12px;
  font-style: normal;
  line-height: 1.25;
}

.workbench {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 16px;
}

.wide { grid-column: span 2; }

.chart-card, .todo-card {
  padding: 18px;
  background: #fff;
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
}

.chart-card h3, .todo-card h3 {
  margin: 0 0 14px;
  font-size: 16px;
  font-weight: 650;
}

.chart {
  height: 320px;
}

.chart-placeholder {
  height: 320px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--muted);
  font-size: 14px;
}

.todo-card {
  display: grid;
  gap: 12px;
  align-content: start;
}

.todo-card h3 { margin-bottom: 6px; }

.todo-card > div {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 14px;
  background: #f8fafc;
  border-radius: var(--radius);
}

.todo-card > div span {
  color: var(--muted);
  font-size: 14px;
}

.todo-card > div strong {
  color: var(--brand);
  font-size: 20px;
  font-weight: 800;
}

@media (max-width: 1100px) {
  .metric-grid { grid-template-columns: repeat(2, 1fr); }
  .quick-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .workbench { grid-template-columns: 1fr; }
  .wide { grid-column: auto; }
}

@media (max-width: 600px) {
  .metric-grid { grid-template-columns: 1fr; }
  .quick-head {
    align-items: flex-start;
    flex-direction: column;
  }
  .quick-grid { grid-template-columns: 1fr; }
}
</style>
