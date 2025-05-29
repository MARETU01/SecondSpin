<template>
  <div>
    <AppHeader />
    <div class="reset-password-container">
      <h2>修改密码</h2>
      <form @submit.prevent="handleResetPassword">
        <div>
          <label for="reset-email">邮箱:</label>
          <input v-model="resetForm.email" type="email" id="reset-email" required>
        </div>
        <div>
          <label for="reset-old-password">旧密码:</label>
          <input v-model="resetForm.oldPassword" type="password" id="reset-old-password">
        </div>
        <div>
          <label for="reset-new-password">新密码:</label>
          <input v-model="resetForm.newPassword" type="password" id="reset-new-password" required>
        </div>
        <div>
          <label for="reset-confirm-password">确认新密码:</label>
          <input v-model="resetForm.confirmPassword" type="password" id="reset-confirm-password" required>
        </div>
        <button type="submit">修改密码</button>
      </form>
      <p><router-link to="/login">返回登录</router-link></p>
    </div>
    <AppFooter />
  </div>
</template>

<script>
import AppHeader from '@/components/AppHeader.vue';
import AppFooter from '@/components/AppFooter.vue';

export default {
  components: {
    AppHeader,
    AppFooter
  },
  data() {
    return {
      resetForm: {
        email: '',
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      }
    }
  },
  methods: {
    async handleResetPassword() {
      if (this.resetForm.newPassword !== this.resetForm.confirmPassword) {
        return this.$message.error('两次输入的新密码不一致');
      }

      try {
        await this.$axios.post('/api/reset-password', this.resetForm);
        this.$message.success('密码修改成功，请使用新密码登录');
        this.$router.push('/login');
      } catch (error) {
        this.$message.error(error.response?.data?.message || '密码修改失败');
      }
    }
  }
}
</script>

<style scoped>
.reset-password-container {
  max-width: 400px;
  margin: 40px auto;
  padding: 20px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.reset-password-container h2 {
  text-align: center;
  color: #ff5722;
}

.reset-password-container label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
}

.reset-password-container input {
  width: 100%;
  padding: 10px;
  margin-bottom: 20px;
  border: 1px solid #ccc;
  border-radius: 4px;
}

.reset-password-container button {
  width: 100%;
  padding: 10px;
  background-color: #ff5722;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.reset-password-container button:hover {
  background-color: #e64a19;
}

.reset-password-container p {
  text-align: center;
  margin-top: 20px;
}

.reset-password-container p a {
  color: #ff5722;
  text-decoration: none;
}

.reset-password-container p a:hover {
  text-decoration: underline;
}
</style>