<template>
  <el-tag :type="type" effect="light">{{ text }}</el-tag>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  value: { type: String, default: '' },
  kind: { type: String, default: 'order' }
})

const maps = {
  order: {
    CREATED: ['待处理', 'warning'],
    SHIPPED: ['已发货', 'primary'],
    COMPLETED: ['已完成', 'success'],
    CANCELLED: ['已取消', 'info']
  },
  payment: {
    UNPAID: ['待支付', 'warning'],
    PAID: ['已支付', 'success'],
    REFUNDED: ['已退款', 'info']
  },
  boolean: {
    true: ['启用', 'success'],
    false: ['停用', 'info']
  }
}

const entry = computed(() => maps[props.kind]?.[String(props.value)] || [props.value || '-', 'info'])
const text = computed(() => entry.value[0])
const type = computed(() => entry.value[1])
</script>
