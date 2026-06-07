<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { fetchAnnouncements, fetchActivityNotices } from '@/api/notice'

const announcements = ref<any[]>([])
const notices = ref<any[]>([])

onMounted(async () => {
  try {
    const [a, n] = await Promise.all([fetchAnnouncements(), fetchActivityNotices()])
    announcements.value = (a as any).data ?? []
    notices.value = (n as any).data ?? []
  } catch { /* handled */ }
})
</script>

<template>
  <div class="page-container notice-page">
    <div class="page-intro">
      <div>
        <h2 class="page-title">公告活动</h2>
        <p>集中展示平台公告和活动通知，和后台内容管理页面联动。</p>
      </div>
    </div>
    <div class="page-metrics">
      <div class="metric-card"><span>公告</span><strong>{{ announcements.length }}</strong><small>平台通知</small></div>
      <div class="metric-card"><span>活动</span><strong>{{ notices.length }}</strong><small>运营内容</small></div>
      <div class="metric-card"><span>总计</span><strong>{{ announcements.length + notices.length }}</strong><small>当前内容</small></div>
    </div>
    <div v-if="notices.length" class="section">
      <h3>活动通知</h3>
      <div v-for="n in notices" :key="n.id" class="item">
        <h4>{{ n.title }}</h4>
        <p>{{ n.content }}</p>
      </div>
    </div>
    <div v-if="announcements.length" class="section">
      <h3>平台公告</h3>
      <div v-for="a in announcements" :key="a.id" class="item">
        <h4>{{ a.title }}</h4>
        <p>{{ a.content }}</p>
      </div>
    </div>
    <div v-if="!notices.length && !announcements.length" class="empty"><el-empty description="暂无公告" /></div>
  </div>
</template>
<style scoped>
.notice-page { max-width: 700px; }
.page-title { font-size: 22px; font-weight: 700; margin-bottom: 20px; }
.section { margin-bottom: 24px; }
.section h3 { font-size: 16px; font-weight: 600; margin-bottom: 12px; }
.item { background: #fff; border-radius: 10px; padding: 16px; margin-bottom: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.04); }
.item h4 { font-size: 15px; font-weight: 600; margin-bottom: 4px; }
.item p { font-size: 13px; color: #666; }
</style>
