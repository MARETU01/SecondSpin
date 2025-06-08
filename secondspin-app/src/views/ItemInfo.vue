<template>
  <div class="item-info-page">
    <Header></Header>

    <div class="container">
      <div v-if="loading" class="loading">
        <i class="fa fa-spinner fa-spin"></i> 加载中...
      </div>

      <div v-else-if="error" class="error">
        <i class="fa fa-exclamation-circle"></i> {{ error }}
      </div>

      <div v-else class="item-content">
        <div class="item-header">
          <div class="item-title">
            <h1>{{ product.title }}</h1>
          </div>
          <div class="item-status">
            <span class="status-tag" :class="product.status === 'available' ? 'available' : 'unavailable'">
              {{ product.status === 'available' ? '可购买' : '已售出' }}
            </span>
            <span class="condition-tag" :class="product.condition === 'new' ? 'new' : 'used'">
              {{ product.condition === 'new' ? '全新' : '二手' }}
            </span>
          </div>
        </div>

        <div class="main-content">
          <div class="item-images">
            <div class="main-image">
              <img
                  :src="getImageUrl(imageUrls[currentImage])"
                  alt="商品主图"
                  class="img-fluid"
                  @error="handleImageError($event)"
              >
            </div>
            <div v-if="imageUrls.length > 1" class="thumbnail-images">
              <div
                  v-for="(url, index) in imageUrls"
                  :key="index"
                  class="thumbnail"
                  @click="changeMainImage(index)"
              >
                <img
                    :src="getImageUrl(url)"
                    alt="商品缩略图"
                    :class="{ 'border-2 border-primary': currentImage === index }"
                    @error="handleImageError($event)"
                >
              </div>
            </div>
          </div>

          <div class="item-details">
            <div class="price-info">
              <div class="current-price">¥{{ product.price.toFixed(2) }}</div>
              <div class="original-price" v-if="product.originalPrice > product.price">
                原价: ¥{{ product.originalPrice.toFixed(2) }}
              </div>
              <div class="discount" v-if="product.originalPrice > product.price">
                节省: ¥{{ (product.originalPrice - product.price).toFixed(2) }}
              </div>
            </div>

            <div class="stats-info">
              <span class="stat-item"><i class="fa fa-eye"></i> {{ product.viewCount }} 次浏览</span>
              <span class="stat-item"><i class="fa fa-heart"></i> {{ product.favoriteCount }} 人收藏</span>
              <span class="stat-item"><i class="fa fa-calendar"></i> 发布于 {{ formatDate(product.postDate) }}</span>
            </div>

            <div class="category-info">
              <span class="category-label">分类:</span>
              <span class="category-name">{{ product.categoryName }}</span>

            </div>

            <div class="seller-info">
              <div class="seller-avatar">
                <img
                    :src="getImageUrl(product.sellerAvatarUrl)"
                    alt="卖家头像"
                    @error="handleAvatarError($event)"
                >
              </div>
              <div class="seller-details">
                <div class="seller-name">{{ product.sellerName }}</div>
                <div class="seller-id">ID: {{ product.sellerId }}</div>
              </div>
              <button class="contact-seller">
                <i class="fa fa-comments"></i> 联系卖家
              </button>
            </div>

            <div class="action-buttons">
              <button class="btn-favorite" :class="{ favorited: product.ifFavorite }" @click="toggleFavorite">
                <i class="fa" :class="product.ifFavorite ? 'fa-heart' : 'fa-heart-o'"></i>
                {{ product.ifFavorite ? '已收藏' : '收藏' }}
              </button>

              <button class="btn-buy" :disabled="product.status !== 'available'">
                <i class="fa fa-shopping-cart"></i> 立即购买
              </button>
            </div>
          </div>
        </div>

        <div class="item-description">
          <h3>商品描述</h3>
          <p>{{ product.description || '暂无商品描述' }}</p>
        </div>


      </div>
    </div>

    <Footer></Footer>
  </div>
</template>

<script>
import Header from '@/components/AppHeader.vue'
import Footer from '@/components/AppFooter.vue'
import http from '@/http';

export default {
  name: 'ItemInfoPage',
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
      product: null,
      loading: true,
      error: null,
      currentImage: 0,
      // 图片URL前缀，根据实际服务器配置调整
      imageUrlPrefix: '/images/products/',
      // 头像URL前缀
      avatarUrlPrefix: '/images/avatar/',
      /// 默认图片URL
      defaultProductImage: '/images/products/default.png', // 更新为指定的默认图片路径
      defaultAvatarImage: '/images/avatar/default.png'
    };
  },
  computed: {
    // 主图URL，优先使用primaryImageUrl，否则使用imageUrls的第一张
    mainImageUrl() {
      if (!this.product) return '';

      if (this.product.primaryImageUrl) {
        return this.product.primaryImageUrl;
      }

      if (this.imageUrls && this.imageUrls.length > 0) {
        return this.imageUrls[0];
      }

      return '';
    },

    // 处理后的图片URL数组
    imageUrls() {
      if (!this.product || !this.product.imageUrls || this.product.imageUrls.length === 0) {
        // 如果没有图片，返回默认图片
        return [this.defaultProductImage];
      }

      // 确保imageUrls是数组
      if (typeof this.product.imageUrls === 'string') {
        try {
          const urls = JSON.parse(this.product.imageUrls);
          return urls.length > 0 ? urls : [this.defaultProductImage];
        } catch (e) {
          // 如果解析失败或为空，返回默认图片
          return [this.defaultProductImage];
        }
      }

      return this.product.imageUrls;
    }
  },
  created() {
    this.fetchItemInfo();
  },
  methods: {
    async fetchItemInfo() {
      this.loading = true;
      this.error = null;

      try {
        const response = await http.get(`/products/${this.id}`);
        console.log('获取商品信息响应:', response.data);

        if (response.data && response.data.code === 1) {
          this.product = response.data.data;
        } else {
          this.error = response.data.message || '获取商品信息失败';
        }
      } catch (error) {
        console.error('获取商品信息错误:', error);
        this.error = error.response?.data?.message || '网络错误，请稍后重试';
      } finally {
        this.loading = false;
      }

    },


    // 格式化日期显示
    formatDate(dateString) {

      if (!dateString) return '';

      const date = new Date(dateString);
      return date.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      });
    },

    // 切换收藏状态
    toggleFavorite() {
      if (this.product){
        this.$http.delete('/favorites', {
          params: {
            ids: this.id
          }
        })
            .then(response => {
              console.log('取消收藏响应:', response.data);
              if (response.data.code === 1) {
                // 从列表中移除该商品
                this.product.ifFavorite = !this.product.ifFavorite;
                alert('取消收藏成功');
              } else {
                alert(response.data.message || '取消收藏失败');
              }
            })
            .catch(error => {
              console.error('取消收藏错误:', error);
              alert(error.response?.data?.message || '网络错误，请稍后重试');
            });
      }else{
        this.$http.post('/favorites', {
          productId: this.id
        })
            .then(response => {
              console.log('取消收藏响应:', response.data);
              if (response.data.code === 1) {
                // 从列表中移除该商品
                this.product.ifFavorite = !this.product.ifFavorite;
                alert('取消收藏成功');
              } else {
                alert(response.data.message || '取消收藏失败');
              }
            })
            .catch(error => {
              console.error('取消收藏错误:', error);
              alert(error.response?.data?.message || '网络错误，请稍后重试');
            });
      }
    },

    // 处理图片URL，添加前缀
    getImageUrl(url) {
      // 如果URL为空，返回默认图片
      if (!url || url.trim() === '') {
        return this.defaultProductImage;
      }

      // 如果是头像图片，使用头像前缀
      if (url.includes('avatar') || url === 'default.png') {
        return this.avatarUrlPrefix + url;
      }

      // 如果URL已经是完整路径，直接返回
      if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('/')) {
        return url;
      }

      // 否则添加商品图片前缀
      return this.imageUrlPrefix + url;
    },

    // 处理图片加载错误
    handleImageError(event) {
      // 图片加载失败时显示默认图片
      event.target.src = this.defaultProductImage;
    },

    // 处理头像加载错误
    handleAvatarError(event) {
      // 头像加载失败时显示默认头像
      event.target.src = this.defaultAvatarImage;
    },

    // 更改主图显示
    changeMainImage(index) {
      this.currentImage = index;
    }
  }
};
</script>

<style scoped>
.item-info-page {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.container {
  max-width: 1200px;
  margin: 20px auto;
  padding: 0 15px;
  flex: 1;
}

.loading, .error {
  text-align: center;
  padding: 20px;
  font-size: 1.2rem;
}

.error {
  color: #ff4444;
}

.item-content {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  padding: 20px;
}

.item-header {
  margin-bottom: 20px;
  border-bottom: 1px solid #eee;
  padding-bottom: 15px;
}

.item-title h1 {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 10px;
}

.item-status {
  display: flex;
  gap: 10px;
}

.status-tag, .condition-tag {
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 14px;
}

.available {
  background-color: #e8f5e9;
  color: #2e7d32;
}

.unavailable {
  background-color: #ffebee;
  color: #c62828;
}

.new {
  background-color: #e3f2fd;
  color: #1565c0;
}

.used {
  background-color: #fff8e1;
  color: #f57c00;
}

.main-content {
  display: flex;
  gap: 30px;
  margin-bottom: 30px;
}

.item-images {
  flex: 1;
  max-width: 500px;
}

.main-image {
  border: 1px solid #eee;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 15px;
  aspect-ratio: 1/1;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f8f9fa;
}

.main-image img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  transition: transform 0.3s ease;
}

.main-image img:hover {
  transform: scale(1.02);
}

.thumbnail-images {
  display: flex;
  gap: 10px;
  overflow-x: auto;
  padding-bottom: 5px;
}

.thumbnail {
  width: 80px;
  height: 80px;
  border: 2px solid transparent;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  transition: border-color 0.3s ease;
  flex-shrink: 0;
}

.thumbnail.active {
  border-color: #2196f3;
}

.thumbnail img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-details {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.price-info {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.current-price {
  font-size: 32px;
  font-weight: bold;
  color: #e53935;
  margin-bottom: 5px;
}

.original-price {
  font-size: 16px;
  color: #757575;
  text-decoration: line-through;
  margin-bottom: 5px;
}

.discount {
  font-size: 16px;
  color: #43a047;
}

.stats-info {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.stat-item {
  font-size: 14px;
  color: #616161;
}

.stat-item i {
  margin-right: 5px;
  color: #9e9e9e;
}

.category-info {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.category-label {
  font-weight: bold;
  margin-right: 10px;
  color: #424242;
}

.category-name {
  margin-right: 10px;
  color: #1976d2;
}

.category-icon {
  width: 24px;
  height: 24px;
  object-fit: contain;
}

.seller-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.seller-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  overflow: hidden;
  margin-right: 15px;
  background-color: #f5f5f5;
}

.seller-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.seller-details {
  flex: 1;
}

.seller-name {
  font-size: 18px;
  font-weight: bold;
  color: #212121;
}

.seller-id {
  font-size: 14px;
  color: #757575;
}

.contact-seller {
  padding: 8px 15px;
  background-color: #2196f3;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.contact-seller:hover {
  background-color: #1976d2;
}

.action-buttons {
  display: flex;
  gap: 15px;
  margin-top: auto;
}

.btn-favorite, .btn-chat, .btn-buy {
  flex: 1;
  padding: 12px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  transition: all 0.3s ease;
}

.btn-favorite {
  background-color: #f5f5f5;
  color: #e91e63;
  border: 1px solid #e0e0e0;
}

.btn-favorite.favorited {
  background-color: #ffebee;
  color: #c2185b;
  border-color: #ef9a9a;
}

.btn-chat {
  background-color: #4caf50;
  color: white;
}

.btn-chat:hover {
  background-color: #388e3c;
}

.btn-buy {
  background-color: #e53935;
  color: white;
  font-weight: bold;
}

.btn-buy:hover {
  background-color: #c62828;
}

.btn-buy:disabled {
  background-color: #bdbdbd;
  cursor: not-allowed;
}

.item-description {
  margin-bottom: 30px;
}

.item-description h3 {
  font-size: 20px;
  margin-bottom: 15px;
  color: #212121;
}

.item-description p {
  font-size: 16px;
  line-height: 1.6;
  color: #424242;
}

.border-2 {
  border-width: 2px !important;
}

.border-primary {
  border-color: #2196f3 !important;
}
</style>


