<template>
  <header class="app-header">
    <div class="container">
      <div class="logo" @click="$router.push('/')">
        <h1>转转</h1>
        <span>turnturn</span>
      </div>
      
      <div class="user-actions">
        <div class="auth-section">
          <button v-if="!isLoggedIn" class="btn login-btn" @click="$router.push('/login')">
            Login/Register
          </button>
          <div v-else class="logged-in-section">
            <button class="btn message-btn" @click="$router.push('/messages')">
              <i class="icon">💬</i> Message
              <span v-if="unreadCount > 0" class="badge">{{ unreadCount }}</span>
            </button>
            <button class="btn publish-btn" @click="$router.push('/create')">
              <i class="icon">+</i> Publish
            </button>
            <div class="user-avatar" @click="gotoProfile">
              <img :src="userAvatar" alt="用户头像" />
            </div>
          </div>
        </div>
      </div>
    </div>
  </header>
</template>

<script>
export default {
  name: 'AppHeader',
  data() {
    return {
      isLoggedIn: false,
      userAvatar: '/public/images/avatar/default.png',
      unreadCount: 0,
      pollInterval: null
    }
  },
  created() {
    this.checkLoginStatus()
  },
  beforeUnmount() {  // 将 beforeDestroy 改为 beforeUnmount
    // 清除定时器
    if (this.pollInterval) {
      clearInterval(this.pollInterval)
    }
  },
  methods: {
    checkLoginStatus() {
      const token = localStorage.getItem('token')
      if (token) {
        this.validateUser()
      } else {
        this.isLoggedIn = false
      }
    },
    validateUser() {
      this.$http.get('/users/validate').then(response => {
        if (response.data.code === 1 && response.data.data) {
          this.isLoggedIn = true
          const user = response.data.data
          this.userId = user.id
          this.userAvatar = `/public/images/avatar/${user.avatarUrl}` || '/public/images/avatar/default.png'
          localStorage.setItem('userInfo', JSON.stringify(user))
          
          // 登录成功后开始获取未读消息
          this.fetchUnreadMessages()
          // 设置定时轮询（每30秒检查一次）
          this.pollInterval = setInterval(this.fetchUnreadMessages, 30000)
        } else {
          this.handleLogout()
        }
      }).catch(() => {
        this.handleLogout()
      })
    },
    fetchUnreadMessages() {
      if (!this.isLoggedIn) return
      
      this.$http.get('/messages/unread').then(response => {
        if (response.data.code === 1) {
          this.unreadCount = response.data.data || 0
        }
      }).catch(error => {
        console.error('Failed to get unread messages:', error)
        // 如果token失效，执行登出
        if (error.response && error.response.status === 401) {
          this.handleLogout()
        }
      })
    },
    handleLogout() {
      this.isLoggedIn = false
      this.unreadCount = 0
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      if (this.pollInterval) {
        clearInterval(this.pollInterval)
      }
    },
    gotoProfile() {
      const user = JSON.parse(localStorage.getItem('userInfo'))
      if (user) {
        this.userAvatar = user.avatarUrl
        this.$router.push(`/profile/${user.userId}`)
      }
    }
  }
}
</script>

<style scoped>
.app-header {
  background-color: #ff5722;
  color: white;
  padding: 15px 0;
  box-shadow: 0 2px 5px rgba(0,0,0,0.1);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 15px;
}

.logo {
  display: flex;
  align-items: baseline;
  cursor: pointer;
}

.logo h1 {
  margin: 0;
  font-size: 1.8rem;
}

.logo span {
  margin-left: 10px;
  font-size: 0.9rem;
  opacity: 0.8;
}

.user-actions {
  display: flex;
  gap: 15px;
  align-items: center;
}

.auth-section {
  display: flex;
  align-items: center;
  gap: 15px;
}

.logged-in-section {
  display: flex;
  align-items: center;
  gap: 15px;
}

.btn {
  padding: 8px 15px;
  border-radius: 4px;
  border: none;
  cursor: pointer;
  font-weight: bold;
  transition: all 0.3s;
  position: relative;
}

.publish-btn {
  background-color: #fff;
  color: #ff5722;
}

.publish-btn:hover {
  background-color: #f5f5f5;
}

.message-btn {
  background-color: transparent;
  color: white;
  border: 1px solid white;
  padding: 8px 12px;
}

.message-btn:hover {
  background-color: rgba(255,255,255,0.1);
}

.login-btn {
  background-color: transparent;
  color: white;
  border: 1px solid white;
}

.login-btn:hover {
  background-color: rgba(255,255,255,0.1);
}

.icon {
  margin-right: 5px;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid white;
  transition: transform 0.3s;
}

.user-avatar:hover {
  transform: scale(1.05);
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.badge {
  position: absolute;
  top: -8px;
  right: -8px;
  background-color: #ffeb3b;
  color: #333;
  border-radius: 50%;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: bold;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
  100% {
    transform: scale(1);
  }
}
</style>