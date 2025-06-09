<script>
import Header from '@/components/AppHeader.vue'
import Footer from '@/components/AppFooter.vue'

export default {
  components: {
    Header,
    Footer
  },
  props: {
    id: {
      type: [String, Number],
      required: true
    }
  },
  data() {
    return {
      userInfo: {
        userId: 1,
        username: 'Test users',
        email: 'test@example.com',
        registrationDate: '2024-03-20 10:00:00',
        accountStatus: 'ACTIVE',
        realName: '',
        phone: ''
      },
      isCurrentUserProfile: false,
      isEditing: false,
      tempUserInfo: {},
      showUploadDialog: false,
      activeTab: 'profile',
      tabs: [
        { id: 'profile', name: 'Personal Information' },
        { id: 'address', name: 'Address of delivery' },
        { id: 'favorites', name: 'My Collection' },
        { id: 'history', name: 'Browsing history' },
        { id: 'orders', name: 'My order' },
        { id: 'posts', name: 'My Release' },
        { id: 'security', name: 'Change your password' }
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
      postsTotalItems: 0,
      passwordForm: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: '',
        verificationCode: ''
      },
      isSendingCode: false,
      countdown: 0,

      // 订单相关数据
      orders: [],
      ordersLoading: false,
      ordersError: null,
      ordersCurrentPage: 1,
      ordersPageSize: 10,
      ordersTotalPages: 1,
      ordersTotalItems: 0,

      addressList: [],  // 收货地址列表
      addressLoading: false,  // 收货地址加载状态
      addressError: null,  // 收货地址错误信息
      isEditingAddress: false,  // 收货地址编辑状态
      tempAddress: {},  // 收货地址临时数据
      currentAddressId: null  // 当前编辑的地址ID

    }
  },
  methods: {
    fetchUserInfo() {
      this.$http.get(`/users/info/${this.id}`)
        .then(response => {
          console.log('Get the user information response:', response.data)
          if (response.data.code === 1) {
            // 更新用户信息
            this.userInfo = {
              ...this.userInfo,
              ...response.data.data
            }
            // 判断是否是当前用户自己的主页 based on realName field
            if (response.data.data.realName !== null && response.data.data.phone !== null) {
              this.isCurrentUserProfile = true
            } else {
              this.isCurrentUserProfile = false
            }
          } else {
            alert(response.data.message || 'Failed to retrieve user information')
          }
        })
        .catch(error => {
          console.error('Error getting user information:', error)
          alert(error.response?.data?.message || 'Network error, please try again later')
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
          console.log('Update the user information response:', response.data);
          if (response.data.code === 1) {
            // 更新成功，更新本地用户信息
            this.userInfo.realName = this.tempUserInfo.realName;
            this.userInfo.phone = this.tempUserInfo.phone;
            this.isEditing = false;
            localStorage.setItem('userInfo', JSON.stringify(this.userInfo));
            alert('Saved successfully!');
          } else {
            alert(response.data.message || 'Failed to save, please try again');
          }
        })
        .catch(error => {
          console.error('Error updating user information:', error);
          alert(error.response?.data?.message || 'Network error, please try again later');
        });
    },
    sendVerificationCode() {
      if (this.isSendingCode) return;
      
      this.$http.post('/users/reset-password/code', 
        { password: this.passwordForm.oldPassword }
      )
        .then(response => {
          console.log('Send the CAPTCHA response:', response.data);
          if (response.data.code === 1) {
            alert('The verification code has been sent to your email');
            this.startCountdown();
          } else {
            alert(response.data.message || 'Failed to send the CAPTCHA');
          }
        })
        .catch(error => {
          console.error('Error sending CAPtCHA:', error);
          alert(error.response?.data?.message || 'Network error, please try again later');
        });
    },
    startCountdown() {
      this.isSendingCode = true;
      this.countdown = 60;
      const timer = setInterval(() => {
        this.countdown--;
        if (this.countdown <= 0) {
          clearInterval(timer);
          this.isSendingCode = false;
        }
      }, 1000);
    },
    changePassword() {
      // 验证新密码和确认密码是否一致
      if (this.passwordForm.newPassword !== this.passwordForm.confirmPassword) {
        alert('The new password and the confirmation password do not match');
        return;
      }

      this.$http.post('/users/reset-password', 
        { password: this.passwordForm.newPassword },
        {
          params: {
            verification: this.passwordForm.verificationCode
          }
        }
      )
        .then(response => {
          console.log('Change Password response:', response.data);
          if (response.data.code === 1) {
            alert('Password changed successfully');
            // 清空表单
            this.passwordForm = {
              oldPassword: '',
              newPassword: '',
              confirmPassword: '',
              verificationCode: ''
            };
            // 清除 token 并跳转到登录页
            localStorage.removeItem('token')
            localStorage.removeItem('userInfo')
            this.$router.push('/login')
          } else {
            alert(response.data.message || 'Failed to change password');
          }
        })
        .catch(error => {
          console.error('Wrong password change:', error);
          alert(error.response?.data?.message || 'Network error, please try again later');
        });
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
        address: '📍',  
        favorites: '❤️',
        history: '📜',
        orders: '📦',
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
      this.$router.push('/login')
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
          console.log('Get the favorite item response:', response.data);
          if (response.data.code === 1) {
            this.favoriteProducts = response.data.data.data || [];
            this.totalPages = response.data.data.totalPage;
            this.totalItems = response.data.data.total;
            console.log('List of Favorite products:', this.favoriteProducts);
          } else {
            this.error = response.data.message || 'Failed to get favorites';
          }
        })
        .catch(error => {
          console.error('Error retrieving favorites:', error);
          this.error = error.response?.data?.message || 'Network error, please try again later';
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
      // 找到要删除的收藏项
      const favoriteItem = this.favoriteProducts.find(item => item.favoriteId === favoriteId);
      if (!favoriteItem) return;

      this.$http.delete('/favorites', {
        params: {
          ids: favoriteItem.product.productId
        }
      })
        .then(response => {
          console.log('Unfavorite response:', response.data);
          if (response.data.code === 1) {
            // 从列表中移除该商品
            this.favoriteProducts = this.favoriteProducts.filter(
              item => item.favoriteId !== favoriteId
            );
            alert('Cancel collection successfully');
          } else {
            alert(response.data.message || 'Failure to cancel favorites');
          }
        })
        .catch(error => {
          console.error('Unbookmark error:', error);
          alert(error.response?.data?.message || 'Network error, please try again later');
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
          console.log('Get the browsing history response:', response.data);
          if (response.data.code === 1) {
            this.viewHistory = response.data.data.data || [];
            this.historyTotalPages = response.data.data.totalPage;
            this.historyTotalItems = response.data.data.total;
            console.log('Browsing history list:', this.viewHistory);
          } else {
            this.historyError = response.data.message || 'Failed to get browsing history';
          }
        })
        .catch(error => {
          console.error('Get the browsing log error:', error);
          this.historyError = error.response?.data?.message || 'Network error, please try again later';
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
          console.log('Delete the browsing history response:', response.data);
          if (response.data.code === 1) {
            this.viewHistory = this.viewHistory.filter(
              item => item.historyId !== historyId
            );
            alert('Delete successfully');
          } else {
            alert(response.data.message || 'Deletion failure');
          }
        })
        .catch(error => {
          console.error('Delete browsing history error:', error);
          alert(error.response?.data?.message || 'Network error, please try again later');
        });
    },
    clearAllHistory() {
      if (confirm('Are you sure you want to clear all your browsing history?')) {
        this.$http.delete('/history/all')
          .then(response => {
            console.log('Clear browsing history response:', response.data);
            if (response.data.code === 1) {
              this.viewHistory = [];
              alert('Empty successfully');
            } else {
              alert(response.data.message || 'Failure to empty');
            }
          })
          .catch(error => {
            console.error('Clear browsing record error:', error);
            alert(error.response?.data?.message || 'Network error, please try again later');
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
          console.log('Get individual release responses:', response.data);
          if (response.data.code === 1) {
            this.myPosts = response.data.data.data || [];
            this.postsTotalPages = response.data.data.totalPage;
            this.postsTotalItems = response.data.data.total;
            console.log('Personal Posting list:', this.myPosts);
          } else {
            this.postsError = response.data.message || 'Failed to obtain a personal release';
          }
        })
        .catch(error => {
          console.error('Get personal release errors:', error);
          this.postsError = error.response?.data?.message || 'Network error, please try again later';
        })
        .finally(() => {
          this.postsLoading = false;
        });
    },
    handlePostsPageChange(page) {
      this.postsCurrentPage = page;
      this.fetchMyPosts();
    },
    fetchOrders() {
      this.ordersLoading = true; // 设置加载状态为true
      this.ordersError = null; // 清除之前的错误信息

      const queryDTO = {
        pageNo: this.ordersCurrentPage, // 当前页码
        pageSize: this.ordersPageSize, // 每页显示数量
        filter: 'all', // 过滤条件，可以根据需要调整
        sortBy: 'createTime', // 排序字段，可以根据需要调整
        isAsc: false // 排序顺序，false表示降序
      };

      // 构建请求头中的用户信息
      const userJson = JSON.stringify({
        userId: this.userInfo.userId,
        username: this.userInfo.username,
        email: this.userInfo.email
      });

      // 发起GET请求获取订单
      this.$http.get('/orders', {
        params: queryDTO,
        headers: {
          'user-info': userJson
        }
      })
          .then(response => {
            console.log('Getting the order response:', response.data);
            if (response.data.code === 1) {
              // 根据后端返回的数据结构提取订单列表
              const orderData = response.data.data;
              this.orders = orderData.data || []; // 订单列表
              this.ordersTotalPages = orderData.totalPage || 1; // 总页数
              this.ordersTotalItems = orderData.total || 0; // 总记录数
            } else {
              // 处理错误信息
              this.ordersError = response.data.message || 'Failed to get order';
              alert(this.ordersError);
            }
          })
          .catch(error => {
            console.error('Get order error:', error);
            this.ordersError = error.response?.data?.message || 'Network error, please try again later';
            alert(this.ordersError);
          })
          .finally(() => {
            this.ordersLoading = false; // 无论成功失败，都结束加载状态
          });
    },

    // 处理订单分页变化
    handleOrdersPageChange(page) {
      this.ordersCurrentPage = page; // 更新当前页码
      this.fetchOrders(); // 重新获取订单列表
    },

    handlePayment(order) {
      // 构建支付请求数据
      const paymentRequest = {
        out_trade_no: order.orderId.toString(), // 假设orderId可以作为交易号
        subject: `购买商品: ${order.title}`, // 订单标题
        total_amount: order.price // 订单价格
      };

      // 发送支付请求
      this.$http.post('/payment/create', paymentRequest)
          .then(response => {
            // 直接从响应中获取数据，不需要检查code
            // const paymentUrl = response.data.paymentUrl;
            // window.location.href = paymentUrl; // 跳转到支付宝支付页面
            console.log(response.data);
            if (response.data.code === 1) {
              const newWindow = window.open('', '_blank');
              // 将返回的HTML写入新窗口
              newWindow.document.open();
              newWindow.document.write(response.data.data); // 假设返回的HTML在data.data中
              newWindow.document.close();
            }
            
          })
          .catch(error => {
            console.error('Payment error:', error);
            alert(error.response?.data?.message || 'Network error, please try again later');
          });
    },


    fetchAddressList() {
      this.addressLoading = true;
      this.addressError = null;

      this.$http.get('/address', {
        headers: {
          'user-info': JSON.stringify({
            userId: this.userInfo.userId,
            username: this.userInfo.username,
            email: this.userInfo.email
          })
        }
      })
          .then(response => {
            console.log('Get the shipping address response:', response.data);
            if (response.data.code === 1) {
              this.addressList = response.data.data || [];
            } else {
              this.addressError = response.data.message || 'Failed to get shipping address';
            }
          })
          .catch(error => {
            console.error('Error in getting shipping address:', error);
            this.addressError = error.response?.data?.message || 'Network error, please try again later';
          })
          .finally(() => {
            this.addressLoading = false;
          });
    },

    startEditingAddress(address) {
      this.currentAddressId = address.addressId;
      this.tempAddress = {
        ...address,
        province: address.province || '', // 确保字段是字符串
        city: address.city || '',
        district: address.district || '',
        detailAddress: address.detailAddress || '',
        isDefault: address.isDefault || false,
      };
      this.isEditingAddress = true;
    },

    cancelEditingAddress() {
      this.isEditingAddress = false;
      this.currentAddressId = null;
      this.tempAddress = {}; // 清空 tempAddress
    },



    saveAddress() {
      // 1. 拼接完整地址（省份 + 城市 + 区县 + 详细地址）
      const detailAddress = `${this.tempAddress.province} ${this.tempAddress.city} ${this.tempAddress.district} ${this.tempAddress.detailAddress}`;

      // 2. 构建最终发送给后端的数据对象
      const addressData = {
        recipientName: this.tempAddress.receiverName,  // 收货人姓名（字段名改为 recipient_name）
        phone: this.tempAddress.receiverPhone,          // 手机号（映射为 phone）
        detailAddress,                                // 拼接后的完整地址
        defaultAddress: this.tempAddress.isDefault ? 1 : 0,  // 默认地址（tinyint）
      };

      // 3. 发送请求到后端
      this.$http.post('/address', addressData, {
        headers: {
          'user-info': JSON.stringify({
            userId: this.userInfo.userId,
            username: this.userInfo.username,
            email: this.userInfo.email
          })
        }
      })
          .then(response => {
            console.log('Save the shipping address response:', response.data);
            if (response.data.code === 1) {
              // 更新成功，更新本地地址列表
              const index = this.addressList.findIndex(item => item.addressId === this.currentAddressId);
              if (index !== -1) {
                // 注意：这里需要确保后端返回的地址数据也包含拼接后的 detail_address
                // 如果后端返回的数据仍然包含 province/city/district，可能需要额外处理
                this.addressList[index] = { ...this.addressList[index], ...addressData };
              }
              this.isEditingAddress = false;
              this.currentAddressId = null;
              alert('Saved successfully!');
            } else {
              alert(response.data.message || 'Failed to save, please try again');
            }
          })
          .catch(error => {
            console.error('Error in saving shipping address:', error);
            alert(error.response?.data?.message || 'Network error, please try again later');
          });
    },

    removeAddress(addressId) {
      if (confirm('Are you sure you want to delete this shipping address?')) {
        this.$http.delete('/address', {
          headers: {
            'user-info': JSON.stringify({
              userId: this.userInfo.userId,
              username: this.userInfo.username,
              email: this.userInfo.email
            })
          },
          data: [addressId]  // 注意这里传递的是数组，因为后端接收的是List<Long> ids
        })
            .then(response => {
              console.log('Remove the shipping address response:', response.data);
              if (response.data.code === 1) {
                // 从列表中移除该地址
                this.addressList = this.addressList.filter(
                    item => item.addressId !== addressId
                );
                alert('Delete successfully');
              } else {
                alert(response.data.message || 'Deletion failure');
              }
            })
            .catch(error => {
              console.error('Remove the shipping address error:', error);
              alert(error.response?.data?.message || 'Network error, please try again later');
            });
      }
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
    else if (this.activeTab === 'address') {  // 新增收货地址标签页的数据加载
      this.fetchAddressList();
    }
    // **确保这里调用了 fetchOrders()**
    if (this.activeTab === 'orders') {
      this.fetchOrders(); // ✅ 确保这一行存在
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
      } else if (newTab === 'address') {  // 新增收货地址标签页的监听
        this.fetchAddressList();
      } else if (newTab === 'orders') { // ✅ 确保这里监听了 orders 标签
        this.fetchOrders(); // ✅ 确保这一行存在
      }
    }
  },
}
</script>

<template>
  <Header></Header>
  <div class="profile-page">
    <div class="container">
      <div class="profile-sidebar">
        <div class="user-card">
          <div class="avatar-container" @click="openUploadDialog">
            <img :src="userInfo.avatarUrl" alt="avatar" class="avatar" />
            <div class="avatar-overlay">
              <i class="icon">📷</i>
            </div>
          </div>
          <h2>{{ userInfo.username }}</h2>
          <div class="user-status">
            <span :class="['status-badge', userInfo.accountStatus === 'active' ? 'active' : 'inactive']">
              {{ userInfo.accountStatus === 'active' ? 'normal' : 'Disabled' }}
            </span>
          </div>
          <div class="user-stats">
            <div class="stat-item">
              <span class="stat-value">12</span>
              <span class="stat-label">Collection</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">8</span>
              <span class="stat-label">Release</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">256</span>
              <span class="stat-label">Browse</span>
            </div>
          </div>
          <!-- 添加退出登录按钮 -->
          <button class="logout-btn" @click="handleLogout">
            <i class="icon">🚪</i> Log Out
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
            <h3>Personal Information</h3>
            <button v-if="!isEditing" class="btn edit-btn" @click="startEditing">
              <i class="icon">✏️</i> Editing
            </button>
          </div>
          
          <template v-if="isEditing">
            <form @submit.prevent="saveProfile" class="profile-form">
              <div class="form-group">
                <label>User name</label>
                <input type="text" :value="userInfo.username" disabled />
                <span class="field-hint">Usernames are immutable</span>
              </div>
              <div class="form-group">
                <label>Email</label>
                <input type="email" :value="userInfo.email" disabled />
                <span class="field-hint">Email is immutable</span>
              </div>
              <div class="form-group">
                <label>Real name</label>
                <input type="text" v-model="tempUserInfo.realName" placeholder="Please enter your real name" />
                <span class="field-hint">Optional</span>
              </div>
              <div class="form-group">
                <label>Phone</label>
                <input type="tel" v-model="tempUserInfo.phone" placeholder="Please enter your mobile phone number" />
                <span class="field-hint">Optional</span>
              </div>
              <div class="form-actions">
                <button type="button" class="btn cancel-btn" @click="cancelEditing">
                  <i class="icon">❌</i> Cancel
                </button>
                <button type="submit" class="btn save-btn">
                  <i class="icon">💾</i> Save
                </button>
              </div>
            </form>
          </template>
          
          <template v-else>
            <div class="info-display">
              <div class="info-item">
                <span class="label">User Name:</span>
                <span class="value">{{ userInfo.username }}</span>
              </div>
              <div class="info-item">
                <span class="label">Email:</span>
                <span class="value">{{ userInfo.email }}</span>
              </div>
              <div class="info-item">
                <span class="label">Real Name:</span>
                <span class="value">{{ userInfo.realName || '未设置' }}</span>
              </div>
              <div class="info-item">
                <span class="label">Mobile Phone Number:</span>
                <span class="value">{{ userInfo.phone || '未设置' }}</span>
              </div>
              <div class="info-item">
                <span class="label">Registration Time:</span>
                <span class="value">{{ formatDate(userInfo.registrationDate) }}</span>
              </div>
              <div class="info-item">
                <span class="label">Account Status:</span>
                <span class="value">
                  <span :class="['status-badge', userInfo.accountStatus === 'active' ? 'active' : 'inactive']">
                    {{ userInfo.accountStatus === 'active' ? 'normal' : 'Disabled' }}
                  </span>
                </span>
              </div>
            </div>
          </template>
        </div>

        <!-- 收货地址 - 新增模块 -->
        <div v-if="activeTab === 'address'" class="profile-section">
          <div class="section-header">
            <h3>Address of delivery</h3>
            <button class="btn post-btn" @click="isEditingAddress = true; tempAddress = {};">
              <i class="icon">➕</i> Adding a new address
            </button>
          </div>

          <div v-if="addressLoading" class="loading-state">
            <i class="loading-icon">⏳</i>
            <p>Loading...</p>
          </div>

          <div v-else-if="addressError" class="error-state">
            <i class="error-icon">❌</i>
            <p>{{ addressError }}</p>
            <button class="btn retry-btn" @click="fetchAddressList">Retry</button>
          </div>

          <div v-else-if="addressList.length === 0" class="empty-state">
            <i class="empty-icon">📦</i>
            <p>No delivery address at present</p>
            <button class="btn explore-btn" @click="isEditingAddress = true; tempAddress = {};">
              <i class="icon">➕</i> Adding a new address
            </button>
          </div>

          <template v-else>
            <div class="address-grid">
              <div v-for="address in addressList" :key="address.addressId" class="address-item">
                <div class="address-info">
                  <h4>{{ address.receiverName }}</h4>
                  <p>{{ address.receiverPhone }}</p>
                  <p>{{ address.province }} {{ address.city }} {{ address.district }} {{ address.detailAddress }}</p>
                  <p v-if="address.isDefault" class="default-tag">Default address</p>
                </div>
                <div class="address-actions">
                  <button class="edit-btn" @click="startEditingAddress(address)">
                    <i class="icon">✏️</i> Editing
                  </button>
                  <button class="remove-btn" @click="removeAddress(address.addressId)">
                    <i class="icon">❌</i> Delete
                  </button>
                </div>
              </div>
            </div>
          </template>
        </div>

        <!-- 收货地址表单弹窗 - 新增 -->
        <div v-if="isEditingAddress" class="upload-dialog-overlay" @click="cancelEditingAddress">
          <div class="upload-dialog" @click.stop>
            <div class="upload-dialog-header">
              <h3>{{ currentAddressId ? 'Edit the shipping address' : 'Add shipping address' }}</h3>
              <button class="close-btn" @click="cancelEditingAddress">&times;</button>
            </div>
            <div class="upload-dialog-content">
              <form @submit.prevent="saveAddress" class="address-form">
                <div class="form-group">
                  <label>Name of consignee</label>
                  <input type="text" v-model="tempAddress.receiverName" placeholder="Please enter the name of consignee" required />
                </div>
                <div class="form-group">
                  <label>Mobile phone number</label>
                  <input type="tel" v-model="tempAddress.receiverPhone" placeholder="Please enter your mobile number" required />
                </div>
                <div class="form-group">
                  <label>Provinces</label>
                  <input type="text" v-model="tempAddress.province" placeholder="Please enter province" required />
                </div>
                <div class="form-group">
                  <label>Cities</label>
                  <input type="text" v-model="tempAddress.city" placeholder="Please enter city" required />
                </div>
                <div class="form-group">
                  <label>District and county</label>
                  <input type="text" v-model="tempAddress.district" placeholder="Please enter the district" required />
                </div>
                <div class="form-group">
                  <label>Full address</label>
                  <input type="text" v-model="tempAddress.detailAddress" placeholder="Please enter the address details" required />
                </div>
                <div class="form-group">
                  <label>Set to the default address</label>
                  <div>
                    <input type="checkbox" v-model="tempAddress.isDefault" />
                  </div>
                </div>
                <div class="form-actions">
                  <button type="button" class="btn cancel-btn" @click="cancelEditingAddress">
                    <i class="icon">❌</i> Cancel
                  </button>
                  <button type="submit" class="btn save-btn">
                    <i class="icon">💾</i> Save
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>

        <!-- 我的收藏 -->
        <div v-if="activeTab === 'favorites'" class="profile-section">
          <h3>My Collection</h3>
          
          <div v-if="loading" class="loading-state">
            <i class="loading-icon">⏳</i>
            <p>loading...</p>
          </div>
          
          <div v-else-if="favoriteProducts.length === 0" class="empty-state">
            <i class="empty-icon">📚</i>
            <p>No collection content</p>
            <button class="btn explore-btn" @click="goToHome">Go and see</button>
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
                    <span>No pictures yet</span>
                  </div>
                </div>
                <div class="product-info">
                  <h4>{{ item.product.title }}</h4>
                  <p class="price">¥{{ item.product.price }}</p>
                  <p class="original-price">Original price: ¥{{ item.product.originalPrice }}</p>
                  <p class="condition">Product status: {{ item.product.condition === 'new' ? 'New' : 'Second hand' }}</p>
                  <p class="add-date">Collection time: {{ formatDate(item.addDate) }}</p>
                </div>
                <button class="remove-btn" @click="removeFavorite(item.favoriteId)">
                  <i class="icon">❌</i> Unbookmark
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
                Previous Page
              </button>
              <span class="page-info">
                Page {{ currentPage }}  /  {{ totalPages }} pages in total
              </span>
              <button 
                class="page-btn" 
                :disabled="currentPage === totalPages"
                @click="handlePageChange(currentPage + 1)"
              >
                Next Page
              </button>
            </div>
          </template>
        </div>

        <!-- 浏览记录 -->
        <div v-if="activeTab === 'history'" class="profile-section">
          <div class="section-header">
            <h3>Browsing history</h3>
            <button v-if="viewHistory.length > 0" class="btn clear-btn" @click="clearAllHistory">
              <i class="icon">🗑️</i> Clear the record
            </button>
          </div>
          
          <div v-if="historyLoading" class="loading-state">
            <i class="loading-icon">⏳</i>
            <p>loading...</p>
          </div>
          
          <div v-else-if="viewHistory.length === 0" class="empty-state">
            <i class="empty-icon">👀</i>
            <p>No browsing history</p>
            <button class="btn explore-btn" @click="goToHome">Go and see</button>
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
                    <span>No pictures yet</span>
                  </div>
                </div>
                <div class="product-info">
                  <h4>{{ item.product.title }}</h4>
                  <p class="price">¥{{ item.product.price }}</p>
                  <p class="original-price">Original price: ¥{{ item.product.originalPrice }}</p>
                  <p class="condition">Product status: {{ item.product.condition === 'new' ? 'New' : 'Second hand' }}</p>
                  <p class="view-date">Browsing time: {{ formatDate(item.viewDate) }}</p>
                </div>
                <button class="remove-btn" @click="removeHistoryItem(item.historyId)">
                  <i class="icon">❌</i> Deleting records
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
                Previous Page
              </button>
              <span class="page-info">
                Page {{ historyCurrentPage }}  /  {{ historyTotalPages }} pages in total
              </span>
              <button 
                class="page-btn" 
                :disabled="historyCurrentPage === historyTotalPages"
                @click="handleHistoryPageChange(historyCurrentPage + 1)"
              >
                Next page
              </button>
            </div>
          </template>
        </div>

        <!-- 我的订单 -->
        <div v-if="activeTab === 'orders'" class="profile-section">
          <div class="section-header">
            <h3>My order</h3>
            <!-- 如果需要添加创建订单的按钮，可以在这里添加 -->
            <!-- <button class="btn post-btn" @click="createOrder">
              <i class="icon">🛒</i> 创建订单
            </button> -->
          </div>

          <div v-if="ordersLoading" class="loading-state">
            <i class="loading-icon">⏳</i>
            <p>loading...</p>
          </div>

          <div v-else-if="orders.length === 0" class="empty-state">
            <i class="empty-icon">📦</i>
            <p>No order record</p>
            <button class="btn explore-btn" @click="goToHome">Go for a stroll</button>
          </div>

          <template v-else>
            <div class="orders-grid">
              <div v-for="item in orders" :key="item.orderId" class="order-item">
                <div class="image-container">
                  <img
                      v-if="item.primaryImageUrl"
                      :src="item.primaryImageUrl"
                      :alt="item.title"
                      class="product-image"
                  />
                  <div v-else class="no-image">
                    <i class="icon">🖼️</i>
                    <span>No pictures yet</span>
                  </div>
                </div>
                <div class="order-info">
                  <h4>Order number: {{ item.orderId }}</h4>
                  <p>Commodity: {{ item.title }}</p>
                  <p>Price: ¥{{ item.price.toFixed(2) }}</p>
                  <p>State:
                    <span :class="['status-badge',
              item.status === 'PENDING' ? 'pending' :
              item.status === 'SHIPPED' ? 'shipped' :
              item.status === 'COMPLETED' ? 'completed' :
              item.status === 'CANCELLED' ? 'cancelled' :
              item.status === 'REFUNDED' ? 'refunded' : 'unknown']">
              {{
                        item.status === 'pending' ? 'Pending payment' :
                            item.status === 'shipped' ? 'Shipped' :
                                item.status === 'completed' ? 'Completed' :
                                    item.status === 'cancelled' ? 'Cancelled' :
                                        item.status === 'refunded' ? 'A refund has been made' :
                                            'Unknown state'
                      }}
            </span>
                  </p>
                  <p>Creation time: {{ formatDate(item.createTime) }}</p>
                  <p v-if="item.payTime">Time of payment: {{ formatDate(item.payTime) }}</p>
                  <p v-if="item.payId">Payment ID: {{ item.payId }}</p>

                  <button
                      v-if="item.status === 'pending'"
                      class="pay-btn"
                      @click="handlePayment(item)">
                    <i class="icon">💳</i> payment
                  </button>
                </div>
              </div>
            </div>

            <!-- 分页组件 -->
            <div class="pagination">
              <button
                  class="page-btn"
                  :disabled="ordersCurrentPage === 1"
                  @click="handleOrdersPageChange(ordersCurrentPage - 1)"
              >
                Previous Page
              </button>
              <span class="page-info">
        Page {{ ordersCurrentPage }}  /  {{ ordersTotalPages }} pages in total
      </span>
              <button
                  class="page-btn"
                  :disabled="ordersCurrentPage === ordersTotalPages"
                  @click="handleOrdersPageChange(ordersCurrentPage + 1)"
              >
                Next Page
              </button>
            </div>
          </template>
        </div>

        <!-- 我的发布 -->
        <div v-if="activeTab === 'posts'" class="profile-section">
          <div class="section-header">
            <h3>My Release</h3>
            <button class="btn post-btn" @click="goToHome">
              <i class="icon">📝</i> Release new products
            </button>
          </div>
          
          <div v-if="postsLoading" class="loading-state">
            <i class="loading-icon">⏳</i>
            <p>loading...</p>
          </div>
          
          <div v-else-if="myPosts.length === 0" class="empty-state">
            <i class="empty-icon">📝</i>
            <p>No release yet</p>
            <button class="btn explore-btn" @click="goToHome">To publish</button>
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
                    <span>No pictures yet</span>
                  </div>
                </div>
                <div class="product-info">
                  <h4>{{ item.title }}</h4>
                  <p class="price">¥{{ item.price }}</p>
                  <p class="original-price">Original price: ¥{{ item.originalPrice }}</p>
                  <p class="condition">Product status: {{ item.condition === 'new' ? 'New' : 'Second hand' }}</p>
                  <p class="post-date">Release time: {{ formatDate(item.postDate) }}</p>
                  <p class="view-count">Browse: {{ item.viewCount }}</p>
                  <p class="favorite-count">Collection: {{ item.favoriteCount }}</p>
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
                Previous Page
              </button>
              <span class="page-info">
                Page {{ postsCurrentPage }}  /  {{ postsTotalPages }} pages in total
              </span>
              <button 
                class="page-btn" 
                :disabled="postsCurrentPage === postsTotalPages"
                @click="handlePostsPageChange(postsCurrentPage + 1)"
              >
                Next Page
              </button>
            </div>
          </template>
        </div>

        <!-- 修改密码 -->
        <div v-if="activeTab === 'security'" class="profile-section">
          <h3>Change your password</h3>
          <form @submit.prevent="changePassword" class="profile-form">
            <div class="form-group">
              <label>Current password</label>
              <input type="password" v-model="passwordForm.oldPassword" placeholder="Please enter your current password" />
            </div>
            <div class="form-group">
              <label>New password</label>
              <input type="password" v-model="passwordForm.newPassword" placeholder="Please enter a new password" />
            </div>
            <div class="form-group">
              <label>Confirm new password</label>
              <input type="password" v-model="passwordForm.confirmPassword" placeholder="Please enter a new password again" />
            </div>
            <div class="form-group verification-group">
              <label>Verification code</label>
              <div class="verification-input">
                <input type="text" v-model="passwordForm.verificationCode" placeholder="Please enter the verification code" />
                <button 
                  type="button" 
                  class="send-code-btn" 
                  @click="sendVerificationCode"
                  :disabled="isSendingCode"
                >
                  {{ isSendingCode ? `Try again in ${countdown} second` : 'Send CAPtCHA' }}
                </button>
              </div>
            </div>
            <button type="submit" class="btn save-btn">
              <i class="icon">🔒</i> Change your password
            </button>
          </form>
        </div>

        <!-- 使用指南区域 -->
        <div class="guide-section">
          <h3>User Guide</h3>
          <div class="guide-grid">
            <div class="guide-card">
              <div class="guide-icon">📝</div>
              <div class="guide-content">
                <h4>How to Publish Content</h4>
                <ul>
                  <li>Click the "Publish" button on the home page</li>
                  <li>Select the type of content you want to publish</li>
                  <li>Fill in the title and detailed description</li>
                  <li>Upload relevant images</li>
                  <li>Just click publish</li>
                </ul>
              </div>
            </div>
            <div class="guide-card">
              <div class="guide-icon">🔍</div>
              <div class="guide-content">
                <h4>How to search for content</h4>
                <ul>
                  <li>Use the top search bar</li>
                  <li>Enter keywords or tags</li>
                  <li>Select filter criteria</li>
                  <li>Click the search button</li>
                </ul>
              </div>
            </div>
            <div class="guide-card">
              <div class="guide-icon">💬</div>
              <div class="guide-content">
                <h4>How to Interact</h4>
                <ul>
                  <li>Leave a comment below</li>
                  <li>Like content of interest</li>
                  <li>Collect quality content</li>
                  <li>Follow interested users</li>
                </ul>
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
          <h3>Change the avatar</h3>
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
              <div class="upload-text">I'll go ahead and select the image</div>
              <div class="upload-hint">Support jpg, png format</div>
            </label>
          </div>
        </div>
      </div>
    </div>
  </div>
  <Footer></Footer>
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

.verification-group {
  margin-bottom: 25px;
}

.verification-input {
  display: flex;
  gap: 10px;
}

.verification-input input {
  flex: 1;
}

.send-code-btn {
  padding: 0 15px;
  background: #ff5722;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: bold;
  white-space: nowrap;
  min-width: 120px;
  transition: all 0.3s;
}

.send-code-btn:hover:not(:disabled) {
  background: #f4511e;
}

.send-code-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

/* 在 <style scoped> 中添加以下内容 */

.status-badge {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
}

.status-badge.pending {
  background-color: #FFC107; /* 黄色 */
  color: #333;
}

.status-badge.shipped {
  background-color: #2196F3; /* 蓝色 */
  color: white;
}

.status-badge.completed {
  background-color: #4CAF50; /* 绿色 */
  color: white;
}

.status-badge.cancelled {
  background-color: #9E9E9E; /* 灰色 */
  color: white;
}

.status-badge.refunded {
  background-color: #FF5722; /* 橙色 */
  color: white;
}

.status-badge.unknown {
  background-color: #F44336; /* 红色 */
  color: white;
}

.orders-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.order-item {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s;
  border: 1px solid #eee; /* 添加边框 */
  padding: 15px; /* 添加内边距 */
}

.order-item:hover {
  transform: translateY(-5px);
}

/* 新增收货地址相关样式 */
.address-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.address-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  transition: transform 0.3s;
}

.address-item:hover {
  transform: translateY(-5px);
}

.address-info h4 {
  margin: 0 0 5px;
  color: #333;
  font-size: 16px;
}

.address-info p {
  margin: 5px 0;
  color: #666;
  font-size: 14px;
}

.default-tag {
  background-color: #4CAF50;
  color: white;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  margin-top: 5px;
}

.address-actions {
  display: flex;
  gap: 10px;
}

.edit-btn {
  background: #ff5722;
  color: white;
  border: none;
  padding: 5px 10px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 3px;
}

.remove-btn {
  background: #f44336;
  color: white;
  border: none;
  padding: 5px 10px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 3px;
}

/* 新增收货地址相关样式 */
.address-form {
  max-width: 100%;
}

.address-form .form-group {
  margin-bottom: 15px;
}

.address-form label {
  display: block;
  margin-bottom: 8px;
  color: #666;
  font-weight: bold;
}

.address-form input[type="text"],
.address-form input[type="tel"] {
  width: 100%;
  padding: 7px;
  border: 2px solid #eee;
  border-radius: 6px;
  font-size: 14px;
  transition: all 0.3s;
}

.address-form input[type="text"]:focus,
.address-form input[type="tel"]:focus {
  border-color: #ff5722;
  outline: none;
  box-shadow: 0 0 0 3px rgba(255,87,34,0.1);
}

.address-form label input[type="checkbox"] {
  margin-right: 8px;
  vertical-align: middle;
}

/* 为上下布局的复选框添加一些间距 */
.form-group div input[type="checkbox"] {
  margin: 5px 0;
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
</style>