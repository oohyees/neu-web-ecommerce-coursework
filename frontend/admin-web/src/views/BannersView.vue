<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchBanners, createBanner, updateBanner, deleteBanner } from '@/api/banner'

const banners = ref<any[]>([]); const dialogVisible = ref(false); const editing = ref<any>(null)
const form = ref({ imageUrl: '', linkUrl: '', sortOrder: 0 })

function resetForm() { form.value = { imageUrl:'', linkUrl:'', sortOrder:0 }; editing.value = null }
function openAdd() { resetForm(); dialogVisible.value = true }
function openEdit(b: any) { editing.value = b; form.value = { imageUrl: b.imageUrl, linkUrl: b.linkUrl||'', sortOrder: b.sortOrder??0 }; dialogVisible.value = true }

async function load() { try { const res: any = await fetchBanners(); banners.value = res.data ?? [] } catch { banners.value = [] } }
async function handleSave() {
  try { if (editing.value) await updateBanner({ id: editing.value.id, ...form.value }); else await createBanner(form.value); ElMessage.success('已保存'); dialogVisible.value = false; load() } catch { /* handled */ }
}
async function handleDelete(id: number) { try { await ElMessageBox.confirm('确认删除？','提示',{type:'warning'}); await deleteBanner(id); ElMessage.success('已删除'); load() } catch { /* cancelled */ } }
onMounted(load)
</script>
<template>
  <div>
    <div class="tb-header"><h2>轮播管理</h2><el-button type="primary" @click="openAdd">新增</el-button></div>
    <div class="banner-grid">
      <div v-for="b in banners" :key="b.id" class="banner-card">
        <img :src="b.imageUrl" />
        <div class="banner-info"><span>排序:{{b.sortOrder}}</span><el-button text size="small" @click="openEdit(b)">编辑</el-button><el-button text size="small" type="danger" @click="handleDelete(b.id)">删除</el-button></div>
      </div>
    </div>
    <el-dialog v-model="dialogVisible" :title="editing?'编辑轮播':'新增轮播'" width="500px">
      <el-form label-width="80px">
        <el-form-item label="图片URL"><el-input v-model="form.imageUrl" /></el-form-item>
        <el-form-item label="链接"><el-input v-model="form.linkUrl" placeholder="点击跳转地址" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSave">保存</el-button></template>
    </el-dialog>
  </div>
</template>
<style scoped>
.tb-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; } .tb-header h2 { font-size: 20px; font-weight: 700; }
.banner-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; }
.banner-card { background: #fff; border-radius: 12px; overflow: hidden; box-shadow: 0 1px 4px rgba(0,0,0,0.04); }
.banner-card img { width: 100%; height: 120px; object-fit: cover; }
.banner-info { padding: 10px; display: flex; align-items: center; gap: 8px; font-size: 13px; }
</style>
