<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchAdminPromotions, createPromotion, updatePromotion, deletePromotion, fetchAdminCoupons, createCoupon, updateCoupon, deleteCoupon } from '@/api/promotion'

const promotions = ref<any[]>([])
const coupons = ref<any[]>([])
const dialogVisible = ref(false)
const editing = ref<any>(null)
const form = ref<any>({})
const tab = ref<'promotion'|'coupon'>('promotion')
const type = ref<'create'|'edit'>('create')

async function loadPromotions() { try { const res: any = await fetchAdminPromotions(); promotions.value = res.data ?? [] } catch { promotions.value = [] } }
async function loadCoupons() { try { const res: any = await fetchAdminCoupons(); coupons.value = res.data ?? [] } catch { coupons.value = [] } }

function openAddPromo() { type.value = 'create'; editing.value = null; form.value = { title:'', productId:null, promotionType:'FLASH_SALE', promotionPrice:0, startAt:'', endAt:'', enabled:true }; dialogVisible.value = true }
function openEditPromo(p: any) { type.value = 'edit'; editing.value = p; form.value = { ...p }; dialogVisible.value = true }
function openAddCoupon() { type.value = 'create'; editing.value = null; form.value = { name:'', thresholdAmount:0, discountAmount:0, enabled:true }; dialogVisible.value = true }
function openEditCoupon(c: any) { type.value = 'edit'; editing.value = c; form.value = { ...c }; dialogVisible.value = true }

async function handleSave() {
  try {
    if (tab.value === 'promotion') { if (editing.value) await updatePromotion({ id: editing.value.id, ...form.value }); else await createPromotion(form.value) }
    else { if (editing.value) await updateCoupon({ id: editing.value.id, ...form.value }); else await createCoupon(form.value) }
    ElMessage.success('已保存'); dialogVisible.value = false; loadPromotions(); loadCoupons()
  } catch { /* handled */ }
}

async function handleDeletePromo(id: number) { try { await ElMessageBox.confirm('确认删除？','提示',{type:'warning'}); await deletePromotion(id); ElMessage.success('已删除'); loadPromotions() } catch { /* cancelled */ } }
async function handleDeleteCoupon(id: number) { try { await ElMessageBox.confirm('确认删除？','提示',{type:'warning'}); await deleteCoupon(id); ElMessage.success('已删除'); loadCoupons() } catch { /* cancelled */ } }

onMounted(() => { loadPromotions(); loadCoupons() })
</script>
<template>
  <div>
    <div class="tb-header"><h2>促销管理</h2></div>
    <el-tabs v-model="tab">
      <el-tab-pane label="促销活动" name="promotion">
        <el-button type="primary" size="small" @click="openAddPromo" style="margin-bottom:12px">新增促销</el-button>
        <el-table :data="promotions" stripe>
          <el-table-column prop="title" label="名称" /><el-table-column prop="productId" label="商品ID" width="80" /><el-table-column prop="promotionType" label="类型" width="100" /><el-table-column prop="promotionPrice" label="促销价" width="90" />
          <el-table-column label="操作" width="120"><template #default="{row}"><el-button text size="small" @click="openEditPromo(row)">编辑</el-button><el-button text size="small" type="danger" @click="handleDeletePromo(row.id)">删除</el-button></template></el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="优惠券" name="coupon">
        <el-button type="primary" size="small" @click="openAddCoupon" style="margin-bottom:12px">新增优惠券</el-button>
        <el-table :data="coupons" stripe>
          <el-table-column prop="name" label="名称" /><el-table-column prop="thresholdAmount" label="满减门槛" /><el-table-column prop="discountAmount" label="优惠金额" />
          <el-table-column label="操作" width="120"><template #default="{row}"><el-button text size="small" @click="openEditCoupon(row)">编辑</el-button><el-button text size="small" type="danger" @click="handleDeleteCoupon(row.id)">删除</el-button></template></el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
    <el-dialog v-model="dialogVisible" :title="type==='edit'?'编辑':'新增'" width="500px">
      <template v-if="tab==='promotion'">
        <el-form label-width="80px">
          <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
          <el-form-item label="商品ID"><el-input-number v-model="form.productId" /></el-form-item>
          <el-form-item label="类型"><el-select v-model="form.promotionType"><el-option value="FLASH_SALE" label="秒杀" /><el-option value="PROMOTION" label="促销" /></el-select></el-form-item>
          <el-form-item label="促销价"><el-input-number v-model="form.promotionPrice" :min="0" :step="0.01" /></el-form-item>
          <el-form-item label="启用"><el-switch v-model="form.enabled" /></el-form-item>
        </el-form>
      </template>
      <template v-else>
        <el-form label-width="80px">
          <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
          <el-form-item label="满减门槛"><el-input-number v-model="form.thresholdAmount" :min="0" :step="0.01" /></el-form-item>
          <el-form-item label="优惠金额"><el-input-number v-model="form.discountAmount" :min="0" :step="0.01" /></el-form-item>
          <el-form-item label="启用"><el-switch v-model="form.enabled" /></el-form-item>
        </el-form>
      </template>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSave">保存</el-button></template>
    </el-dialog>
  </div>
</template>
<style scoped>.tb-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; } .tb-header h2 { font-size: 20px; font-weight: 700; }</style>
