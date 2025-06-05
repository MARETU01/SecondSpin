<template>
  <div>
    <AppHeader />
    <div class="reset-password-container">
      <h2>重置密码</h2>
      <form @submit.prevent="handleResetPassword">
        <div>
          <label for="reset-email">邮箱:</label>
          <input v-model="resetForm.email" type="email" id="reset-email" required>
        </div>
        <div>
          <label for="reset-new-password">新密码:</label>
          <input v-model="resetForm.newPassword" type="password" id="reset-new-password" required>
        </div>
        <div>
          <label for="reset-confirm-password">确认新密码:</label>
          <input v-model="resetForm.confirmPassword" type="password" id="reset-confirm-password" required>
        </div>
        <div>
          <label for="reset-verification">验证码:</label>
          <input v-model="verification" type="text" id="reset-verification" required>
          <button type="button" @click="sendVerificationCode" ref="verificationBtn">获取验证码</button>
        </div>
        <button type="submit">重置密码</button>
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
        newPassword: '',
        confirmPassword: ''
      },
      verification: '',
      isSendingCode: false
    };
  },
  methods: {
    sendVerificationCode() {
      console.log('1. 方法开始执行');
      if (this.isSendingCode) return;
      this.isSendingCode = true;
      console.log('2. 防重复点击逻辑通过');

      if (this.resetForm.newPassword !== this.resetForm.confirmPassword) {
        alert('两次输入的新密码不一致'); 
        this.isSendingCode = false;
        return;
      }
      console.log('3. 密码验证通过，准备发送请求');

      this.$http.post('/users/forget-password/code', { email: this.resetForm.email })
        .then(response => {
          console.log('验证码响应数据:', response.data);
          if (response.data.code === 1) {
            alert(response.data.message || '验证码发送成功'); 
            this.startCountdown();
          } else {
            alert(response.data.message || '发送验证码失败'); 
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

    handleResetPassword() {
      if (this.resetForm.newPassword !== this.resetForm.confirmPassword) {
        alert('两次输入的新密码不一致');
        return;
      }

      const payload = {
        email: this.resetForm.email,
        password: this.resetForm.newPassword
      };

      this.$http.post('/users/forget-password', payload, {
        params: {
          verification: this.verification
        }
      })
        .then(response => {
          console.log('重置密码响应数据:', response.data);
          if (response.data.code === 1) {
            if (confirm(response.data.message || '密码重置成功，请使用新密码登录')) { 
              this.$router.push('/login');
            }
          } else {
            alert(response.data.message || '密码重置失败');
          }
        })
        .catch(error => {
          console.error('重置密码请求错误:', error);
          alert(error.response?.data?.message || '密码重置失败，请检查网络后重试');
        });
    }
  }
};
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