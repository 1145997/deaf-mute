<script setup lang="ts">
import type { FormInstance, FormRules } from "element-plus"
import { getEnabledCategoryList, type CategoryOption } from "@@/apis/category"
import {
  approvePost,
  getPendingPostList,
  getPostDetail,
  rejectPost,
  type PostDetail,
  type PostItem,
  type PostStatus
} from "@@/apis/post"

defineOptions({
  name: "PostPending"
})

const loading = ref(false)
const detailLoading = ref(false)
const submitLoading = ref(false)
const actionLoadingId = ref<number | null>(null)

const tableData = ref<PostItem[]>([])
const total = ref(0)
const categoryOptions = ref<CategoryOption[]>([])

const queryForm = reactive({
  pageNum: 1,
  pageSize: 10,
  categoryId: undefined as number | undefined,
  keyword: ""
})

const detailVisible = ref(false)
const rejectVisible = ref(false)

const currentId = ref<number | null>(null)
const detailData = ref<PostDetail | null>(null)

const rejectFormRef = ref<FormInstance>()
const rejectForm = reactive({
  auditReason: ""
})

const detailPreviewImages = computed(() => {
  if (!detailData.value) return []
  const images: string[] = []

  if (detailData.value.coverImage) {
    images.push(detailData.value.coverImage)
  }

  for (const image of detailData.value.imageList || []) {
    if (image && !images.includes(image)) {
      images.push(image)
    }
  }

  return images
})

const rejectRules: FormRules = {
  auditReason: [
    { required: true, message: "请输入驳回原因", trigger: "blur" },
    { min: 2, max: 100, message: "长度在 2 到 100 个字符", trigger: "blur" }
  ]
}

function getStatusText(status: PostStatus) {
  const map: Record<PostStatus, string> = {
    0: "待审核",
    1: "已发布",
    2: "已驳回",
    3: "已下架"
  }
  return map[status]
}

function getStatusTagType(status: PostStatus) {
  const map: Record<PostStatus, "success" | "warning" | "info" | "danger"> = {
    0: "warning",
    1: "success",
    2: "danger",
    3: "info"
  }
  return map[status]
}

function getSourceTypeText(sourceType?: string | null) {
  if (!sourceType) return "-"
  if (sourceType === "MANUAL") return "手动发布"
  return sourceType
}

async function fetchCategories() {
  const res = await getEnabledCategoryList("POST")
  categoryOptions.value = res.data
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getPendingPostList({
      pageNum: queryForm.pageNum,
      pageSize: queryForm.pageSize,
      categoryId: queryForm.categoryId,
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
  queryForm.categoryId = undefined
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

async function handleView(row: PostItem) {
  currentId.value = row.id
  detailVisible.value = true
  detailLoading.value = true
  try {
    const res = await getPostDetail(row.id)
    detailData.value = res.data
  } finally {
    detailLoading.value = false
  }
}

async function handleApprove(row: PostItem) {
  await ElMessageBox.confirm(
    `确认通过帖子「${row.title}」的审核吗？`,
    "审核确认",
    {
      type: "warning",
      confirmButtonText: "确认通过",
      cancelButtonText: "取消"
    }
  )

  actionLoadingId.value = row.id
  try {
    await approvePost(row.id)
    ElMessage.success("审核通过成功")

    if (detailVisible.value && currentId.value === row.id) {
      detailVisible.value = false
    }

    await fetchList()
  } finally {
    actionLoadingId.value = null
  }
}

function openRejectDialog(row: PostItem) {
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
    await rejectPost(currentId.value, rejectForm.auditReason)
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

onMounted(async () => {
  await Promise.allSettled([
    fetchCategories(),
    fetchList()
  ])
})
</script>

<template>
  <div class="app-container">
    <el-card shadow="never" class="search-card">
      <el-form :inline="true">
        <el-form-item label="分类">
          <el-select
            v-model="queryForm.categoryId"
            placeholder="请选择分类"
            clearable
            style="width: 180px"
          >
            <el-option
              v-for="item in categoryOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="关键词">
          <el-input
            v-model.trim="queryForm.keyword"
            placeholder="请输入标题或内容关键词"
            clearable
            style="width: 260px"
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
          <span>帖子审核</span>
          <span class="table-total">共 {{ total }} 条</span>
        </div>
      </template>

      <el-table v-loading="loading" :data="tableData" border>
        <el-table-column prop="id" label="帖子 ID" width="100" align="center" />
        <el-table-column label="封面" width="100" align="center">
          <template #default="{ row }">
            <el-image
              v-if="row.coverImage"
              :src="row.coverImage"
              style="width: 56px; height: 56px; border-radius: 6px"
              fit="cover"
              :preview-src-list="[row.coverImage]"
              preview-teleported
            />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
        <el-table-column prop="contentPreview" label="内容摘要" min-width="260" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="分类" width="140" show-overflow-tooltip />
        <el-table-column label="发布者" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.userNickname || `用户 #${row.userId}` }}
          </template>
        </el-table-column>
        <el-table-column label="来源" width="120" align="center">
          <template #default="{ row }">
            {{ getSourceTypeText(row.sourceType) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" min-width="180" />
        <el-table-column label="操作" fixed="right" width="220" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">
              查看详情
            </el-button>
            <el-button
              type="success"
              link
              :loading="actionLoadingId === row.id"
              @click="handleApprove(row)"
            >
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

    <el-drawer v-model="detailVisible" title="审核详情" size="720px">
      <div v-loading="detailLoading">
        <template v-if="detailData">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="帖子 ID">
              {{ detailData.id }}
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="getStatusTagType(detailData.status)">
                {{ getStatusText(detailData.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="发布者">
              {{ detailData.userNickname || `用户 #${detailData.userId}` }}
            </el-descriptions-item>
            <el-descriptions-item label="分类">
              {{ detailData.categoryName || `分类 #${detailData.categoryId}` }}
            </el-descriptions-item>
            <el-descriptions-item label="来源">
              {{ getSourceTypeText(detailData.sourceType) }}
            </el-descriptions-item>
            <el-descriptions-item label="来源记录">
              {{ detailData.sourceRecordId ?? "-" }}
            </el-descriptions-item>
            <el-descriptions-item label="浏览量">
              {{ detailData.viewCount }}
            </el-descriptions-item>
            <el-descriptions-item label="评论数">
              {{ detailData.commentCount }}
            </el-descriptions-item>
            <el-descriptions-item label="点赞数">
              {{ detailData.likeCount }}
            </el-descriptions-item>
            <el-descriptions-item label="提交时间">
              {{ detailData.createTime || "-" }}
            </el-descriptions-item>
            <el-descriptions-item label="标题" :span="2">
              {{ detailData.title }}
            </el-descriptions-item>
            <el-descriptions-item label="正文内容" :span="2">
              <div class="content-block">
                {{ detailData.content || "-" }}
              </div>
            </el-descriptions-item>
            <el-descriptions-item label="封面图" :span="2">
              <el-image
                v-if="detailData.coverImage"
                :src="detailData.coverImage"
                class="cover-image"
                fit="cover"
                :preview-src-list="detailPreviewImages"
                preview-teleported
              />
              <span v-else>-</span>
            </el-descriptions-item>
            <el-descriptions-item label="帖子图片" :span="2">
              <div v-if="detailPreviewImages.length" class="image-grid">
                <el-image
                  v-for="image in detailPreviewImages"
                  :key="image"
                  :src="image"
                  class="detail-image"
                  fit="cover"
                  :preview-src-list="detailPreviewImages"
                  preview-teleported
                />
              </div>
              <span v-else>暂无图片</span>
            </el-descriptions-item>
            <el-descriptions-item label="历史驳回原因" :span="2">
              {{ detailData.auditReason || "-" }}
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

    <el-dialog v-model="rejectVisible" title="驳回帖子" width="500px">
      <el-form ref="rejectFormRef" :model="rejectForm" :rules="rejectRules" label-width="90px">
        <el-form-item label="驳回原因" prop="auditReason">
          <el-input
            v-model.trim="rejectForm.auditReason"
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

.content-block {
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
}

.cover-image {
  width: 180px;
  height: 180px;
  border-radius: 8px;
}

.image-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 12px;
}

.detail-image {
  width: 100%;
  height: 120px;
  border-radius: 8px;
}

.drawer-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
