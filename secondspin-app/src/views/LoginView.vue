<template>
  <div>
    <AppHeader />
    <div class="login-container">
      <h2>登录</h2>
      <form @submit.prevent="handleLogin">
        <div>
          <label for="login-email">邮箱:</label>
          <input v-model="loginForm.email" type="text" id="login-email" required>
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
        email: '',
        password: ''
      }
    }
  },
  methods: {
    handleLogin() {
      this.$http.post('/users/login', this.loginForm)
        .then(response => {
          console.log('登录响应:', response.data);
          if (response.data.code === 1) {
            // 登录成功，存储token到localStorage
            localStorage.setItem('token', response.data.data);
            // 存储用户信息到Vuex
            this.$store.commit('setUser', {
              token: response.data.data,
              // 可从JWT中解析用户信息，或等待后续接口获取
            });
            alert('登录成功');
            // 跳转到根路径'/'
            this.$router.push('/');
          } else {
            // 登录失败，显示错误信息
            alert(response.data.message || '登录失败，请重试');
          }
        })
        .catch(error => {
          console.error('登录错误:', error);
          alert(error.response?.data?.message || '网络错误，请稍后重试');
        });
    }
  }
}
</script>

<style scoped>
/* 样式保持不变 */
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