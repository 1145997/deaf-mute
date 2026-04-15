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
  totalPosts: 0,
  pendingPosts: 0,
  totalComments: 0,
  totalCategories: 0,
  totalNotices: 0
})

const trendList = ref<TrendItem[]>([])

const lineChartRef = ref<HTMLDivElement | null>(null)
const pieChartRef = ref<HTMLDivElement | null>(null)

let lineChart: echarts.ECharts | null = null
let pieChart: echarts.ECharts | null = null

const contentComposition = computed(() => [
  { name: "帖子", value: overview.totalPosts },
  { name: "评论", value: overview.totalComments },
  { name: "分类", value: overview.totalCategories },
  { name: "公告", value: overview.totalNotices }
])

const cardList = computed(() => [
  { label: "用户总数", value: overview.totalUsers },
  { label: "帖子总数", value: overview.totalPosts },
  { label: "待审核帖子", value: overview.pendingPosts },
  { label: "评论总数", value: overview.totalComments },
  { label: "启用分类", value: overview.totalCategories },
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
        name: "新增帖子数",
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
        name: "内容构成",
        type: "pie",
        radius: ["45%", "70%"],
        center: ["50%", "45%"],
        avoidLabelOverlap: true,
        label: {
          formatter: "{b}: {c}"
        },
        data: contentComposition.value
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
      <div class="page-desc">智能手语翻译工具一期内容平台运营概览</div>
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
            <div class="block-title">近 7 天帖子新增趋势</div>
          </template>
          <div ref="lineChartRef" class="chart-box" />
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="8">
        <el-card shadow="never">
          <template #header>
            <div class="block-title">平台内容构成</div>
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
