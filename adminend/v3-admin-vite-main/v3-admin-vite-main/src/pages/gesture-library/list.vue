<script setup lang="ts">
import type { FormInstance, FormRules } from "element-plus"
import {
  createGestureLibrary,
  getGestureLibraryDetail,
  getGestureLibraryList,
  updateGestureLibrary,
  updateGestureLibraryStatus,
  type GestureLibraryFormData,
  type GestureLibraryItem
} from "@@/apis/gesture-library"

defineOptions({
  name: "GestureLibraryList"
})

const loading = ref(false)
const submitLoading = ref(false)
const detailLoading = ref(false)
const actionLoadingId = ref<number | null>(null)

const tableData = ref<GestureLibraryItem[]>([])
const total = ref(0)

const queryForm = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: "",
  status: undefined as number | undefined,
  isBuiltin: undefined as number | undefined
})

const dialogVisible = ref(false)
const dialogType = ref<"create" | "edit">("create")
const currentId = ref<number | null>(null)

const detailVisible = ref(false)
const detailData = ref<GestureLibraryItem | null>(null)

const formRef = ref<FormInstance>()
const formData = reactive<GestureLibraryFormData>({
  gestureCode: "",
  gestureName: "",
  description: "",
  previewImage: "",
  status: 1,
  sort: 1,
  isBuiltin: 1,
  detectionKey: ""
})

const formRules: FormRules = {
  gestureCode: [
    { required: true, message: "请输入手势编码", trigger: "blur" },
    { min: 2, max: 50, message: "长度需在 2 到 50 个字符之间", trigger: "blur" }
  ],
  gestureName: [
    { required: true, message: "请输入手势名称", trigger: "blur" },
    { min: 2, max: 50, message: "长度需在 2 到 50 个字符之间", trigger: "blur" }
  ],
  status: [
    { required: true, message: "请选择状态", trigger: "change" }
  ],
  sort: [
    { required: true, message: "请输入排序值", trigger: "change" }
  ],
  isBuiltin: [
    { required: true, message: "请选择内置属性", trigger: "change" }
  ],
  detectionKey: [
    { required: true, message: "请输入 detectionKey", trigger: "blur" },
    { min: 2, max: 100, message: "长度需在 2 到 100 个字符之间", trigger: "blur" }
  ]
}

function getStatusText(status: number) {
  return status === 1 ? "启用" : "禁用"
}

function getStatusTagType(status: number): "success" | "info" {
  return status === 1 ? "success" : "info"
}

function getBuiltinText(isBuiltin: number) {
  return isBuiltin === 1 ? "内置" : "自定义"
}

function getBuiltinTagType(isBuiltin: number): "warning" | "info" {
  return isBuiltin === 1 ? "warning" : "info"
}

function resetFormData() {
  formData.gestureCode = ""
  formData.gestureName = ""
  formData.description = ""
  formData.previewImage = ""
  formData.status = 1
  formData.sort = 1
  formData.isBuiltin = 1
  formData.detectionKey = ""
  currentId.value = null
}

function fillFormData(row: GestureLibraryItem) {
  formData.gestureCode = row.gestureCode
  formData.gestureName = row.gestureName
  formData.description = row.description || ""
  formData.previewImage = row.previewImage || ""
  formData.status = row.status
  formData.sort = row.sort
  formData.isBuiltin = row.isBuiltin
  formData.detectionKey = row.detectionKey
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getGestureLibraryList({
      pageNum: queryForm.pageNum,
      pageSize: queryForm.pageSize,
      keyword: queryForm.keyword || undefined,
      status: queryForm.status,
      isBuiltin: queryForm.isBuiltin
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
  queryForm.status = undefined
  queryForm.isBuiltin = undefined
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

function handleCreate() {
  dialogType.value = "create"
  dialogVisible.value = true
  resetFormData()
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

function handleEdit(row: GestureLibraryItem) {
  dialogType.value = "edit"
  dialogVisible.value = true
  currentId.value = row.id
  fillFormData(row)
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

async function handleView(row: GestureLibraryItem) {
  detailVisible.value = true
  detailLoading.value = true
  try {
    const res = await getGestureLibraryDetail(row.id)
    detailData.value = res.data
  } finally {
    detailLoading.value = false
  }
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate()

  const payload: GestureLibraryFormData = {
    gestureCode: formData.gestureCode.trim(),
    gestureName: formData.gestureName.trim(),
    description: formData.description.trim(),
    previewImage: formData.previewImage.trim(),
    status: formData.status,
    sort: formData.sort,
    isBuiltin: formData.isBuiltin,
    detectionKey: formData.detectionKey.trim()
  }

  submitLoading.value = true
  try {
    if (dialogType.value === "create") {
      await createGestureLibrary(payload)
      ElMessage.success("新增基础手势成功")
    } else if (currentId.value !== null) {
      await updateGestureLibrary(currentId.value, payload)
      ElMessage.success("修改基础手势成功")
    }

    dialogVisible.value = false
    await fetchList()
  } finally {
    submitLoading.value = false
  }
}

async function handleToggleStatus(row: GestureLibraryItem) {
  const nextStatus = row.status === 1 ? 0 : 1
  const actionText = nextStatus === 1 ? "启用" : "禁用"

  await ElMessageBox.confirm(
    `确认${actionText}手势“${row.gestureName}”吗？`,
    "状态确认",
    {
      type: "warning",
      confirmButtonText: `确认${actionText}`,
      cancelButtonText: "取消"
    }
  )

  actionLoadingId.value = row.id
  try {
    await updateGestureLibraryStatus(row.id, nextStatus)
    ElMessage.success(`${actionText}成功`)

    if (detailVisible.value && detailData.value?.id === row.id) {
      detailData.value = {
        ...detailData.value,
        status: nextStatus
      }
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
        <el-form-item label="关键词">
          <el-input
            v-model.trim="queryForm.keyword"
            placeholder="请输入手势编码或名称"
            clearable
            style="width: 240px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>

        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable style="width: 160px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>

        <el-form-item label="类型">
          <el-select v-model="queryForm.isBuiltin" placeholder="请选择类型" clearable style="width: 160px">
            <el-option label="内置" :value="1" />
            <el-option label="自定义" :value="0" />
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
            新增手势
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <template #header>
        <div class="table-header">
          <span>基础手势库</span>
          <span class="table-total">共 {{ total }} 条</span>
        </div>
      </template>

      <el-table v-loading="loading" :data="tableData" border>
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column label="预览图" width="100" align="center">
          <template #default="{ row }">
            <el-image
              v-if="row.previewImage"
              :src="row.previewImage"
              style="width: 56px; height: 56px; border-radius: 6px"
              fit="cover"
              :preview-src-list="[row.previewImage]"
              preview-teleported
            />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="gestureCode" label="手势编码" min-width="160" show-overflow-tooltip />
        <el-table-column prop="gestureName" label="手势名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="detectionKey" label="Detection Key" min-width="180" show-overflow-tooltip />
        <el-table-column label="类型" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="getBuiltinTagType(row.isBuiltin)">
              {{ getBuiltinText(row.isBuiltin) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="90" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="220" show-overflow-tooltip />
        <el-table-column prop="updateTime" label="更新时间" min-width="180" />
        <el-table-column label="操作" fixed="right" width="220" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">
              详情
            </el-button>
            <el-button type="success" link @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button
              :type="row.status === 1 ? 'warning' : 'success'"
              link
              :loading="actionLoadingId === row.id"
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 1 ? "禁用" : "启用" }}
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
      :title="dialogType === 'create' ? '新增基础手势' : '编辑基础手势'"
      width="720px"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="110px">
        <el-form-item label="手势编码" prop="gestureCode">
          <el-input
            v-model.trim="formData.gestureCode"
            placeholder="请输入手势编码"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="手势名称" prop="gestureName">
          <el-input
            v-model.trim="formData.gestureName"
            placeholder="请输入手势名称"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="识别键" prop="detectionKey">
          <el-input
            v-model.trim="formData.detectionKey"
            placeholder="请输入 detectionKey"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="预览图地址" prop="previewImage">
          <el-input
            v-model.trim="formData.previewImage"
            placeholder="请输入图片 URL，可为空"
            maxlength="255"
          />
          <el-image
            v-if="formData.previewImage"
            :src="formData.previewImage"
            class="form-preview-image"
            fit="cover"
            :preview-src-list="[formData.previewImage]"
            preview-teleported
          />
        </el-form-item>

        <el-form-item label="描述" prop="description">
          <el-input
            v-model.trim="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入手势描述，可为空"
            maxlength="255"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="排序" prop="sort">
          <el-input-number
            v-model="formData.sort"
            :min="1"
            :max="9999"
            controls-position="right"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="类型" prop="isBuiltin">
          <el-radio-group v-model="formData.isBuiltin">
            <el-radio :value="1">内置</el-radio>
            <el-radio :value="0">自定义</el-radio>
          </el-radio-group>
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

    <el-drawer v-model="detailVisible" title="基础手势详情" size="680px">
      <div v-loading="detailLoading">
        <template v-if="detailData">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="ID">
              {{ detailData.id }}
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="getStatusTagType(detailData.status)">
                {{ getStatusText(detailData.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="手势编码">
              {{ detailData.gestureCode }}
            </el-descriptions-item>
            <el-descriptions-item label="手势名称">
              {{ detailData.gestureName }}
            </el-descriptions-item>
            <el-descriptions-item label="类型">
              <el-tag :type="getBuiltinTagType(detailData.isBuiltin)">
                {{ getBuiltinText(detailData.isBuiltin) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="排序">
              {{ detailData.sort }}
            </el-descriptions-item>
            <el-descriptions-item label="Detection Key" :span="2">
              {{ detailData.detectionKey }}
            </el-descriptions-item>
            <el-descriptions-item label="描述" :span="2">
              {{ detailData.description || "-" }}
            </el-descriptions-item>
            <el-descriptions-item label="预览图" :span="2">
              <el-image
                v-if="detailData.previewImage"
                :src="detailData.previewImage"
                class="detail-preview-image"
                fit="cover"
                :preview-src-list="[detailData.previewImage]"
                preview-teleported
              />
              <span v-else>-</span>
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">
              {{ detailData.createTime || "-" }}
            </el-descriptions-item>
            <el-descriptions-item label="更新时间">
              {{ detailData.updateTime || "-" }}
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

.form-preview-image {
  width: 96px;
  height: 96px;
  margin-top: 12px;
  border-radius: 8px;
}

.detail-preview-image {
  width: 180px;
  height: 180px;
  border-radius: 8px;
}

.drawer-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
