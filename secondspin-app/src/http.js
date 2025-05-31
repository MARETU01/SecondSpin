import axios from 'axios';

const instance = axios.create({
    baseURL: 'http://localhost:8080',
    timeout: 10000,
    withCredentials: true,
});

// 请求拦截器
instance.interceptors.request.use(
    config => {
        // 在发送请求之前做些什么
        // 例如：添加token、显示加载状态等

        // 从localStorage获取token（根据您的项目实际情况调整）
        const token = localStorage.getItem('token');
        if (token) {
            // 让每个请求携带token--['Authorization']为自定义key 请根据实际情况自行修改
            config.headers['SecondSpin'] = token;
        }

        // 显示加载状态（根据您的UI库调整）
        // showLoading();

        return config;
    },
);

export default instance;