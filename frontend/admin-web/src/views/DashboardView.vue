<script setup lang="ts">
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'
import { fetchDashboard } from '@/api/dashboard'

const stats = ref<any>({})
const trendRef = ref<HTMLElement>()
const hotRef = ref<HTMLElement>()
const statusRef = ref<HTMLElement>()

async function load() {
  try { const res: any = await fetchDashboard(); stats.value = res.data ?? res } catch { /* handled */ }
}

function renderCharts() {
  if (trendRef.value && stats.value.salesTrend) {
    const c = echarts.init(trendRef.value)
    c.setOption({ tooltip:{trigger:'axis'}, xAxis:{type:'category',data:(stats.value.salesTrend||[]).map((t:any)=>t.day).reverse()}, yAxis:{type:'value'}, series:[{data:(stats.value.salesTrend||[]).map((t:any)=>t.amount).reverse(),type:'line',smooth:true,color:'#ff6b35',areaStyle:{color:'rgba(255,107,53,0.1)'}}] })
  }
  if (hotRef.value && stats.value.hotProducts) {
    const c = echarts.init(hotRef.value)
    c.setOption({ tooltip:{trigger:'axis'}, xAxis:{type:'category',data:(stats.value.hotProducts||[]).map((p:any)=>p.name?.slice(0,8))}, yAxis:{type:'value'}, series:[{data:(stats.value.hotProducts||[]).map((p:any)=>p.sales),type:'bar',color:'#ff8c5a'}] })
  }
  if (statusRef.value && stats.value.orderStatus) {
    const c = echarts.init(statusRef.value)
    c.setOption({ tooltip:{trigger:'item'}, series:[{type:'pie',radius:['40%','70%'],data:(stats.value.orderStatus||[]).map((s:any)=>({name:s.status,value:s.value})),color:['#f59e0b','#3b82f6','#8b5cf6','#22c55e','#ef4444','#6b7280']}] })
  }
}

onMounted(async () => { await load(); setTimeout(renderCharts, 200) })
</script>
<template>
  <div>
    <h2 class="dv-title">数据看板</h2>
    <div class="stat-grid">
      <div class="stat-card" v-for="s in [{v:stats.userCount,l:'用户数'},{v:stats.orderCount,l:'订单数'},{v:'¥'+(stats.salesAmount||0).toFixed(0),l:'销售额'},{v:stats.todayOrderCount,l:'今日订单'},{v:'¥'+(stats.todaySalesAmount||0).toFixed(0),l:'今日销售'},{v:stats.productCount,l:'商品数'}]" :key="s.l">
        <div class="stat-val">{{ s.v || 0 }}</div><div class="stat-label">{{ s.l }}</div>
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
.dv-title { font-size: 20px; font-weight: 700; margin-bottom: 20px; }
.stat-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; margin-bottom: 24px; }
.stat-card { background: #fff; border-radius: 12px; padding: 24px; text-align: center; box-shadow: 0 1px 4px rgba(0,0,0,0.04); }
.stat-val { font-size: 28px; font-weight: 700; color: var(--color-primary); }
.stat-label { font-size: 13px; color: #888; margin-top: 4px; }
.chart-row { display: flex; gap: 16px; margin-bottom: 16px; }
.chart-box { flex: 1; background: #fff; border-radius: 12px; padding: 16px; box-shadow: 0 1px 4px rgba(0,0,0,0.04); }
.chart-box h4 { font-size: 14px; font-weight: 600; margin-bottom: 8px; color: #666; }
@media (max-width: 900px) { .stat-grid { grid-template-columns: repeat(2, 1fr); } .chart-row { flex-direction: column; } }
</style>
