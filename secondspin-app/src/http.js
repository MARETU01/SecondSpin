import axios from 'axios';

const instance = axios.create({
    baseURL: 'http://localhost:8080',
    timeout: 10000,
    withCredentials: true,
});

// 请求拦截器
instance.interceptors.request.use(
    config => {
        // 检查 token 是否存在且是否过期
        const token = localStorage.getItem('token');
        const tokenExpireTime = localStorage.getItem('tokenExpireTime');
        
        // 如果 token 存在但已过期，则清除 token
        if (token && tokenExpireTime && Date.now() > parseInt(tokenExpireTime)) {
            localStorage.removeItem('token');
            localStorage.removeItem('tokenExpireTime');
            localStorage.removeItem('userInfo');
            
            // 可以根据需要跳转到登录页或显示提示
            // window.location.href = '/login';
            console.warn('Token 已过期，请重新登录');
            
            // 这里可以抛出一个错误，阻止请求继续发送
            return Promise.reject(new Error('Token 已过期'));
        }

        // 如果 token 存在且未过期，添加到请求头
        if (token) {
            config.headers['SecondSpin'] = token;
        }

        return config;
    },
    error => {
        // 对请求错误做些什么
        return Promise.reject(error);
    }
);

// 响应拦截器 - 处理 token 过期的情况
instance.interceptors.response.use(
    response => {
        // 对响应数据做点什么
        return response;
    },
    error => {
        if (error.response) {
            // 根据后端返回的状态码处理 token 过期
            if (error.response.status === 401) {
                // 清除本地存储的 token 和用户信息
                localStorage.removeItem('token');
                localStorage.removeItem('tokenExpireTime');
                localStorage.removeItem('userInfo');
                
                // 可以根据需要跳转到登录页或显示提示
                console.warn('登录状态已过期，请重新登录');
                // window.location.href = '/login';
            }
        }
        return Promise.reject(error);
    }
);

export default instance;