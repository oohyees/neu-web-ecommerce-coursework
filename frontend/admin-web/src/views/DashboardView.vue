<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { fetchDashboard } from '@/api/dashboard'

const stats = ref<any>({})
const trendRef = ref<HTMLElement>()
const hotRef = ref<HTMLElement>()
const statusRef = ref<HTMLElement>()
const chartInstances: echarts.ECharts[] = []

async function load() {
  try { const res: any = await fetchDashboard(); stats.value = res.data ?? res } catch { /* handled */ }
}

function renderCharts() {
  if (trendRef.value && stats.value.salesTrend) {
    const c = echarts.init(trendRef.value)
    chartInstances.push(c)
    c.setOption({ tooltip:{trigger:'axis'}, xAxis:{type:'category',data:(stats.value.salesTrend||[]).map((t:any)=>t.day).reverse()}, yAxis:{type:'value'}, series:[{data:(stats.value.salesTrend||[]).map((t:any)=>t.amount).reverse(),type:'line',smooth:true,color:'#ff6b35',areaStyle:{color:'rgba(255,107,53,0.1)'}}] })
  }
  if (hotRef.value && stats.value.hotProducts) {
    const c = echarts.init(hotRef.value)
    chartInstances.push(c)
    c.setOption({ tooltip:{trigger:'axis'}, xAxis:{type:'category',data:(stats.value.hotProducts||[]).map((p:any)=>p.name?.slice(0,8))}, yAxis:{type:'value'}, series:[{data:(stats.value.hotProducts||[]).map((p:any)=>p.sales),type:'bar',color:'#ff8c5a'}] })
  }
  if (statusRef.value && stats.value.orderStatus) {
    const c = echarts.init(statusRef.value)
    chartInstances.push(c)
    c.setOption({ tooltip:{trigger:'item'}, series:[{type:'pie',radius:['40%','70%'],data:(stats.value.orderStatus||[]).map((s:any)=>({name:s.status,value:s.value})),color:['#f59e0b','#3b82f6','#8b5cf6','#22c55e','#ef4444','#6b7280']}] })
  }
}

onMounted(async () => { await load(); setTimeout(renderCharts, 200) })
onUnmounted(() => { chartInstances.forEach(c => c.dispose()); chartInstances.length = 0 })
</script>
<template>
  <div class="admin-page dashboard-page">
    <div class="tb-header">
      <div>
        <h2>数据看板</h2>
        <p class="page-subtitle">集中查看用户、订单、销售额和商品表现，是后台演示的第一屏。</p>
      </div>
      <div class="dashboard-date">今日运营概览</div>
    </div>
    <div class="stat-grid">
      <div class="stat-card" v-for="s in [{v:stats.userCount,l:'用户数',h:'注册用户规模'},{v:stats.orderCount,l:'订单数',h:'交易闭环证据'},{v:'¥'+(stats.salesAmount||0).toFixed(0),l:'销售额',h:'平台累计收入'},{v:stats.todayOrderCount,l:'今日订单',h:'实时运营状态'},{v:'¥'+(stats.todaySalesAmount||0).toFixed(0),l:'今日销售',h:'今日成交金额'},{v:stats.productCount,l:'商品数',h:'可售商品规模'}]" :key="s.l">
        <div class="stat-label">{{ s.l }}</div>
        <div class="stat-val">{{ s.v || 0 }}</div>
        <div class="stat-hint">{{ s.h }}</div>
      </div>
    </div>
    <div class="chart-row">
      <div class="chart-box"><h4>销量趋势</h4><div ref="trendRef" style="width:100%;height:280px" /></div>
      <div class="chart-box"><h4>热销排行</h4><div ref="hotRef" style="width:100%;height:280px" /></div>
    </div>
    <div class="chart-row">
      <div class="chart-box"><h4>订单状态分布</h4><div ref="statusRef" style="width:100%;height:280px" /></div>
    </div>
  </div>
</template>
<style scoped>
.page-subtitle { margin-top: 8px; color: #6b7280; font-size: 13px; }
.dashboard-date { align-self: center; padding: 8px 12px; border-radius: 999px; background: #fff5f0; color: var(--color-primary); font-size: 13px; font-weight: 700; }
.stat-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; margin-bottom: 24px; }
.stat-card { background: #fff; border-radius: 8px; padding: 22px 24px; box-shadow: var(--shadow-card); border: 1px solid var(--color-line); }
.stat-val { margin-top: 8px; font-size: 30px; font-weight: 800; color: #111827; }
.stat-label { font-size: 13px; color: #6b7280; }
.stat-hint { margin-top: 4px; color: #9ca3af; font-size: 12px; }
.chart-row { display: flex; gap: 16px; margin-bottom: 16px; }
.chart-box { flex: 1; background: #fff; border-radius: 8px; padding: 18px; box-shadow: var(--shadow-card); border: 1px solid var(--color-line); }
.chart-box h4 { font-size: 15px; font-weight: 800; margin-bottom: 8px; color: #1f2937; }
@media (max-width: 900px) { .stat-grid { grid-template-columns: repeat(2, 1fr); } .chart-row { flex-direction: column; } }
@media (max-width: 560px) { .stat-grid { grid-template-columns: 1fr; } }
</style>
