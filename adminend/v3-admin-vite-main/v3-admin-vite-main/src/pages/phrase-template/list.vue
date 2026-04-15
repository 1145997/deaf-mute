<script setup lang="ts">
import type { FormInstance, FormRules } from "element-plus"
import {
  createPhraseTemplate,
  getPhraseTemplateDetail,
  getPhraseTemplateList,
  updatePhraseTemplate,
  updatePhraseTemplateStatus,
  type PhraseTemplateFormData,
  type PhraseTemplateItem
} from "@@/apis/phrase-template"

defineOptions({
  name: "PhraseTemplateList"
})

const loading = ref(false)
const submitLoading = ref(false)
const detailLoading = ref(false)
const actionLoadingId = ref<number | null>(null)

const tableData = ref<PhraseTemplateItem[]>([])
const total = ref(0)

const queryForm = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: "",
  sceneType: "",
  status: undefined as number | undefined
})

const dialogVisible = ref(false)
const dialogType = ref<"create" | "edit">("create")
const currentId = ref<number | null>(null)

const detailVisible = ref(false)
const detailData = ref<PhraseTemplateItem | null>(null)

const formRef = ref<FormInstance>()
const formData = reactive<PhraseTemplateFormData>({
  phraseCode: "",
  phraseText: "",
  ttsText: "",
  sceneType: "",
  status: 1,
  sort: 1
})

const formRules: FormRules = {
  phraseCode: [
    { required: true, message: "请输入短语编码", trigger: "blur" },
    { min: 2, max: 50, message: "长度需在 2 到 50 个字符之间", trigger: "blur" }
  ],
  phraseText: [
    { required: true, message: "请输入短语文本", trigger: "blur" },
    { min: 1, max: 255, message: "长度需在 1 到 255 个字符之间", trigger: "blur" }
  ],
  status: [
    { required: true, message: "请选择状态", trigger: "change" }
  ],
  sort: [
    { required: true, message: "请输入排序值", trigger: "change" }
  ]
}

function getStatusText(status: number) {
  return status === 1 ? "启用" : "禁用"
}

function getStatusTagType(status: number): "success" | "info" {
  return status === 1 ? "success" : "info"
}

function getSceneTypeText(sceneType?: string | null) {
  return sceneType?.trim() || "-"
}

function resetFormData() {
  formData.phraseCode = ""
  formData.phraseText = ""
  formData.ttsText = ""
  formData.sceneType = ""
  formData.status = 1
  formData.sort = 1
  currentId.value = null
}

function fillFormData(row: PhraseTemplateItem) {
  formData.phraseCode = row.phraseCode
  formData.phraseText = row.phraseText
  formData.ttsText = row.ttsText || ""
  formData.sceneType = row.sceneType || ""
  formData.status = row.status
  formData.sort = row.sort
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getPhraseTemplateList({
      pageNum: queryForm.pageNum,
      pageSize: queryForm.pageSize,
      keyword: queryForm.keyword || undefined,
      sceneType: queryForm.sceneType || undefined,
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
  queryForm.sceneType = ""
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

function handleCreate() {
  dialogType.value = "create"
  dialogVisible.value = true
  resetFormData()
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

function handleEdit(row: PhraseTemplateItem) {
  dialogType.value = "edit"
  dialogVisible.value = true
  currentId.value = row.id
  fillFormData(row)
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

async function handleView(row: PhraseTemplateItem) {
  detailVisible.value = true
  detailLoading.value = true
  try {
    const res = await getPhraseTemplateDetail(row.id)
    detailData.value = res.data
  } finally {
    detailLoading.value = false
  }
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate()

  const payload: PhraseTemplateFormData = {
    phraseCode: formData.phraseCode.trim(),
    phraseText: formData.phraseText.trim(),
    ttsText: formData.ttsText.trim(),
    sceneType: formData.sceneType.trim(),
    status: formData.status,
    sort: formData.sort
  }

  submitLoading.value = true
  try {
    if (dialogType.value === "create") {
      await createPhraseTemplate(payload)
      ElMessage.success("新增短语模板成功")
    } else if (currentId.value !== null) {
      await updatePhraseTemplate(currentId.value, payload)
      ElMessage.success("修改短语模板成功")
    }

    dialogVisible.value = false
    await fetchList()
  } finally {
    submitLoading.value = false
  }
}

async function handleToggleStatus(row: PhraseTemplateItem) {
  const nextStatus = row.status === 1 ? 0 : 1
  const actionText = nextStatus === 1 ? "启用" : "禁用"

  await ElMessageBox.confirm(
    `确认${actionText}短语模板“${row.phraseText}”吗？`,
    "状态确认",
    {
      type: "warning",
      confirmButtonText: `确认${actionText}`,
      cancelButtonText: "取消"
    }
  )

  actionLoadingId.value = row.id
  try {
    await updatePhraseTemplateStatus(row.id, nextStatus)
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
            placeholder="请输入短语编码或文本"
            clearable
            style="width: 240px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>

        <el-form-item label="场景类型">
          <el-input
            v-model.trim="queryForm.sceneType"
            placeholder="如 daily / control"
            clearable
            style="width: 180px"
            @keyup.enter="handleSearch"
          />
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
            新增模板
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <template #header>
        <div class="table-header">
          <span>短语模板</span>
          <span class="table-total">共 {{ total }} 条</span>
        </div>
      </template>

      <el-table v-loading="loading" :data="tableData" border>
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="phraseCode" label="短语编码" min-width="140" show-overflow-tooltip />
        <el-table-column prop="phraseText" label="短语文本" min-width="220" show-overflow-tooltip />
        <el-table-column prop="ttsText" label="播报文本" min-width="220" show-overflow-tooltip />
        <el-table-column label="场景类型" width="130" align="center">
          <template #default="{ row }">
            {{ getSceneTypeText(row.sceneType) }}
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
      :title="dialogType === 'create' ? '新增短语模板' : '编辑短语模板'"
      width="720px"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="短语编码" prop="phraseCode">
          <el-input
            v-model.trim="formData.phraseCode"
            placeholder="请输入短语编码"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="短语文本" prop="phraseText">
          <el-input
            v-model.trim="formData.phraseText"
            placeholder="请输入短语文本"
            maxlength="255"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="播报文本" prop="ttsText">
          <el-input
            v-model.trim="formData.ttsText"
            placeholder="为空时后端会自动回落到短语文本"
            maxlength="255"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="场景类型" prop="sceneType">
          <el-input
            v-model.trim="formData.sceneType"
            placeholder="请输入场景类型，如 daily / control"
            maxlength="50"
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

    <el-drawer v-model="detailVisible" title="短语模板详情" size="680px">
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
            <el-descriptions-item label="短语编码">
              {{ detailData.phraseCode }}
            </el-descriptions-item>
            <el-descriptions-item label="场景类型">
              {{ getSceneTypeText(detailData.sceneType) }}
            </el-descriptions-item>
            <el-descriptions-item label="短语文本" :span="2">
              {{ detailData.phraseText }}
            </el-descriptions-item>
            <el-descriptions-item label="播报文本" :span="2">
              {{ detailData.ttsText || "-" }}
            </el-descriptions-item>
            <el-descriptions-item label="排序">
              {{ detailData.sort }}
            </el-descriptions-item>
            <el-descriptions-item label="更新时间">
              {{ detailData.updateTime || "-" }}
            </el-descriptions-item>
            <el-descriptions-item label="创建时间" :span="2">
              {{ detailData.createTime || "-" }}
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
  margin-top: 16px;
}
</style>
