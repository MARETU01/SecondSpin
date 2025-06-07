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
      <h2 v-else>热门商品</h2>
      
      <div v-if="loadingProducts" class="loading">加载中...</div>
      <div v-else-if="productError" class="error">{{ productError }}</div>
      <div v-else class="product-grid">
        <ProductCard 
          v-for="product in products" 
          :key="product.productId"
          :product="product"
          @click="viewProductDetail(product.productId)"
        />
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
      pagination: {
        currentPage: 1,
        pageSize: 10,
        total: 0
      }
    }
  },
  computed: {
    filteredProducts() {
      let filtered = this.products
      
      if (this.selectedCategory && this.selectedCategory !== '全部') {
        filtered = filtered.filter(p => p.category === this.selectedCategory)
      }
      
      if (this.searchQuery) {
        const query = this.searchQuery.toLowerCase()
        filtered = filtered.filter(p => 
          p.title.toLowerCase().includes(query) || 
          p.description?.toLowerCase().includes(query))
      }
      
      return filtered
    }
  },
  methods: {
    filterByCategory(category) {
      this.selectedCategory = category
      // 如果需要根据分类重新从服务器获取数据，可以取消下面注释
      // this.pagination.currentPage = 1
      // this.fetchProducts()
    },
    handleSearch(query) {
      this.searchQuery = query
      // 如果需要根据搜索词重新从服务器获取数据，可以取消下面注释
      // this.pagination.currentPage = 1
      // this.fetchProducts()
    },
    viewProductDetail(productId) {
      this.$router.push(`/product/${productId}`)
    },
    handleCategorySelected(categoryId) {
      if (categoryId === 0) {
        // 选择了"全部"分类
        this.currentCategory = null
        this.fetchAllProducts()
      } else {
        this.fetchCategoryProducts(categoryId)
      }
    },
    async fetchCategoryProducts(categoryId) {
      this.loadingProducts = true
      this.productError = null
      try {
        const response = await http.get(`/categories/${categoryId}`)
        if (response.data && response.data.code === 1) {
          this.currentCategory = {
            name: response.data.data.name,
            description: response.data.data.description
          }
          this.products = response.data.data.pageDTO.data || []
          this.pagination.total = response.data.data.pageDTO.total || 0
        } else {
          this.productError = response.data.message || '获取分类商品失败'
        }
      } catch (err) {
        this.productError = err.response?.data?.message || err.message || '请求分类商品时出错'
        console.error('获取分类商品出错:', err)
      } finally {
        this.loadingProducts = false
      }
    },
    async fetchAllProducts() {
      this.loadingProducts = true
      this.productError = null
      try {
        const response = await http.get('/products/home', {
          params: {
            pageNum: this.pagination.currentPage,
            pageSize: this.pagination.pageSize
          }
        })
        if (response.data && response.data.code === 1) {
          this.products = response.data.data.data || []
          this.pagination.total = response.data.data.total || 0
        } else {
          this.productError = response.data.message || '获取商品列表失败'
        }
      } catch (err) {
        this.productError = err.response?.data?.message || err.message || '请求商品列表时出错'
        console.error('获取商品列表出错:', err)
      } finally {
        this.loadingProducts = false
      }
    },
  
  created() {
    this.fetchAllProducts()
  },
  watch: {
    // 如果需要根据分类或搜索词重新获取数据，可以取消下面注释
    /*
    selectedCategory(newVal, oldVal) {
      if (newVal !== oldVal) {
        this.fetchProducts()
      }
    },
    searchQuery(newVal, oldVal) {
      if (newVal !== oldVal) {
        this.fetchProducts()
      }
    }
    */
  }
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
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
  margin-top: 20px;
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
</style>