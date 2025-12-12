# java2ai.com 风格应用说明

## 🎨 设计风格特点

本项目已成功模仿 [java2ai.com](https://java2ai.com/) 的现代简洁设计风格：

### ✨ 核心特征

- **简洁现代**：白色卡片 + 柔和阴影，去除花哨装饰
- **蓝色主调**：专业的科技蓝色系，传递信任感
- **宽松布局**：更大的内边距（24-32px）和间距
- **卡片设计**：清晰的视觉层次，12px 圆角
- **响应式**：完美适配桌面、平板、手机

## 📋 已应用的改变

### 1. 配色方案

```css
/* 主色调 - 专业蓝色系 */
--primary-blue: #0066cc
--primary-blue-light: #3385e0
--primary-blue-lighter: #e6f2ff

/* 背景 - 简洁白灰 */
--bg-white: #ffffff
--bg-light: #f8f9fa

/* 文字 - 高对比度 */
--text-primary: #1a1a1a
--text-secondary: #666666
```

### 2. 布局间距

```css
/* 更宽松的间距 */
--page-padding-horizontal: 24px (原 8px)
--chat-area-padding: 24px (原 16px)
--card-padding: 32px (原 24px)
--card-gap: 32px (原 24px)
```

### 3. 视觉效果

```css
/* 柔和阴影 */
--shadow-card: 0 2px 8px 0 rgba(0, 0, 0, 0.08)

/* 统一圆角 */
--card-border-radius: 12px
--chat-area-border-radius: 12px

/* 细线边框 */
border: 1px solid #e8e8e8 (替代虚线)
```

### 4. 页面背景

- **原来**：渐变蓝色背景
- **现在**：纯净的浅灰背景 `#f8f9fa`

### 5. 卡片样式

- **原来**：半透明卡片 + 虚线边框 + 毛玻璃效果
- **现在**：纯白卡片 + 细线边框 + 柔和阴影

## 🎯 对比效果

| 元素     | 原风格   | java2ai.com 风格 |
| -------- | -------- | ---------------- |
| 页面背景 | 渐变蓝色 | 浅灰纯色         |
| 卡片背景 | 半透明   | 纯白             |
| 边框样式 | 虚线 2px | 实线 1px         |
| 阴影效果 | 偏重     | 柔和             |
| 圆角大小 | 16-20px  | 12px             |
| 内边距   | 紧凑     | 宽松             |
| 按钮样式 | 毛玻璃   | 扁平化           |

## 📱 响应式设计

### 桌面端 (>768px)

- 容器最大宽度：1200px
- 左右边距：24px
- 卡片内边距：32px

### 平板端 (≤768px)

- 左右边距：16px
- 卡片内边距：24px
- 卡片间距：20px

### 手机端 (≤480px)

- 左右边距：12px
- 卡片内边距：20px
- 卡片间距：16px

## 🔧 如何使用

### 自动应用

所有页面已自动应用新风格，无需手动修改！

### 手动调整（可选）

如需微调某个页面，在该页面的 `<style>` 中覆盖变量：

```vue
<style scoped>
.my-container {
  /* 使用全局变量 */
  padding: var(--card-padding);
  background: var(--bg-white);
  border: 1px solid var(--border-light);
  box-shadow: var(--shadow-card);
  border-radius: var(--card-border-radius);
}
</style>
```

### 应用主题类

```html
<!-- 蓝色主题（默认） -->
<div class="page-container theme-love">
  <!-- 橙色主题（矿山） -->
  <div class="page-container theme-mine">
    <!-- 紫色主题（超级智能体） -->
    <div class="page-container theme-super"></div>
  </div>
</div>
```

## 🎨 快速定制

### 想要更大的容器宽度？

```css
/* 在 global-layout.css 中修改 */
:root {
  --container-max-width: 1400px; /* 默认 1200px */
}
```

### 想要更紧凑的布局？

```css
:root {
  --card-padding: 20px;
  --card-gap: 20px;
  --page-padding-horizontal: 16px;
}
```

### 想要不同的主色调？

```css
:root {
  --primary-blue: #0052cc; /* 更深的蓝色 */
  --primary-blue: #1890ff; /* 更亮的蓝色 */
  --primary-blue: #6366f1; /* 紫蓝色 */
}
```

## 📂 相关文件

- **全局样式**：`src/styles/global-layout.css`
- **入口文件**：`src/main.js` (已引入全局样式)
- **详细文档**：`全局布局配置说明.md`

## ✅ 验证清单

已完成的改造项目：

- [x] 页面背景改为浅灰色
- [x] 卡片样式改为白色+柔和阴影
- [x] 边框改为细线实线
- [x] 圆角统一为 12px
- [x] 内边距增大到 24-32px
- [x] 按钮样式扁平化
- [x] 配色改为蓝色系
- [x] 响应式优化
- [x] 阴影效果柔和化

## 🚀 效果预览

刷新页面即可看到新风格！主要体现在：

1. **首页**：白色卡片、蓝色渐变头部
2. **聊天页面**：白色卡片对话框
3. **按钮**：扁平化设计
4. **整体**：更加现代、专业、简洁

---

**提示**：所有修改都集中在 `src/styles/global-layout.css`，方便后续统一调整！
