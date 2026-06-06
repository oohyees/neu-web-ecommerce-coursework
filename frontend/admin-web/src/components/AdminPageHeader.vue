<template>
  <header class="admin-page-header">
    <div class="header-text">
      <p v-if="eyebrow" class="eyebrow">{{ eyebrow }}</p>
      <h1>{{ title }}</h1>
      <span v-if="subtitle" class="subtitle">{{ subtitle }}</span>
    </div>
    <div class="header-actions">
      <slot />
    </div>
  </header>
</template>

<script setup>
defineProps({
  eyebrow: { type: String, default: '' },
  title: { type: String, required: true },
  subtitle: { type: String, default: '' }
})
</script>

<style scoped>
.admin-page-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
}

.eyebrow,
h1,
.subtitle {
  margin: 0;
}

.eyebrow {
  color: var(--brand);
  font-size: 13px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .5px;
}

h1 {
  margin-top: 4px;
  font-size: 24px;
  font-weight: 700;
  line-height: 1.25;
  color: var(--ink);
}

.subtitle {
  display: block;
  margin-top: 6px;
  color: var(--muted);
  font-size: 14px;
}

.header-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: flex-end;
}

@media (max-width: 768px) {
  .admin-page-header {
    align-items: flex-start;
    flex-direction: column;
  }
  .header-actions {
    justify-content: flex-start;
    width: 100%;
  }
}
</style>
