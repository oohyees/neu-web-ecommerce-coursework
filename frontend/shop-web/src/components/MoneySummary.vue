<template>
  <section class="money-summary">
    <div><span>商品金额</span><strong>&yen;{{ amount(total) }}</strong></div>
    <div><span>运费</span><strong>&yen;{{ amount(shipping) }}</strong></div>
    <div><span>优惠</span><strong class="discount">-&yen;{{ amount(discount) }}</strong></div>
    <div class="payable"><span>应付金额</span><strong>&yen;{{ amount(payable) }}</strong></div>
  </section>
</template>

<script setup lang="ts">
defineProps({
  total: { type: Number, default: 0 },
  shipping: { type: Number, default: 0 },
  discount: { type: Number, default: 0 },
  payable: { type: Number, default: 0 }
})
function amount(value: number) { return Number(value || 0).toFixed(2) }
</script>

<style scoped>
.money-summary {
  display: grid;
  gap: 10px;
  padding: 18px 20px;
  background: #fff;
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
}

div {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  font-size: 14px;
}

span { color: var(--muted); }
strong { color: var(--ink); font-weight: 650; }

.discount { color: var(--success); }

.payable {
  padding-top: 12px;
  border-top: 1px solid var(--line);
}

.payable strong {
  color: var(--brand);
  font-size: 24px;
  font-weight: 800;
}
</style>
