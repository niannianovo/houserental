// axios基础的封装
import axios from 'axios'
import { ElMessage } from 'element-plus';
const httpInstance = axios.create({
    baseURL: '/api',
    timeout: 15000
})

// 拦截器

// axios请求拦截器
httpInstance.interceptors.request.use(config => {
    config.headers['Content-Type'] = 'application/json;charset=utf-8';
    return config
}, e => Promise.reject(e))

// axios响应式拦截器
httpInstance.interceptors.response.use(res => {
    //检查业务状态码
    if (res.data.code === '200') {  // 假设成功状态码是200
        return res.data;  // 返回数据
    } else {
        // 业务错误（如用户名密码错误）
        ElMessage({
            type: 'error',
            message: res.data.msg || '操作失败'
        });
        return Promise.reject(new Error(res.data.msg));  // 返回错误，阻止后续执行
    }
},
    e => {
        //统一错误提示
        const msg = e.response?.data?.msg
            ? e.response.data.msg.split(":")[0]
            : (e.message || '网络请求失败')
        ElMessage({
            type: 'error',
            message: msg
        })
        return Promise.reject(e)
    })


export default httpInstance