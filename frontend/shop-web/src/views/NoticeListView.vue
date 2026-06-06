<template>
  <ShopLayout>
    <div class="page-wrap">
      <div class="page-head">
        <h1>活动与公告</h1>
        <p class="muted">查看商城最新活动和系统公告</p>
      </div>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="系统公告" name="announcements" />
        <el-tab-pane label="活动通知" name="notices" />
      </el-tabs>

      <section v-if="activeTab === 'announcements'">
        <article v-if="announcements.length" class="notice-list">
          <div v-for="a in announcements" :key="a.id" class="notice-card">
            <strong>{{ a.title }}</strong>
            <p>{{ a.content }}</p>
            <span class="muted">{{ a.createdAt?.slice(0, 10) }}</span>
          </div>
        </article>
        <EmptyState v-else title="暂无公告" description="平台暂未发布公告。" />
      </section>

      <section v-else>
        <article v-if="notices.length" class="notice-list">
          <div v-for="n in notices" :key="n.id" class="notice-card">
            <strong><el-tag size="small" type="danger" style="margin-right:8px">{{ n.type || '活动' }}</el-tag>{{ n.title }}</strong>
            <p>{{ n.content }}</p>
            <span class="muted">{{ n.createdAt?.slice(0, 10) }}</span>
          </div>
        </article>
        <EmptyState v-else title="暂无活动通知" description="敬请期待更多精彩活动。" />
      </section>
    </div>
  </ShopLayout>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import type { Announcement, ActivityNotice } from '@/types'
import { getAnnouncements, getActivityNotices } from '@/api/marketing'
import ShopLayout from '@/layouts/ShopLayout.vue'
import EmptyState from '@/components/EmptyState.vue'

const activeTab = ref('announcements')
const announcements = ref<Announcement[]>([])
const notices = ref<ActivityNotice[]>([])

onMounted(async () => {
  try {
    const [aRes, nRes] = await Promise.all([getAnnouncements(), getActivityNotices()])
    announcements.value = aRes.data || []
    notices.value = nRes.data || []
  } catch { /* empty */ }
})
</script>

<style scoped>
.page-head { margin-bottom: 16px; }
.page-head h1 { font-size: 22px; margin: 0 0 6px; }
.page-head p { font-size: 14px; }

.notice-list { display: grid; gap: 14px; }
.notice-card {
  padding: 18px;
  background: var(--panel);
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
}
.notice-card strong { display: flex; align-items: center; font-size: 16px; margin-bottom: 8px; }
.notice-card p { color: var(--muted); line-height: 1.7; margin: 0 0 8px; }
.notice-card span { font-size: 13px; }
</style>
