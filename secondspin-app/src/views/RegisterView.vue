<template>
  <div>
    <AppHeader />
    <div class="register-container">
      <h2>Registration</h2>
      <form @submit.prevent="handleRegister">
        <!-- 表单字段保持不变 -->
        <div>
          <label for="register-username">User name:</label>
          <input v-model="registerForm.username" type="text" id="register-username" required>
        </div>
        <div>
          <label for="register-realname">Real name:</label>
          <input v-model="registerForm.realName" type="text" id="register-realname" required>
        </div>
        <div>
          <label for="register-email">Email:</label>
          <input v-model="registerForm.email" type="email" id="register-email" required>
        </div>
        <div>
          <label for="register-phone">Phone number:</label>
          <input v-model="registerForm.phone" type="tel" id="register-phone" required>
        </div>
        <div>
          <label for="register-password">Password:</label>
          <input v-model="registerForm.password" type="password" id="register-password" required>
        </div>
        <div>
          <label for="register-confirm-password">Confirm Password:</label>
          <input v-model="registerForm.confirmPassword" type="password" id="register-confirm-password" required>
        </div>
        <div>
          <label for="register-verification">Verification code:</label>
          <input v-model="verification" type="text" id="register-verification" required>
          <button type="button" @click="sendVerificationCode" ref="verificationBtn">Getting a CAPTCHA</button>
        </div>
        <button type="submit">Registration</button>
      </form>
      <p>Have an account? <router-link to="/login">Return to Login</router-link></p>
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
      console.log('1. The method starts executing');
      if (this.isSendingCode) return;
      this.isSendingCode = true;
      console.log('2. Anti-repeated click logic passes');

      if (this.registerForm.password !== this.registerForm.confirmPassword) {
        alert('The password is not the same'); // 原生alert
        this.isSendingCode = false;
        return;
      }
      console.log('3. The password is validated and ready to send the request');

      this.$http.post('/users/register/code', this.registerForm)
        .then(response => {
          console.log('Captcha response data:', response.data);
          if (response.data.code === 1) {
            alert(response.data.message || 'The CAPTCHA was sent successfully'); // 成功提示
            this.startCountdown();
          } else {
            alert(response.data.message || 'Failed to send the CAPTCHA'); // 失败提示
          }
        })
        .catch(error => {
          console.error('Captcha request error:', error);
          alert('Network error, please try again later');
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
        btn.textContent = `resend(${countdown})`;
        countdown--;
        if (countdown < 0) {
          clearInterval(timer);
          btn.disabled = false;
          btn.textContent = 'Getting a CAPTCHA';
        }
      }, 1000);
    },
    
    handleRegister() {
      if (this.registerForm.password !== this.registerForm.confirmPassword) {
        alert('The password is not the same');
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
          console.log('Registering Response data:', response.data);
          if (response.data.code === 1) {
            if (confirm(response.data.message || 'Successful registration, please log in')) { // 带确认按钮的弹窗
              this.$router.push('/login');
            }
          } else {
            alert(response.data.message || 'Registration failed');
          }
        })
        .catch(error => {
          console.error('Registration request error:', error);
          alert(error.response?.data?.message || 'Registration failed. Please check the network and try again');
        });
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