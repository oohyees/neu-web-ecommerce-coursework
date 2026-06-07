<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { fetchBanners, createBanner, updateBanner, deleteBanner } from '@/api/banner'
import { uploadImage } from '@/api/product'

const banners = ref<any[]>([]); const dialogVisible = ref(false); const editing = ref<any>(null)
const form = ref({ imageUrl: '', linkUrl: '', sortOrder: 0 })
const uploading = ref(false)

function resetForm() { form.value = { imageUrl:'', linkUrl:'', sortOrder:0 }; editing.value = null }
function openAdd() { resetForm(); dialogVisible.value = true }
function openEdit(b: any) { editing.value = b; form.value = { imageUrl: b.imageUrl, linkUrl: b.linkUrl||'', sortOrder: b.sortOrder??0 }; dialogVisible.value = true }

async function load() { try { const res: any = await fetchBanners(); banners.value = res.data ?? [] } catch { banners.value = [] } }
async function handleSave() {
  try { if (editing.value) await updateBanner({ id: editing.value.id, ...form.value }); else await createBanner(form.value); ElMessage.success('已保存'); dialogVisible.value = false; load() } catch { /* handled */ }
}
async function handleDelete(id: number) { try { await ElMessageBox.confirm('确认删除？','提示',{type:'warning'}); await deleteBanner(id); ElMessage.success('已删除'); load() } catch { /* cancelled */ } }
async function handleUpload(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  uploading.value = true
  try {
    const res: any = await uploadImage(file)
    form.value.imageUrl = res.data ?? res
    ElMessage.success('图片上传成功')
  } catch { ElMessage.error('上传失败') }
  finally { uploading.value = false; (e.target as HTMLInputElement).value = '' }
}
const linkedCount = computed(() => banners.value.filter((b) => b.linkUrl).length)
onMounted(load)
</script>
<template>
  <div class="admin-page banners-page">
    <div class="tb-header">
      <div>
        <h2>轮播管理</h2>
        <p class="page-subtitle">管理首页首屏轮播图、跳转链接和展示顺序。</p>
      </div>
      <div class="tb-actions"><el-button type="primary" :icon="Plus" @click="openAdd">新增轮播</el-button></div>
    </div>
    <div class="admin-summary">
      <div class="summary-card"><div class="summary-card__label">轮播数量</div><div class="summary-card__value">{{ banners.length }}</div><div class="summary-card__hint">首页视觉素材</div></div>
      <div class="summary-card"><div class="summary-card__label">已配置链接</div><div class="summary-card__value">{{ linkedCount }}</div><div class="summary-card__hint">可点击跳转</div></div>
      <div class="summary-card"><div class="summary-card__label">排序字段</div><div class="summary-card__value">sort</div><div class="summary-card__hint">控制展示顺序</div></div>
      <div class="summary-card"><div class="summary-card__label">图片策略</div><div class="summary-card__value">本地</div><div class="summary-card__hint">避免外网依赖</div></div>
    </div>
    <div class="banner-grid">
      <div v-for="b in banners" :key="b.id" class="banner-card">
        <img :src="b.imageUrl" />
        <div class="banner-info">
          <div><strong>排序 {{b.sortOrder}}</strong><span>{{ b.linkUrl || '未配置跳转' }}</span></div>
          <div class="action-stack"><el-button size="small" @click="openEdit(b)">编辑</el-button><el-button size="small" type="danger" @click="handleDelete(b.id)">删除</el-button></div>
        </div>
      </div>
    </div>
    <el-dialog v-model="dialogVisible" :title="editing?'编辑轮播':'新增轮播'" width="500px">
      <el-form label-width="80px">
        <el-form-item label="图片">
          <div style="display:flex;gap:8px;width:100%">
            <el-input v-model="form.imageUrl" placeholder="图片URL" style="flex:1" />
            <label class="upload-btn"><span :class="{ 'is-loading': uploading }">{{ uploading ? '上传中...' : '上传图片' }}</span><input type="file" accept="image/*" hidden @change="handleUpload" :disabled="uploading" /></label>
          </div>
          <div v-if="form.imageUrl" style="margin-top:8px"><img :src="form.imageUrl" style="max-width:200px;max-height:100px;border-radius:6px;border:1px solid #eee" /></div>
        </el-form-item>
        <el-form-item label="链接"><el-input v-model="form.linkUrl" placeholder="点击跳转地址" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSave">保存</el-button></template>
    </el-dialog>
  </div>
</template>
<style scoped>
.page-subtitle { margin-top: 8px; color: #6b7280; font-size: 13px; }
.banner-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; }
.banner-card { background: #fff; border: 1px solid var(--color-line); border-radius: 8px; overflow: hidden; box-shadow: var(--shadow-card); }
.banner-card img { width: 100%; height: 150px; object-fit: cover; background: #f8fafc; }
.banner-info { padding: 12px; display: flex; justify-content: space-between; align-items: center; gap: 10px; font-size: 13px; }
.banner-info div:first-child { display: grid; gap: 4px; min-width: 0; }
.banner-info span { color: #8a94a6; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
@media (max-width: 1100px) { .banner-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 640px) { .banner-grid { grid-template-columns: 1fr; } }
.upload-btn { display:inline-flex;align-items:center;padding:0 15px;border:1px solid var(--color-primary);border-radius:4px;color:var(--color-primary);cursor:pointer;font-size:13px;white-space:nowrap;transition:all .2s; }
.upload-btn:hover { background:var(--color-primary);color:#fff; }
.upload-btn .is-loading { opacity:.6; }
</style>
