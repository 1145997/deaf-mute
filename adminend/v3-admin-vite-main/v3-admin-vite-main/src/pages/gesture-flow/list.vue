<script setup lang="ts">
import type { FormInstance, FormRules } from "element-plus"
import {
  createGestureFlow,
  deleteGestureFlow,
  getEnabledGestureLibraryOptions,
  getEnabledPhraseTemplateOptions,
  getGestureFlowDetail,
  getGestureFlowList,
  updateGestureFlow,
  type GestureFlowDetail,
  type GestureFlowFormData,
  type GestureFlowItem,
  type GestureFlowNodeFormData,
  type GestureFlowOutputFormData,
  type GestureLibraryOptionItem,
  type PhraseTemplateOptionItem
} from "@@/apis/gesture-flow"

defineOptions({
  name: "GestureFlowList"
})

type TreeNode<T> = T & { children: TreeNode<T>[] }
type EditableNode = Omit<GestureFlowNodeFormData, "id"> & { id: number }
type EditableOutput = Omit<GestureFlowOutputFormData, "id"> & { id: number }

interface ParentOption {
  value: number
  label: string
}

const flowTypeOptions = [
  { label: "单动作", value: "SINGLE" },
  { label: "序列动作", value: "SEQUENCE" },
  { label: "控制动作", value: "CONTROL" }
]

const triggerModeOptions = [
  { label: "直接触发", value: "DIRECT" },
  { label: "状态机", value: "STATE_MACHINE" }
]

const outputTypeOptions = [
  { label: "直接文本", value: "TEXT" },
  { label: "短语模板", value: "PHRASE" },
  { label: "控制动作", value: "CONTROL" },
  { label: "不输出", value: "NONE" }
]

const controlActionOptions = [
  { label: "删除上一个", value: "DELETE_LAST" },
  { label: "清空全部", value: "CLEAR_ALL" },
  { label: "完成输入", value: "FINISH_INPUT" },
  { label: "确认", value: "CONFIRM" },
  { label: "取消", value: "CANCEL" }
]

const loading = ref(false)
const editorLoading = ref(false)
const submitLoading = ref(false)
const detailLoading = ref(false)

const tableData = ref<GestureFlowItem[]>([])
const total = ref(0)

const gestureOptions = ref<GestureLibraryOptionItem[]>([])
const phraseOptions = ref<PhraseTemplateOptionItem[]>([])

const queryForm = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: "",
  flowType: "",
  status: undefined as number | undefined,
  isBuiltin: undefined as number | undefined
})

const editorVisible = ref(false)
const editorType = ref<"create" | "edit">("create")
const currentId = ref<number | null>(null)
const editorTab = ref("basic")

const detailVisible = ref(false)
const detailTab = ref("basic")
const detailData = ref<GestureFlowDetail | null>(null)

const flowFormRef = ref<FormInstance>()
const flowForm = reactive({
  flowCode: "",
  flowName: "",
  flowType: "SINGLE",
  triggerMode: "DIRECT",
  status: 1,
  priority: 100,
  isBuiltin: 1,
  description: ""
})

const editableNodes = ref<EditableNode[]>([])
const editableOutputs = ref<EditableOutput[]>([])

const nodeDialogVisible = ref(false)
const nodeDialogType = ref<"create" | "edit">("create")
const editingNodeId = ref<number | null>(null)
const nodeFormRef = ref<FormInstance>()
const outputDialogVisible = ref(false)
const outputDialogType = ref<"create" | "edit">("create")
const editingOutputId = ref<number | null>(null)
const outputFormRef = ref<FormInstance>()

const tempIdSeed = ref(-1)

function nextTempId() {
  const id = tempIdSeed.value
  tempIdSeed.value -= 1
  return id
}

function createDefaultNode(): EditableNode {
  return {
    id: nextTempId(),
    nodeCode: "",
    nodeName: "",
    parentNodeId: null,
    parentNodeCode: "",
    gestureLibraryId: null,
    isStart: 0,
    isEnd: 0,
    nodeOrder: editableNodes.value.length + 1,
    confidenceMin: 0.5,
    holdMs: 300,
    debounceMs: 500,
    cooldownMs: 1000,
    requiredHits: 3,
    maxIntervalMs: 1500,
    resetOnFail: 1,
    allowRepeat: 0,
    successNextStrategy: "",
    failStrategy: "",
    remark: ""
  }
}

function createDefaultOutput(): EditableOutput {
  return {
    id: nextTempId(),
    endNodeId: null,
    endNodeCode: "",
    outputType: "TEXT",
    outputText: "",
    phraseTemplateId: null,
    controlAction: "",
    ttsText: "",
    displayText: ""
  }
}

const nodeForm = reactive<EditableNode>(createDefaultNode())
const outputForm = reactive<EditableOutput>(createDefaultOutput())

const flowFormRules: FormRules = {
  flowCode: [
    { required: true, message: "请输入动作流编码", trigger: "blur" },
    { min: 2, max: 50, message: "长度需在 2 到 50 个字符之间", trigger: "blur" }
  ],
  flowName: [
    { required: true, message: "请输入动作流名称", trigger: "blur" },
    { min: 2, max: 100, message: "长度需在 2 到 100 个字符之间", trigger: "blur" }
  ],
  flowType: [
    { required: true, message: "请选择动作流类型", trigger: "change" }
  ],
  triggerMode: [
    { required: true, message: "请选择触发模式", trigger: "change" }
  ],
  status: [
    { required: true, message: "请选择状态", trigger: "change" }
  ],
  priority: [
    { required: true, message: "请输入优先级", trigger: "change" }
  ],
  isBuiltin: [
    { required: true, message: "请选择是否内置", trigger: "change" }
  ]
}

const nodeFormRules: FormRules = {
  nodeCode: [
    { required: true, message: "请输入节点编码", trigger: "blur" },
    { min: 1, max: 50, message: "长度需在 1 到 50 个字符之间", trigger: "blur" }
  ],
  nodeName: [
    { required: true, message: "请输入节点名称", trigger: "blur" },
    { min: 1, max: 100, message: "长度需在 1 到 100 个字符之间", trigger: "blur" }
  ],
  gestureLibraryId: [
    { required: true, message: "请选择基础手势", trigger: "change" }
  ],
  nodeOrder: [
    { required: true, message: "请输入节点顺序", trigger: "change" }
  ]
}

const outputFormRules: FormRules = {
  endNodeId: [
    { required: true, message: "请选择结束节点", trigger: "change" }
  ],
  outputType: [
    { required: true, message: "请选择输出类型", trigger: "change" }
  ],
  outputText: [
    {
      validator: (_rule, value, callback) => {
        if (outputForm.outputType === "TEXT" && !String(value || "").trim()) {
          callback(new Error("直接文本输出时请输入输出文本"))
          return
        }
        callback()
      },
      trigger: "blur"
    }
  ],
  phraseTemplateId: [
    {
      validator: (_rule, value, callback) => {
        if (outputForm.outputType === "PHRASE" && !value) {
          callback(new Error("短语模板输出时请选择短语模板"))
          return
        }
        callback()
      },
      trigger: "change"
    }
  ],
  controlAction: [
    {
      validator: (_rule, value, callback) => {
        if (outputForm.outputType === "CONTROL" && !String(value || "").trim()) {
          callback(new Error("控制动作输出时请选择控制动作"))
          return
        }
        callback()
      },
      trigger: "change"
    }
  ]
}

function getFlowTypeText(flowType?: string | null) {
  return flowTypeOptions.find(item => item.value === flowType)?.label || flowType || "-"
}

function getTriggerModeText(triggerMode?: string | null) {
  return triggerModeOptions.find(item => item.value === triggerMode)?.label || triggerMode || "-"
}

function getStatusText(status: number) {
  return status === 1 ? "启用" : "停用"
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

function getOutputTypeText(outputType?: string | null) {
  return outputTypeOptions.find(item => item.value === outputType)?.label || outputType || "-"
}

function getControlActionText(controlAction?: string | null) {
  return controlActionOptions.find(item => item.value === controlAction)?.label || controlAction || "-"
}

function buildTree<T extends { id: number; parentNodeId: number | null }>(list: T[]): TreeNode<T>[] {
  const nodeMap = new Map<number, TreeNode<T>>()
  const roots: TreeNode<T>[] = []

  list.forEach((item) => {
    nodeMap.set(item.id, { ...item, children: [] })
  })

  nodeMap.forEach((node) => {
    if (node.parentNodeId !== null && nodeMap.has(node.parentNodeId)) {
      nodeMap.get(node.parentNodeId)?.children.push(node)
    } else {
      roots.push(node)
    }
  })

  return roots
}

function getGestureLabel(gestureLibraryId?: number | null) {
  if (!gestureLibraryId) return "-"
  const item = gestureOptions.value.find(option => option.id === gestureLibraryId)
  if (!item) return `手势 #${gestureLibraryId}`
  return `${item.gestureName} (${item.gestureCode})`
}

function getPhraseLabel(phraseTemplateId?: number | null) {
  if (!phraseTemplateId) return "-"
  const item = phraseOptions.value.find(option => option.id === phraseTemplateId)
  if (!item) return `短语 #${phraseTemplateId}`
  return `${item.phraseText} (${item.phraseCode})`
}

function getNodeLabelById(nodeId?: number | null) {
  if (!nodeId) return "-"
  const node = editableNodes.value.find(item => item.id === nodeId)
  return node ? `${node.nodeName} (${node.nodeCode})` : `节点 #${nodeId}`
}

function getDetailNodeLabelById(nodeId?: number | null) {
  if (!nodeId || !detailData.value) return "-"
  const node = detailData.value.nodeList.find(item => item.id === nodeId)
  return node ? `${node.nodeName} (${node.nodeCode})` : `节点 #${nodeId}`
}

function resetFlowForm() {
  flowForm.flowCode = ""
  flowForm.flowName = ""
  flowForm.flowType = "SINGLE"
  flowForm.triggerMode = "DIRECT"
  flowForm.status = 1
  flowForm.priority = 100
  flowForm.isBuiltin = 1
  flowForm.description = ""
  editableNodes.value = []
  editableOutputs.value = []
  currentId.value = null
  tempIdSeed.value = -1
}

function fillFlowForm(detail: GestureFlowDetail) {
  flowForm.flowCode = detail.flowCode
  flowForm.flowName = detail.flowName
  flowForm.flowType = detail.flowType
  flowForm.triggerMode = detail.triggerMode
  flowForm.status = detail.status
  flowForm.priority = detail.priority
  flowForm.isBuiltin = detail.isBuiltin
  flowForm.description = detail.description || ""

  editableNodes.value = detail.nodeList.map(item => ({
    id: item.id,
    nodeCode: item.nodeCode,
    nodeName: item.nodeName,
    parentNodeId: item.parentNodeId,
    parentNodeCode: "",
    gestureLibraryId: item.gestureLibraryId,
    isStart: item.isStart ?? 0,
    isEnd: item.isEnd ?? 0,
    nodeOrder: item.nodeOrder ?? 1,
    confidenceMin: item.confidenceMin !== null ? Number(item.confidenceMin) : null,
    holdMs: item.holdMs,
    debounceMs: item.debounceMs,
    cooldownMs: item.cooldownMs,
    requiredHits: item.requiredHits,
    maxIntervalMs: item.maxIntervalMs,
    resetOnFail: item.resetOnFail ?? 1,
    allowRepeat: item.allowRepeat ?? 0,
    successNextStrategy: item.successNextStrategy || "",
    failStrategy: item.failStrategy || "",
    remark: item.remark || ""
  }))

  editableOutputs.value = detail.outputList.map(item => ({
    id: item.id,
    endNodeId: item.endNodeId,
    endNodeCode: "",
    outputType: item.outputType,
    outputText: item.outputText || "",
    phraseTemplateId: item.phraseTemplateId,
    controlAction: item.controlAction || "",
    ttsText: item.ttsText || "",
    displayText: item.displayText || ""
  }))
}

const editableNodeTree = computed(() => buildTree(editableNodes.value))
const detailNodeTree = computed(() => detailData.value ? buildTree(detailData.value.nodeList) : [])

function collectDescendantIds(nodeId: number) {
  const result = new Set<number>()
  const childMap = new Map<number, number[]>()

  editableNodes.value.forEach((node) => {
    if (node.parentNodeId !== null) {
      const children = childMap.get(node.parentNodeId) || []
      children.push(node.id)
      childMap.set(node.parentNodeId, children)
    }
  })

  const queue = [...(childMap.get(nodeId) || [])]
  while (queue.length) {
    const current = queue.shift()!
    if (result.has(current)) continue
    result.add(current)
    queue.push(...(childMap.get(current) || []))
  }

  return result
}

const nodeParentOptions = computed<ParentOption[]>(() => {
  const disallowedIds = new Set<number>()
  if (editingNodeId.value !== null) {
    disallowedIds.add(editingNodeId.value)
    collectDescendantIds(editingNodeId.value).forEach(id => disallowedIds.add(id))
  }

  const flatten = (tree: TreeNode<EditableNode>[], depth = 0): ParentOption[] => {
    const result: ParentOption[] = []
    tree.forEach((node) => {
      if (!disallowedIds.has(node.id)) {
        result.push({
          value: node.id,
          label: `${"　".repeat(depth)}${node.nodeName} (${node.nodeCode})`
        })
      }
      result.push(...flatten(node.children, depth + 1))
    })
    return result
  }

  return flatten(editableNodeTree.value)
})

const endNodeOptions = computed(() =>
  editableNodes.value
    .filter(node => node.isEnd === 1)
    .sort((a, b) => {
      const orderCompare = (a.nodeOrder || 0) - (b.nodeOrder || 0)
      return orderCompare !== 0 ? orderCompare : (a.id ?? 0) - (b.id ?? 0)
    })
    .map(node => ({
      value: node.id,
      label: `${node.nodeName} (${node.nodeCode})`
    }))
)

async function ensureOptionsLoaded() {
  const tasks: Promise<unknown>[] = []
  if (!gestureOptions.value.length) {
    tasks.push(
      getEnabledGestureLibraryOptions().then((res) => {
        gestureOptions.value = res.data
      })
    )
  }
  if (!phraseOptions.value.length) {
    tasks.push(
      getEnabledPhraseTemplateOptions().then((res) => {
        phraseOptions.value = res.data
      })
    )
  }
  if (tasks.length) {
    await Promise.all(tasks)
  }
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getGestureFlowList({
      pageNum: queryForm.pageNum,
      pageSize: queryForm.pageSize,
      keyword: queryForm.keyword || undefined,
      flowType: queryForm.flowType || undefined,
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
  queryForm.flowType = ""
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

async function handleCreate() {
  editorType.value = "create"
  editorVisible.value = true
  editorLoading.value = true
  editorTab.value = "basic"
  resetFlowForm()
  try {
    await ensureOptionsLoaded()
  } finally {
    editorLoading.value = false
  }
  nextTick(() => {
    flowFormRef.value?.clearValidate()
  })
}

async function handleEdit(row: GestureFlowItem) {
  editorType.value = "edit"
  editorVisible.value = true
  editorLoading.value = true
  editorTab.value = "basic"
  currentId.value = row.id
  try {
    const [detailRes] = await Promise.all([
      getGestureFlowDetail(row.id),
      ensureOptionsLoaded()
    ])
    fillFlowForm(detailRes.data)
  } finally {
    editorLoading.value = false
  }
  nextTick(() => {
    flowFormRef.value?.clearValidate()
  })
}

async function handleView(row: GestureFlowItem) {
  detailVisible.value = true
  detailLoading.value = true
  detailTab.value = "basic"
  try {
    const res = await getGestureFlowDetail(row.id)
    detailData.value = res.data
  } finally {
    detailLoading.value = false
  }
}

function buildFlowPayload(): GestureFlowFormData {
  const nodeCodeMap = new Map<number, string>()
  editableNodes.value.forEach((node) => {
    if (node.id !== null) {
      nodeCodeMap.set(node.id, node.nodeCode.trim())
    }
  })

  return {
    flowCode: flowForm.flowCode.trim(),
    flowName: flowForm.flowName.trim(),
    flowType: flowForm.flowType,
    triggerMode: flowForm.triggerMode,
    status: flowForm.status,
    priority: flowForm.priority,
    isBuiltin: flowForm.isBuiltin,
    description: flowForm.description.trim(),
    nodeList: [...editableNodes.value]
      .sort((a, b) => {
        const orderCompare = (a.nodeOrder || 0) - (b.nodeOrder || 0)
        return orderCompare !== 0 ? orderCompare : (a.id ?? 0) - (b.id ?? 0)
      })
      .map(node => ({
        id: null,
        nodeCode: node.nodeCode.trim(),
        nodeName: node.nodeName.trim(),
        parentNodeId: null,
        parentNodeCode: node.parentNodeId !== null ? (nodeCodeMap.get(node.parentNodeId) || "") : "",
        gestureLibraryId: node.gestureLibraryId,
        isStart: node.isStart,
        isEnd: node.isEnd,
        nodeOrder: node.nodeOrder,
        confidenceMin: node.confidenceMin !== null ? Number(node.confidenceMin) : null,
        holdMs: node.holdMs,
        debounceMs: node.debounceMs,
        cooldownMs: node.cooldownMs,
        requiredHits: node.requiredHits,
        maxIntervalMs: node.maxIntervalMs,
        resetOnFail: node.resetOnFail,
        allowRepeat: node.allowRepeat,
        successNextStrategy: node.successNextStrategy.trim(),
        failStrategy: node.failStrategy.trim(),
        remark: node.remark.trim()
      })),
    outputList: editableOutputs.value.map(output => ({
      id: null,
      endNodeId: null,
      endNodeCode: output.endNodeId !== null ? (nodeCodeMap.get(output.endNodeId) || "") : "",
      outputType: output.outputType,
      outputText: output.outputText.trim(),
      phraseTemplateId: output.outputType === "PHRASE" ? output.phraseTemplateId : null,
      controlAction: output.outputType === "CONTROL" ? output.controlAction : "",
      ttsText: output.ttsText.trim(),
      displayText: output.displayText.trim()
    }))
  }
}

async function handleSubmit() {
  if (!flowFormRef.value) return
  await flowFormRef.value.validate()

  if (!editableNodes.value.length) {
    editorTab.value = "nodes"
    ElMessage.error("请至少配置一个节点")
    return
  }

  const nodeCodeSet = new Set<string>()
  for (const node of editableNodes.value) {
    const code = node.nodeCode.trim()
    if (!code) {
      editorTab.value = "nodes"
      ElMessage.error("节点编码不能为空")
      return
    }
    if (nodeCodeSet.has(code)) {
      editorTab.value = "nodes"
      ElMessage.error(`节点编码重复：${code}`)
      return
    }
    nodeCodeSet.add(code)
  }

  if (!editableNodes.value.some(node => node.isStart === 1)) {
    editorTab.value = "nodes"
    ElMessage.error("请至少标记一个起始节点")
    return
  }

  if (editableOutputs.value.some(output => output.endNodeId === null)) {
    editorTab.value = "outputs"
    ElMessage.error("输出配置必须绑定结束节点")
    return
  }

  submitLoading.value = true
  try {
    const payload = buildFlowPayload()
    if (editorType.value === "create") {
      await createGestureFlow(payload)
      ElMessage.success("新增动作流成功")
    } else if (currentId.value !== null) {
      await updateGestureFlow(currentId.value, payload)
      ElMessage.success("修改动作流成功")
    }

    editorVisible.value = false
    await fetchList()
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row: GestureFlowItem) {
  await ElMessageBox.confirm(
    `确认删除动作流“${row.flowName}”吗？删除后节点和输出配置会一起移除。`,
    "删除确认",
    {
      type: "warning",
      confirmButtonText: "确认删除",
      cancelButtonText: "取消"
    }
  )

  await deleteGestureFlow(row.id)
  ElMessage.success("删除动作流成功")

  if (tableData.value.length === 1 && queryForm.pageNum > 1) {
    queryForm.pageNum -= 1
  }

  await fetchList()
}

function openCreateNode() {
  nodeDialogType.value = "create"
  nodeDialogVisible.value = true
  editingNodeId.value = null
  Object.assign(nodeForm, createDefaultNode())
  nextTick(() => {
    nodeFormRef.value?.clearValidate()
  })
}

function openEditNode(row: EditableNode) {
  const source = editableNodes.value.find(item => item.id === row.id)
  if (!source) return
  nodeDialogType.value = "edit"
  nodeDialogVisible.value = true
  editingNodeId.value = source.id
  Object.assign(nodeForm, { ...source })
  nextTick(() => {
    nodeFormRef.value?.clearValidate()
  })
}

async function handleSubmitNode() {
  if (!nodeFormRef.value) return
  await nodeFormRef.value.validate()

  const normalizedCode = nodeForm.nodeCode.trim()
  const duplicated = editableNodes.value.some(item => item.id !== nodeForm.id && item.nodeCode.trim() === normalizedCode)
  if (duplicated) {
    ElMessage.error(`节点编码重复：${normalizedCode}`)
    return
  }

  const payload: EditableNode = {
    id: nodeForm.id,
    nodeCode: normalizedCode,
    nodeName: nodeForm.nodeName.trim(),
    parentNodeId: nodeForm.parentNodeId ?? null,
    parentNodeCode: "",
    gestureLibraryId: nodeForm.gestureLibraryId,
    isStart: nodeForm.isStart,
    isEnd: nodeForm.isEnd,
    nodeOrder: nodeForm.nodeOrder,
    confidenceMin: nodeForm.confidenceMin !== null ? Number(nodeForm.confidenceMin) : null,
    holdMs: nodeForm.holdMs,
    debounceMs: nodeForm.debounceMs,
    cooldownMs: nodeForm.cooldownMs,
    requiredHits: nodeForm.requiredHits,
    maxIntervalMs: nodeForm.maxIntervalMs,
    resetOnFail: nodeForm.resetOnFail,
    allowRepeat: nodeForm.allowRepeat,
    successNextStrategy: nodeForm.successNextStrategy.trim(),
    failStrategy: nodeForm.failStrategy.trim(),
    remark: nodeForm.remark.trim()
  }

  if (nodeDialogType.value === "create") {
    editableNodes.value = [...editableNodes.value, payload]
    ElMessage.success("已新增节点")
  } else {
    editableNodes.value = editableNodes.value.map(item => (item.id === payload.id ? payload : item))
    ElMessage.success("已更新节点")
  }

  nodeDialogVisible.value = false
}

function handleDeleteNode(row: EditableNode) {
  const hasChildren = editableNodes.value.some(item => item.parentNodeId === row.id)
  if (hasChildren) {
    ElMessage.warning("请先删除或调整当前节点的子节点")
    return
  }

  const referencedByOutput = editableOutputs.value.some(item => item.endNodeId === row.id)
  if (referencedByOutput) {
    ElMessage.warning("请先删除或调整引用该节点的输出配置")
    return
  }

  editableNodes.value = editableNodes.value.filter(item => item.id !== row.id)
  ElMessage.success("已删除节点")
}

function openCreateOutput() {
  if (!endNodeOptions.value.length) {
    ElMessage.warning("请先在节点配置中标记至少一个结束节点")
    return
  }

  outputDialogType.value = "create"
  outputDialogVisible.value = true
  editingOutputId.value = null
  Object.assign(outputForm, createDefaultOutput(), {
    endNodeId: endNodeOptions.value[0]?.value ?? null
  })
  nextTick(() => {
    outputFormRef.value?.clearValidate()
  })
}

function openEditOutput(row: EditableOutput) {
  const source = editableOutputs.value.find(item => item.id === row.id)
  if (!source) return
  outputDialogType.value = "edit"
  outputDialogVisible.value = true
  editingOutputId.value = source.id
  Object.assign(outputForm, { ...source })
  nextTick(() => {
    outputFormRef.value?.clearValidate()
  })
}

async function handleSubmitOutput() {
  if (!outputFormRef.value) return
  await outputFormRef.value.validate()

  const payload: EditableOutput = {
    id: outputForm.id,
    endNodeId: outputForm.endNodeId ?? null,
    endNodeCode: "",
    outputType: outputForm.outputType,
    outputText: outputForm.outputText.trim(),
    phraseTemplateId: outputForm.outputType === "PHRASE" ? outputForm.phraseTemplateId : null,
    controlAction: outputForm.outputType === "CONTROL" ? outputForm.controlAction : "",
    ttsText: outputForm.ttsText.trim(),
    displayText: outputForm.displayText.trim()
  }

  if (outputDialogType.value === "create") {
    editableOutputs.value = [...editableOutputs.value, payload]
    ElMessage.success("已新增输出配置")
  } else {
    editableOutputs.value = editableOutputs.value.map(item => (item.id === payload.id ? payload : item))
    ElMessage.success("已更新输出配置")
  }

  outputDialogVisible.value = false
}

function handleDeleteOutput(row: EditableOutput) {
  editableOutputs.value = editableOutputs.value.filter(item => item.id !== row.id)
  ElMessage.success("已删除输出配置")
}

onMounted(async () => {
  await fetchList()
})
</script>

<template>
  <div class="app-container">
    <el-card shadow="never" class="search-card">
      <el-form :inline="true">
        <el-form-item label="关键词">
          <el-input
            v-model.trim="queryForm.keyword"
            placeholder="请输入动作流编码或名称"
            clearable
            style="width: 240px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>

        <el-form-item label="动作流类型">
          <el-select v-model="queryForm.flowType" placeholder="请选择类型" clearable style="width: 160px">
            <el-option v-for="item in flowTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>

        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable style="width: 160px">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>

        <el-form-item label="来源">
          <el-select v-model="queryForm.isBuiltin" placeholder="请选择来源" clearable style="width: 160px">
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
            新增动作流
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <template #header>
        <div class="table-header">
          <span>动作流管理</span>
          <span class="table-total">共 {{ total }} 条</span>
        </div>
      </template>

      <el-table v-loading="loading" :data="tableData" border>
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="flowCode" label="动作流编码" min-width="150" show-overflow-tooltip />
        <el-table-column prop="flowName" label="动作流名称" min-width="180" show-overflow-tooltip />
        <el-table-column label="类型" width="110" align="center">
          <template #default="{ row }">
            {{ getFlowTypeText(row.flowType) }}
          </template>
        </el-table-column>
        <el-table-column label="触发模式" width="110" align="center">
          <template #default="{ row }">
            {{ getTriggerModeText(row.triggerMode) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="来源" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getBuiltinTagType(row.isBuiltin)">
              {{ getBuiltinText(row.isBuiltin) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="90" align="center" />
        <el-table-column prop="versionNo" label="版本号" width="90" align="center" />
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
      v-model="editorVisible"
      :title="editorType === 'create' ? '新增动作流' : '编辑动作流'"
      width="1200px"
      top="4vh"
      destroy-on-close
    >
      <div v-loading="editorLoading">
        <el-tabs v-model="editorTab">
          <el-tab-pane label="基本信息" name="basic">
            <el-form ref="flowFormRef" :model="flowForm" :rules="flowFormRules" label-width="110px">
              <el-row :gutter="16">
                <el-col :span="12">
                  <el-form-item label="动作流编码" prop="flowCode">
                    <el-input
                      v-model.trim="flowForm.flowCode"
                      placeholder="请输入动作流编码"
                      maxlength="50"
                      show-word-limit
                    />
                  </el-form-item>
                </el-col>

                <el-col :span="12">
                  <el-form-item label="动作流名称" prop="flowName">
                    <el-input
                      v-model.trim="flowForm.flowName"
                      placeholder="请输入动作流名称"
                      maxlength="100"
                      show-word-limit
                    />
                  </el-form-item>
                </el-col>

                <el-col :span="12">
                  <el-form-item label="动作流类型" prop="flowType">
                    <el-select v-model="flowForm.flowType" placeholder="请选择动作流类型" style="width: 100%">
                      <el-option v-for="item in flowTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
                    </el-select>
                  </el-form-item>
                </el-col>

                <el-col :span="12">
                  <el-form-item label="触发模式" prop="triggerMode">
                    <el-select v-model="flowForm.triggerMode" placeholder="请选择触发模式" style="width: 100%">
                      <el-option
                        v-for="item in triggerModeOptions"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value"
                      />
                    </el-select>
                  </el-form-item>
                </el-col>

                <el-col :span="12">
                  <el-form-item label="优先级" prop="priority">
                    <el-input-number
                      v-model="flowForm.priority"
                      :min="0"
                      :max="9999"
                      controls-position="right"
                      style="width: 100%"
                    />
                  </el-form-item>
                </el-col>

                <el-col :span="12">
                  <el-form-item label="状态" prop="status">
                    <el-radio-group v-model="flowForm.status">
                      <el-radio :value="1">启用</el-radio>
                      <el-radio :value="0">停用</el-radio>
                    </el-radio-group>
                  </el-form-item>
                </el-col>

                <el-col :span="12">
                  <el-form-item label="是否内置" prop="isBuiltin">
                    <el-radio-group v-model="flowForm.isBuiltin">
                      <el-radio :value="1">内置</el-radio>
                      <el-radio :value="0">自定义</el-radio>
                    </el-radio-group>
                  </el-form-item>
                </el-col>
              </el-row>

              <el-form-item label="描述" prop="description">
                <el-input
                  v-model.trim="flowForm.description"
                  type="textarea"
                  :rows="4"
                  placeholder="请输入动作流描述，可为空"
                  maxlength="255"
                  show-word-limit
                />
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <el-tab-pane :label="`节点配置 (${editableNodes.length})`" name="nodes">
            <div class="toolbar">
              <el-button type="success" @click="openCreateNode">
                新增节点
              </el-button>
            </div>

            <el-table
              :data="editableNodeTree"
              border
              row-key="id"
              default-expand-all
              :tree-props="{ children: 'children' }"
            >
              <el-table-column prop="nodeCode" label="节点编码" min-width="140" show-overflow-tooltip />
              <el-table-column prop="nodeName" label="节点名称" min-width="160" show-overflow-tooltip />
              <el-table-column label="手势" min-width="180" show-overflow-tooltip>
                <template #default="{ row }">
                  {{ getGestureLabel(row.gestureLibraryId) }}
                </template>
              </el-table-column>
              <el-table-column label="起始" width="80" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.isStart === 1 ? 'success' : 'info'">
                    {{ row.isStart === 1 ? "是" : "否" }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="结束" width="80" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.isEnd === 1 ? 'warning' : 'info'">
                    {{ row.isEnd === 1 ? "是" : "否" }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="nodeOrder" label="顺序" width="80" align="center" />
              <el-table-column prop="confidenceMin" label="置信度" width="100" align="center" />
              <el-table-column prop="requiredHits" label="命中次数" width="100" align="center" />
              <el-table-column prop="successNextStrategy" label="成功策略" min-width="120" show-overflow-tooltip />
              <el-table-column prop="failStrategy" label="失败策略" min-width="120" show-overflow-tooltip />
              <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
              <el-table-column label="操作" fixed="right" width="180" align="center">
                <template #default="{ row }">
                  <el-button type="primary" link @click="openEditNode(row)">
                    编辑
                  </el-button>
                  <el-button type="danger" link @click="handleDeleteNode(row)">
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <el-tab-pane :label="`输出配置 (${editableOutputs.length})`" name="outputs">
            <div class="toolbar">
              <el-button type="success" @click="openCreateOutput">
                新增输出
              </el-button>
              <span class="toolbar-tip">输出配置默认绑定结束节点，请先在节点里勾选结束节点。</span>
            </div>

            <el-table :data="editableOutputs" border>
              <el-table-column label="结束节点" min-width="180" show-overflow-tooltip>
                <template #default="{ row }">
                  {{ getNodeLabelById(row.endNodeId) }}
                </template>
              </el-table-column>
              <el-table-column label="输出类型" width="110" align="center">
                <template #default="{ row }">
                  {{ getOutputTypeText(row.outputType) }}
                </template>
              </el-table-column>
              <el-table-column label="直接输出" min-width="160" show-overflow-tooltip>
                <template #default="{ row }">
                  {{ row.outputText || "-" }}
                </template>
              </el-table-column>
              <el-table-column label="短语模板" min-width="180" show-overflow-tooltip>
                <template #default="{ row }">
                  {{ getPhraseLabel(row.phraseTemplateId) }}
                </template>
              </el-table-column>
              <el-table-column label="控制动作" min-width="130" show-overflow-tooltip>
                <template #default="{ row }">
                  {{ getControlActionText(row.controlAction) }}
                </template>
              </el-table-column>
              <el-table-column prop="ttsText" label="播报文本" min-width="160" show-overflow-tooltip />
              <el-table-column prop="displayText" label="展示文本" min-width="160" show-overflow-tooltip />
              <el-table-column label="操作" fixed="right" width="180" align="center">
                <template #default="{ row }">
                  <el-button type="primary" link @click="openEditOutput(row)">
                    编辑
                  </el-button>
                  <el-button type="danger" link @click="handleDeleteOutput(row)">
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </div>

      <template #footer>
        <el-button @click="editorVisible = false">
          取消
        </el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          保存
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="nodeDialogVisible" title="节点配置" width="920px" destroy-on-close>
      <el-form ref="nodeFormRef" :model="nodeForm" :rules="nodeFormRules" label-width="120px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="节点编码" prop="nodeCode">
              <el-input v-model.trim="nodeForm.nodeCode" placeholder="请输入节点编码" maxlength="50" show-word-limit />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="节点名称" prop="nodeName">
              <el-input v-model.trim="nodeForm.nodeName" placeholder="请输入节点名称" maxlength="100" show-word-limit />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="父节点">
              <el-select
                v-model="nodeForm.parentNodeId"
                placeholder="不选则为根节点"
                clearable
                filterable
                style="width: 100%"
              >
                <el-option v-for="item in nodeParentOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="基础手势" prop="gestureLibraryId">
              <el-select v-model="nodeForm.gestureLibraryId" placeholder="请选择基础手势" filterable style="width: 100%">
                <el-option
                  v-for="item in gestureOptions"
                  :key="item.id"
                  :label="`${item.gestureName} (${item.gestureCode})`"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="节点顺序" prop="nodeOrder">
              <el-input-number
                v-model="nodeForm.nodeOrder"
                :min="1"
                :max="9999"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="最小置信度">
              <el-input-number
                v-model="nodeForm.confidenceMin"
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
            <el-form-item label="持续命中时长">
              <el-input-number
                v-model="nodeForm.holdMs"
                :min="0"
                :max="60000"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="防抖时间">
              <el-input-number
                v-model="nodeForm.debounceMs"
                :min="0"
                :max="60000"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="冷却时间">
              <el-input-number
                v-model="nodeForm.cooldownMs"
                :min="0"
                :max="60000"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="连续命中次数">
              <el-input-number
                v-model="nodeForm.requiredHits"
                :min="1"
                :max="999"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="最大间隔时间">
              <el-input-number
                v-model="nodeForm.maxIntervalMs"
                :min="0"
                :max="60000"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="起始节点">
          <el-radio-group v-model="nodeForm.isStart">
            <el-radio :value="1">是</el-radio>
            <el-radio :value="0">否</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="结束节点">
          <el-radio-group v-model="nodeForm.isEnd">
            <el-radio :value="1">是</el-radio>
            <el-radio :value="0">否</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="失败后重置">
          <el-radio-group v-model="nodeForm.resetOnFail">
            <el-radio :value="1">是</el-radio>
            <el-radio :value="0">否</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="允许重复触发">
          <el-radio-group v-model="nodeForm.allowRepeat">
            <el-radio :value="1">是</el-radio>
            <el-radio :value="0">否</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="成功策略">
          <el-input
            v-model.trim="nodeForm.successNextStrategy"
            placeholder="可选，按后端约定填写 successNextStrategy"
            maxlength="50"
          />
        </el-form-item>

        <el-form-item label="失败策略">
          <el-input
            v-model.trim="nodeForm.failStrategy"
            placeholder="可选，按后端约定填写 failStrategy"
            maxlength="50"
          />
        </el-form-item>

        <el-form-item label="备注">
          <el-input
            v-model.trim="nodeForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入节点备注，可为空"
            maxlength="255"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="nodeDialogVisible = false">
          取消
        </el-button>
        <el-button type="primary" @click="handleSubmitNode">
          确定
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="outputDialogVisible" title="输出配置" width="720px" destroy-on-close>
      <el-form ref="outputFormRef" :model="outputForm" :rules="outputFormRules" label-width="100px">
        <el-form-item label="结束节点" prop="endNodeId">
          <el-select v-model="outputForm.endNodeId" placeholder="请选择结束节点" filterable style="width: 100%">
            <el-option v-for="item in endNodeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>

        <el-form-item label="输出类型" prop="outputType">
          <el-select v-model="outputForm.outputType" placeholder="请选择输出类型" style="width: 100%">
            <el-option v-for="item in outputTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>

        <el-form-item v-if="outputForm.outputType === 'TEXT'" label="输出文本" prop="outputText">
          <el-input
            v-model.trim="outputForm.outputText"
            type="textarea"
            :rows="3"
            placeholder="请输入直接输出文本"
            maxlength="255"
            show-word-limit
          />
        </el-form-item>

        <el-form-item v-if="outputForm.outputType === 'PHRASE'" label="短语模板" prop="phraseTemplateId">
          <el-select v-model="outputForm.phraseTemplateId" placeholder="请选择短语模板" filterable style="width: 100%">
            <el-option
              v-for="item in phraseOptions"
              :key="item.id"
              :label="`${item.phraseText} (${item.phraseCode})`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item v-if="outputForm.outputType === 'CONTROL'" label="控制动作" prop="controlAction">
          <el-select v-model="outputForm.controlAction" placeholder="请选择控制动作" style="width: 100%">
            <el-option
              v-for="item in controlActionOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="播报文本">
          <el-input
            v-model.trim="outputForm.ttsText"
            placeholder="请输入播报文本，可为空"
            maxlength="255"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="展示文本">
          <el-input
            v-model.trim="outputForm.displayText"
            placeholder="请输入展示文本，可为空"
            maxlength="255"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="outputDialogVisible = false">
          取消
        </el-button>
        <el-button type="primary" @click="handleSubmitOutput">
          确定
        </el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="detailVisible" title="动作流详情" size="980px">
      <div v-loading="detailLoading">
        <template v-if="detailData">
          <el-tabs v-model="detailTab">
            <el-tab-pane label="基本信息" name="basic">
              <el-descriptions :column="2" border>
                <el-descriptions-item label="动作流 ID">
                  {{ detailData.id }}
                </el-descriptions-item>
                <el-descriptions-item label="版本号">
                  {{ detailData.versionNo }}
                </el-descriptions-item>
                <el-descriptions-item label="动作流编码">
                  {{ detailData.flowCode }}
                </el-descriptions-item>
                <el-descriptions-item label="动作流名称">
                  {{ detailData.flowName }}
                </el-descriptions-item>
                <el-descriptions-item label="动作流类型">
                  {{ getFlowTypeText(detailData.flowType) }}
                </el-descriptions-item>
                <el-descriptions-item label="触发模式">
                  {{ getTriggerModeText(detailData.triggerMode) }}
                </el-descriptions-item>
                <el-descriptions-item label="状态">
                  <el-tag :type="getStatusTagType(detailData.status)">
                    {{ getStatusText(detailData.status) }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="来源">
                  <el-tag :type="getBuiltinTagType(detailData.isBuiltin)">
                    {{ getBuiltinText(detailData.isBuiltin) }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="优先级">
                  {{ detailData.priority }}
                </el-descriptions-item>
                <el-descriptions-item label="起始节点">
                  {{ getDetailNodeLabelById(detailData.startNodeId) }}
                </el-descriptions-item>
                <el-descriptions-item label="描述" :span="2">
                  {{ detailData.description || "-" }}
                </el-descriptions-item>
                <el-descriptions-item label="创建时间">
                  {{ detailData.createTime || "-" }}
                </el-descriptions-item>
                <el-descriptions-item label="更新时间">
                  {{ detailData.updateTime || "-" }}
                </el-descriptions-item>
              </el-descriptions>
            </el-tab-pane>

            <el-tab-pane :label="`节点配置 (${detailData.nodeList.length})`" name="nodes">
              <el-table
                :data="detailNodeTree"
                border
                row-key="id"
                default-expand-all
                :tree-props="{ children: 'children' }"
              >
                <el-table-column prop="nodeCode" label="节点编码" min-width="140" show-overflow-tooltip />
                <el-table-column prop="nodeName" label="节点名称" min-width="160" show-overflow-tooltip />
                <el-table-column label="手势" min-width="180" show-overflow-tooltip>
                  <template #default="{ row }">
                    {{ row.gestureName ? `${row.gestureName} (${row.gestureCode})` : `手势 #${row.gestureLibraryId}` }}
                  </template>
                </el-table-column>
                <el-table-column label="起始" width="80" align="center">
                  <template #default="{ row }">
                    <el-tag :type="row.isStart === 1 ? 'success' : 'info'">
                      {{ row.isStart === 1 ? "是" : "否" }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="结束" width="80" align="center">
                  <template #default="{ row }">
                    <el-tag :type="row.isEnd === 1 ? 'warning' : 'info'">
                      {{ row.isEnd === 1 ? "是" : "否" }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="nodeOrder" label="顺序" width="80" align="center" />
                <el-table-column prop="confidenceMin" label="置信度" width="100" align="center" />
                <el-table-column prop="requiredHits" label="命中次数" width="100" align="center" />
                <el-table-column prop="successNextStrategy" label="成功策略" min-width="120" show-overflow-tooltip />
                <el-table-column prop="failStrategy" label="失败策略" min-width="120" show-overflow-tooltip />
                <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
              </el-table>
            </el-tab-pane>

            <el-tab-pane :label="`输出配置 (${detailData.outputList.length})`" name="outputs">
              <el-table :data="detailData.outputList" border>
                <el-table-column label="结束节点" min-width="180" show-overflow-tooltip>
                  <template #default="{ row }">
                    {{ row.endNodeName ? `${row.endNodeName} (${row.endNodeCode})` : "-" }}
                  </template>
                </el-table-column>
                <el-table-column label="输出类型" width="110" align="center">
                  <template #default="{ row }">
                    {{ getOutputTypeText(row.outputType) }}
                  </template>
                </el-table-column>
                <el-table-column prop="outputText" label="直接输出" min-width="160" show-overflow-tooltip />
                <el-table-column label="短语模板" min-width="180" show-overflow-tooltip>
                  <template #default="{ row }">
                    {{ row.phraseText || "-" }}
                  </template>
                </el-table-column>
                <el-table-column label="控制动作" min-width="130" show-overflow-tooltip>
                  <template #default="{ row }">
                    {{ getControlActionText(row.controlAction) }}
                  </template>
                </el-table-column>
                <el-table-column prop="ttsText" label="播报文本" min-width="160" show-overflow-tooltip />
                <el-table-column prop="displayText" label="展示文本" min-width="160" show-overflow-tooltip />
              </el-table>
            </el-tab-pane>
          </el-tabs>

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

.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.toolbar-tip {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.drawer-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
