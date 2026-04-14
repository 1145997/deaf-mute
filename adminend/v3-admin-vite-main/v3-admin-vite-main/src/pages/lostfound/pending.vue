<script setup lang="ts">
import type { FormInstance, FormRules } from "element-plus"
import {
  approveLostFound,
  getLostFoundDetail,
  getPendingList,
  rejectLostFound,
  type LostFoundDetail,
  type LostFoundItem
} from "@@/apis/lostfound"

defineOptions({
  name: "LostFoundPending"
})

const loading = ref(false)
const detailLoading = ref(false)
const submitLoading = ref(false)

const tableData = ref<LostFoundItem[]>([])
const total = ref(0)

const queryForm = reactive({
  pageNum: 1,
  pageSize: 10
})

const detailVisible = ref(false)
const rejectVisible = ref(false)

const currentId = ref<number | null>(null)
const detailData = ref<LostFoundDetail | null>(null)

const rejectFormRef = ref<FormInstance>()
const rejectForm = reactive({
  auditReason: ""
})

const rejectRules: FormRules = {
  auditReason: [
    { required: true, message: "请输入驳回原因", trigger: "blur" },
    { min: 2, max: 100, message: "长度在 2 到 100 个字符", trigger: "blur" }
  ]
}

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
    const res = await getPendingList({ ...queryForm })
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
  currentId.value = row.id
  detailVisible.value = true
  detailLoading.value = true
  try {
    const res = await getLostFoundDetail(row.id)
    detailData.value = res.data
  } finally {
    detailLoading.value = false
  }
}

async function handleApprove(row: LostFoundItem) {
  await ElMessageBox.confirm(
    `确认通过「${row.title}」的审核吗？`,
    "审核确认",
    {
      type: "warning",
      confirmButtonText: "确认通过",
      cancelButtonText: "取消"
    }
  )

  submitLoading.value = true
  try {
    await approveLostFound(row.id)
    ElMessage.success("审核通过成功")
    if (detailVisible.value && currentId.value === row.id) {
      detailVisible.value = false
    }
    await fetchList()
  } finally {
    submitLoading.value = false
  }
}

function openRejectDialog(row: LostFoundItem) {
  currentId.value = row.id
  rejectForm.auditReason = ""
  rejectVisible.value = true
  nextTick(() => {
    rejectFormRef.value?.clearValidate()
  })
}

async function submitReject() {
  if (!rejectFormRef.value || currentId.value === null) return

  await rejectFormRef.value.validate()

  submitLoading.value = true
  try {
    await rejectLostFound(currentId.value, rejectForm.auditReason)
    ElMessage.success("驳回成功")
    rejectVisible.value = false
    if (detailVisible.value) {
      detailVisible.value = false
    }
    await fetchList()
  } finally {
    submitLoading.value = false
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
        <el-form-item label="关键词">
          <el-input
            placeholder="当前页仅做待审核分页，可后续补关键词查询"
            disabled
            style="width: 320px"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            刷新
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
          <span>待审核信息</span>
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
        <el-table-column label="图片" width="100" align="center">
          <template #default="{ row }">
            <el-image
              v-if="row.image"
              :src="row.image"
              style="width: 56px; height: 56px; border-radius: 6px"
              fit="cover"
              :preview-src-list="[row.image]"
              preview-teleported
            />
            <span v-else>无</span>
          </template>
        </el-table-column>
        <el-table-column prop="eventPlace" label="地点" min-width="140" show-overflow-tooltip />
        <el-table-column prop="eventTime" label="事件时间" min-width="170" />
        <el-table-column prop="contactName" label="联系人" width="110" align="center" />
        <el-table-column prop="contactPhone" label="联系电话" min-width="140" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="170" />
        <el-table-column label="操作" fixed="right" width="260" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">
              查看详情
            </el-button>
            <el-button type="success" link :loading="submitLoading" @click="handleApprove(row)">
              审核通过
            </el-button>
            <el-button type="danger" link @click="openRejectDialog(row)">
              驳回
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
            <el-button type="danger" @click="openRejectDialog(detailData)">
              驳回
            </el-button>
            <el-button type="success" :loading="submitLoading" @click="handleApprove(detailData)">
              审核通过
            </el-button>
          </div>
        </template>
      </div>
    </el-drawer>

    <el-dialog v-model="rejectVisible" title="驳回原因" width="500px">
      <el-form ref="rejectFormRef" :model="rejectForm" :rules="rejectRules" label-width="80px">
        <el-form-item label="驳回原因" prop="auditReason">
          <el-input
            v-model="rejectForm.auditReason"
            type="textarea"
            :rows="4"
            maxlength="100"
            show-word-limit
            placeholder="请输入驳回原因"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="rejectVisible = false">
          取消
        </el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitReject">
          确认驳回
        </el-button>
      </template>
    </el-dialog>
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
  gap: 12px;
  margin-top: 24px;
}
</style>
