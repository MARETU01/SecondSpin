<template>
  <div>
    <AppHeader />
    <div class="login-container">
      <h2>Login</h2>
      <form @submit.prevent="handleLogin">
        <div>
          <label for="login-email">Email:</label>
          <input v-model="loginForm.email" type="text" id="login-email" required>
        </div>
        <div>
          <label for="login-password">Password:</label>
          <input v-model="loginForm.password" type="password" id="login-password" required>
        </div>
        <button type="submit">Login</button>
      </form>
      <p>No account yet? <router-link to="/register">Sign up Now</router-link></p>
      <p><router-link to="/reset-password">Forgot password?</router-link></p>
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
          console.log('Login response:', response.data);
          if (response.data.code === 1) {
            // 登录成功，存储token到localStorage
            localStorage.setItem('token', response.data.data);
            
            alert('Login success');
            // 跳转到根路径'/'
            this.$router.push('/');
          } else {
            // 登录失败，显示错误信息
            alert(response.data.message || 'Login failed, please try again');
          }
        })
        .catch(error => {
          console.error('Login error:', error);
          alert(error.response?.data?.message || 'Network error, please try again later');
        });
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