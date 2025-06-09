<template>
  <nav class="category-nav">
    <div v-if="loading" class="loading">Load in category...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    <div v-else class="category-grid">
      <div 
        v-for="category in categoryList" 
        :key="category.categoryId"
        class="category-item"
        :class="{ active: selectedCategoryId === category.categoryId }"
        @click="selectCategory(category)"
      >
        {{ category.name }}
      </div>
    </div>
  </nav>
</template>

<script>
import http from '@/http';

export default {
  name: 'CategoryNav',
  data() {
    return {
      categoryList: [],
      selectedCategoryId: 0, // 默认选中"全部"
      loading: false,
      error: null
    }
  },
  methods: {
    selectCategory(category) {
      this.selectedCategoryId = category.categoryId
      this.$emit('category-selected', category.categoryId)
    },
    async fetchCategories() {
      this.loading = true
      this.error = null
      try {
        const response = await http.get('/categories')
        if (response.data && response.data.code === 1) {
          this.categoryList = response.data.data
          // 添加"全部"选项作为第一个分类
          this.categoryList.unshift({
            categoryId: 0,
            name: 'All',
            description: 'All Product Categories',
            iconUrl: ''
          })
        } else {
          this.error = response.data.message || 'Failure to get classification'
        }
      } catch (err) {
        this.error = err.response?.data?.message || err.message || 'An error occurred while requesting classification'
        console.error('Get the classification error:', err)
        // 出错时使用默认分类
        this.categoryList = this.getDefaultCategories()
      } finally {
        this.loading = false
      }
    },
    getDefaultCategories() {
      return [
        { categoryId: 0, name: 'All', description: '' },
      ]
    }
  },
  created() {
    this.fetchCategories()
  }
}
</script>

<style scoped>
.category-nav {
  background-color: white;
  padding: 15px 0;
  box-shadow: 0 2px 5px rgba(0,0,0,0.05);
}

.loading, .error {
  text-align: center;
  padding: 10px;
}

.error {
  color: #ff4444;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 10px;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 15px;
}

.category-item {
  padding: 10px 5px;
  cursor: pointer;
  white-space: nowrap;
  text-align: center;
  border-radius: 15px;
  transition: all 0.3s;
  overflow: hidden;
  text-overflow: ellipsis;
}

.category-item:hover {
  background-color: #f5f5f5;
}

.category-item.active {
  background-color: #ff5722;
  color: white;
}

@media (max-width: 768px) {
  .category-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 480px) {
  .category-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>