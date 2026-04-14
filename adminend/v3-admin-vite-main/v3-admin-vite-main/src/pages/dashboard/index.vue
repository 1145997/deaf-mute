<script setup lang="ts">
import * as echarts from "echarts"
import {
  getOverview,
  getRecent7DaysTrend,
  type OverviewData,
  type TrendItem
} from "@@/apis/dashboard"

defineOptions({
  name: "Dashboard"
})

const loading = ref(false)

const overview = reactive<OverviewData>({
  totalUsers: 0,
  totalInfos: 0,
  pendingInfos: 0,
  publishedInfos: 0,
  finishedInfos: 0,
  rejectedInfos: 0,
  totalNotices: 0
})

const trendList = ref<TrendItem[]>([])

const lineChartRef = ref<HTMLDivElement | null>(null)
const pieChartRef = ref<HTMLDivElement | null>(null)

let lineChart: echarts.ECharts | null = null
let pieChart: echarts.ECharts | null = null

const cardList = computed(() => [
  { label: "用户总数", value: overview.totalUsers },
  { label: "信息总数", value: overview.totalInfos },
  { label: "待审核", value: overview.pendingInfos },
  { label: "已发布", value: overview.publishedInfos },
  { label: "已完结", value: overview.finishedInfos },
  { label: "已驳回", value: overview.rejectedInfos },
  { label: "公告总数", value: overview.totalNotices }
])

function renderLineChart() {
  if (!lineChartRef.value) return

  if (!lineChart) {
    lineChart = echarts.init(lineChartRef.value)
  }

  lineChart.setOption({
    tooltip: {
      trigger: "axis"
    },
    grid: {
      left: 40,
      right: 20,
      top: 30,
      bottom: 40
    },
    xAxis: {
      type: "category",
      data: trendList.value.map(item => item.date),
      boundaryGap: false
    },
    yAxis: {
      type: "value",
      minInterval: 1
    },
    series: [
      {
        name: "发布数量",
        type: "line",
        smooth: true,
        data: trendList.value.map(item => item.count),
        areaStyle: {}
      }
    ]
  })
}

function renderPieChart() {
  if (!pieChartRef.value) return

  if (!pieChart) {
    pieChart = echarts.init(pieChartRef.value)
  }

  pieChart.setOption({
    tooltip: {
      trigger: "item"
    },
    legend: {
      bottom: 0
    },
    series: [
      {
        name: "信息状态分布",
        type: "pie",
        radius: ["45%", "70%"],
        center: ["50%", "45%"],
        avoidLabelOverlap: true,
        label: {
          formatter: "{b}: {c}"
        },
        data: [
          { name: "待审核", value: overview.pendingInfos },
          { name: "已发布", value: overview.publishedInfos },
          { name: "已完结", value: overview.finishedInfos },
          { name: "已驳回", value: overview.rejectedInfos }
        ]
      }
    ]
  })
}

function renderCharts() {
  renderLineChart()
  renderPieChart()
}

async function fetchData() {
  loading.value = true
  try {
    const [overviewRes, trendRes] = await Promise.all([
      getOverview(),
      getRecent7DaysTrend()
    ])

    Object.assign(overview, overviewRes.data)
    trendList.value = trendRes.data.list || []

    await nextTick()
    renderCharts()
  } finally {
    loading.value = false
  }
}

function handleResize() {
  lineChart?.resize()
  pieChart?.resize()
}

onMounted(async () => {
  await fetchData()
  window.addEventListener("resize", handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener("resize", handleResize)
  lineChart?.dispose()
  pieChart?.dispose()
  lineChart = null
  pieChart = null
})
</script>

<template>
  <div class="app-container" v-loading="loading">
    <div class="page-header">
      <div class="page-title">仪表盘</div>
      <div class="page-desc">校园失物招领后台工作台首页</div>
    </div>

    <div class="card-grid">
      <el-card
        v-for="item in cardList"
        :key="item.label"
        shadow="hover"
        class="stat-card"
      >
        <div class="stat-card__label">{{ item.label }}</div>
        <div class="stat-card__value">{{ item.value }}</div>
      </el-card>
    </div>

    <el-row :gutter="16" class="content-row">
      <el-col :xs="24" :lg="16">
        <el-card shadow="never">
          <template #header>
            <div class="block-title">近 7 天发布趋势</div>
          </template>
          <div ref="lineChartRef" class="chart-box" />
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="8">
        <el-card shadow="never">
          <template #header>
            <div class="block-title">信息状态分布</div>
          </template>
          <div ref="pieChartRef" class="chart-box" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped lang="scss">
.app-container {
  padding: 16px;
}

.page-header {
  margin-bottom: 16px;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.page-desc {
  margin-top: 6px;
  font-size: 14px;
  color: var(--el-text-color-secondary);
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.stat-card {
  border-radius: 12px;

  &__label {
    font-size: 14px;
    color: var(--el-text-color-secondary);
  }

  &__value {
    margin-top: 12px;
    font-size: 30px;
    font-weight: 700;
    color: var(--el-text-color-primary);
    line-height: 1;
  }
}

.content-row {
  margin-top: 0;
}

.block-title {
  font-size: 16px;
  font-weight: 600;
}

.chart-box {
  width: 100%;
  height: 360px;
}
</style>
