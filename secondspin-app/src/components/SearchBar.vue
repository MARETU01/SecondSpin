<template>
  <div class="search-bar-container">
    <div class="search-bar">
      <input 
        type="text" 
        v-model="searchQuery"
        placeholder="Search for the product you want..."
        @keyup.enter="handleSearch"
      />
      <button class="search-btn" @click="handleSearch">
        <i class="search-icon">🔍</i>
      </button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'SearchBar',
  data() {
    return {
      searchQuery: ''
    }
  },
  methods: {
    handleSearch() {
      this.$emit('search', this.searchQuery)
    }
  }
}
</script>

<style scoped>
.search-bar-container {
  background-color: #f5f5f5;
  padding: 20px 0;
}

.search-bar {
  max-width: 800px;
  margin: 0 auto;
  display: flex;
  border-radius: 25px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.search-bar input {
  flex: 1;
  padding: 12px 20px;
  border: none;
  font-size: 1rem;
  outline: none;
}

.search-btn {
  width: 60px;
  background-color: #ff5722;
  color: white;
  border: none;
  cursor: pointer;
  font-size: 1.2rem;
  display: flex;
  align-items: center;
  justify-content: center;
}

.search-btn:hover {
  background-color: #e64a19;
}
</style>