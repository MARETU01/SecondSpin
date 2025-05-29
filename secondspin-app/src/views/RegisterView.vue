<template>
  <div>
    <AppHeader />
    <div class="register-container">
      <h2>注册</h2>
      <form @submit.prevent="handleRegister">
        <div>
          <label for="register-username">用户名:</label>
          <input v-model="registerForm.username" type="text" id="register-username" required>
        </div>
        <div>
          <label for="register-email">邮箱:</label>
          <input v-model="registerForm.email" type="email" id="register-email" required>
        </div>
        <div>
          <label for="register-password">密码:</label>
          <input v-model="registerForm.password" type="password" id="register-password" required>
        </div>
        <div>
          <label for="register-confirm-password">确认密码:</label>
          <input v-model="registerForm.confirmPassword" type="password" id="register-confirm-password" required>
        </div>
        <button type="submit">注册</button>
      </form>
      <p>已有账号? <router-link to="/login">返回登录</router-link></p>
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
      registerForm: {
        username: '',
        email: '',
        password: '',
        confirmPassword: ''
      }
    }
  },
  methods: {
    async handleRegister() {
      if (this.registerForm.password !== this.registerForm.confirmPassword) {
        return this.$message.error('两次输入的密码不一致');
      }

      try {
        await this.$axios.post('/api/register', this.registerForm);
        this.$message.success('注册成功，请登录');
        this.$router.push('/login');
      } catch (error) {
        this.$message.error(error.response?.data?.message || '注册失败');
      }
    }
  }
}
</script>

<style scoped>
.register-container {
  max-width: 400px;
  margin: 40px auto;
  padding: 20px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.register-container h2 {
  text-align: center;
  color: #ff5722;
}

.register-container label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
}

.register-container input {
  width: 100%;
  padding: 10px;
  margin-bottom: 20px;
  border: 1px solid #ccc;
  border-radius: 4px;
}

.register-container button {
  width: 100%;
  padding: 10px;
  background-color: #ff5722;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.register-container button:hover {
  background-color: #e64a19;
}

.register-container p {
  text-align: center;
  margin-top: 20px;
}

.register-container p a {
  color: #ff5722;
  text-decoration: none;
}

.register-container p a:hover {
  text-decoration: underline;
}
</style>