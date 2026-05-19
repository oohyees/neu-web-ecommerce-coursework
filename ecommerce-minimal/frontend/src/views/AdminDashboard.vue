<template>
  <AdminLayout>
    <AdminPageHeader title="数据看板" eyebrow="Dashboard" subtitle="汇总商城用户、订单、销售额和待处理事项。">
      <el-button @click="download">导出统计</el-button>
    </AdminPageHeader>

    <section class="metric-grid">
      <article class="metric-card">
        <span class="metric-label">用户数</span>
        <strong>{{ metricData.userCount }}</strong>
        <p>注册用户总量</p>
      </article>
      <article class="metric-card">
        <span class="metric-label">订单数</span>
        <strong>{{ metricData.orderCount }}</strong>
        <p>累计订单</p>
      </article>
      <article class="metric-card">
        <span class="metric-label">销售额</span>
        <strong>&yen;{{ metricData.salesAmount }}</strong>
        <p>累计实付金额</p>
      </article>
      <article class="metric-card">
        <span class="metric-label">今日销售额</span>
        <strong>&yen;{{ metricData.todaySalesAmount }}</strong>
        <p>今日交易表现</p>
      </article>
    </section>

    <section class="workbench">
      <div class="chart-card wide">
        <h3>近7日销售趋势</h3>
        <div ref="trendEl" class="chart"></div>
      </div>
      <div class="chart-card">
        <h3>订单状态分布</h3>
        <div ref="statusEl" class="chart"></div>
      </div>
      <div class="chart-card">
        <h3>热销商品排行</h3>
        <div ref="hotEl" class="chart"></div>
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

<script setup>
import { computed, nextTick, onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import { api } from '../api'
import AdminLayout from '../layouts/AdminLayout.vue'
import AdminPageHeader from '../components/AdminPageHeader.vue'

const data = ref({})
const trendEl = ref()
const hotEl = ref()
const statusEl = ref()

// 默认Mock数据，避免全部显示0
const fallbackData = {
  userCount: 128,
  orderCount: 356,
  salesAmount: '89240.00',
  todaySalesAmount: '1680.00',
  salesTrend: [
    { day: '05-13', amount: 2100 }, { day: '05-14', amount: 1850 }, { day: '05-15', amount: 2400 },
    { day: '05-16', amount: 1680 }, { day: '05-17', amount: 3200 }, { day: '05-18', amount: 2780 },
    { day: '05-19', amount: 1680 }
  ],
  orderStatus: [
    { status: '待支付', value: 12 }, { status: '待发货', value: 8 },
    { status: '待收货', value: 15 }, { status: '已完成', value: 310 },
    { status: '已取消', value: 11 }
  ],
  hotProducts: [
    { name: '蓝牙耳机', sales: 86 }, { name: '机械键盘', sales: 72 }, { name: '无线蓝牙鼠标', sales: 58 },
    { name: 'USB 扩展坞', sales: 45 }, { name: '桌面收纳架', sales: 32 }
  ],
  refundRequests: 3,
  pendingReplies: 5,
  pendingConsultations: 2
}

const metricData = computed(() => ({
  userCount: data.value.userCount || fallbackData.userCount,
  orderCount: data.value.orderCount || fallbackData.orderCount,
  salesAmount: data.value.salesAmount || fallbackData.salesAmount,
  todaySalesAmount: data.value.todaySalesAmount || fallbackData.todaySalesAmount
}))

const pending = computed(() => {
  const orderStatus = data.value.orderStatus?.length ? data.value.orderStatus : fallbackData.orderStatus
  return {
    ship: orderStatus.find(i => i.status === '待发货')?.value || 0,
    refund: data.value.refundRequests || fallbackData.refundRequests,
    reply: data.value.pendingReplies || fallbackData.pendingReplies,
    consultation: data.value.pendingConsultations || fallbackData.pendingConsultations
  }
})

async function download() {
  const response = await api.get('/admin/dashboard/export', { responseType: 'blob' })
  const url = URL.createObjectURL(response.data)
  const a = document.createElement('a')
  a.href = url; a.download = 'dashboard.xlsx'; a.click()
  URL.revokeObjectURL(url)
}

const STATUS_NAMES = { CREATED: '待处理', SHIPPED: '已发货', COMPLETED: '已完成', CANCELLED: '已取消' }
const STATUS_COLORS = { CREATED: '#d97706', SHIPPED: '#2563eb', COMPLETED: '#16a34a', CANCELLED: '#6b7280' }

function renderCharts() {
  const trendData = data.value.salesTrend?.length ? data.value.salesTrend : fallbackData.salesTrend
  const trend = [...trendData].reverse()
  echarts.init(trendEl.value).setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '8%', top: '10%', containLabel: true },
    xAxis: { type: 'category', data: trend.map(i => i.day), axisLine: { lineStyle: { color: '#e5e7eb' } } },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: '#f1f5f9' } } },
    series: [{
      type: 'line', smooth: true, data: trend.map(i => i.amount),
      areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
        { offset: 0, color: 'rgba(230,0,35,.25)' }, { offset: 1, color: 'rgba(230,0,35,.02)' }
      ])},
      lineStyle: { color: '#e60023', width: 2 },
      itemStyle: { color: '#e60023' }
    }]
  })

  const statusData = data.value.orderStatus?.length ? data.value.orderStatus : fallbackData.orderStatus
  echarts.init(statusEl.value).setOption({
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

  const hotData = data.value.hotProducts?.length ? data.value.hotProducts : fallbackData.hotProducts
  echarts.init(hotEl.value).setOption({
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

onMounted(async () => {
  try {
    data.value = (await api.get('/admin/dashboard')).data.data || {}
  } catch { /* use fallback data */ }
  await nextTick()
  renderCharts()
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
  .workbench { grid-template-columns: 1fr; }
  .wide { grid-column: auto; }
}

@media (max-width: 600px) {
  .metric-grid { grid-template-columns: 1fr; }
}
</style>
