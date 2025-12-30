# AppButton 通用按钮组件使用指南

## 📋 组件概述

`AppButton` 是一个完整的设计系统按钮组件，提供多种样式、尺寸和状态变体，统一整个应用的按钮交互体验。

## 🎨 基础使用

### 主色按钮（默认）

用于主要操作和关键行动

```vue
<AppButton text="确认" variant="primary" size="medium" @click="handleConfirm" />
```

### 次要按钮

用于次要操作和取消行动

```vue
<AppButton text="取消" variant="secondary" size="medium" />
```

### 成功按钮

用于成功、保存等正面操作

```vue
<AppButton text="保存" variant="success" size="medium" />
```

### 警告按钮

用于警告和需要注意的操作

```vue
<AppButton text="警告" variant="warning" size="medium" />
```

### 危险按钮

用于删除、危险操作

```vue
<AppButton text="删除" variant="danger" size="medium" />
```

### 幽灵按钮

用于轻量级操作，只显示边框

```vue
<AppButton text="更多" variant="ghost" size="medium" />
```

## 📏 尺寸变体

### 小尺寸（Small）

28px 高度，适用于表格行操作、标签按钮

```vue
<AppButton text="编辑" variant="primary" size="small" />
```

### 中尺寸（Medium）

36px 高度，默认尺寸，适用于大多数场景

```vue
<AppButton text="确认" variant="primary" size="medium" />
```

### 大尺寸（Large）

44px 高度，适用于主要操作、表单提交

```vue
<AppButton text="提交表单" variant="primary" size="large" />
```

## 🔧 属性详解

| 属性       | 类型    | 默认值    | 说明                                                          |
| ---------- | ------- | --------- | ------------------------------------------------------------- |
| `text`     | String  | ''        | 按钮显示文本                                                  |
| `variant`  | String  | 'primary' | 样式类型：primary、secondary、success、warning、danger、ghost |
| `size`     | String  | 'medium'  | 尺寸：small、medium、large                                    |
| `disabled` | Boolean | false     | 是否禁用                                                      |
| `loading`  | Boolean | false     | 是否加载中                                                    |

## 📡 事件

### click 事件

```vue
<AppButton text="提交" @click="handleSubmit" />

<script setup>
const handleSubmit = (event) => {
  console.log('点击了按钮', event)
}
</script>
```

## 🎬 状态管理

### 禁用状态

```vue
<AppButton text="确认" :disabled="isLoading" />
```

### 加载状态

加载中时自动显示加载动画，并禁用交互

```vue
<AppButton text="提交中..." :loading="isSubmitting" />
```

## 💡 实际应用示例

### 确认对话框按钮组

```vue
<div class="button-group">
  <AppButton text="取消" variant="secondary" size="medium" @click="handleCancel" />
  <AppButton text="确认" variant="primary" size="medium" @click="handleConfirm" />
</div>
```

### 表格行操作按钮

```vue
<template v-for="item in items" :key="item.id">
  <AppButton text="编辑" variant="primary" size="small" @click="edit(item)" />
  <AppButton text="删除" variant="danger" size="small" @click="delete item" />
</template>
```

### 表单提交

```vue
<form @submit.prevent="submitForm">
  <!-- 表单字段 -->
  <AppButton 
    text="提交" 
    variant="primary" 
    size="large" 
    :loading="isSubmitting"
    :disabled="!isFormValid"
  />
</form>
```

## 🎨 主题适配

按钮组件自动适配浅色和深色主题：

```vue
<!-- 浅色模式（默认） -->
<AppButton text="确认" variant="primary" size="medium" />

<!-- 深色模式 - 自动应用深色样式 -->
<!-- 通过添加 dark-mode 类到 :root 实现 -->
```

在 `index.html` 或 `main.js` 中切换主题：

```javascript
// 启用深色模式
document.documentElement.classList.add('dark-mode')

// 禁用深色模式
document.documentElement.classList.remove('dark-mode')
```

## 📱 响应式设计

按钮在不同屏幕尺寸上自动调整：

- **桌面端 (>768px)**：正常显示
- **移动端 (<768px)**：自动调整间距和字体大小

## ♿ 无障碍支持

- ✅ 完整的 keyboard 支持
- ✅ Focus-visible 视觉反馈
- ✅ 禁用状态明确显示
- ✅ 适当的 ARIA 属性

## 🔄 迁移指南（从旧样式到 AppButton）

### 之前（旧样式）

```vue
<div class="app-button">
  <span class="btn-text">开始咨询</span>
  <span class="btn-icon">→</span>
</div>
```

### 之后（AppButton 组件）

```vue
<AppButton text="开始咨询" variant="primary" size="medium" />
```

## 💻 CSS 变量自定义

如需自定义按钮样式，可修改 `AppButton.vue` 中的 CSS 变量：

```css
:root {
  /* 主色系 */
  --primary-color: #1677ff;
  --primary-hover: #40a9ff;
  --primary-active: #096dd9;

  /* 其他颜色 */
  --success-color: #52c41a;
  --warning-color: #faad14;
  --error-color: #f5222d;
}
```

## 📝 常见问题

### Q: 如何让按钮占满容器宽度？

A: 目前按钮不支持 block 属性，可在外层容器设置：

```vue
<div style="width: 100%;">
  <AppButton text="提交" variant="primary" size="large" />
</div>
```

### Q: 如何添加加载动画？

A: 使用 `loading` 属性：

```vue
<AppButton text="保存中..." :loading="isSaving" />
```

### Q: 如何在点击时禁用按钮？

A: 使用 `disabled` 属性：

```vue
<AppButton text="提交" :disabled="isProcessing" @click="handleSubmit" />
```

## 🚀 性能优化

- ✨ 轻量级组件（仅 193 行）
- ⚡ 使用 CSS 变量实现主题切换，避免重新渲染
- 🎯 优化过的动画（使用 transform 和 opacity）
- 📦 支持按需加载
