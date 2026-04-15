<script setup lang="ts">
import type { FormInstance, FormRules } from "element-plus"
import {
  activateRecognitionConfig,
  createRecognitionConfig,
  getRecognitionConfigDetail,
  getRecognitionConfigList,
  updateRecognitionConfig,
  type RecognitionConfigFormData,
  type RecognitionConfigItem
} from "@@/apis/recognition-config"

defineOptions({
  name: "RecognitionConfigList"
})

type RecognitionConfigFormState = Omit<RecognitionConfigFormData, "gestureOrderJson"> & {
  gestureOrder: string
}

const loading = ref(false)
const submitLoading = ref(false)
const detailLoading = ref(false)
const actionLoadingId = ref<number | null>(null)

const tableData = ref<RecognitionConfigItem[]>([])
const total = ref(0)

const queryForm = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: "",
  activeFlag: undefined as number | undefined
})

const dialogVisible = ref(false)
const dialogType = ref<"create" | "edit">("create")
const currentId = ref<number | null>(null)

const detailVisible = ref(false)
const detailData = ref<RecognitionConfigItem | null>(null)

const formRef = ref<FormInstance>()
const formData = reactive<RecognitionConfigFormState>({
  configName: "",
  confidenceMin: 0.5,
  holdMs: 300,
  debounceMs: 500,
  cooldownMs: 1000,
  requiredHits: 3,
  maxIntervalMs: 1500,
  lockTimeoutMs: 3000,
  resetOnFail: 1,
  allowRepeat: 0,
  gestureOrder: "",
  activeFlag: 0,
  remark: ""
})

function parseGestureOrder(value?: string | null) {
  const text = (value || "").trim()
  if (!text) return [] as string[]

  if (text.startsWith("[")) {
    const parsed = JSON.parse(text)
    if (!Array.isArray(parsed)) {
      throw new Error("手势顺序必须是数组")
    }
    return parsed.map(item => String(item).trim()).filter(Boolean)
  }

  return text
    .split(/[\n,，]+/)
    .map(item => item.trim())
    .filter(Boolean)
}

function serializeGestureOrder(value: string) {
  return JSON.stringify(parseGestureOrder(value))
}

function formatGestureOrderEditor(value?: string | null) {
  if (!value) return ""

  try {
    return parseGestureOrder(value).join("\n")
  } catch {
    return value
  }
}

function getGestureOrderSummary(value?: string | null) {
  try {
    const list = parseGestureOrder(value)
    if (!list.length) return "-"
    if (list.length <= 3) return list.join(" / ")
    return `${list.slice(0, 3).join(" / ")} 等 ${list.length} 项`
  } catch {
    return value || "-"
  }
}

function getGestureOrderList(value?: string | null) {
  try {
    return parseGestureOrder(value)
  } catch {
    return []
  }
}

const gestureOrderPreview = computed(() => {
  try {
    return parseGestureOrder(formData.gestureOrder)
  } catch {
    return []
  }
})

const formRules: FormRules = {
  configName: [
    { required: true, message: "请输入配置名称", trigger: "blur" },
    { min: 2, max: 100, message: "长度需在 2 到 100 个字符之间", trigger: "blur" }
  ],
  confidenceMin: [
    { required: true, message: "请输入最小置信度", trigger: "change" }
  ],
  holdMs: [
    { required: true, message: "请输入持续命中时长", trigger: "change" }
  ],
  debounceMs: [
    { required: true, message: "请输入防抖时间", trigger: "change" }
  ],
  cooldownMs: [
    { required: true, message: "请输入冷却时间", trigger: "change" }
  ],
  requiredHits: [
    { required: true, message: "请输入连续命中次数", trigger: "change" }
  ],
  maxIntervalMs: [
    { required: true, message: "请输入最大间隔时间", trigger: "change" }
  ],
  lockTimeoutMs: [
    { required: true, message: "请输入锁定超时", trigger: "change" }
  ],
  resetOnFail: [
    { required: true, message: "请选择失败后是否重置", trigger: "change" }
  ],
  allowRepeat: [
    { required: true, message: "请选择是否允许重复触发", trigger: "change" }
  ],
  activeFlag: [
    { required: true, message: "请选择生效状态", trigger: "change" }
  ],
  gestureOrder: [
    {
      validator: (_rule, value, callback) => {
        if (!value) {
          callback()
          return
        }

        try {
          parseGestureOrder(String(value))
          callback()
        } catch {
          callback(new Error("请输入 JSON 数组或按逗号/换行分隔的手势编码"))
        }
      },
      trigger: "blur"
    }
  ]
}

function getActiveText(activeFlag: number) {
  return activeFlag === 1 ? "生效中" : "未启用"
}

function getActiveTagType(activeFlag: number): "success" | "info" {
  return activeFlag === 1 ? "success" : "info"
}

function getBooleanText(value: number) {
  return value === 1 ? "是" : "否"
}

function getBooleanTagType(value: number): "success" | "info" {
  return value === 1 ? "success" : "info"
}

function resetFormData() {
  formData.configName = ""
  formData.confidenceMin = 0.5
  formData.holdMs = 300
  formData.debounceMs = 500
  formData.cooldownMs = 1000
  formData.requiredHits = 3
  formData.maxIntervalMs = 1500
  formData.lockTimeoutMs = 3000
  formData.resetOnFail = 1
  formData.allowRepeat = 0
  formData.gestureOrder = ""
  formData.activeFlag = 0
  formData.remark = ""
  currentId.value = null
}

function fillFormData(row: RecognitionConfigItem) {
  formData.configName = row.configName
  formData.confidenceMin = Number(row.confidenceMin)
  formData.holdMs = row.holdMs
  formData.debounceMs = row.debounceMs
  formData.cooldownMs = row.cooldownMs
  formData.requiredHits = row.requiredHits
  formData.maxIntervalMs = row.maxIntervalMs
  formData.lockTimeoutMs = row.lockTimeoutMs
  formData.resetOnFail = row.resetOnFail
  formData.allowRepeat = row.allowRepeat
  formData.gestureOrder = formatGestureOrderEditor(row.gestureOrderJson)
  formData.activeFlag = row.activeFlag
  formData.remark = row.remark || ""
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getRecognitionConfigList({
      pageNum: queryForm.pageNum,
      pageSize: queryForm.pageSize,
      keyword: queryForm.keyword || undefined,
      activeFlag: queryForm.activeFlag
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
  queryForm.activeFlag = undefined
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

function handleEdit(row: RecognitionConfigItem) {
  dialogType.value = "edit"
  dialogVisible.value = true
  currentId.value = row.id
  fillFormData(row)
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

async function handleView(row: RecognitionConfigItem) {
  detailVisible.value = true
  detailLoading.value = true
  try {
    const res = await getRecognitionConfigDetail(row.id)
    detailData.value = res.data
  } finally {
    detailLoading.value = false
  }
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate()

  const payload: RecognitionConfigFormData = {
    configName: formData.configName.trim(),
    confidenceMin: Number(formData.confidenceMin),
    holdMs: formData.holdMs,
    debounceMs: formData.debounceMs,
    cooldownMs: formData.cooldownMs,
    requiredHits: formData.requiredHits,
    maxIntervalMs: formData.maxIntervalMs,
    lockTimeoutMs: formData.lockTimeoutMs,
    resetOnFail: formData.resetOnFail,
    allowRepeat: formData.allowRepeat,
    gestureOrderJson: serializeGestureOrder(formData.gestureOrder),
    activeFlag: formData.activeFlag,
    remark: formData.remark.trim()
  }

  submitLoading.value = true
  try {
    if (dialogType.value === "create") {
      await createRecognitionConfig(payload)
      ElMessage.success("新增识别配置成功")
    } else if (currentId.value !== null) {
      await updateRecognitionConfig(currentId.value, payload)
      ElMessage.success("修改识别配置成功")
    }

    dialogVisible.value = false
    await fetchList()
  } finally {
    submitLoading.value = false
  }
}

async function handleActivate(row: RecognitionConfigItem) {
  await ElMessageBox.confirm(
    `确认启用配置“${row.configName}”吗？启用后会自动取消其他生效配置。`,
    "启用确认",
    {
      type: "warning",
      confirmButtonText: "确认启用",
      cancelButtonText: "取消"
    }
  )

  actionLoadingId.value = row.id
  try {
    await activateRecognitionConfig(row.id)
    ElMessage.success("启用识别配置成功")

    if (detailVisible.value && detailData.value?.id === row.id) {
      detailData.value = {
        ...detailData.value,
        activeFlag: 1
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
            placeholder="请输入配置名称"
            clearable
            style="width: 240px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>

        <el-form-item label="生效状态">
          <el-select v-model="queryForm.activeFlag" placeholder="请选择状态" clearable style="width: 160px">
            <el-option label="生效中" :value="1" />
            <el-option label="未启用" :value="0" />
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
            新增配置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <template #header>
        <div class="table-header">
          <span>全局识别配置</span>
          <span class="table-total">共 {{ total }} 条</span>
        </div>
      </template>

      <el-table v-loading="loading" :data="tableData" border>
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="configName" label="配置名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="confidenceMin" label="最小置信度" width="110" align="center" />
        <el-table-column prop="holdMs" label="持续命中" width="110" align="center" />
        <el-table-column prop="debounceMs" label="防抖" width="100" align="center" />
        <el-table-column prop="cooldownMs" label="冷却" width="100" align="center" />
        <el-table-column prop="requiredHits" label="连续命中" width="110" align="center" />
        <el-table-column label="手势顺序" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            {{ getGestureOrderSummary(row.gestureOrderJson) }}
          </template>
        </el-table-column>
        <el-table-column label="生效状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="getActiveTagType(row.activeFlag)">
              {{ getActiveText(row.activeFlag) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
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
              v-if="row.activeFlag !== 1"
              type="warning"
              link
              :loading="actionLoadingId === row.id"
              @click="handleActivate(row)"
            >
              启用
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
      :title="dialogType === 'create' ? '新增识别配置' : '编辑识别配置'"
      width="920px"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="配置名称" prop="configName">
              <el-input
                v-model.trim="formData.configName"
                placeholder="请输入配置名称"
                maxlength="100"
                show-word-limit
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="最小置信度" prop="confidenceMin">
              <el-input-number
                v-model="formData.confidenceMin"
                :min="0"
                :max="1"
                :step="0.01"
                :precision="4"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="持续命中时长" prop="holdMs">
              <el-input-number
                v-model="formData.holdMs"
                :min="0"
                :max="60000"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="防抖时间" prop="debounceMs">
              <el-input-number
                v-model="formData.debounceMs"
                :min="0"
                :max="60000"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="冷却时间" prop="cooldownMs">
              <el-input-number
                v-model="formData.cooldownMs"
                :min="0"
                :max="60000"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="连续命中次数" prop="requiredHits">
              <el-input-number
                v-model="formData.requiredHits"
                :min="1"
                :max="999"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="最大间隔时间" prop="maxIntervalMs">
              <el-input-number
                v-model="formData.maxIntervalMs"
                :min="0"
                :max="60000"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="锁定超时" prop="lockTimeoutMs">
              <el-input-number
                v-model="formData.lockTimeoutMs"
                :min="0"
                :max="60000"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="失败后重置" prop="resetOnFail">
          <el-radio-group v-model="formData.resetOnFail">
            <el-radio :value="1">是</el-radio>
            <el-radio :value="0">否</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="允许重复触发" prop="allowRepeat">
          <el-radio-group v-model="formData.allowRepeat">
            <el-radio :value="1">是</el-radio>
            <el-radio :value="0">否</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="生效状态" prop="activeFlag">
          <el-radio-group v-model="formData.activeFlag">
            <el-radio :value="1">保存后立即生效</el-radio>
            <el-radio :value="0">仅保存，不启用</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="手势顺序" prop="gestureOrder">
          <el-input
            v-model.trim="formData.gestureOrder"
            type="textarea"
            :rows="5"
            placeholder="支持 JSON 数组，或按逗号 / 换行输入手势编码"
          />
          <div class="form-tip">
            例如：`is_thumbs_up,is_v_sign` 或 `["is_thumbs_up","is_v_sign"]`
          </div>
          <div v-if="gestureOrderPreview.length" class="tag-list">
            <el-tag v-for="item in gestureOrderPreview" :key="item" class="tag-item">
              {{ item }}
            </el-tag>
          </div>
        </el-form-item>

        <el-form-item label="备注" prop="remark">
          <el-input
            v-model.trim="formData.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注，可为空"
            maxlength="255"
            show-word-limit
          />
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

    <el-drawer v-model="detailVisible" title="识别配置详情" size="820px">
      <div v-loading="detailLoading">
        <template v-if="detailData">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="ID">
              {{ detailData.id }}
            </el-descriptions-item>
            <el-descriptions-item label="生效状态">
              <el-tag :type="getActiveTagType(detailData.activeFlag)">
                {{ getActiveText(detailData.activeFlag) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="配置名称">
              {{ detailData.configName }}
            </el-descriptions-item>
            <el-descriptions-item label="最小置信度">
              {{ detailData.confidenceMin }}
            </el-descriptions-item>
            <el-descriptions-item label="持续命中时长">
              {{ detailData.holdMs }}
            </el-descriptions-item>
            <el-descriptions-item label="防抖时间">
              {{ detailData.debounceMs }}
            </el-descriptions-item>
            <el-descriptions-item label="冷却时间">
              {{ detailData.cooldownMs }}
            </el-descriptions-item>
            <el-descriptions-item label="连续命中次数">
              {{ detailData.requiredHits }}
            </el-descriptions-item>
            <el-descriptions-item label="最大间隔时间">
              {{ detailData.maxIntervalMs }}
            </el-descriptions-item>
            <el-descriptions-item label="锁定超时">
              {{ detailData.lockTimeoutMs }}
            </el-descriptions-item>
            <el-descriptions-item label="失败后重置">
              <el-tag :type="getBooleanTagType(detailData.resetOnFail)">
                {{ getBooleanText(detailData.resetOnFail) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="允许重复触发">
              <el-tag :type="getBooleanTagType(detailData.allowRepeat)">
                {{ getBooleanText(detailData.allowRepeat) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="手势顺序" :span="2">
              <div v-if="getGestureOrderList(detailData.gestureOrderJson).length" class="tag-list">
                <el-tag
                  v-for="item in getGestureOrderList(detailData.gestureOrderJson)"
                  :key="item"
                  class="tag-item"
                >
                  {{ item }}
                </el-tag>
              </div>
              <span v-else>{{ detailData.gestureOrderJson || "-" }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="备注" :span="2">
              {{ detailData.remark || "-" }}
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

.form-tip {
  width: 100%;
  margin-top: 8px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}

.tag-item {
  margin-right: 0;
}

.drawer-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
