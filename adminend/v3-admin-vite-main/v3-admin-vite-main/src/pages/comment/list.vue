<script setup lang="ts">
import {
  deleteComment,
  getCommentList,
  hideComment,
  showComment,
  type CommentItem
} from "@@/apis/comment"

defineOptions({
  name: "CommentList"
})

const loading = ref(false)
const actionLoadingId = ref<number | null>(null)

const tableData = ref<CommentItem[]>([])
const total = ref(0)

const queryForm = reactive({
  pageNum: 1,
  pageSize: 10,
  infoId: undefined as number | undefined,
  status: undefined as number | undefined,
  keyword: ""
})

function getStatusText(status: number) {
  return status === 1 ? "正常" : "已屏蔽"
}

function getStatusTagType(status: number) {
  return status === 1 ? "success" : "info"
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getCommentList({
      pageNum: queryForm.pageNum,
      pageSize: queryForm.pageSize,
      infoId: queryForm.infoId,
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
  queryForm.infoId = undefined
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

async function handleHide(row: CommentItem) {
  await ElMessageBox.confirm(
    "确认屏蔽这条评论吗？",
    "屏蔽确认",
    {
      type: "warning",
      confirmButtonText: "确认屏蔽",
      cancelButtonText: "取消"
    }
  )

  actionLoadingId.value = row.id
  try {
    await hideComment(row.id)
    ElMessage.success("屏蔽评论成功")
    await fetchList()
  } finally {
    actionLoadingId.value = null
  }
}

async function handleShow(row: CommentItem) {
  await ElMessageBox.confirm(
    "确认取消屏蔽这条评论吗？",
    "取消屏蔽确认",
    {
      type: "warning",
      confirmButtonText: "确认恢复",
      cancelButtonText: "取消"
    }
  )

  actionLoadingId.value = row.id
  try {
    await showComment(row.id)
    ElMessage.success("取消屏蔽成功")
    await fetchList()
  } finally {
    actionLoadingId.value = null
  }
}

async function handleDelete(row: CommentItem) {
  await ElMessageBox.confirm(
    "确认删除这条评论吗？删除后不可恢复。",
    "删除确认",
    {
      type: "warning",
      confirmButtonText: "确认删除",
      cancelButtonText: "取消"
    }
  )

  actionLoadingId.value = row.id
  try {
    await deleteComment(row.id)
    ElMessage.success("删除评论成功")

    if (tableData.value.length === 1 && queryForm.pageNum > 1) {
      queryForm.pageNum -= 1
    }

    await fetchList()
  } finally {
    actionLoadingId.value = null
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
        <el-form-item label="信息 ID">
          <el-input-number
            v-model="queryForm.infoId"
            :min="1"
            controls-position="right"
            placeholder="请输入信息ID"
            style="width: 180px"
          />
        </el-form-item>

        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable style="width: 160px">
            <el-option label="正常" :value="1" />
            <el-option label="已屏蔽" :value="0" />
          </el-select>
        </el-form-item>

        <el-form-item label="关键词">
          <el-input
            v-model.trim="queryForm.keyword"
            placeholder="请输入评论内容关键词"
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
          <span>评论管理</span>
          <span class="table-total">共 {{ total }} 条</span>
        </div>
      </template>

      <el-table v-loading="loading" :data="tableData" border>
        <el-table-column prop="id" label="评论 ID" width="100" align="center" />
        <el-table-column prop="infoId" label="信息 ID" width="100" align="center" />
        <el-table-column prop="userId" label="用户 ID" width="100" align="center" />
        <el-table-column label="父评论 ID" width="110" align="center">
          <template #default="{ row }">
            {{ row.parentId ?? "-" }}
          </template>
        </el-table-column>
        <el-table-column prop="content" label="评论内容" min-width="320" show-overflow-tooltip />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="180" />
        <el-table-column label="操作" fixed="right" width="220" align="center">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 1"
              type="warning"
              link
              :loading="actionLoadingId === row.id"
              @click="handleHide(row)"
            >
              屏蔽
            </el-button>

            <el-button
              v-else
              type="success"
              link
              :loading="actionLoadingId === row.id"
              @click="handleShow(row)"
            >
              取消屏蔽
            </el-button>

            <el-button
              type="danger"
              link
              :loading="actionLoadingId === row.id"
              @click="handleDelete(row)"
            >
              删除
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
</style>
