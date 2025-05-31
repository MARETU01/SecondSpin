<template>
  <div>
    <AppHeader />
    <div class="register-container">
      <h2>注册</h2>
      <form @submit.prevent="handleRegister">
        <!-- 表单字段保持不变 -->
        <div>
          <label for="register-username">用户名:</label>
          <input v-model="registerForm.username" type="text" id="register-username" required>
        </div>
        <div>
          <label for="register-realname">真实姓名:</label>
          <input v-model="registerForm.realName" type="text" id="register-realname" required>
        </div>
        <div>
          <label for="register-email">邮箱:</label>
          <input v-model="registerForm.email" type="email" id="register-email" required>
        </div>
        <div>
          <label for="register-phone">手机号:</label>
          <input v-model="registerForm.phone" type="tel" id="register-phone" required>
        </div>
        <div>
          <label for="register-password">密码:</label>
          <input v-model="registerForm.password" type="password" id="register-password" required>
        </div>
        <div>
          <label for="register-confirm-password">确认密码:</label>
          <input v-model="registerForm.confirmPassword" type="password" id="register-confirm-password" required>
        </div>
        <div>
          <label for="register-verification">验证码:</label>
          <input v-model="verification" type="text" id="register-verification" required>
          <button type="button" @click="sendVerificationCode" ref="verificationBtn">获取验证码</button>
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
        realName: '',
        email: '',
        phone: '',
        password: '',
        confirmPassword: ''
      },
      verification: '',
      isSendingCode: false
    }
  },
  methods: {
    sendVerificationCode() {
      console.log('1. 方法开始执行');
      if (this.isSendingCode) return;
      this.isSendingCode = true;
      console.log('2. 防重复点击逻辑通过');

      if (this.registerForm.password !== this.registerForm.confirmPassword) {
        alert('两次输入的密码不一致'); // 原生alert
        this.isSendingCode = false;
        return;
      }
      console.log('3. 密码验证通过，准备发送请求');

      this.$http.post('/users/register/code', this.registerForm)
        .then(response => {
          console.log('验证码响应数据:', response.data);
          if (response.data.code === 1) {
            alert(response.data.message || '验证码发送成功'); // 成功提示
            this.startCountdown();
          } else {
            alert(response.data.message || '发送验证码失败'); // 失败提示
          }
        })
        .catch(error => {
          console.error('验证码请求错误:', error);
          alert('网络错误，请稍后重试');
        })
        .finally(() => {
          this.isSendingCode = false;
        });
    },
    
    startCountdown() {
      let countdown = 60;
      const btn = this.$refs.verificationBtn;
      btn.disabled = true;
      const timer = setInterval(() => {
        btn.textContent = `重新发送(${countdown})`;
        countdown--;
        if (countdown < 0) {
          clearInterval(timer);
          btn.disabled = false;
          btn.textContent = '获取验证码';
        }
      }, 1000);
    },
    
    handleRegister() {
      if (this.registerForm.password !== this.registerForm.confirmPassword) {
        alert('两次输入的密码不一致');
        return;
      }

      const payload = {
        username: this.registerForm.username,
        realName: this.registerForm.realName,
        password: this.registerForm.password,
        email: this.registerForm.email,
        phone: this.registerForm.phone
      };

      this.$http.post('/users/register', payload, {
        params: {
          verification: this.verification
        }
      })
        .then(response => {
          console.log('注册响应数据:', response.data);
          if (response.data.code === 1) {
            if (confirm(response.data.message || '注册成功，请登录')) { // 带确认按钮的弹窗
              this.$router.push('/login');
            }
          } else {
            alert(response.data.message || '注册失败');
          }
        })
        .catch(error => {
          console.error('注册请求错误:', error);
          alert(error.response?.data?.message || '注册失败，请检查网络后重试');
        });
    }
  }
}
</script>

<style scoped>
/* 样式保持不变 */
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