<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const userInfo = ref({
  userId: 1,
  username: '测试用户',
  email: 'test@example.com',
  registrationDate: '2024-03-20 10:00:00',
  accountStatus: 'ACTIVE',
  realName: '',
  phone: ''
})

const isEditing = ref(false)
const tempUserInfo = ref({})
const showUploadDialog = ref(false)

const activeTab = ref('profile')

const tabs = [
  { id: 'profile', name: '个人信息' },
  { id: 'favorites', name: '我的收藏' },
  { id: 'history', name: '浏览记录' },
  { id: 'posts', name: '我的发布' },
  { id: 'security', name: '修改密码' }
]

const handleAvatarUpload = (event) => {
  const file = event.target.files[0]
  if (file) {
    // 这里应该调用后端API上传头像
    const reader = new FileReader()
    reader.onload = (e) => {
      userInfo.value.avatarUrl = e.target.result
      // 保存更新后的用户信息到localStorage
      localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
      showUploadDialog.value = false
    }
    reader.readAsDataURL(file)
  }
}

const openUploadDialog = () => {
  showUploadDialog.value = true
}

const closeUploadDialog = () => {
  showUploadDialog.value = false
}

const startEditing = () => {
  tempUserInfo.value = {
    realName: userInfo.value.realName,
    phone: userInfo.value.phone
  }
  isEditing.value = true
}

const cancelEditing = () => {
  isEditing.value = false
}

const saveProfile = () => {
  userInfo.value.realName = tempUserInfo.value.realName
  userInfo.value.phone = tempUserInfo.value.phone
  isEditing.value = false
  // 保存用户信息到localStorage
  localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
  alert('保存成功！')
}

const changePassword = () => {
  // 这里应该调用后端API修改密码
  alert('密码修改成功！')
}

// 格式化日期显示
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 添加获取标签图标的函数
const getTabIcon = (tabId) => {
  const icons = {
    profile: '👤',
    favorites: '❤️',
    history: '📜',
    posts: '📝',
    security: '🔒'
  }
  return icons[tabId] || ''
}

// 添加跳转到首页的方法
const goToHome = () => {
  router.push('/')
}

// 添加退出登录方法
const handleLogout = () => {
  // 清除本地存储的用户信息和token
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  // 跳转到首页
  router.push('/')
}
</script>

<template>
  <div class="profile-page">
    <div class="container">
      <div class="profile-sidebar">
        <div class="user-card">
          <div class="avatar-container" @click="openUploadDialog">
            <img :src="userInfo.avatarUrl" alt="用户头像" class="avatar" />
            <div class="avatar-overlay">
              <i class="icon">📷</i>
            </div>
          </div>
          <h2>{{ userInfo.username }}</h2>
          <div class="user-status">
            <span :class="['status-badge', userInfo.accountStatus.toLowerCase()]">
              {{ userInfo.accountStatus === 'ACTIVE' ? '正常' : '已禁用' }}
            </span>
          </div>
          <div class="user-stats">
            <div class="stat-item">
              <span class="stat-value">12</span>
              <span class="stat-label">收藏</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">8</span>
              <span class="stat-label">发布</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">256</span>
              <span class="stat-label">浏览</span>
            </div>
          </div>
          <!-- 添加退出登录按钮 -->
          <button class="logout-btn" @click="handleLogout">
            <i class="icon">🚪</i> 退出登录
          </button>
        </div>
        
        <nav class="profile-nav">
          <button
            v-for="tab in tabs"
            :key="tab.id"
            :class="['nav-item', { active: activeTab === tab.id }]"
            @click="activeTab = tab.id"
          >
            <i :class="getTabIcon(tab.id)"></i>
            {{ tab.name }}
          </button>
        </nav>
      </div>

      <div class="profile-content">
        <!-- 个人信息 -->
        <div v-if="activeTab === 'profile'" class="profile-section">
          <div class="section-header">
            <h3>个人信息</h3>
            <button v-if="!isEditing" class="btn edit-btn" @click="startEditing">
              <i class="icon">✏️</i> 编辑
            </button>
          </div>
          
          <template v-if="isEditing">
            <form @submit.prevent="saveProfile" class="profile-form">
              <div class="form-group">
                <label>用户名</label>
                <input type="text" :value="userInfo.username" disabled />
                <span class="field-hint">用户名不可修改</span>
              </div>
              <div class="form-group">
                <label>邮箱</label>
                <input type="email" :value="userInfo.email" disabled />
                <span class="field-hint">邮箱不可修改</span>
              </div>
              <div class="form-group">
                <label>真实姓名</label>
                <input type="text" v-model="tempUserInfo.realName" placeholder="请输入真实姓名" />
                <span class="field-hint">选填</span>
              </div>
              <div class="form-group">
                <label>手机号</label>
                <input type="tel" v-model="tempUserInfo.phone" placeholder="请输入手机号" />
                <span class="field-hint">选填</span>
              </div>
              <div class="form-actions">
                <button type="button" class="btn cancel-btn" @click="cancelEditing">
                  <i class="icon">❌</i> 取消
                </button>
                <button type="submit" class="btn save-btn">
                  <i class="icon">💾</i> 保存
                </button>
              </div>
            </form>
          </template>
          
          <template v-else>
            <div class="info-display">
              <div class="info-item">
                <span class="label">用户名：</span>
                <span class="value">{{ userInfo.username }}</span>
              </div>
              <div class="info-item">
                <span class="label">邮箱：</span>
                <span class="value">{{ userInfo.email }}</span>
              </div>
              <div class="info-item">
                <span class="label">真实姓名：</span>
                <span class="value">{{ userInfo.realName || '未设置' }}</span>
              </div>
              <div class="info-item">
                <span class="label">手机号：</span>
                <span class="value">{{ userInfo.phone || '未设置' }}</span>
              </div>
              <div class="info-item">
                <span class="label">注册时间：</span>
                <span class="value">{{ formatDate(userInfo.registrationDate) }}</span>
              </div>
              <div class="info-item">
                <span class="label">账号状态：</span>
                <span class="value">
                  <span :class="['status-badge', userInfo.accountStatus.toLowerCase()]">
                    {{ userInfo.accountStatus === 'ACTIVE' ? '正常' : '已禁用' }}
                  </span>
                </span>
              </div>
            </div>
          </template>
        </div>

        <!-- 我的收藏 -->
        <div v-if="activeTab === 'favorites'" class="profile-section">
          <h3>我的收藏</h3>
          <div class="empty-state">
            <i class="empty-icon">📚</i>
            <p>暂无收藏内容</p>
            <button class="btn explore-btn" @click="goToHome">去发现</button>
          </div>
        </div>

        <!-- 浏览记录 -->
        <div v-if="activeTab === 'history'" class="profile-section">
          <h3>浏览记录</h3>
          <div class="empty-state">
            <i class="empty-icon">👀</i>
            <p>暂无浏览记录</p>
            <button class="btn explore-btn" @click="goToHome">去逛逛</button>
          </div>
        </div>

        <!-- 我的发布 -->
        <div v-if="activeTab === 'posts'" class="profile-section">
          <h3>我的发布</h3>
          <div class="empty-state">
            <i class="empty-icon">📝</i>
            <p>暂无发布内容</p>
            <button class="btn explore-btn" @click="goToHome">去发布</button>
          </div>
        </div>

        <!-- 修改密码 -->
        <div v-if="activeTab === 'security'" class="profile-section">
          <h3>修改密码</h3>
          <form @submit.prevent="changePassword" class="profile-form">
            <div class="form-group">
              <label>当前密码</label>
              <input type="password" />
            </div>
            <div class="form-group">
              <label>新密码</label>
              <input type="password" />
            </div>
            <div class="form-group">
              <label>确认新密码</label>
              <input type="password" />
            </div>
            <button type="submit" class="btn save-btn">
              <i class="icon">🔒</i> 修改密码
            </button>
          </form>
        </div>

        <!-- 使用指南区域 -->
        <div class="guide-section">
          <h3>使用指南</h3>
          <div class="guide-grid">
            <div class="guide-card">
              <div class="guide-icon">📝</div>
              <div class="guide-content">
                <h4>如何发布内容</h4>
                <ul>
                  <li>点击首页的"发布"按钮</li>
                  <li>选择要发布的内容类型</li>
                  <li>填写标题和详细描述</li>
                  <li>上传相关图片</li>
                  <li>点击发布即可</li>
                </ul>
              </div>
            </div>
            <div class="guide-card">
              <div class="guide-icon">🔍</div>
              <div class="guide-content">
                <h4>如何搜索内容</h4>
                <ul>
                  <li>使用顶部搜索栏</li>
                  <li>输入关键词或标签</li>
                  <li>选择筛选条件</li>
                  <li>点击搜索按钮</li>
                </ul>
              </div>
            </div>
            <div class="guide-card">
              <div class="guide-icon">💬</div>
              <div class="guide-content">
                <h4>如何互动交流</h4>
                <ul>
                  <li>在内容下方发表评论</li>
                  <li>点赞感兴趣的内容</li>
                  <li>收藏优质内容</li>
                  <li>关注感兴趣的用户</li>
                </ul>
              </div>
            </div>
          </div>
        </div>

        <!-- 平台介绍区域 -->
        <div class="platform-section">
          <h3>关于我们</h3>
          <div class="platform-content">
            <p>SecondSpin是一个专注于二手交易的社区平台，我们致力于：</p>
            <div class="platform-features">
              <div class="feature-item">
                <span class="feature-icon">🔄</span>
                <span class="feature-text">促进物品循环利用</span>
              </div>
              <div class="feature-item">
                <span class="feature-icon">🤝</span>
                <span class="feature-text">建立诚信交易环境</span>
              </div>
              <div class="feature-item">
                <span class="feature-icon">🌱</span>
                <span class="feature-text">践行环保理念</span>
              </div>
              <div class="feature-item">
                <span class="feature-icon">👥</span>
                <span class="feature-text">打造友好社区氛围</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 头像上传弹窗 -->
    <div v-if="showUploadDialog" class="upload-dialog-overlay" @click="closeUploadDialog">
      <div class="upload-dialog" @click.stop>
        <div class="upload-dialog-header">
          <h3>更换头像</h3>
          <button class="close-btn" @click="closeUploadDialog">&times;</button>
        </div>
        <div class="upload-dialog-content">
          <div class="upload-area">
            <input
              type="file"
              accept="image/*"
              @change="handleAvatarUpload"
              class="file-input"
              id="avatar-upload"
            />
            <label for="avatar-upload" class="upload-label">
              <div class="upload-icon">📁</div>
              <div class="upload-text">点击选择图片</div>
              <div class="upload-hint">支持 jpg、png 格式</div>
            </label>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.profile-page {
  padding: 40px 0;
  background-color: #f5f5f5;
  min-height: calc(100vh - 70px);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 15px;
  display: flex;
  gap: 30px;
}

.profile-sidebar {
  width: 280px;
  flex-shrink: 0;
}

.user-card {
  background: white;
  padding: 30px 20px;
  border-radius: 12px;
  text-align: center;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.avatar-container {
  position: relative;
  width: 120px;
  height: 120px;
  margin: 0 auto 20px;
  cursor: pointer;
  transition: transform 0.3s;
}

.avatar-container:hover {
  transform: scale(1.05);
}

.avatar {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #fff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0,0,0,0.5);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.avatar-container:hover .avatar-overlay {
  opacity: 1;
}

.user-stats {
  display: flex;
  justify-content: space-around;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-value {
  font-size: 20px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 12px;
  color: #666;
  margin-top: 4px;
}

.profile-nav {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.nav-item {
  display: flex;
  align-items: center;
  width: 100%;
  padding: 15px 20px;
  text-align: left;
  border: none;
  background: none;
  cursor: pointer;
  transition: all 0.3s;
  color: #666;
}

.nav-item i {
  margin-right: 10px;
  font-size: 18px;
}

.nav-item:hover {
  background: #f5f5f5;
  color: #ff5722;
}

.nav-item.active {
  background: #ff5722;
  color: white;
}

.profile-content {
  flex: 1;
  background: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 15px;
  border-bottom: 2px solid #f5f5f5;
}

.section-header h3 {
  margin: 0;
  color: #333;
  font-size: 24px;
}

.edit-btn {
  background: #4CAF50;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: bold;
  display: flex;
  align-items: center;
  gap: 5px;
}

.edit-btn:hover {
  background: #45a049;
}

.info-display {
  max-width: 600px;
}

.info-item {
  margin-bottom: 20px;
  padding: 15px;
  background: #f9f9f9;
  border-radius: 8px;
  transition: all 0.3s;
}

.info-item:hover {
  background: #f0f0f0;
  transform: translateX(5px);
}

.info-item .label {
  color: #666;
  font-weight: bold;
  margin-right: 10px;
}

.info-item .value {
  color: #333;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 20px;
  display: block;
}

.explore-btn {
  background: #ff5722;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: bold;
  margin-top: 20px;
}

.explore-btn:hover {
  background: #f4511e;
}

.form-group {
  margin-bottom: 25px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #666;
  font-weight: bold;
}

.form-group input {
  width: 100%;
  padding: 12px;
  border: 2px solid #eee;
  border-radius: 6px;
  font-size: 14px;
  transition: all 0.3s;
}

.form-group input:focus {
  border-color: #ff5722;
  outline: none;
  box-shadow: 0 0 0 3px rgba(255,87,34,0.1);
}

.form-actions {
  display: flex;
  gap: 15px;
  margin-top: 30px;
}

.cancel-btn {
  background: #9e9e9e;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: bold;
  display: flex;
  align-items: center;
  gap: 5px;
}

.cancel-btn:hover {
  background: #757575;
}

.save-btn {
  background: #ff5722;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: bold;
  display: flex;
  align-items: center;
  gap: 5px;
}

.save-btn:hover {
  background: #f4511e;
}

.user-status {
  margin-top: 10px;
}

.status-badge {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
}

.status-badge.active {
  background-color: #4CAF50;
  color: white;
}

.status-badge.inactive {
  background-color: #f44336;
  color: white;
}

.upload-dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.upload-dialog {
  background: white;
  border-radius: 8px;
  width: 400px;
  max-width: 90%;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
}

.upload-dialog-header {
  padding: 16px 20px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.upload-dialog-header h3 {
  margin: 0;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  color: #999;
  cursor: pointer;
  padding: 0;
  line-height: 1;
}

.close-btn:hover {
  color: #666;
}

.upload-dialog-content {
  padding: 20px;
}

.upload-area {
  border: 2px dashed #ddd;
  border-radius: 8px;
  padding: 30px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.upload-area:hover {
  border-color: #ff5722;
}

.file-input {
  display: none;
}

.upload-label {
  cursor: pointer;
}

.upload-icon {
  font-size: 48px;
  margin-bottom: 10px;
}

.upload-text {
  font-size: 16px;
  color: #333;
  margin-bottom: 8px;
}

.upload-hint {
  font-size: 12px;
  color: #999;
}

.guide-section {
  margin-top: 40px;
  padding-top: 30px;
  border-top: 2px solid #f5f5f5;
}

.guide-section h3 {
  margin-bottom: 20px;
  color: #333;
  font-size: 20px;
}

.guide-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.guide-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  transition: transform 0.3s;
}

.guide-card:hover {
  transform: translateY(-5px);
}

.guide-icon {
  font-size: 32px;
  background: #f5f5f5;
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 15px;
}

.guide-content h4 {
  margin: 0 0 15px;
  color: #333;
  font-size: 18px;
}

.guide-content ul {
  margin: 0;
  padding-left: 20px;
  color: #666;
}

.guide-content li {
  margin-bottom: 8px;
  line-height: 1.5;
}

.platform-section {
  margin-top: 40px;
  padding-top: 30px;
  border-top: 2px solid #f5f5f5;
}

.platform-section h3 {
  margin-bottom: 20px;
  color: #333;
  font-size: 20px;
}

.platform-content {
  background: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.platform-content p {
  margin: 0 0 20px;
  color: #666;
  font-size: 16px;
  line-height: 1.6;
}

.platform-features {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 15px;
  background: #f9f9f9;
  border-radius: 8px;
  transition: transform 0.3s;
}

.feature-item:hover {
  transform: translateX(5px);
}

.feature-icon {
  font-size: 24px;
}

.feature-text {
  color: #333;
  font-size: 16px;
}

.field-hint {
  display: block;
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.form-group input:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

.logout-btn {
  width: 100%;
  margin-top: 20px;
  padding: 12px;
  background-color: #f44336;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.3s;
}

.logout-btn:hover {
  background-color: #d32f2f;
}

.logout-btn .icon {
  font-size: 18px;
}
</style>