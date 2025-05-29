<template>
  <div class="home">
    <Header></Header>
    <SearchBar @search="handleSearch" />
    <CategoryNav :categories="categories" @category-selected="filterByCategory" />
    
    <div class="container">
      <h2 v-if="selectedCategory">当前分类: {{ selectedCategory }}</h2>
      <h2 v-else>热门商品</h2>
      
      <div class="product-grid">
        <ProductCard 
          v-for="product in filteredProducts" 
          :key="product.id"
          :product="product"
          @click="viewProductDetail(product.id)"
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
      categories: [
        '全部', '电子产品', '服装', '家具', '书籍', '运动器材', '母婴用品', '其他'
      ],
      products: [
        {
          id: 1,
          title: 'iPhone 12 Pro',
          price: 4500,
          originalPrice: 8499,
          category: '电子产品',
          image: 'https://example.com/iphone12.jpg',
          location: '北京',
          publishTime: '3天前',
          condition: '9成新'
        },
        {
          id: 2,
          title: 'MacBook Pro 2020',
          price: 8000,
          originalPrice: 12999,
          category: '电子产品',
          image: 'https://example.com/macbook.jpg',
          location: '上海',
          publishTime: '1周前',
          condition: '95成新'
        },
        {
          id: 3,
          title: '男士冬季夹克',
          price: 200,
          originalPrice: 599,
          category: '服装',
          image: 'https://example.com/jacket.jpg',
          location: '广州',
          publishTime: '2天前',
          condition: '8成新'
        },
        // 更多商品数据...
      ],
      selectedCategory: '',
      searchQuery: ''
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
    },
    handleSearch(query) {
      this.searchQuery = query
    },
    viewProductDetail(productId) {
      this.$router.push(`/product/${productId}`)
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
</style>