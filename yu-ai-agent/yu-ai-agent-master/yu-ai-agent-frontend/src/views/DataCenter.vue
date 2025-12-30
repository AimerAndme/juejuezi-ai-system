<template>
  <div class="data-center">
    <h2>数据中心</h2>
    <p class="description">矿山地质数据管理平台</p>

    <div class="category-grid">
      <!-- 基础信息 -->
      <div class="category-section">
        <h3>基础信息</h3>
        <div class="card-grid">
          <div class="data-card" @click="navigateToTable('mine-area')">
            <div class="card-icon">🏔️</div>
            <div class="card-title">矿区信息</div>
            <div class="card-desc">矿区基本信息管理</div>
          </div>
          <div class="data-card" @click="navigateToTable('coal-seam')">
            <div class="card-icon">⚫</div>
            <div class="card-title">煤层信息</div>
            <div class="card-desc">煤层参数管理</div>
          </div>
        </div>
      </div>

      <!-- 钻探数据 -->
      <div class="category-section">
        <h3>钻探数据</h3>
        <div class="card-grid">
          <div class="data-card" @click="navigateToTable('borehole')">
            <div class="card-icon">🔩</div>
            <div class="card-title">钻孔信息</div>
            <div class="card-desc">钻孔基本信息</div>
          </div>
          <div class="data-card" @click="navigateToTable('lithology-log')">
            <div class="card-icon">📊</div>
            <div class="card-title">岩性分层</div>
            <div class="card-desc">岩性分层记录</div>
          </div>
          <div class="data-card" @click="navigateToTable('seam-intercept')">
            <div class="card-icon">📍</div>
            <div class="card-title">见煤记录</div>
            <div class="card-desc">钻孔见煤记录</div>
          </div>
        </div>
      </div>

      <!-- 巷道工作面 -->
      <div class="category-section">
        <h3>巷道工作面</h3>
        <div class="card-grid">
          <div class="data-card" @click="navigateToTable('roadway-point')">
            <div class="card-icon">📌</div>
            <div class="card-title">巷道点位</div>
            <div class="card-desc">巷道中心线点</div>
          </div>
          <div class="data-card" @click="navigateToTable('roadway')">
            <div class="card-icon">🛤️</div>
            <div class="card-title">巷道信息</div>
            <div class="card-desc">巷道基本信息</div>
          </div>
          <div class="data-card" @click="navigateToTable('mining-face')">
            <div class="card-icon">⛏️</div>
            <div class="card-title">采煤工作面</div>
            <div class="card-desc">工作面信息管理</div>
          </div>
        </div>
      </div>

      <!-- 储量统计 -->
      <div class="category-section">
        <h3>储量统计</h3>
        <div class="card-grid">
          <div class="data-card" @click="navigateToTable('reserve-block')">
            <div class="card-icon">📦</div>
            <div class="card-title">储量块段</div>
            <div class="card-desc">资源储量块段</div>
          </div>
          <div class="data-card" @click="navigateToTable('monthly-production')">
            <div class="card-icon">📈</div>
            <div class="card-title">月度产量</div>
            <div class="card-desc">月度采出量统计</div>
          </div>
          <div class="data-card" @click="navigateToTable('three-quantities')">
            <div class="card-icon">📋</div>
            <div class="card-title">三量台账</div>
            <div class="card-desc">三量动态台账</div>
          </div>
        </div>
      </div>

      <!-- 地表监测 -->
      <div class="category-section">
        <h3>地表监测</h3>
        <div class="card-grid">
          <div class="data-card" @click="navigateToTable('surface-station')">
            <div class="card-icon">📡</div>
            <div class="card-title">观测站</div>
            <div class="card-desc">地表观测站管理</div>
          </div>
          <div
            class="data-card"
            @click="navigateToTable('subsidence-observation')"
          >
            <div class="card-icon">📉</div>
            <div class="card-title">沉降观测</div>
            <div class="card-desc">沉降观测记录</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const navigateToTable = (tableName) => {
  router.push(`/data-center/${tableName}`)
}
</script>

<style scoped>
.data-center {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}
h2 {
  margin-bottom: 10px;
  color: #333;
}
.description {
  color: #666;
  margin-bottom: 30px;
}
.category-section {
  margin-bottom: 30px;
}
.category-section h3 {
  color: #409eff;
  margin-bottom: 15px;
  border-left: 4px solid #409eff;
  padding-left: 10px;
}
.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 15px;
}
.data-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s;
}
.data-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}
.card-icon {
  font-size: 32px;
  margin-bottom: 10px;
}
.card-title {
  font-weight: 600;
  margin-bottom: 5px;
}
.card-desc {
  font-size: 12px;
  color: #999;
}
</style>
