<script setup lang="ts">
import {
  getLostFoundDetail,
  getLostFoundList,
  type LostFoundDetail,
  type LostFoundItem
} from "@@/apis/lostfound"

defineOptions({
  name: "LostFoundList"
})

const loading = ref(false)
const detailLoading = ref(false)

const tableData = ref<LostFoundItem[]>([])
const total = ref(0)

const queryForm = reactive({
  pageNum: 1,
  pageSize: 10,
  type: undefined as number | undefined,
  status: undefined as number | undefined,
  keyword: ""
})

const detailVisible = ref(false)
const detailData = ref<LostFoundDetail | null>(null)

function getTypeText(type: number) {
  const map: Record<number, string> = {
    1: "寻物启事",
    2: "招领启事"
  }
  return map[type] || "未知"
}

function getStatusText(status: number) {
  const map: Record<number, string> = {
    0: "待审核",
    1: "已发布",
    2: "已完结",
    3: "已驳回"
  }
  return map[status] || "未知"
}

function getStatusTagType(status: number) {
  const map: Record<number, "" | "success" | "warning" | "info" | "danger"> = {
    0: "warning",
    1: "success",
    2: "info"   ,
    3: "danger"
  }
  return map[status] || "info"
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getLostFoundList({
      pageNum: queryForm.pageNum,
      pageSize: queryForm.pageSize,
      type: queryForm.type,
      status: queryForm.status,
      keyword: queryForm.keyword || undefined
    })
    tableData.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

async function handleSearch() {
  queryForm.pageNum = 1
  await fetchList()
}

async function handleReset() {
  queryForm.pageNum = 1
  queryForm.pageSize = 10
  queryForm.type = undefined
  queryForm.status = undefined
  queryForm.keyword = ""
  await fetchList()
}

async function handleSizeChange(size: number) {
  queryForm.pageSize = size
  queryForm.pageNum = 1
  await fetchList()
}

async function handleCurrentChange(page: number) {
  queryForm.pageNum = page
  await fetchList()
}

async function handleView(row: LostFoundItem) {
  detailVisible.value = true
  detailLoading.value = true
  try {
    const res = await getLostFoundDetail(row.id)
    detailData.value = res.data
  } finally {
    detailLoading.value = false
  }
}

onMounted(() => {
  fetchList()
})
</script>

<template>
  <div class="app-container">
    <el-card shadow="never" class="search-card">
      <el-form :inline="true">
        <el-form-item label="类型">
          <el-select v-model="queryForm.type" placeholder="请选择类型" clearable style="width: 160px">
            <el-option label="寻物启事" :value="1" />
            <el-option label="招领启事" :value="2" />
          </el-select>
        </el-form-item>

        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable style="width: 160px">
            <el-option label="待审核" :value="0" />
            <el-option label="已发布" :value="1" />
            <el-option label="已完结" :value="2" />
            <el-option label="已驳回" :value="3" />
          </el-select>
        </el-form-item>

        <el-form-item label="关键词">
          <el-input
            v-model.trim="queryForm.keyword"
            placeholder="请输入标题或物品名称"
            clearable
            style="width: 240px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            查询
          </el-button>
          <el-button @click="handleReset">
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <template #header>
        <div class="table-header">
          <span>信息管理</span>
          <span class="table-total">共 {{ total }} 条</span>
        </div>
      </template>

      <el-table v-loading="loading" :data="tableData" border>
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column label="类型" width="110" align="center">
          <template #default="{ row }">
            {{ getTypeText(row.type) }}
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
        <el-table-column prop="itemName" label="物品名称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="eventPlace" label="地点" min-width="140" show-overflow-tooltip />
        <el-table-column prop="contactName" label="联系人" width="110" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="浏览量" width="90" align="center" />
        <el-table-column prop="createTime" label="创建时间" min-width="170" />
        <el-table-column label="操作" fixed="right" width="120" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager-wrapper">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :current-page="queryForm.pageNum"
          :page-size="queryForm.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <el-drawer v-model="detailVisible" title="信息详情" size="620px">
      <div v-loading="detailLoading">
        <template v-if="detailData">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="ID">
              {{ detailData.id }}
            </el-descriptions-item>
            <el-descriptions-item label="用户ID">
              {{ detailData.userId }}
            </el-descriptions-item>
            <el-descriptions-item label="类型">
              {{ getTypeText(detailData.type) }}
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="getStatusTagType(detailData.status)">
                {{ getStatusText(detailData.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="标题" :span="2">
              {{ detailData.title }}
            </el-descriptions-item>
            <el-descriptions-item label="物品名称">
              {{ detailData.itemName || "-" }}
            </el-descriptions-item>
            <el-descriptions-item label="分类ID">
              {{ detailData.categoryId }}
            </el-descriptions-item>
            <el-descriptions-item label="品牌">
              {{ detailData.brand || "-" }}
            </el-descriptions-item>
            <el-descriptions-item label="颜色">
              {{ detailData.color || "-" }}
            </el-descriptions-item>
            <el-descriptions-item label="事件地点">
              {{ detailData.eventPlace || "-" }}
            </el-descriptions-item>
            <el-descriptions-item label="事件时间">
              {{ detailData.eventTime || "-" }}
            </el-descriptions-item>
            <el-descriptions-item label="联系人">
              {{ detailData.contactName || "-" }}
            </el-descriptions-item>
            <el-descriptions-item label="联系电话">
              {{ detailData.contactPhone || "-" }}
            </el-descriptions-item>
            <el-descriptions-item label="微信">
              {{ detailData.contactWechat || "-" }}
            </el-descriptions-item>
            <el-descriptions-item label="浏览量">
              {{ detailData.viewCount }}
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">
              {{ detailData.createTime || "-" }}
            </el-descriptions-item>
            <el-descriptions-item label="审核时间">
              {{ detailData.auditTime || "-" }}
            </el-descriptions-item>
            <el-descriptions-item label="驳回原因" :span="2">
              {{ detailData.auditReason || "-" }}
            </el-descriptions-item>
            <el-descriptions-item label="描述" :span="2">
              {{ detailData.description || "-" }}
            </el-descriptions-item>
            <el-descriptions-item label="图片" :span="2">
              <el-image
                v-if="detailData.image"
                :src="detailData.image"
                style="width: 180px; height: 180px; border-radius: 8px"
                fit="cover"
                :preview-src-list="[detailData.image]"
                preview-teleported
              />
              <span v-else>无图片</span>
            </el-descriptions-item>
          </el-descriptions>

          <div class="drawer-footer">
            <el-button @click="detailVisible = false">
              关闭
            </el-button>
          </div>
        </template>
      </div>
    </el-drawer>
  </div>
</template>

<style scoped lang="scss">
.app-container {
  padding: 16px;
}

.search-card {
  margin-bottom: 16px;
}

.table-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.table-total {
  font-size: 14px;
  color: var(--el-text-color-secondary);
}

.pager-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.drawer-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 24px;
}
</style>
