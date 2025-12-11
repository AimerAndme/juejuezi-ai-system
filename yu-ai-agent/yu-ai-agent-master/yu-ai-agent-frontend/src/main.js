import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createHead } from '@vueuse/head'
import './style.css'
import './styles/global-layout.css' // 引入全局布局配置

const app = createApp(App)
const head = createHead()

app.use(router)
app.use(head)
app.mount('#app')
