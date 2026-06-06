<template>
  <section class="page-card">
    <h2 class="card-title">基本资料</h2>
    <div class="avatar-block">
      <el-avatar :size="80" :src="form.avatarUrl" />
      <el-upload :show-file-list="false" :http-request="uploadAvatar">
        <el-button size="small">更换头像</el-button>
      </el-upload>
      <span class="muted">支持 JPG、PNG 格式</span>
    </div>
    <el-form label-width="80px" class="profile-form">
      <el-form-item label="昵称">
        <el-input v-model="form.nickname" />
      </el-form-item>
      <el-form-item label="邮箱">
        <el-input v-model="form.email" />
      </el-form-item>
      <el-form-item label="手机号">
        <el-input v-model="form.phone" />
      </el-form-item>
      <el-form-item>
        <el-button type="danger" @click="saveProfile">保存资料</el-button>
      </el-form-item>
    </el-form>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '@/api'
import { useUserStore, useCartStore, useFavoriteStore } from '@/stores'

const userStore = useUserStore()
const form = ref<any>({})

async function saveProfile() {
  await api.put('/auth/profile', form.value)
  ElMessage.success('资料已保存')
}

async function uploadAvatar({ file }) {
  const fd = new FormData()
  fd.append('file', file)
  form.value.avatarUrl = (await api.post('/files/upload', fd)).data.data
}

onMounted(async () => {
  form.value = (await api.get('/auth/profile', { params: { userId: userStore.userId } })).data.data || {}
})
</script>

<style scoped>
.card-title {
  margin: 0 0 20px;
  font-size: 18px;
  font-weight: 700;
}

.avatar-block {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--line);
}

.profile-form {
  max-width: 420px;
}
</style>
