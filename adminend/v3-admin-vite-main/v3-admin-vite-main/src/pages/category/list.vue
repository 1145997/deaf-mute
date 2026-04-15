<script setup lang="ts">
import type { FormInstance, FormRules } from "element-plus"
import {
  createCategory,
  deleteCategory,
  getCategoryList,
  updateCategory,
  type CategoryType,
  type CategoryFormData,
  type CategoryItem
} from "@@/apis/category"

defineOptions({
  name: "CategoryList"
})

const loading = ref(false)
const submitLoading = ref(false)

const tableData = ref<CategoryItem[]>([])
const total = ref(0)

const queryForm = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: "",
  type: undefined as CategoryType | undefined,
  status: undefined as number | undefined
})

const dialogVisible = ref(false)
const dialogType = ref<"create" | "edit">("create")
const currentId = ref<number | null>(null)

const formRef = ref<FormInstance>()
const formData = reactive<CategoryFormData>({
  name: "",
  type: "POST",
  icon: "",
  description: "",
  sort: 1,
  status: 1
})

const formRules: FormRules = {
  name: [
    { required: true, message: "请输入分类名称", trigger: "blur" },
    { min: 1, max: 20, message: "长度在 1 到 20 个字符", trigger: "blur" }
  ],
  type: [
    { required: true, message: "请选择分类类型", trigger: "change" }
  ],
  sort: [
    { required: true, message: "请输入排序号", trigger: "blur" }
  ],
  status: [
    { required: true, message: "请选择状态", trigger: "change" }
  ]
}

function getStatusText(status: number) {
  return status === 1 ? "启用" : "禁用"
}

function getStatusTagType(status: number) {
  return status === 1 ? "success" : "info"
}

function getTypeText(type: CategoryType) {
  const map: Record<CategoryType, string> = {
    POST: "帖子分类",
    LEARNING: "学习分类"
  }
  return map[type]
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getCategoryList({
      pageNum: queryForm.pageNum,
      pageSize: queryForm.pageSize,
      keyword: queryForm.keyword || undefined,
      type: queryForm.type,
      status: queryForm.status
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
  queryForm.keyword = ""
  queryForm.type = undefined
  queryForm.status = undefined
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

function resetFormData() {
  formData.name = ""
  formData.type = "POST"
  formData.icon = ""
  formData.description = ""
  formData.sort = 1
  formData.status = 1
  currentId.value = null
}

function handleCreate() {
  dialogType.value = "create"
  dialogVisible.value = true
  resetFormData()
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

function handleEdit(row: CategoryItem) {
  dialogType.value = "edit"
  dialogVisible.value = true
  currentId.value = row.id
  formData.name = row.name
  formData.type = row.type
  formData.icon = row.icon || ""
  formData.description = row.description || ""
  formData.sort = row.sort
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
      await createCategory(formData)
      ElMessage.success("新增分类成功")
    } else if (currentId.value !== null) {
      await updateCategory(currentId.value, formData)
      ElMessage.success("修改分类成功")
    }

    dialogVisible.value = false
    await fetchList()
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row: CategoryItem) {
  await ElMessageBox.confirm(
    `确认删除分类「${row.name}」吗？`,
    "删除确认",
    {
      type: "warning",
      confirmButtonText: "确认删除",
      cancelButtonText: "取消"
    }
  )

  await deleteCategory(row.id)
  ElMessage.success("删除分类成功")

  if (tableData.value.length === 1 && queryForm.pageNum > 1) {
    queryForm.pageNum -= 1
  }

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
        <el-form-item label="关键词">
          <el-input
            v-model.trim="queryForm.keyword"
            placeholder="请输入分类名称"
            clearable
            style="width: 220px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>

        <el-form-item label="类型">
          <el-select v-model="queryForm.type" placeholder="请选择类型" clearable style="width: 160px">
            <el-option label="帖子分类" value="POST" />
            <el-option label="学习分类" value="LEARNING" />
          </el-select>
        </el-form-item>

        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable style="width: 160px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            查询
          </el-button>
          <el-button @click="handleReset">
            重置
          </el-button>
          <el-button type="success" @click="handleCreate">
            新增分类
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <template #header>
        <div class="table-header">
          <span>分类管理</span>
          <span class="table-total">共 {{ total }} 条</span>
        </div>
      </template>

      <el-table v-loading="loading" :data="tableData" border>
        <el-table-column prop="id" label="分类 ID" width="100" align="center" />
        <el-table-column prop="name" label="分类名称" min-width="180" show-overflow-tooltip />
        <el-table-column label="分类类型" width="120" align="center">
          <template #default="{ row }">
            {{ getTypeText(row.type) }}
          </template>
        </el-table-column>
        <el-table-column prop="description" label="分类描述" min-width="220" show-overflow-tooltip />
        <el-table-column prop="sort" label="排序号" width="100" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="180" />
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
      :title="dialogType === 'create' ? '新增分类' : '编辑分类'"
      width="500px"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px">
        <el-form-item label="分类名称" prop="name">
          <el-input
            v-model.trim="formData.name"
            placeholder="请输入分类名称"
            maxlength="20"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="分类类型" prop="type">
          <el-select v-model="formData.type" placeholder="请选择分类类型" style="width: 100%">
            <el-option label="帖子分类" value="POST" />
            <el-option label="学习分类" value="LEARNING" />
          </el-select>
        </el-form-item>

        <el-form-item label="图标" prop="icon">
          <el-input
            v-model.trim="formData.icon"
            placeholder="请输入图标名称或资源地址，可选"
            maxlength="100"
          />
        </el-form-item>

        <el-form-item label="分类描述" prop="description">
          <el-input
            v-model.trim="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入分类描述，可选"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="排序号" prop="sort">
          <el-input-number
            v-model="formData.sort"
            :min="1"
            :max="9999"
            controls-position="right"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">启用</el-radio>
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
