<script setup lang="ts">
import type { FormInstance, FormRules } from "element-plus"
import {
  createNotice,
  deleteNotice,
  getNoticeList,
  updateNotice,
  type NoticeFormData,
  type NoticeItem
} from "@@/apis/notice"

defineOptions({
  name: "NoticeList"
})

const loading = ref(false)
const submitLoading = ref(false)

const tableData = ref<NoticeItem[]>([])
const total = ref(0)

const queryForm = reactive({
  pageNum: 1,
  pageSize: 10
})

const dialogVisible = ref(false)
const dialogType = ref<"create" | "edit">("create")
const currentId = ref<number | null>(null)

const formRef = ref<FormInstance>()
const formData = reactive<NoticeFormData>({
  title: "",
  content: "",
  isTop: 0,
  status: 1
})

const formRules: FormRules = {
  title: [
    { required: true, message: "请输入公告标题", trigger: "blur" },
    { min: 2, max: 50, message: "长度在 2 到 50 个字符", trigger: "blur" }
  ],
  content: [
    { required: true, message: "请输入公告内容", trigger: "blur" },
    { min: 5, max: 1000, message: "长度在 5 到 1000 个字符", trigger: "blur" }
  ],
  isTop: [
    { required: true, message: "请选择是否置顶", trigger: "change" }
  ],
  status: [
    { required: true, message: "请选择状态", trigger: "change" }
  ]
}

function getTopText(isTop: number) {
  return isTop === 1 ? "置顶" : "普通"
}

function getTopTagType(isTop: number) {
  return isTop === 1 ? "danger" : "info"
}

function getStatusText(status: number) {
  return status === 1 ? "发布" : "禁用"
}

function getStatusTagType(status: number) {
  return status === 1 ? "success" : "info"
}

function resetFormData() {
  formData.title = ""
  formData.content = ""
  formData.isTop = 0
  formData.status = 1
  currentId.value = null
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getNoticeList({
      pageNum: queryForm.pageNum,
      pageSize: queryForm.pageSize
    })
    tableData.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function handleCreate() {
  dialogType.value = "create"
  dialogVisible.value = true
  resetFormData()
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

function handleEdit(row: NoticeItem) {
  dialogType.value = "edit"
  dialogVisible.value = true
  currentId.value = row.id
  formData.title = row.title
  formData.content = row.content
  formData.isTop = row.isTop
  formData.status = row.status
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate()

  submitLoading.value = true
  try {
    if (dialogType.value === "create") {
      await createNotice(formData)
      ElMessage.success("新增公告成功")
    } else if (currentId.value !== null) {
      await updateNotice(currentId.value, formData)
      ElMessage.success("修改公告成功")
    }

    dialogVisible.value = false
    await fetchList()
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row: NoticeItem) {
  await ElMessageBox.confirm(
    `确认删除公告「${row.title}」吗？`,
    "删除确认",
    {
      type: "warning",
      confirmButtonText: "确认删除",
      cancelButtonText: "取消"
    }
  )

  await deleteNotice(row.id)
  ElMessage.success("删除公告成功")

  if (tableData.value.length === 1 && queryForm.pageNum > 1) {
    queryForm.pageNum -= 1
  }

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

onMounted(() => {
  fetchList()
})
</script>

<template>
  <div class="app-container">
    <el-card shadow="never" class="search-card">
      <el-form :inline="true">
        <el-form-item>
          <el-button type="success" @click="handleCreate">
            新增公告
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <template #header>
        <div class="table-header">
          <span>公告管理</span>
          <span class="table-total">共 {{ total }} 条</span>
        </div>
      </template>

      <el-table v-loading="loading" :data="tableData" border>
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
        <el-table-column label="内容摘要" min-width="320" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.content }}
          </template>
        </el-table-column>
        <el-table-column label="是否置顶" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getTopTagType(row.isTop)">
              {{ getTopText(row.isTop) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publishAdminId" label="发布管理员ID" width="120" align="center" />
        <el-table-column prop="createTime" label="创建时间" min-width="180" />
        <el-table-column prop="updateTime" label="更新时间" min-width="180" />
        <el-table-column label="操作" fixed="right" width="160" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">
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

    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'create' ? '新增公告' : '编辑公告'"
      width="720px"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px">
        <el-form-item label="公告标题" prop="title">
          <el-input
            v-model.trim="formData.title"
            placeholder="请输入公告标题"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="公告内容" prop="content">
          <el-input
            v-model.trim="formData.content"
            type="textarea"
            :rows="6"
            placeholder="请输入公告内容"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="是否置顶" prop="isTop">
          <el-radio-group v-model="formData.isTop">
            <el-radio :value="1">置顶</el-radio>
            <el-radio :value="0">普通</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">发布</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">
          取消
        </el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          确定
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
</style>
