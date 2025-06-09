<template>
  <div>
    <AppHeader />
    <div class="reset-password-container">
      <h2>Reset Password</h2>
      <form @submit.prevent="handleResetPassword">
        <div>
          <label for="reset-email">Email:</label>
          <input v-model="resetForm.email" type="email" id="reset-email" required>
        </div>
        <div>
          <label for="reset-new-password">New password:</label>
          <input v-model="resetForm.newPassword" type="password" id="reset-new-password" required>
        </div>
        <div>
          <label for="reset-confirm-password">Confirm new password:</label>
          <input v-model="resetForm.confirmPassword" type="password" id="reset-confirm-password" required>
        </div>
        <div>
          <label for="reset-verification">Verification code:</label>
          <input v-model="verification" type="text" id="reset-verification" required>
          <button type="button" @click="sendVerificationCode" ref="verificationBtn">Getting a CAPTCHA</button>
        </div>
        <button type="submit">Reset Password</button>
      </form>
      <p><router-link to="/login">Return to Login</router-link></p>
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
      console.log('1. The method starts executing');
      if (this.isSendingCode) return;
      this.isSendingCode = true;
      console.log('2. Anti-repeated click logic passes');

      if (this.resetForm.newPassword !== this.resetForm.confirmPassword) {
        alert('The new password entered twice does not match'); 
        this.isSendingCode = false;
        return;
      }
      console.log('3. The password is validated and ready to send the request');

      this.$http.post('/users/forget-password/code', { email: this.resetForm.email })
        .then(response => {
          console.log('Captcha response data:', response.data);
          if (response.data.code === 1) {
            alert(response.data.message || 'The CAPTCHA was sent successfully'); 
            this.startCountdown();
          } else {
            alert(response.data.message || 'Failed to send the CAPTCHA'); 
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

    handleResetPassword() {
      if (this.resetForm.newPassword !== this.resetForm.confirmPassword) {
        alert('The new password entered twice does not match');
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
          console.log('Reset password response data:', response.data);
          if (response.data.code === 1) {
            if (confirm(response.data.message || 'Password reset successfully, please login with the new password')) { 
              this.$router.push('/login');
            }
          } else {
            alert(response.data.message || 'Password reset failed');
          }
        })
        .catch(error => {
          console.error('Error in password reset request:', error);
          alert(error.response?.data?.message || 'Password reset failed, please check the network and try again');
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