<template>
  <header class="app-header">
    <div class="container">
      <div class="logo" @click="$router.push('/')">
        <h1>转转</h1>
        <span>turnturn</span>
      </div>
      
      <div class="user-actions">
        <button class="btn publish-btn" @click="$router.push('/publish')">
          <i class="icon">+</i> 发布商品
        </button>
        <button class="btn login-btn" @click="$router.push('/login')">
          登录/注册
        </button>
      </div>
    </div>
  </header>
</template>

<script>
export default {
  name: 'AppHeader'
}
</script>

<style scoped>
.app-header {
  background-color: #ff5722;
  color: white;
  padding: 15px 0;
  box-shadow: 0 2px 5px rgba(0,0,0,0.1);
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
}

.btn {
  padding: 8px 15px;
  border-radius: 4px;
  border: none;
  cursor: pointer;
  font-weight: bold;
  transition: all 0.3s;
}

.publish-btn {
  background-color: #fff;
  color: #ff5722;
}

.publish-btn:hover {
  background-color: #f5f5f5;
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
</style>