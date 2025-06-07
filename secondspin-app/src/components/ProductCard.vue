<template>
  <div class="product-card">
    <div class="product-image">
      <img :src="getProductImageUrl(product.primaryImageUrl)" :alt="product.title">
      <span class="condition" :class="getConditionClass(product.condition)">
        {{ formatCondition(product.condition) }}
      </span>
    </div>
    
    <div class="product-info">
      <h3 class="title">{{ product.title }}</h3>
      <div class="price-section">
        <span class="current-price">¥{{ product.price.toFixed(2) }}</span>
        <span v-if="product.originalPrice" class="original-price">¥{{ product.originalPrice.toFixed(2) }}</span>
      </div>
      <div class="meta-info">
        <span class="stats">
          <span class="view-count">👁️ {{ product.viewCount }}</span>
          <span class="favorite-count">❤️ {{ product.favoriteCount }}</span>
        </span>
        <span class="time">{{ formatDate(product.postDate) }}</span>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ProductCard',
  props: {
    product: {
      type: Object,
      required: true
    }
  },
  methods: {
    getConditionClass(condition) {
      const conditionMap = {
        'new': 'new',
        'like-new': 'like-new',
        'good': 'good',
        'fair': 'fair',
        'poor': 'poor'
      }
      return conditionMap[condition] || 'default'
    },
    formatCondition(condition) {
      const conditionTextMap = {
        'new': '全新',
        'like-new': '95成新',
        'good': '9成新',
        'fair': '8成新',
        'poor': '7成新'
      }
      return conditionTextMap[condition] || condition || '未知'
    },
    getProductImageUrl(imageName) {
      if (!imageName) {
        return '/images/products/8e9e7e86de484a269a143d7e726f8f4c.jpg'
      }
      return `/images/products/${imageName}`
    },
    formatDate(dateString) {
      if (!dateString) return '未知时间'
      try {
        const date = new Date(dateString)
        return date.toLocaleDateString()
      } catch (e) {
        return dateString
      }
    }
  }
}
</script>

<style scoped>
/* 保持原有的基础样式不变 */
.product-card {
  background-color: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  transition: transform 0.3s, box-shadow 0.3s;
  cursor: pointer;
}

.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0,0,0,0.1);
}

.product-image {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.product-card:hover .product-image img {
  transform: scale(1.05);
}

.condition {
  position: absolute;
  bottom: 10px;
  right: 10px;
  padding: 3px 8px;
  border-radius: 3px;
  font-size: 0.8rem;
  color: white;
}

.condition.new {
  background-color: #4caf50;
}

.condition.like-new {
  background-color: #8bc34a;
}

.condition.good {
  background-color: #ffc107;
}

.condition.fair {
  background-color: #ff9800;
}

.condition.poor {
  background-color: #f44336;
}

.condition.default {
  background-color: #9e9e9e;
}

.product-info {
  padding: 15px;
}

.title {
  margin: 0 0 10px 0;
  font-size: 1rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.price-section {
  display: flex;
  align-items: baseline;
  margin-bottom: 10px;
}

.current-price {
  font-size: 1.2rem;
  font-weight: bold;
  color: #ff5722;
  margin-right: 8px;
}

.original-price {
  font-size: 0.9rem;
  color: #999;
  text-decoration: line-through;
}

.meta-info {
  display: flex;
  justify-content: space-between;
  font-size: 0.8rem;
  color: #666;
}

/* 新增的统计信息样式 */
.stats {
  display: flex;
  gap: 8px;
}

.view-count, .favorite-count {
  display: flex;
  align-items: center;
  gap: 2px;
}
</style>