import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: {
      title: '登录 - 鱼皑AI超级智能体应用平台',
    },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: {
      title: '注册 - 鱼皑AI超级智能体应用平台',
    },
  },
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue'),
    meta: {
      title: '首页 - 鱼皑AI超级智能体应用平台',
      description:
        '鱼皑AI超级智能体应用平台提供AI恋爱大师、AI矿山专家和AI超级智能体服务，满足您的各种AI对话需求',
    },
  },
  {
    path: '/love-master',
    name: 'LoveMaster',
    component: () => import('../views/LoveMaster.vue'),
    meta: {
      title: 'AI恋爱大师 - 鱼皮AI超级智能体应用平台',
      description:
        'AI恋爱大师是鱼皮AI超级智能体应用平台的专业情感顾问，帮你解答各种恋爱问题，提供情感建议',
    },
  },
  {
    path: '/mine-agent',
    name: 'MineAgent',
    component: () => import('../views/MineAgent.vue'),
    meta: {
      title: 'AI矿山专家 - 鱼皮AI超级智能体应用平台',
      description:
        'AI矿山专家是鱼皮AI超级智能体应用平台的专业矿业顾问，帮您解答各种矿山技术问题，提供专业建议',
    },
  },
  {
    path: '/super-agent',
    name: 'SuperAgent',
    component: () => import('../views/SuperAgent.vue'),
    meta: {
      title: 'AI超级智能体 - 鱼皮AI超级智能体应用平台',
      description:
        'AI超级智能体是鱼皮AI超级智能体应用平台的全能助手，能解答各类专业问题，提供精准建议和解决方案',
    },
  },
  {
    path: '/file-center',
    name: 'FileCenter',
    component: () => import('../views/FileCenter.vue'),
    meta: {
      title: '文件中心 - 鱼ai AI超级智能体应用平台',
      description:
        '统一的文件上传与管理平台，支持大文件分片上传、断点续传、秒传和文件管理',
    },
  },
  {
    path: '/vector-manage',
    name: 'VectorManage',
    component: () => import('../views/DocumentVectorManage.vue'),
    meta: {
      title: '文档向量管理 - 鱼皑AI超级智能体应用平台',
      description: '查看和管理您上传的文档向量数据',
    },
  },
  {
    path: '/es-doc-manage',
    name: 'EsDocManage',
    component: () => import('../views/EsDocManage.vue'),
    meta: {
      title: 'ES索引文档管理 - 鱼皑AI超级智能体应用平台',
      description: '查看和管理Elasticsearch中的文档向量数据',
    },
  },
  {
    path: '/data-center',
    name: 'DataCenter',
    component: () => import('../views/DataCenter.vue'),
    meta: {
      title: '数据中心 - 鱼皑AI超级智能体应用平台',
      description:
        '矿山地质数据管理平台，支持矿区、煤层、钻孔、巷道等数据的增删改查',
    },
  },
  {
    path: '/data-center/:table',
    name: 'DataDetail',
    component: () => import('../views/DataDetail.vue'),
    meta: {
      title: '数据详情 - 鱼皑AI超级智能体应用平台',
      description: '矿山数据详情管理页面',
    },
  },
  // 保留旧路由以便兼容
  {
    path: '/file-upload',
    redirect: '/file-center',
  },
  {
    path: '/file-manage',
    redirect: '/file-center',
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 全局导航守卫，设置文档标题
router.beforeEach((to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = to.meta.title
  }
  next()
})

export default router
