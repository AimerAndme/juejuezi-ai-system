<template>
  <div class="data-detail">
    <!-- 页面头部 -->
    <div class="data-detail__header">
      <div class="data-detail__header-left">
        <AppButton
          text="← 返回"
          variant="secondary"
          size="medium"
          @click="goBack"
        />
        <h1 class="data-detail__title">{{ pageTitle }}</h1>
      </div>
      <div class="data-detail__header-right">
        <!-- 矿区过滤下拉框（仅钻孔页面显示） -->
        <div
          v-if="route.params.table === 'borehole'"
          class="data-detail__filter"
        >
          <select
            v-model="selectedAreaId"
            @change="filterByAreaId"
            class="data-detail__filter-select"
          >
            <option value="">全部矿区</option>
            <option
              v-for="area in mineAreas"
              :key="area.areaId"
              :value="area.areaId"
            >
              {{ area.areaName }} ({{ area.areaId }})
            </option>
          </select>
        </div>
        <AppButton
          text="+ 新增"
          variant="primary"
          size="medium"
          @click="showAddForm"
        />
        <AppButton
          text="刷新"
          variant="success"
          size="medium"
          @click="loadData"
        />
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="data-detail__table-wrapper">
      <div v-if="loading" class="data-detail__loading">
        <div class="data-detail__spinner"></div>
        <p style="margin-top: 16px; color: #8c8c8c">加载中...</p>
      </div>
      <div v-else-if="tableData.length === 0" class="data-detail__empty">
        <div class="data-detail__empty-icon">📋</div>
        <div class="data-detail__empty-text">暂无数据</div>
        <AppButton
          text="创建第一条记录"
          variant="primary"
          size="medium"
          @click="showAddForm"
        />
      </div>
      <table v-else class="data-detail__table">
        <thead>
          <tr>
            <th v-for="col in currentColumns" :key="col.key">
              {{ col.label }}
            </th>
            <th class="th-actions">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(row, idx) in tableData" :key="idx">
            <td v-for="col in currentColumns" :key="col.key">
              {{ formatCellValue(row[col.key], col.type) }}
            </td>
            <td class="td-actions">
              <div class="data-detail__actions">
                <AppButton
                  text="编辑"
                  variant="warning"
                  size="small"
                  @click="editRow(row)"
                />
                <AppButton
                  text="删除"
                  variant="danger"
                  size="small"
                  @click="deleteRow(row)"
                />
              </div>
            </td>
          </tr>
        </tbody>
      </table>

      <!-- 分页控件（仅钻孔页面显示） -->
      <div
        v-if="route.params.table === 'borehole'"
        class="data-detail__pagination"
      >
        <div class="data-detail__pagination-info">
          共 {{ total }} 条记录，当前第 {{ currentPage }} / {{ totalPages }} 页
        </div>
        <div class="data-detail__pagination-controls">
          <button
            class="data-detail__pagination-btn"
            :disabled="currentPage === 1"
            @click="handlePageChange(currentPage - 1)"
          >
            上一页
          </button>
          <button
            class="data-detail__pagination-btn"
            :disabled="currentPage === totalPages"
            @click="handlePageChange(currentPage + 1)"
          >
            下一页
          </button>
          <select
            v-model="pageSize"
            @change="handlePageSizeChange(pageSize)"
            class="data-detail__pagination-select"
          >
            <option value="10">10条/页</option>
            <option value="20">20条/页</option>
            <option value="50">50条/页</option>
            <option value="100">100条/页</option>
          </select>
        </div>
      </div>
    </div>

    <!-- 表单弹窗 -->
    <div
      v-if="showForm"
      class="data-detail__form-modal"
      @click.self="closeForm"
    >
      <div class="data-detail__form-container">
        <div class="data-detail__form-header">
          <h3 class="data-detail__form-title">
            {{ isEdit ? '编辑' : '新增' }}{{ pageTitle }}
          </h3>
          <button
            class="data-detail__form-close"
            @click="closeForm"
            aria-label="关闭"
          >
            ×
          </button>
        </div>
        <div class="data-detail__form-content">
          <div
            v-for="col in currentColumns"
            :key="col.key"
            class="data-detail__form-group"
          >
            <label :for="`field-${col.key}`" class="data-detail__form-label">
              {{ col.label }}
            </label>
            <input
              :id="`field-${col.key}`"
              v-model="formData[col.key]"
              :type="col.type || 'text'"
              :disabled="isEdit && col.isPrimary"
              :placeholder="col.label"
              class="data-detail__form-input"
            />
          </div>
        </div>
        <div class="data-detail__form-footer">
          <AppButton
            text="取消"
            variant="secondary"
            size="medium"
            @click="closeForm"
          />
          <AppButton
            text="保存"
            variant="primary"
            size="medium"
            @click="saveForm"
          />
        </div>
      </div>
    </div>

    <!-- 提示消息 -->
    <div
      v-if="message"
      :class="['message', `message--${messageType}`]"
      role="alert"
      aria-live="assertive"
    >
      {{ message }}
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import AppButton from '../components/AppButton.vue'
import * as api from '../api/dataCenterApi'
import '../styles/data-detail.scss'

const router = useRouter()
const route = useRoute()

const tableData = ref([])
const loading = ref(false)
const showForm = ref(false)
const isEdit = ref(false)
const formData = reactive({})
const message = ref('')
const messageType = ref('')
const currentColumns = ref([])
const pageTitle = ref('')
const mineAreas = ref([])
const selectedAreaId = ref('')

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const totalPages = ref(1)

let currentApi = null
let currentIdKey = ''

// 表格配置
const tableConfigs = {
  'mine-area': {
    title: '矿区信息',
    api: api.mineAreaApi,
    idKey: 'areaId',
    columns: [
      { key: 'areaId', label: '矿区编码', isPrimary: true },
      { key: 'areaName', label: '矿区名称' },
      { key: 'coordinateSystem', label: '坐标系' },
      { key: 'datumHeight', label: '高程基准(m)', type: 'number' },
    ],
  },
  'coal-seam': {
    title: '煤层信息',
    api: api.coalSeamApi,
    idKey: 'seamId',
    columns: [
      { key: 'seamId', label: '煤层编号', isPrimary: true },
      { key: 'seamName', label: '煤层名称' },
      { key: 'averageThickness', label: '平均厚度(m)', type: 'number' },
      { key: 'dipAngle', label: '平均倾角(°)', type: 'number' },
      { key: 'roofLithology', label: '顶板岩性' },
      { key: 'floorLithology', label: '底板岩性' },
    ],
  },
  borehole: {
    title: '钻孔信息',
    api: api.boreholeApi,
    idKey: 'holeId',
    columns: [
      { key: 'holeId', label: '钻孔编号', isPrimary: true },
      { key: 'areaId', label: '矿区编码' },
      { key: 'x', label: 'X坐标', type: 'number' },
      { key: 'y', label: 'Y坐标', type: 'number' },
      { key: 'z', label: 'Z坐标', type: 'number' },
      { key: 'totalDepth', label: '总深度(m)', type: 'number' },
      { key: 'drillPurpose', label: '钻探目的' },
      { key: 'drillDate', label: '钻探日期', type: 'date' },
      { key: 'status', label: '状态' },
    ],
  },
  'lithology-log': {
    title: '岩性分层',
    api: api.lithologyLogApi,
    idKey: 'logId',
    columns: [
      { key: 'logId', label: '记录ID', isPrimary: true },
      { key: 'holeId', label: '钻孔编号' },
      { key: 'fromDepth', label: '起始深度(m)', type: 'number' },
      { key: 'toDepth', label: '终止深度(m)', type: 'number' },
      { key: 'rockType', label: '岩石类型' },
    ],
  },
  'seam-intercept': {
    title: '见煤记录',
    api: api.seamInterceptApi,
    idKey: 'interceptId',
    columns: [
      { key: 'interceptId', label: '记录ID', isPrimary: true },
      { key: 'holeId', label: '钻孔编号' },
      { key: 'seamId', label: '煤层编号' },
      { key: 'fromDepth', label: '起始深度(m)', type: 'number' },
      { key: 'toDepth', label: '终止深度(m)', type: 'number' },
      { key: 'thickness', label: '真厚度(m)', type: 'number' },
    ],
  },
  'roadway-point': {
    title: '巷道点位',
    api: api.roadwayPointApi,
    idKey: 'pointId',
    columns: [
      { key: 'pointId', label: '点号', isPrimary: true },
      { key: 'areaId', label: '矿区编码' },
      { key: 'x', label: 'X坐标', type: 'number' },
      { key: 'y', label: 'Y坐标', type: 'number' },
      { key: 'z', label: 'Z坐标', type: 'number' },
      { key: 'pointType', label: '点类型' },
    ],
  },
  roadway: {
    title: '巷道信息',
    api: api.roadwayApi,
    idKey: 'roadwayId',
    columns: [
      { key: 'roadwayId', label: '巷道编号', isPrimary: true },
      { key: 'areaId', label: '矿区编码' },
      { key: 'name', label: '巷道名称' },
      { key: 'startPoint', label: '起点编号' },
      { key: 'endPoint', label: '终点编号' },
      { key: 'roadwayType', label: '巷道类型' },
      { key: 'crossSection', label: '断面面积', type: 'number' },
      { key: 'status', label: '状态' },
    ],
  },
  'mining-face': {
    title: '采煤工作面',
    api: api.miningFaceApi,
    idKey: 'faceId',
    columns: [
      { key: 'faceId', label: '工作面编号', isPrimary: true },
      { key: 'areaId', label: '矿区编码' },
      { key: 'seamId', label: '煤层编号' },
      { key: 'startDate', label: '开始日期', type: 'date' },
      { key: 'endDate', label: '结束日期', type: 'date' },
      { key: 'length', label: '长度(m)', type: 'number' },
      { key: 'status', label: '状态' },
    ],
  },
  'reserve-block': {
    title: '储量块段',
    api: api.reserveBlockApi,
    idKey: 'blockId',
    columns: [
      { key: 'blockId', label: '块段编号', isPrimary: true },
      { key: 'areaId', label: '矿区编码' },
      { key: 'seamId', label: '煤层编号' },
      { key: 'blockType', label: '块段类型' },
      { key: 'geologicalReserve', label: '地质储量', type: 'number' },
      { key: 'recoverableReserve', label: '可采储量', type: 'number' },
      { key: 'recoveryRate', label: '回采率(%)', type: 'number' },
    ],
  },
  'monthly-production': {
    title: '月度产量',
    api: api.monthlyProductionApi,
    idKey: 'recordId',
    columns: [
      { key: 'recordId', label: '记录ID', isPrimary: true },
      { key: 'blockId', label: '块段编号' },
      { key: 'reportMonth', label: '报告月份', type: 'date' },
      { key: 'minedTonnage', label: '采出量', type: 'number' },
      { key: 'lossTonnage', label: '损失量', type: 'number' },
      { key: 'actualRecoveryRate', label: '实际回采率(%)', type: 'number' },
    ],
  },
  'three-quantities': {
    title: '三量台账',
    api: api.threeQuantitiesApi,
    idKey: 'recordId',
    columns: [
      { key: 'recordId', label: '记录ID', isPrimary: true },
      { key: 'areaId', label: '矿区编码' },
      { key: 'calcDate', label: '计算日期', type: 'date' },
      { key: 'developmentReserve', label: '开拓煤量', type: 'number' },
      { key: 'preparationReserve', label: '准备煤量', type: 'number' },
      { key: 'miningReserve', label: '回采煤量', type: 'number' },
    ],
  },
  'surface-station': {
    title: '观测站',
    api: api.surfaceStationApi,
    idKey: 'stationId',
    columns: [
      { key: 'stationId', label: '站点编号', isPrimary: true },
      { key: 'areaId', label: '矿区编码' },
      { key: 'x', label: 'X坐标', type: 'number' },
      { key: 'y', label: 'Y坐标', type: 'number' },
      { key: 'zInitial', label: '初始高程', type: 'number' },
    ],
  },
  'subsidence-observation': {
    title: '沉降观测',
    api: api.subsidenceObservationApi,
    idKey: 'obsId',
    columns: [
      { key: 'obsId', label: '记录ID', isPrimary: true },
      { key: 'stationId', label: '站点编号' },
      { key: 'obsDate', label: '观测日期', type: 'date' },
      { key: 'zInitial', label: '初始高程', type: 'number' },
      { key: 'zCurrent', label: '当前高程', type: 'number' },
      { key: 'subsidence', label: '沉降量', type: 'number' },
    ],
  },
}

const formatCellValue = (value, type) => {
  if (!value) return ''
  if (type === 'date' && value) {
    return new Date(value).toLocaleDateString()
  }
  if (type === 'number') {
    return typeof value === 'number' ? value.toFixed(2) : value
  }
  return value
}

const goBack = () => {
  router.back()
}

const loadMineAreas = async () => {
  try {
    const res = await api.mineAreaApi.getAll()
    if (res.data.code === 200) {
      mineAreas.value = res.data.data || []
    }
  } catch (e) {
    console.error('加载矿区数据失败:', e)
  }
}

const initPage = async () => {
  const tableName = route.params.table
  const config = tableConfigs[tableName]

  if (!config) {
    showMessage('表格配置不存在', 'error')
    goBack()
    return
  }

  pageTitle.value = config.title
  currentColumns.value = config.columns
  currentApi = config.api
  currentIdKey = config.idKey

  // 如果是钻孔页面，加载矿区数据
  if (tableName === 'borehole') {
    await loadMineAreas()
  }

  // 初始化时默认使用分页接口（钻孔页面）
  loadData()
}

const loadData = async () => {
  loading.value = true
  try {
    let res
    // 如果是钻孔页面且选择了矿区，按矿区编码过滤
    if (route.params.table === 'borehole' && selectedAreaId.value) {
      res = await currentApi.getByAreaId(
        selectedAreaId.value,
        currentPage.value,
        pageSize.value,
      )
    } else if (route.params.table === 'borehole') {
      // 钻孔页面默认分页
      res = await currentApi.getAll(currentPage.value, pageSize.value)
    } else {
      // 其他页面保持原有逻辑
      res = await currentApi.getAll()
    }
    if (res.data.code === 200) {
      tableData.value = res.data.data || []
      // 更新分页信息
      if (res.data.total !== undefined) {
        total.value = res.data.total
      }
      if (res.data.pages !== undefined) {
        totalPages.value = res.data.pages
      }
    } else {
      showMessage('加载失败', 'error')
    }
  } catch (e) {
    showMessage('加载失败: ' + e.message, 'error')
  } finally {
    loading.value = false
  }
}

const filterByAreaId = () => {
  // 重置到第一页
  currentPage.value = 1
  loadData()
}

// 分页方法
const handlePageChange = (page) => {
  currentPage.value = page
  loadData()
}

const handlePageSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadData()
}

const showAddForm = () => {
  isEdit.value = false
  Object.keys(formData).forEach((k) => delete formData[k])
  showForm.value = true
}

const editRow = (row) => {
  isEdit.value = true
  Object.keys(formData).forEach((k) => delete formData[k])
  Object.assign(formData, row)
  showForm.value = true
}

const closeForm = () => {
  showForm.value = false
}

const saveForm = async () => {
  try {
    const res = isEdit.value
      ? await currentApi.update(formData)
      : await currentApi.add(formData)
    if (res.data.code === 200) {
      showMessage(isEdit.value ? '更新成功' : '添加成功', 'success')
      closeForm()
      await loadData()
    } else {
      showMessage(res.data.message || '操作失败', 'error')
    }
  } catch (e) {
    showMessage('操作失败: ' + e.message, 'error')
  }
}

const deleteRow = async (row) => {
  if (!confirm('确定要删除吗？')) return
  try {
    const id = row[currentIdKey]
    const res = await currentApi.delete(id)
    if (res.data.code === 200) {
      showMessage('删除成功', 'success')
      await loadData()
    } else {
      showMessage(res.data.message || '删除失败', 'error')
    }
  } catch (e) {
    showMessage('删除失败: ' + e.message, 'error')
  }
}

const showMessage = (msg, type) => {
  message.value = msg
  messageType.value = type
  setTimeout(() => {
    message.value = ''
  }, 3000)
}

onMounted(() => {
  initPage()
})
</script>

<!-- 样式已移至 src/styles/data-detail.scss -->
<!-- 采用 BEM 命名规范，支持响应式、无障碍访问（a11y）和深色模式 -->
