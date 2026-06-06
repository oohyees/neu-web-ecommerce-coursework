<template>
  <el-tag :type="type" effect="light">{{ text }}</el-tag>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps({
  value: { type: String, default: '' },
  kind: { type: String, default: 'order' }
})

const maps: Record<string, Record<string, [string, string]>> = {
  order: {
    CREATED: ['待处理', 'warning'],
    PAID: ['已支付', 'primary'],
    SHIPPED: ['已发货', 'primary'],
    DELIVERED: ['已送达', 'success'],
    COMPLETED: ['已完成', 'success'],
    CANCELLED: ['已取消', 'info'],
    PENDING_PAYMENT: ['待支付', 'warning'],
    PENDING_DELIVERY: ['待发货', 'warning'],
    PENDING_RECEIPT: ['待收货', 'primary'],
  },
  payment: {
    UNPAID: ['待支付', 'warning'],
    PAID: ['已支付', 'success'],
    REFUNDED: ['已退款', 'info'],
    REFUNDING: ['退款中', 'warning'],
    REQUESTED: ['退款申请', 'warning'],
  },
  refund: {
    REQUESTED: ['退款申请中', 'warning'],
    APPROVED: ['已同意退款', 'success'],
    REJECTED: ['已拒绝退款', 'danger'],
    COMPLETED: ['退款完成', 'success'],
  },
  boolean: {
    true: ['启用', 'success'],
    false: ['停用', 'info'],
  },
  product: {
    ON_SALE: ['在售', 'success'],
    OFF_SALE: ['下架', 'info'],
    DELETED: ['已删除', 'danger'],
  },
}

const entry = computed(() => maps[props.kind]?.[String(props.value)] || [props.value || '-', 'info'])
const text = computed(() => entry.value[0])
const type = computed(() => entry.value[1])
</script>
