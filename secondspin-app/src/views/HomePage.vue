<template>
  <div class="home">
    <Header></Header>
    <SearchBar @search="handleSearch" />
    <CategoryNav @category-selected="handleCategorySelected" />
    
    <div class="container">
      <div v-if="currentCategory" class="category-info">
        <h2>{{ currentCategory.name }}</h2>
        <p>{{ currentCategory.description }}</p>
      </div>
      <h2 v-else-if="searchQuery">Search results: {{ searchQuery }}</h2>
      <h2 v-else>Hot items</h2>
      
      <div v-if="loadingProducts" class="loading">Loading...</div>
      <div v-else-if="productError" class="error">{{ productError }}</div>
      <div v-else class="product-grid">
        <ProductCard 
          v-for="product in products" 
          :key="product.productId"
          :product="product"
          @click="$router.push(`/iteminfo/${product.productId}`)"
        />
      </div>

      <!-- 分页组件 -->
      <div v-if="pagination.total > 0" class="pagination">
        <button 
          :disabled="pagination.currentPage === 1" 
          @click="changePage(pagination.currentPage - 1)"
        >
          Previous Page
        </button>
        <span>Page {{ pagination.currentPage }}  /  {{ Math.ceil(pagination.total / pagination.pageSize) }} pages in total</span>
        <button 
          :disabled="pagination.currentPage * pagination.pageSize >= pagination.total" 
          @click="changePage(pagination.currentPage + 1)"
        >
          Next page
        </button>
      </div>
    </div>
    
    <Footer></Footer>
  </div>
</template>

<script>
import Header from '@/components/AppHeader.vue'
import SearchBar from '@/components/SearchBar.vue'
import CategoryNav from '@/components/CategoryNav.vue'
import ProductCard from '@/components/ProductCard.vue'
import Footer from '@/components/AppFooter.vue'
import http from '@/http';

export default {
  name: 'HomePage',
  components: {
    Header,
    SearchBar,
    CategoryNav,
    ProductCard,
    Footer
  },
  data() {
    return {
      products: [],
      currentCategory: null,
      loadingProducts: false,
      productError: null,
      searchQuery: '',
      pagination: {
        currentPage: 1,
        pageSize: 10,
        total: 0
      }
    }
  },
  methods: {
    handleSearch(query) {
      this.searchQuery = query
      this.currentCategory = null
      this.pagination.currentPage = 1
      this.fetchProducts()
    },
    viewProductDetail(productId) {
      this.$router.push(`/product/${productId}`)
    },
    handleCategorySelected(categoryId) {
      this.searchQuery = ''
      this.pagination.currentPage = 1
      if (categoryId === 0) {
        this.currentCategory = null
        this.fetchProducts()
      } else {
        this.fetchCategoryProducts(categoryId)
      }
    },
    async fetchCategoryProducts(categoryId) {
      this.loadingProducts = true
      this.productError = null
      try {
        const response = await http.get(`/categories/${categoryId}`, {
          params: {
            pageNum: this.pagination.currentPage,
            pageSize: this.pagination.pageSize
          }
        })
        if (response.data && response.data.code === 1) {
          this.currentCategory = {
            id: categoryId,
            name: response.data.data.name,
            description: response.data.data.description
          }
          this.products = response.data.data.pageDTO.data || []
          this.pagination.total = response.data.data.pageDTO.total || 0
        } else {
          this.productError = response.data.message || 'Failed to get classifieds'
        }
      } catch (err) {
        this.productError = err.response?.data?.message || err.message || 'An error occurred while requesting classifieds'
        console.error('Error in getting classifieds:', err)
      } finally {
        this.loadingProducts = false
      }
    },
    async fetchProducts() {
      this.loadingProducts = true
      this.productError = null
      try {
        let response
        if (this.searchQuery) {
          // 使用搜索接口
          response = await http.get('/products/search', {
            params: {
              keyword: this.searchQuery,
              pageNum: this.pagination.currentPage,
              pageSize: this.pagination.pageSize
            }
          })
        } else {
          // 使用首页商品接口
          response = await http.get('/products/home', {
            params: {
              pageNo: this.pagination.currentPage,
              pageSize: this.pagination.pageSize
            }
          })
        }
        
        if (response.data && response.data.code === 1) {
          this.products = response.data.data.data || []
          this.pagination.total = response.data.data.total || 0
        } else {
          this.productError = response.data.message || 'Failed to get the list of products'
        }
      } catch (err) {
        this.productError = err.response?.data?.message || err.message || 'An error occurred while requesting a list of products'
        console.error('Error getting the list of products:', err)
      } finally {
        this.loadingProducts = false
      }
    },
    changePage(page) {
      if (page < 1 || page > Math.ceil(this.pagination.total / this.pagination.pageSize)) {
        return
      }
      this.pagination.currentPage = page
      if (this.currentCategory) {
        this.fetchCategoryProducts(this.currentCategory.id)
      } else {
        this.fetchProducts()
      }
      // 滚动到顶部
      window.scrollTo({ top: 0, behavior: 'smooth' })
    }
  },
  created() {
    // 初始加载时获取全部商品
    this.fetchProducts()
  }
}
</script>

<style scoped>
.home {
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

.product-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 20px;
  padding: 20px 0;
}

.category-info {
  margin-bottom: 20px;
}

h2 {
  color: #333;
  font-size: 1.5rem;
  margin-bottom: 15px;
}

.loading, .error {
  text-align: center;
  padding: 20px;
  font-size: 1.2rem;
}

.error {
  color: #ff4444;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 30px 0;
  gap: 20px;
}

.pagination button {
  padding: 8px 16px;
  background-color: #f0f0f0;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.pagination button:hover:not(:disabled) {
  background-color: #e0e0e0;
}

.pagination button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .product-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

@media (max-width: 992px) {
  .product-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .product-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 480px) {
  .product-grid {
    grid-template-columns: 1fr;
  }
  
  .pagination {
    flex-direction: column;
    gap: 10px;
  }
}
</style>