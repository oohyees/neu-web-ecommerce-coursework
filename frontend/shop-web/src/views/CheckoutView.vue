<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { fetchCart, type CartItem } from '@/api/cart'
import { fetchAddresses, type UserAddress } from '@/api/address'
import { fetchMyCoupons, type Coupon } from '@/api/coupon'
import { createOrder } from '@/api/order'
import { imageOrPlaceholder } from '@/utils/image'

const router = useRouter()
const route = useRoute()

const cartItems = ref<CartItem[]>([])
const addresses = ref<UserAddress[]>([])
const coupons = ref<Coupon[]>([])
const selectedAddrId = ref<number | null>(null)
const selectedCouponId = ref<number | null>(null)
const submitting = ref(false)

const totalAmount = computed(() => {
  return cartItems.value.reduce((s, i) => s + i.price * i.quantity, 0).toFixed(2)
})

const cartItemIds = computed(() => {
  const ids = route.query.cartItemIds as string
  return ids ? ids.split(',').map(Number) : []
})

async function load() {
  try {
    const [cartRes, addrRes, couponRes] = await Promise.all([
      fetchCart(),
      fetchAddresses(),
      fetchMyCoupons(),
    ])
    const allItems: CartItem[] = (cartRes as any).data?.items ?? (Array.isArray((cartRes as any).data) ? (cartRes as any).data : [])
    if (cartItemIds.value.length) {
      cartItems.value = allItems.filter(i => cartItemIds.value.includes(i.id))
    } else {
      cartItems.value = allItems
    }
    addresses.value = (addrRes as any).data ?? []
    coupons.value = (couponRes as any).data ?? []
    if (addresses.value.length) selectedAddrId.value = addresses.value.find(a => a.isDefault)?.id ?? addresses.value[0].id
  } catch { /* handled */ }
}

async function handleSubmit() {
  if (!selectedAddrId.value) { ElMessage.warning('请选择收货地址'); return }
  if (!cartItems.value.length) { ElMessage.warning('没有待结算商品'); return }
  submitting.value = true
  try {
    const res: any = await createOrder({
      addressId: selectedAddrId.value,
      cartItemIds: cartItems.value.map(i => i.id),
      couponId: selectedCouponId.value ?? undefined,
    })
    const orderNo = res.data?.orderNo ?? res.data
    ElMessage.success('下单成功')
    router.push(`/payment?orderNo=${orderNo}`)
  } catch { /* handled */ }
  finally { submitting.value = false }
}

onMounted(load)
</script>

<template>
  <div class="page-container checkout-page">
    <h2 class="page-title">确认订单</h2>

    <div v-if="!cartItems.length" class="empty">
      <el-empty description="没有待结算商品" />
    </div>

    <template v-else>
      <!-- 收货地址 -->
      <div class="section">
        <h3 class="section-head">收货地址</h3>
        <div class="address-list">
          <div v-for="a in addresses" :key="a.id"
               :class="['addr-card', { active: selectedAddrId === a.id }]"
               @click="selectedAddrId = a.id">
            <div class="addr-receiver">{{ a.receiver }} {{ a.phone }}</div>
            <div class="addr-detail">{{ a.province }}{{ a.city }}{{ a.district }} {{ a.detail }}</div>
            <span v-if="a.isDefault" class="addr-default">默认</span>
          </div>
        </div>
        <div v-if="!addresses.length" class="no-addr">
          <span>暂无地址，</span><router-link to="/address">去添加 →</router-link>
        </div>
      </div>

      <!-- 商品清单 -->
      <div class="section">
        <h3 class="section-head">商品清单</h3>
        <div v-for="item in cartItems" :key="item.id" class="checkout-item">
          <img :src="imageOrPlaceholder(item.imageUrl)" class="item-img" />
          <div class="item-info">
            <div class="item-name">{{ item.productName }}</div>
            <div v-if="item.color||item.size" class="item-sku">{{ [item.color,item.size].filter(Boolean).join(' / ') }}</div>
          </div>
          <div class="item-price">¥{{ item.price }} × {{ item.quantity }}</div>
        </div>
      </div>

      <!-- 优惠券 -->
      <div v-if="coupons.length" class="section">
        <h3 class="section-head">优惠券</h3>
        <div class="coupon-list">
          <div v-for="c in coupons" :key="c.id"
               :class="['coupon-item', { active: selectedCouponId === c.id }]"
               @click="selectedCouponId = selectedCouponId === c.id ? null : c.id">
            ¥{{ c.value }} 满{{ c.minAmount }}减
          </div>
        </div>
      </div>

      <!-- 总计 + 提交 -->
      <div class="checkout-footer">
        <span class="total">合计：<b>¥{{ totalAmount }}</b></span>
        <el-button type="primary" size="large" :loading="submitting" @click="handleSubmit">提交订单</el-button>
      </div>
    </template>
  </div>
</template>

<style scoped>
.checkout-page { max-width: 800px; padding-bottom: 120px; }
.page-title { font-size: 22px; font-weight: 700; margin-bottom: 24px; }
.section { margin-bottom: 24px; }
.section-head { font-size: 16px; font-weight: 600; margin-bottom: 12px; padding-left: 10px; border-left: 3px solid var(--color-primary); }

.address-list { display: flex; flex-wrap: wrap; gap: 10px; }
.addr-card { padding: 12px 16px; border: 2px solid #eee; border-radius: 8px; cursor: pointer; min-width: 200px; position: relative; }
.addr-card.active { border-color: var(--color-primary); background: #fff8f5; }
.addr-receiver { font-weight: 600; font-size: 14px; margin-bottom: 4px; }
.addr-detail { font-size: 13px; color: #666; }
.addr-default { position: absolute; top: 8px; right: 10px; font-size: 11px; color: var(--color-primary); background: #fff0eb; padding: 1px 6px; border-radius: 3px; }
.no-addr { color: #999; font-size: 14px; }
.no-addr a { color: var(--color-primary); }

.checkout-item { display: flex; align-items: center; gap: 12px; padding: 12px 0; border-bottom: 1px solid #f0f0f0; }
.item-img { width: 60px; height: 60px; border-radius: 8px; object-fit: cover; }
.item-name { font-size: 14px; }
.item-sku { font-size: 12px; color: #888; }
.item-price { margin-left: auto; font-size: 14px; font-weight: 600; flex-shrink: 0; }

.coupon-list { display: flex; gap: 8px; flex-wrap: wrap; }
.coupon-item { padding: 6px 14px; border: 1px solid #eee; border-radius: 6px; cursor: pointer; font-size: 13px; }
.coupon-item.active { border-color: var(--color-primary); background: #fff8f5; color: var(--color-primary); }

.checkout-footer { position: fixed; bottom: 0; left: 0; right: 0; background: #fff; border-top: 2px solid var(--color-primary); padding: 14px 24px; display: flex; align-items: center; justify-content: flex-end; gap: 20px; z-index: 50; }
.total { font-size: 14px; }
.total b { font-size: 22px; color: var(--color-price, #ff0036); }
</style>
