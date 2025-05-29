import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import http from './http'
import './styles/tailwind.css';
// 配置Tailwind主题
import './tailwind.config.js';

// 导入Font Awesome
import 'font-awesome/css/font-awesome.min.css';
const app = createApp(App)

app.config.globalProperties.$http = http

app.use(store).use(router).mount('#app')