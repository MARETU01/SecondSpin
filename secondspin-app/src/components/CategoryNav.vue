<template>
  <nav class="category-nav">
    <ul>
      <li 
        v-for="category in categories" 
        :key="category"
        :class="{ active: selectedCategory === category }"
        @click="selectCategory(category)"
      >
        {{ category }}
      </li>
    </ul>
  </nav>
</template>

<script>
export default {
  name: 'CategoryNav',
  props: {
    categories: {
      type: Array,
      required: true
    }
  },
  data() {
    return {
      selectedCategory: '全部'
    }
  },
  methods: {
    selectCategory(category) {
      this.selectedCategory = category
      this.$emit('category-selected', category)
    }
  }
}
</script>

<style scoped>
.category-nav {
  background-color: white;
  padding: 15px 0;
  box-shadow: 0 2px 5px rgba(0,0,0,0.05);
}

ul {
  list-style: none;
  display: flex;
  justify-content: center;
  gap: 20px;
  margin: 0;
  padding: 0;
  max-width: 1200px;
  margin: 0 auto;
  overflow-x: auto;
  padding: 0 15px;
}

li {
  padding: 8px 15px;
  cursor: pointer;
  white-space: nowrap;
  border-radius: 15px;
  transition: all 0.3s;
}

li:hover {
  background-color: #f5f5f5;
}

li.active {
  background-color: #ff5722;
  color: white;
}
</style>