<template>
  <nav class="floating-actions" aria-label="验收快捷入口">
    <button type="button" class="float-btn" title="返回顶部" @click="scrollTop">
      <span>↑</span>
      <em>顶部</em>
    </button>
    <RouterLink class="float-btn" to="/cart" title="购物车">
      <span>🛒</span>
      <em>购物车</em>
    </RouterLink>
    <RouterLink class="float-btn" to="/user/coupons" title="优惠券">
      <span>券</span>
      <em>领券</em>
    </RouterLink>
    <RouterLink class="float-btn" to="/consultations" title="在线客服">
      <span>💬</span>
      <em>客服</em>
    </RouterLink>
  </nav>
</template>

<script setup lang="ts">
function scrollTop() {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}
</script>

<style scoped>
.floating-actions {
  position: fixed;
  right: 18px;
  bottom: 96px;
  z-index: 30;
  display: grid;
  gap: 8px;
}

.float-btn {
  display: grid;
  place-items: center;
  width: 54px;
  min-height: 54px;
  padding: 7px 4px;
  color: var(--ink);
  text-align: center;
  cursor: pointer;
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid var(--line);
  border-radius: 14px;
  box-shadow: var(--shadow-sm);
  transition: color .18s ease, border-color .18s ease, transform .18s ease;
}

.float-btn:hover {
  color: var(--brand);
  border-color: #f2b8c2;
  transform: translateY(-2px);
}

.float-btn span {
  display: grid;
  place-items: center;
  min-width: 22px;
  height: 22px;
  font-size: 17px;
  font-weight: 800;
  line-height: 1;
}

.float-btn em {
  margin-top: 3px;
  font-size: 11px;
  font-style: normal;
  line-height: 1;
  white-space: nowrap;
}

@media (max-width: 768px) {
  .floating-actions {
    right: 12px;
    bottom: 14px;
    grid-auto-flow: column;
    gap: 6px;
    max-width: calc(100vw - 24px);
    padding: 6px;
    overflow-x: auto;
    background: rgba(255, 255, 255, 0.92);
    border: 1px solid var(--line);
    border-radius: 16px;
    box-shadow: var(--shadow-md);
  }

  .float-btn {
    width: 48px;
    min-height: 46px;
    padding: 5px 3px;
    border: 0;
    box-shadow: none;
  }

  .float-btn span {
    height: 19px;
    font-size: 15px;
  }

  .float-btn em {
    font-size: 10px;
  }
}
</style>
