<script>

export default {
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
        username: '测试用户',
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
        { id: 'profile', name: '个人信息' },
        { id: 'address', name: '收货地址' },
        { id: 'favorites', name: '我的收藏' },
        { id: 'history', name: '浏览记录' },
        { id: 'orders', name: '我的订单' },
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
          console.log('获取用户信息响应:', response.data)
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
    sendVerificationCode() {
      if (this.isSendingCode) return;
      
      this.$http.post('/users/reset-password/code', 
        { password: this.passwordForm.oldPassword }
      )
        .then(response => {
          console.log('发送验证码响应:', response.data);
          if (response.data.code === 1) {
            alert('验证码已发送到您的邮箱');
            this.startCountdown();
          } else {
            alert(response.data.message || '发送验证码失败');
          }
        })
        .catch(error => {
          console.error('发送验证码错误:', error);
          alert(error.response?.data?.message || '网络错误，请稍后重试');
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
        alert('新密码和确认密码不一致');
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
          console.log('修改密码响应:', response.data);
          if (response.data.code === 1) {
            alert('密码修改成功');
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
            alert(response.data.message || '修改密码失败');
          }
        })
        .catch(error => {
          console.error('修改密码错误:', error);
          alert(error.response?.data?.message || '网络错误，请稍后重试');
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
        address: '📍',  // 修改为收货地址图标
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
      // 找到要删除的收藏项
      const favoriteItem = this.favoriteProducts.find(item => item.favoriteId === favoriteId);
      if (!favoriteItem) return;

      this.$http.delete('/favorites', {
        params: {
          ids: favoriteItem.product.productId
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
    },

    // 获取订单列表
    // fetchOrders() {
    //   this.ordersLoading = true;
    //   this.ordersError = null;
    //
    //   // ====== Mock 数据(实际逻辑在下方被注释掉了，手动改回来即可) ======
    //   const mockResponse = {
    //     code: 1,
    //     message: "success",
    //     data: {
    //       data: [
    //         {
    //           orderId: 1,
    //           productId: 101,
    //           createTime: "2025-06-01T10:00:00",
    //           price: 99.99,
    //           status: "PENDING",
    //           payId: "PAY20250601100000",
    //           payTime: null,
    //           title: "示例商品 1",
    //           primaryImageUrl: "http://example.com/image1.jpg"
    //         },
    //         {
    //           orderId: 2,
    //           productId: 102,
    //           createTime: "2025-06-02T11:00:00",
    //           price: 199.99,
    //           status: "COMPLETED",
    //           payId: "PAY20250602110000",
    //           payTime: "2025-06-02T12:00:00",
    //           title: "示例商品 2",
    //           primaryImageUrl: "http://example.com/image2.jpg"
    //         },
    //         {
    //           orderId: 3,
    //           productId: 103,
    //           createTime: "2025-06-03T12:00:00",
    //           price: 299.99,
    //           status: "SHIPPED",
    //           payId: "PAY20250603120000",
    //           payTime: "2025-06-03T13:00:00",
    //           title: "示例商品 3",
    //           primaryImageUrl: "http://example.com/image3.jpg"
    //         }
    //       ],
    //       total: 100,
    //       totalPage: 10
    //     },
    //     timestamp: 1754633400000
    //   };
    //
    //   // 模拟网络延迟
    //   setTimeout(() => {
    //     console.log("Mock 数据加载完成:", mockResponse);
    //     if (mockResponse.code === 1) {
    //       this.orders = mockResponse.data.data || [];
    //       this.ordersTotalPages = mockResponse.data.totalPage || 1;
    //       this.ordersTotalItems = mockResponse.data.total || 0;
    //     } else {
    //       this.ordersError = mockResponse.message || "获取订单失败";
    //       alert(this.ordersError);
    //     }
    //     this.ordersLoading = false;
    //   }, 1000);
    // },
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
            console.log('获取订单响应:', response.data);
            if (response.data.code === 1) {
              // 根据后端返回的数据结构提取订单列表
              const orderData = response.data.data;
              this.orders = orderData.data || []; // 订单列表
              this.ordersTotalPages = orderData.totalPage || 1; // 总页数
              this.ordersTotalItems = orderData.total || 0; // 总记录数
            } else {
              // 处理错误信息
              this.ordersError = response.data.message || '获取订单失败';
              alert(this.ordersError);
            }
          })
          .catch(error => {
            console.error('获取订单错误:', error);
            this.ordersError = error.response?.data?.message || '网络错误，请稍后重试';
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
            console.error('支付错误:', error);
            alert(error.response?.data?.message || '网络错误，请稍后重试');
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
            console.log('获取收货地址响应:', response.data);
            if (response.data.code === 1) {
              this.addressList = response.data.data || [];
            } else {
              this.addressError = response.data.message || '获取收货地址失败';
            }
          })
          .catch(error => {
            console.error('获取收货地址错误:', error);
            this.addressError = error.response?.data?.message || '网络错误，请稍后重试';
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

    // startEditingAddress(address) {
    //   this.currentAddressId = address.addressId;
    //   this.tempAddress = { ...address };
    //   this.isEditingAddress = true;
    // },
    //
    // cancelEditingAddress() {
    //   this.isEditingAddress = false;
    //   this.currentAddressId = null;
    // },

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
            console.log('保存收货地址响应:', response.data);
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
              alert('保存成功！');
            } else {
              alert(response.data.message || '保存失败，请重试');
            }
          })
          .catch(error => {
            console.error('保存收货地址错误:', error);
            alert(error.response?.data?.message || '网络错误，请稍后重试');
          });
    },

    removeAddress(addressId) {
      if (confirm('确定要删除这个收货地址吗？')) {
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
              console.log('删除收货地址响应:', response.data);
              if (response.data.code === 1) {
                // 从列表中移除该地址
                this.addressList = this.addressList.filter(
                    item => item.addressId !== addressId
                );
                alert('删除成功');
              } else {
                alert(response.data.message || '删除失败');
              }
            })
            .catch(error => {
              console.error('删除收货地址错误:', error);
              alert(error.response?.data?.message || '网络错误，请稍后重试');
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

        <!-- 收货地址 - 新增模块 -->
        <div v-if="activeTab === 'address'" class="profile-section">
          <div class="section-header">
            <h3>收货地址</h3>
            <button class="btn post-btn" @click="isEditingAddress = true; tempAddress = {};">
              <i class="icon">➕</i> 添加新地址
            </button>
          </div>

          <div v-if="addressLoading" class="loading-state">
            <i class="loading-icon">⏳</i>
            <p>加载中...</p>
          </div>

          <div v-else-if="addressError" class="error-state">
            <i class="error-icon">❌</i>
            <p>{{ addressError }}</p>
            <button class="btn retry-btn" @click="fetchAddressList">重试</button>
          </div>

          <div v-else-if="addressList.length === 0" class="empty-state">
            <i class="empty-icon">📦</i>
            <p>暂无收货地址</p>
            <button class="btn explore-btn" @click="isEditingAddress = true; tempAddress = {};">
              <i class="icon">➕</i> 添加新地址
            </button>
          </div>

          <template v-else>
            <div class="address-grid">
              <div v-for="address in addressList" :key="address.addressId" class="address-item">
                <div class="address-info">
                  <h4>{{ address.receiverName }}</h4>
                  <p>{{ address.receiverPhone }}</p>
                  <p>{{ address.province }} {{ address.city }} {{ address.district }} {{ address.detailAddress }}</p>
                  <p v-if="address.isDefault" class="default-tag">默认地址</p>
                </div>
                <div class="address-actions">
                  <button class="edit-btn" @click="startEditingAddress(address)">
                    <i class="icon">✏️</i> 编辑
                  </button>
                  <button class="remove-btn" @click="removeAddress(address.addressId)">
                    <i class="icon">❌</i> 删除
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
              <h3>{{ currentAddressId ? '编辑收货地址' : '添加收货地址' }}</h3>
              <button class="close-btn" @click="cancelEditingAddress">&times;</button>
            </div>
            <div class="upload-dialog-content">
              <form @submit.prevent="saveAddress" class="address-form">
                <div class="form-group">
                  <label>收货人姓名</label>
                  <input type="text" v-model="tempAddress.receiverName" placeholder="请输入收货人姓名" required />
                </div>
                <div class="form-group">
                  <label>手机号码</label>
                  <input type="tel" v-model="tempAddress.receiverPhone" placeholder="请输入手机号码" required />
                </div>
                <div class="form-group">
                  <label>省份</label>
                  <input type="text" v-model="tempAddress.province" placeholder="请输入省份" required />
                </div>
                <div class="form-group">
                  <label>城市</label>
                  <input type="text" v-model="tempAddress.city" placeholder="请输入城市" required />
                </div>
                <div class="form-group">
                  <label>区县</label>
                  <input type="text" v-model="tempAddress.district" placeholder="请输入区县" required />
                </div>
                <div class="form-group">
                  <label>详细地址</label>
                  <input type="text" v-model="tempAddress.detailAddress" placeholder="请输入详细地址" required />
                </div>
                <div class="form-group">
                  <label>设为默认地址</label>
                  <div>
                    <input type="checkbox" v-model="tempAddress.isDefault" />
                  </div>
                </div>
                <div class="form-actions">
                  <button type="button" class="btn cancel-btn" @click="cancelEditingAddress">
                    <i class="icon">❌</i> 取消
                  </button>
                  <button type="submit" class="btn save-btn">
                    <i class="icon">💾</i> 保存
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>

        <!-- 我的收藏 -->
        <div v-if="activeTab === 'favorites'" class="profile-section">
          <h3>我的收藏</h3>
          
          <div v-if="loading" class="loading-state">
            <i class="loading-icon">⏳</i>
            <p>加载中...</p>
          </div>
          
          <div v-else-if="favoriteProducts.length === 0" class="empty-state">
            <i class="empty-icon">📚</i>
            <p>暂无收藏内容</p>
            <button class="btn explore-btn" @click="goToHome">去看看</button>
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
          
          <div v-else-if="viewHistory.length === 0" class="empty-state">
            <i class="empty-icon">👀</i>
            <p>暂无浏览记录</p>
            <button class="btn explore-btn" @click="goToHome">去看看</button>
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

        <!-- 我的订单 -->
        <div v-if="activeTab === 'orders'" class="profile-section">
          <div class="section-header">
            <h3>我的订单</h3>
            <!-- 如果需要添加创建订单的按钮，可以在这里添加 -->
            <!-- <button class="btn post-btn" @click="createOrder">
              <i class="icon">🛒</i> 创建订单
            </button> -->
          </div>

          <div v-if="ordersLoading" class="loading-state">
            <i class="loading-icon">⏳</i>
            <p>加载中...</p>
          </div>

          <div v-else-if="orders.length === 0" class="empty-state">
            <i class="empty-icon">📦</i>
            <p>暂无订单记录</p>
            <button class="btn explore-btn" @click="goToHome">去逛逛</button>
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
                    <span>暂无图片</span>
                  </div>
                </div>
                <div class="order-info">
                  <h4>订单号: {{ item.orderId }}</h4>
                  <p>商品: {{ item.title }}</p>
                  <p>价格: ¥{{ item.price.toFixed(2) }}</p>
                  <p>状态:
                    <span :class="['status-badge',
              item.status === 'PENDING' ? 'pending' :
              item.status === 'SHIPPED' ? 'shipped' :
              item.status === 'COMPLETED' ? 'completed' :
              item.status === 'CANCELLED' ? 'cancelled' :
              item.status === 'REFUNDED' ? 'refunded' : 'unknown']">
              {{
                        item.status === 'pending' ? '待付款' :
                            item.status === 'shipped' ? '已发货' :
                                item.status === 'completed' ? '已完成' :
                                    item.status === 'cancelled' ? '已取消' :
                                        item.status === 'refunded' ? '已退款' :
                                            '未知状态'
                      }}
            </span>
                  </p>
                  <p>创建时间: {{ formatDate(item.createTime) }}</p>
                  <p v-if="item.payTime">支付时间: {{ formatDate(item.payTime) }}</p>
                  <p v-if="item.payId">支付ID: {{ item.payId }}</p>

                  <button
                      v-if="item.status === 'pending'"
                      class="pay-btn"
                      @click="handlePayment(item)">
                    <i class="icon">💳</i> 支付
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
                上一页
              </button>
              <span class="page-info">
        第 {{ ordersCurrentPage }} 页 / 共 {{ ordersTotalPages }} 页
      </span>
              <button
                  class="page-btn"
                  :disabled="ordersCurrentPage === ordersTotalPages"
                  @click="handleOrdersPageChange(ordersCurrentPage + 1)"
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
              <input type="password" v-model="passwordForm.oldPassword" placeholder="请输入当前密码" />
            </div>
            <div class="form-group">
              <label>新密码</label>
              <input type="password" v-model="passwordForm.newPassword" placeholder="请输入新密码" />
            </div>
            <div class="form-group">
              <label>确认新密码</label>
              <input type="password" v-model="passwordForm.confirmPassword" placeholder="请再次输入新密码" />
            </div>
            <div class="form-group verification-group">
              <label>验证码</label>
              <div class="verification-input">
                <input type="text" v-model="passwordForm.verificationCode" placeholder="请输入验证码" />
                <button 
                  type="button" 
                  class="send-code-btn" 
                  @click="sendVerificationCode"
                  :disabled="isSendingCode"
                >
                  {{ isSendingCode ? `${countdown}秒后重试` : '发送验证码' }}
                </button>
              </div>
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