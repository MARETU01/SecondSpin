<script>

export default {
  data() {
    return {
      userInfo: {
        userId: 1,
        username: '测试用户',
        email: 'test@example.com',
        registrationDate: '2024-03-20 10:00:00',
        accountStatus: 'ACTIVE',
        realName: '',
        phone: ''
      },
      isEditing: false,
      tempUserInfo: {},
      showUploadDialog: false,
      activeTab: 'profile',
      tabs: [
        { id: 'profile', name: '个人信息' },
        { id: 'favorites', name: '我的收藏' },
        { id: 'history', name: '浏览记录' },
        { id: 'posts', name: '我的发布' },
        { id: 'security', name: '修改密码' }
      ],
      favoriteProducts: [],
      loading: false,
      error: null,
      currentPage: 1,
      pageSize: 10,
      totalPages: 1,
      totalItems: 0,
      viewHistory: [],
      historyLoading: false,
      historyError: null,
      historyCurrentPage: 1,
      historyPageSize: 10,
      historyTotalPages: 1,
      historyTotalItems: 0,
      myPosts: [],
      postsLoading: false,
      postsError: null,
      postsCurrentPage: 1,
      postsPageSize: 10,
      postsTotalPages: 1,
      postsTotalItems: 0
    }
  },
  methods: {
    fetchUserInfo() {
      this.$http.get(`/users/info/1`)
        .then(response => {
          console.log('获取用户信息响应:', response.data)
          if (response.data.code === 1) {
            // 更新用户信息
            this.userInfo = {
              ...this.userInfo,
              ...response.data.data
            }
          } else {
            alert(response.data.message || '获取用户信息失败')
          }
        })
        .catch(error => {
          console.error('获取用户信息错误:', error)
          alert(error.response?.data?.message || '网络错误，请稍后重试')
        })
    },
    handleAvatarUpload(event) {
      const file = event.target.files[0]
      if (file) {
        const reader = new FileReader()
        reader.onload = (e) => {
          this.userInfo.avatarUrl = e.target.result
          localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
          this.showUploadDialog = false
        }
        reader.readAsDataURL(file)
      }
    },
    openUploadDialog() {
      this.showUploadDialog = true
    },
    closeUploadDialog() {
      this.showUploadDialog = false
    },
    startEditing() {
      this.tempUserInfo = {
        realName: this.userInfo.realName,
        phone: this.userInfo.phone
      }
      this.isEditing = true
    },
    cancelEditing() {
      this.isEditing = false
    },
    saveProfile() {
      // 构建请求头中的用户信息
      const userJson = JSON.stringify({
        userId: this.userInfo.userId,
        username: this.userInfo.username,
        email: this.userInfo.email
      });

      // 构建请求体
      const userInfo = {
        realName: this.tempUserInfo.realName,
        phone: this.tempUserInfo.phone
      };

      this.$http.put('/users/info', userInfo, {
        headers: {
          'user-info': userJson
        }
      })
        .then(response => {
          console.log('更新用户信息响应:', response.data);
          if (response.data.code === 1) {
            // 更新成功，更新本地用户信息
            this.userInfo.realName = this.tempUserInfo.realName;
            this.userInfo.phone = this.tempUserInfo.phone;
            this.isEditing = false;
            localStorage.setItem('userInfo', JSON.stringify(this.userInfo));
            alert('保存成功！');
          } else {
            alert(response.data.message || '保存失败，请重试');
          }
        })
        .catch(error => {
          console.error('更新用户信息错误:', error);
          alert(error.response?.data?.message || '网络错误，请稍后重试');
        });
    },
    changePassword() {
      alert('密码修改成功！')
    },
    formatDate(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    },
    getTabIcon(tabId) {
      const icons = {
        profile: '👤',
        favorites: '❤️',
        history: '📜',
        posts: '📝',
        security: '🔒'
      }
      return icons[tabId] || ''
    },
    goToHome() {
      this.$router.push('/')
    },
    handleLogout() {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      this.$router.push('/')
    },
    fetchFavoriteProducts() {
      this.loading = true;
      this.error = null;
      
      this.$http.get('/favorites', {
        params: {
          page: this.currentPage,
          size: this.pageSize
        }
      })
        .then(response => {
          console.log('获取收藏商品响应:', response.data);
          if (response.data.code === 1) {
            this.favoriteProducts = response.data.data.data || [];
            this.totalPages = response.data.data.totalPage;
            this.totalItems = response.data.data.total;
            console.log('收藏商品列表:', this.favoriteProducts);
          } else {
            this.error = response.data.message || '获取收藏商品失败';
          }
        })
        .catch(error => {
          console.error('获取收藏商品错误:', error);
          this.error = error.response?.data?.message || '网络错误，请稍后重试';
        })
        .finally(() => {
          this.loading = false;
        });
    },
    handlePageChange(page) {
      this.currentPage = page;
      this.fetchFavoriteProducts();
    },
    removeFavorite(favoriteId) {
      this.$http.delete('/favorites', {
        params: {
          ids: favoriteId
        }
      })
        .then(response => {
          console.log('取消收藏响应:', response.data);
          if (response.data.code === 1) {
            // 从列表中移除该商品
            this.favoriteProducts = this.favoriteProducts.filter(
              item => item.favoriteId !== favoriteId
            );
            alert('取消收藏成功');
          } else {
            alert(response.data.message || '取消收藏失败');
          }
        })
        .catch(error => {
          console.error('取消收藏错误:', error);
          alert(error.response?.data?.message || '网络错误，请稍后重试');
        });
    },
    fetchViewHistory() {
      this.historyLoading = true;
      this.historyError = null;
      
      this.$http.get('/history', {
        params: {
          page: this.historyCurrentPage,
          size: this.historyPageSize
        }
      })
        .then(response => {
          console.log('获取浏览记录响应:', response.data);
          if (response.data.code === 1) {
            this.viewHistory = response.data.data.data || [];
            this.historyTotalPages = response.data.data.totalPage;
            this.historyTotalItems = response.data.data.total;
            console.log('浏览记录列表:', this.viewHistory);
          } else {
            this.historyError = response.data.message || '获取浏览记录失败';
          }
        })
        .catch(error => {
          console.error('获取浏览记录错误:', error);
          this.historyError = error.response?.data?.message || '网络错误，请稍后重试';
        })
        .finally(() => {
          this.historyLoading = false;
        });
    },
    handleHistoryPageChange(page) {
      this.historyCurrentPage = page;
      this.fetchViewHistory();
    },
    removeHistoryItem(historyId) {
      this.$http.delete('/history', {
        params: {
          ids: historyId
        }
      })
        .then(response => {
          console.log('删除浏览记录响应:', response.data);
          if (response.data.code === 1) {
            this.viewHistory = this.viewHistory.filter(
              item => item.historyId !== historyId
            );
            alert('删除成功');
          } else {
            alert(response.data.message || '删除失败');
          }
        })
        .catch(error => {
          console.error('删除浏览记录错误:', error);
          alert(error.response?.data?.message || '网络错误，请稍后重试');
        });
    },
    clearAllHistory() {
      if (confirm('确定要清空所有浏览记录吗？')) {
        this.$http.delete('/history/all')
          .then(response => {
            console.log('清空浏览记录响应:', response.data);
            if (response.data.code === 1) {
              this.viewHistory = [];
              alert('清空成功');
            } else {
              alert(response.data.message || '清空失败');
            }
          })
          .catch(error => {
            console.error('清空浏览记录错误:', error);
            alert(error.response?.data?.message || '网络错误，请稍后重试');
          });
      }
    },
    fetchMyPosts() {
      this.postsLoading = true;
      this.postsError = null;
      
      this.$http.get(`/products/post/${this.userInfo.userId}`, {
        params: {
          page: this.postsCurrentPage,
          size: this.postsPageSize
        }
      })
        .then(response => {
          console.log('获取个人发布响应:', response.data);
          if (response.data.code === 1) {
            this.myPosts = response.data.data.data || [];
            this.postsTotalPages = response.data.data.totalPage;
            this.postsTotalItems = response.data.data.total;
            console.log('个人发布列表:', this.myPosts);
          } else {
            this.postsError = response.data.message || '获取个人发布失败';
          }
        })
        .catch(error => {
          console.error('获取个人发布错误:', error);
          this.postsError = error.response?.data?.message || '网络错误，请稍后重试';
        })
        .finally(() => {
          this.postsLoading = false;
        });
    },
    handlePostsPageChange(page) {
      this.postsCurrentPage = page;
      this.fetchMyPosts();
    }
  },
  mounted() {
    this.fetchUserInfo();
    if (this.activeTab === 'favorites') {
      this.fetchFavoriteProducts();
    } else if (this.activeTab === 'history') {
      this.fetchViewHistory();
    } else if (this.activeTab === 'posts') {
      this.fetchMyPosts();
    }
  },
  watch: {
    activeTab(newTab) {
      if (newTab === 'favorites') {
        this.fetchFavoriteProducts();
      } else if (newTab === 'history') {
        this.fetchViewHistory();
      } else if (newTab === 'posts') {
        this.fetchMyPosts();
      }
    }
  }
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
            <span :class="['status-badge', userInfo.accountStatus === 'active' ? 'active' : 'inactive']">
              {{ userInfo.accountStatus === 'active' ? '正常' : '已禁用' }}
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
                  <span :class="['status-badge', userInfo.accountStatus === 'active' ? 'active' : 'inactive']">
                    {{ userInfo.accountStatus === 'active' ? '正常' : '已禁用' }}
                  </span>
                </span>
              </div>
            </div>
          </template>
        </div>

        <!-- 我的收藏 -->
        <div v-if="activeTab === 'favorites'" class="profile-section">
          <h3>我的收藏</h3>
          
          <div v-if="loading" class="loading-state">
            <i class="loading-icon">⏳</i>
            <p>加载中...</p>
          </div>
          
          <div v-else-if="error" class="error-state">
            <i class="error-icon">❌</i>
            <p>{{ error }}</p>
            <button class="btn retry-btn" @click="fetchFavoriteProducts">重试</button>
          </div>
          
          <div v-else-if="favoriteProducts.length === 0" class="empty-state">
            <i class="empty-icon">📚</i>
            <p>暂无收藏内容</p>
            <button class="btn explore-btn" @click="goToHome">去发现</button>
          </div>
          
          <template v-else>
            <div class="favorites-grid">
              <div v-for="item in favoriteProducts" :key="item.favoriteId" class="favorite-item">
                <div class="image-container">
                  <img 
                    v-if="item.product.primaryImageUrl"
                    :src="`/images/products/${item.product.primaryImageUrl}`" 
                    :alt="item.product.title" 
                    class="product-image" 
                  />
                  <div v-else class="no-image">
                    <i class="icon">🖼️</i>
                    <span>暂无图片</span>
                  </div>
                </div>
                <div class="product-info">
                  <h4>{{ item.product.title }}</h4>
                  <p class="price">¥{{ item.product.price }}</p>
                  <p class="original-price">原价: ¥{{ item.product.originalPrice }}</p>
                  <p class="condition">商品状态: {{ item.product.condition === 'new' ? '全新' : '二手' }}</p>
                  <p class="add-date">收藏时间: {{ formatDate(item.addDate) }}</p>
                </div>
                <button class="remove-btn" @click="removeFavorite(item.favoriteId)">
                  <i class="icon">❌</i> 取消收藏
                </button>
              </div>
            </div>
            
            <!-- 分页组件 -->
            <div class="pagination">
              <button 
                class="page-btn" 
                :disabled="currentPage === 1"
                @click="handlePageChange(currentPage - 1)"
              >
                上一页
              </button>
              <span class="page-info">
                第 {{ currentPage }} 页 / 共 {{ totalPages }} 页
              </span>
              <button 
                class="page-btn" 
                :disabled="currentPage === totalPages"
                @click="handlePageChange(currentPage + 1)"
              >
                下一页
              </button>
            </div>
          </template>
        </div>

        <!-- 浏览记录 -->
        <div v-if="activeTab === 'history'" class="profile-section">
          <div class="section-header">
            <h3>浏览记录</h3>
            <button v-if="viewHistory.length > 0" class="btn clear-btn" @click="clearAllHistory">
              <i class="icon">🗑️</i> 清空记录
            </button>
          </div>
          
          <div v-if="historyLoading" class="loading-state">
            <i class="loading-icon">⏳</i>
            <p>加载中...</p>
          </div>
          
          <div v-else-if="historyError" class="error-state">
            <i class="error-icon">❌</i>
            <p>{{ historyError }}</p>
            <button class="btn retry-btn" @click="fetchViewHistory">重试</button>
          </div>
          
          <div v-else-if="viewHistory.length === 0" class="empty-state">
            <i class="empty-icon">👀</i>
            <p>暂无浏览记录</p>
            <button class="btn explore-btn" @click="goToHome">去逛逛</button>
          </div>
          
          <template v-else>
            <div class="history-grid">
              <div v-for="item in viewHistory" :key="item.historyId" class="history-item">
                <div class="image-container">
                  <img 
                    v-if="item.product.primaryImageUrl"
                    :src="`/images/products/${item.product.primaryImageUrl}`" 
                    :alt="item.product.title" 
                    class="product-image" 
                  />
                  <div v-else class="no-image">
                    <i class="icon">🖼️</i>
                    <span>暂无图片</span>
                  </div>
                </div>
                <div class="product-info">
                  <h4>{{ item.product.title }}</h4>
                  <p class="price">¥{{ item.product.price }}</p>
                  <p class="original-price">原价: ¥{{ item.product.originalPrice }}</p>
                  <p class="condition">商品状态: {{ item.product.condition === 'new' ? '全新' : '二手' }}</p>
                  <p class="view-date">浏览时间: {{ formatDate(item.viewDate) }}</p>
                </div>
                <button class="remove-btn" @click="removeHistoryItem(item.historyId)">
                  <i class="icon">❌</i> 删除记录
                </button>
              </div>
            </div>
            
            <!-- 分页组件 -->
            <div class="pagination">
              <button 
                class="page-btn" 
                :disabled="historyCurrentPage === 1"
                @click="handleHistoryPageChange(historyCurrentPage - 1)"
              >
                上一页
              </button>
              <span class="page-info">
                第 {{ historyCurrentPage }} 页 / 共 {{ historyTotalPages }} 页
              </span>
              <button 
                class="page-btn" 
                :disabled="historyCurrentPage === historyTotalPages"
                @click="handleHistoryPageChange(historyCurrentPage + 1)"
              >
                下一页
              </button>
            </div>
          </template>
        </div>

        <!-- 我的发布 -->
        <div v-if="activeTab === 'posts'" class="profile-section">
          <div class="section-header">
            <h3>我的发布</h3>
            <button class="btn post-btn" @click="goToHome">
              <i class="icon">📝</i> 发布新商品
            </button>
          </div>
          
          <div v-if="postsLoading" class="loading-state">
            <i class="loading-icon">⏳</i>
            <p>加载中...</p>
          </div>
          
          <div v-else-if="postsError" class="error-state">
            <i class="error-icon">❌</i>
            <p>{{ postsError }}</p>
            <button class="btn retry-btn" @click="fetchMyPosts">重试</button>
          </div>
          
          <div v-else-if="myPosts.length === 0" class="empty-state">
            <i class="empty-icon">📝</i>
            <p>暂无发布内容</p>
            <button class="btn explore-btn" @click="goToHome">去发布</button>
          </div>
          
          <template v-else>
            <div class="posts-grid">
              <div v-for="item in myPosts" :key="item.productId" class="post-item">
                <div class="image-container">
                  <img 
                    v-if="item.primaryImageUrl"
                    :src="`/images/products/${item.primaryImageUrl}`" 
                    :alt="item.title" 
                    class="product-image" 
                  />
                  <div v-else class="no-image">
                    <i class="icon">🖼️</i>
                    <span>暂无图片</span>
                  </div>
                </div>
                <div class="product-info">
                  <h4>{{ item.title }}</h4>
                  <p class="price">¥{{ item.price }}</p>
                  <p class="original-price">原价: ¥{{ item.originalPrice }}</p>
                  <p class="condition">商品状态: {{ item.condition === 'new' ? '全新' : '二手' }}</p>
                  <p class="post-date">发布时间: {{ formatDate(item.postDate) }}</p>
                  <p class="view-count">浏览: {{ item.viewCount }}</p>
                  <p class="favorite-count">收藏: {{ item.favoriteCount }}</p>
                </div>
              </div>
            </div>
            
            <!-- 个人发布分页组件 -->
            <div class="pagination">
              <button 
                class="page-btn" 
                :disabled="postsCurrentPage === 1"
                @click="handlePostsPageChange(postsCurrentPage - 1)"
              >
                上一页
              </button>
              <span class="page-info">
                第 {{ postsCurrentPage }} 页 / 共 {{ postsTotalPages }} 页
              </span>
              <button 
                class="page-btn" 
                :disabled="postsCurrentPage === postsTotalPages"
                @click="handlePostsPageChange(postsCurrentPage + 1)"
              >
                下一页
              </button>
            </div>
          </template>
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

.loading-state,
.error-state {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}

.loading-icon,
.error-icon {
  font-size: 48px;
  margin-bottom: 20px;
  display: block;
}

.retry-btn {
  background: #ff5722;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: bold;
  margin-top: 20px;
}

.retry-btn:hover {
  background: #f4511e;
}

.favorites-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.favorite-item {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  transition: transform 0.3s;
}

.favorite-item:hover {
  transform: translateY(-5px);
}

.image-container {
  width: 100%;
  height: 200px;
  position: relative;
  background: #f5f5f5;
  overflow: hidden;
}

.no-image {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  color: #999;
  font-size: 14px;
  gap: 8px;
}

.no-image .icon {
  font-size: 32px;
  opacity: 0.5;
}

.no-image span {
  font-size: 12px;
}

.product-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.product-info {
  padding: 15px;
}

.product-info h4 {
  margin: 0 0 10px;
  color: #333;
  font-size: 16px;
}

.price {
  color: #ff5722;
  font-weight: bold;
  font-size: 18px;
  margin: 10px 0;
}

.original-price {
  color: #999;
  font-size: 14px;
  text-decoration: line-through;
  margin: 5px 0;
}

.condition {
  color: #666;
  font-size: 14px;
  margin: 5px 0;
}

.add-date {
  color: #999;
  font-size: 12px;
  margin: 5px 0;
}

.description {
  color: #666;
  font-size: 14px;
  margin: 10px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.remove-btn {
  width: 100%;
  padding: 10px;
  background: #f44336;
  color: white;
  border: none;
  cursor: pointer;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  transition: all 0.3s;
}

.remove-btn:hover {
  background: #d32f2f;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 30px;
  gap: 15px;
}

.page-btn {
  padding: 8px 16px;
  border: 1px solid #ddd;
  background: white;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.page-btn:hover:not(:disabled) {
  background: #ff5722;
  color: white;
  border-color: #ff5722;
}

.page-btn:disabled {
  background: #f5f5f5;
  color: #999;
  cursor: not-allowed;
}

.page-info {
  color: #666;
  font-size: 14px;
}

.history-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.history-item {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  transition: transform 0.3s;
}

.history-item:hover {
  transform: translateY(-5px);
}

.view-date {
  color: #999;
  font-size: 12px;
  margin: 5px 0;
}

.clear-btn {
  background: #f44336;
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

.clear-btn:hover {
  background: #d32f2f;
}

.posts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.post-item {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  transition: transform 0.3s;
}

.post-item:hover {
  transform: translateY(-5px);
}

.post-date {
  color: #999;
  font-size: 12px;
  margin: 5px 0;
}

.view-count,
.favorite-count {
  color: #666;
  font-size: 12px;
  margin: 5px 0;
}

.post-btn {
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

.post-btn:hover {
  background: #45a049;
}
</style>