<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchAllConsultations, replyConsultation, markConsultationProcessed } from '@/api/cs'

const items = ref<any[]>([]); const total = ref(0); const page = ref(1)
const replyForm = ref({ id: 0, reply: '' }); const replyVisible = ref(false)

async function load() {
  try { const res: any = await fetchAllConsultations({ page: page.value, size: 10 }); items.value = res.data?.items ?? []; total.value = res.data?.total ?? 0 } catch { items.value = [] }
}
function openReply(item: any) { replyForm.value = { id: item.id, reply: '' }; replyVisible.value = true }
async function handleReply() { try { await replyConsultation(replyForm.value); ElMessage.success('已回复'); replyVisible.value = false; load() } catch { /* handled */ } }
async function handleProcessed(id: number) { try { await markConsultationProcessed(id); ElMessage.success('已标记处理'); load() } catch { /* handled */ } }
const repliedCount = computed(() => items.value.filter((i) => i.status === 'REPLIED' || i.reply).length)
const pendingCount = computed(() => items.value.length - repliedCount.value)
onMounted(load)
</script>
<template>
  <div class="admin-page cs-page">
    <div class="tb-header"><div><h2>客服咨询</h2><p class="page-subtitle">管理用户售前售后咨询，展示后台服务能力。</p></div></div>
    <div class="admin-summary">
      <div class="summary-card"><div class="summary-card__label">当前页咨询</div><div class="summary-card__value">{{ items.length }}</div><div class="summary-card__hint">用户咨询记录</div></div>
      <div class="summary-card"><div class="summary-card__label">已回复</div><div class="summary-card__value">{{ repliedCount }}</div><div class="summary-card__hint">客服闭环</div></div>
      <div class="summary-card"><div class="summary-card__label">待处理</div><div class="summary-card__value">{{ pendingCount }}</div><div class="summary-card__hint">需要回复</div></div>
      <div class="summary-card"><div class="summary-card__label">总记录</div><div class="summary-card__value">{{ total }}</div><div class="summary-card__hint">分页统计</div></div>
    </div>
    <div class="table-panel">
    <el-table :data="items" stripe>
      <el-table-column prop="userId" label="用户ID" width="80" />
      <el-table-column prop="subject" label="主题" width="140" show-overflow-tooltip />
      <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip />
      <el-table-column prop="reply" label="回复" min-width="150" show-overflow-tooltip />
      <el-table-column label="状态" width="110"><template #default="{row}"><span :class="['status-pill', row.status==='REPLIED' || row.reply ? 'status-pill--success' : 'status-pill--warning']">{{row.status}}</span></template></el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{row}">
          <div class="action-stack"><el-button size="small" @click="openReply(row)">{{row.reply?'修改':'回复'}}</el-button>
          <el-button v-if="row.status!=='REPLIED'" size="small" type="success" @click="handleProcessed(row.id)">处理</el-button></div>
        </template>
      </el-table-column>
    </el-table>
    <div class="table-panel__footer"><el-pagination v-model:current-page="page" :total="total" :page-size="10" layout="prev,pager,next" @change="load" /></div>
    </div>
    <el-dialog v-model="replyVisible" title="回复咨询" width="400px">
      <el-input v-model="replyForm.reply" type="textarea" :rows="4" placeholder="输入回复..." />
      <template #footer><el-button @click="replyVisible=false">取消</el-button><el-button type="primary" @click="handleReply">回复</el-button></template>
    </el-dialog>
  </div>
</template>
<style scoped>.page-subtitle { margin-top: 8px; color: #6b7280; font-size: 13px; }</style>
