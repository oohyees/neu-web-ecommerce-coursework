<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchAllFeedback, replyFeedback, markFeedbackProcessed } from '@/api/feedback'

const items = ref<any[]>([]); const total = ref(0); const page = ref(1)
const replyForm = ref({ id: 0, reply: '' }); const replyVisible = ref(false)

async function load() {
  try { const res: any = await fetchAllFeedback({ page: page.value, size: 10 }); items.value = res.data?.items ?? []; total.value = res.data?.total ?? 0 } catch { items.value = [] }
}
function openReply(item: any) { replyForm.value = { id: item.id, reply: '' }; replyVisible.value = true }
async function handleReply() {
  try { await replyFeedback(replyForm.value); ElMessage.success('已回复'); replyVisible.value = false; load() } catch { /* handled */ }
}
async function handleProcessed(id: number) { try { await markFeedbackProcessed(id); ElMessage.success('已标记处理'); load() } catch { /* handled */ } }
onMounted(load)
</script>
<template>
  <div>
    <div class="tb-header"><h2>反馈管理</h2></div>
    <el-table :data="items" stripe>
      <el-table-column prop="userId" label="用户ID" width="80" />
      <el-table-column prop="type" label="类型" width="80" />
      <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip />
      <el-table-column prop="reply" label="回复" min-width="150" show-overflow-tooltip />
      <el-table-column label="状态" width="80"><template #default="{row}"><el-tag :type="row.status==='REPLIED'?'success':'warning'" size="small">{{row.status}}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{row}">
          <el-button text size="small" @click="openReply(row)">{{row.reply?'修改回复':'回复'}}</el-button>
          <el-button v-if="row.status!=='REPLIED'" text size="small" type="success" @click="handleProcessed(row.id)">标记处理</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="page" :total="total" :page-size="10" layout="prev,pager,next" style="margin-top:16px;justify-content:flex-end" @change="load" />
    <el-dialog v-model="replyVisible" title="回复反馈" width="400px">
      <el-input v-model="replyForm.reply" type="textarea" :rows="4" placeholder="输入回复内容..." />
      <template #footer><el-button @click="replyVisible=false">取消</el-button><el-button type="primary" @click="handleReply">回复</el-button></template>
    </el-dialog>
  </div>
</template>
<style scoped>.tb-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; } .tb-header h2 { font-size: 20px; font-weight: 700; }</style>
