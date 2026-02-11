<template>
  <div class="file-upload-page">
    <div class="container">
      <FileUploader />
    </div>
  </div>
</template>

<script setup>
import FileUploader from '../components/file/FileUploader.vue'
</script>

<style scoped>
.file-upload-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 20px;
}

.container {
  max-width: 800px;
  margin: 0 auto;
}
</style>
