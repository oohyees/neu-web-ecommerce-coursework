<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchAnnouncements, createAnnouncement, updateAnnouncement, deleteAnnouncement, fetchAdminActivityNotices, createActivityNotice, updateActivityNotice, deleteActivityNotice } from '@/api/notice'

const announcements = ref<any[]>([]); const notices = ref<any[]>([])
const dialogVisible = ref(false); const editing = ref<any>(null); const tab = ref<'announcement'|'notice'>('announcement')
const form = ref({ title: '', content: '' })

function resetForm() { form.value = { title:'', content:'' }; editing.value = null }
function openAddAnn() { resetForm(); tab.value = 'announcement'; dialogVisible.value = true }
function openEditAnn(a: any) { editing.value = a; form.value = { title:a.title, content:a.content }; tab.value = 'announcement'; dialogVisible.value = true }
function openAddNot() { resetForm(); tab.value = 'notice'; dialogVisible.value = true }
function openEditNot(n: any) { editing.value = n; form.value = { title:n.title, content:n.content }; tab.value = 'notice'; dialogVisible.value = true }

async function loadAll() {
  try { const [a, n] = await Promise.all([fetchAnnouncements(), fetchAdminActivityNotices()]); announcements.value = (a as any).data ?? []; notices.value = (n as any).data ?? [] } catch { /* handled */ }
}
async function handleSave() {
  try {
    if (tab.value === 'announcement') { if (editing.value) await updateAnnouncement({ id:editing.value.id, ...form.value }); else await createAnnouncement(form.value) }
    else { if (editing.value) await updateActivityNotice({ id:editing.value.id, ...form.value }); else await createActivityNotice(form.value) }
    ElMessage.success('已保存'); dialogVisible.value = false; loadAll()
  } catch { /* handled */ }
}
async function handleDeleteAnn(id: number) { try { await ElMessageBox.confirm('确认删除？','提示',{type:'warning'}); await deleteAnnouncement(id); ElMessage.success('已删除'); loadAll() } catch { /* cancelled */ } }
async function handleDeleteNot(id: number) { try { await ElMessageBox.confirm('确认删除？','提示',{type:'warning'}); await deleteActivityNotice(id); ElMessage.success('已删除'); loadAll() } catch { /* cancelled */ } }
onMounted(loadAll)
</script>
<template>
  <div>
    <div class="tb-header"><h2>公告管理</h2></div>
    <el-tabs>
      <el-tab-pane label="公告">
        <el-button type="primary" size="small" @click="openAddAnn" style="margin-bottom:12px">发布公告</el-button>
        <el-table :data="announcements" stripe>
          <el-table-column prop="title" label="标题" /><el-table-column prop="content" label="内容" show-overflow-tooltip />
          <el-table-column label="操作" width="120"><template #default="{row}"><el-button text size="small" @click="openEditAnn(row)">编辑</el-button><el-button text size="small" type="danger" @click="handleDeleteAnn(row.id)">删除</el-button></template></el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="活动通知">
        <el-button type="primary" size="small" @click="openAddNot" style="margin-bottom:12px">发布通知</el-button>
        <el-table :data="notices" stripe>
          <el-table-column prop="title" label="标题" /><el-table-column prop="content" label="内容" show-overflow-tooltip />
          <el-table-column label="操作" width="120"><template #default="{row}"><el-button text size="small" @click="openEditNot(row)">编辑</el-button><el-button text size="small" type="danger" @click="handleDeleteNot(row.id)">删除</el-button></template></el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
    <el-dialog v-model="dialogVisible" :title="editing?'编辑':'新增'" width="500px">
      <el-form label-width="60px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="form.content" type="textarea" :rows="4" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSave">保存</el-button></template>
    </el-dialog>
  </div>
</template>
<style scoped>.tb-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; } .tb-header h2 { font-size: 20px; font-weight: 700; }</style>
