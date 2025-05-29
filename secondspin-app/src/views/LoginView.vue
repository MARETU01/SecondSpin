<template>
  <div>
    <AppHeader />
    <div class="login-container">
      <h2>登录</h2>
      <form @submit.prevent="handleLogin">
        <div>
          <label for="login-username">用户名:</label>
          <input v-model="loginForm.username" type="text" id="login-username" required>
        </div>
        <div>
          <label for="login-password">密码:</label>
          <input v-model="loginForm.password" type="password" id="login-password" required>
        </div>
        <button type="submit">登录</button>
      </form>
      <p>还没有账号? <router-link to="/register">立即注册</router-link></p>
      <p><router-link to="/reset-password">忘记密码?</router-link></p>
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
      loginForm: {
        username: '',
        password: ''
      }
    }
  },
  methods: {
    async handleLogin() {
      try {
        const response = await this.$axios.post('/api/login', this.loginForm);
        this.$store.commit('setUser', response.data.user);
        this.$message.success('登录成功');
        this.$router.push('/dashboard');
      } catch (error) {
        this.$message.error(error.response?.data?.message || '登录失败');
      }
    }
  }
}
</script>

<style scoped>
.login-container {
  max-width: 400px;
  margin: 40px auto;
  padding: 20px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.login-container h2 {
  text-align: center;
  color: #ff5722;
}

.login-container label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
}

.login-container input {
  width: 100%;
  padding: 10px;
  margin-bottom: 20px;
  border: 1px solid #ccc;
  border-radius: 4px;
}

.login-container button {
  width: 100%;
  padding: 10px;
  background-color: #ff5722;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.login-container button:hover {
  background-color: #e64a19;
}

.login-container p {
  text-align: center;
  margin-top: 20px;
}

.login-container p a {
  color: #ff5722;
  text-decoration: none;
}

.login-container p a:hover {
  text-decoration: underline;
}
</style>