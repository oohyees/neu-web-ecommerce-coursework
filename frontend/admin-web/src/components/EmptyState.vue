<template>
  <div class="empty-state">
    <div class="empty-icon">📦</div>
    <strong>{{ title }}</strong>
    <p>{{ description }}</p>
    <div v-if="$slots.default" class="empty-actions">
      <slot />
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: { type: String, default: '暂无数据' },
  description: { type: String, default: '当前没有可展示的内容。' }
})
</script>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 48px 20px;
  color: var(--muted);
  text-align: center;
  background: #fff;
  border: 1px dashed var(--line);
  border-radius: var(--radius-lg);
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
  opacity: .6;
}

strong {
  display: block;
  color: var(--ink);
  font-size: 18px;
  margin-bottom: 4px;
}

p {
  margin: 0 0 16px;
  font-size: 14px;
}

.empty-actions {
  display: flex;
  gap: 10px;
  margin-top: 4px;
}
</style>
