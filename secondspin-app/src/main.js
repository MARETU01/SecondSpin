import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import http from './http'
// 导入Font Awesome
import 'font-awesome/css/font-awesome.min.css';
import ElementPlus from 'element-plus'
const app = createApp(App)

app.config.globalProperties.$http = http

app.use(store).use(router).use(ElementPlus).mount('#app')