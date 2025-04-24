<template>
  <div class="home">
    <img alt="Vue logo" src="../assets/logo.png">
    <HelloWorld msg="Welcome to Your Vue.js App"/>
  </div>
</template>

<script>
// @ is an alias to /src
import HelloWorld from '@/components/HelloWorld.vue'

export default {
  name: 'HomeView',
  components: {
    HelloWorld
  },
  methods: {
    async fetchData() {
      try {
        // 通过 this.$axios 调用
        const response = await this.$http.get('/users/test')
        console.log(response.data)
      } catch (error) {
        console.error('请求失败:', error)
      }
    }
  },
  mounted() {
    this.fetchData()
  }

}


</script>

