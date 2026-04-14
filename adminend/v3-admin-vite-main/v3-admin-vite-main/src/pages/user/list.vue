<script setup lang="ts">
import {
  getUserDetail,
  getUserList,
  updateUserStatus,
  type UserDetail,
  type UserItem
} from "@@/apis/user"

defineOptions({
  name: "UserList"
})

const loading = ref(false)
const detailLoading = ref(false)
const actionLoadingId = ref<number | null>(null)

const tableData = ref<UserItem[]>([])
const total = ref(0)

const queryForm = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: "",
  status: undefined as number | undefined
})

const detailVisible = ref(false)
const detailData = ref<UserDetail | null>(null)

function getStatusText(status: number) {
  return status === 1 ? "启用" : "禁用"
}

function getStatusTagType(status: number) {
  return status === 1 ? "success" : "info"
}

function getGenderText(gender: number | null) {
  const map: Record<number, string> = {
    1: "男",
    2: "女"
  }
  if (gender === null || gender === undefined) return "-"
  return map[gender] || "未知"
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getUserList({
      pageNum: queryForm.pageNum,
      pageSize: queryForm.pageSize,
      keyword: queryForm.keyword || undefined,
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

async function handleView(row: UserItem) {
  detailVisible.value = true
  detailLoading.value = true
  try {
    const res = await getUserDetail(row.id)
    detailData.value = res.data
  } finally {
    detailLoading.value = false
  }
}

async function handleToggleStatus(row: UserItem) {
  const nextStatus = row.status === 1 ? 0 : 1
  const actionText = nextStatus === 1 ? "启用" : "禁用"

  await ElMessageBox.confirm(
    `确认${actionText}用户「${row.nickname}」吗？`,
    `${actionText}确认`,
    {
      type: "warning",
      confirmButtonText: `确认${actionText}`,
      cancelButtonText: "取消"
    }
  )

  actionLoadingId.value = row.id
  try {
    await updateUserStatus(row.id, nextStatus as 0 | 1)
    ElMessage.success(`用户${actionText}成功`)
    await fetchList()

    if (detailVisible.value && detailData.value?.id === row.id) {
      detailData.value.status = nextStatus
    }
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
            placeholder="请输入昵称 / openid / 学号 / 手机号"
            clearable
            style="width: 260px"
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
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <template #header>
        <div class="table-header">
          <span>用户管理</span>
          <span class="table-total">共 {{ total }} 条</span>
        </div>
      </template>

      <el-table v-loading="loading" :data="tableData" border>
        <el-table-column prop="id" label="用户 ID" width="90" align="center" />
        <el-table-column label="头像" width="90" align="center">
          <template #default="{ row }">
            <el-avatar :src="row.avatar" :size="40">
              {{ row.nickname?.slice(0, 1) || "U" }}
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="nickname" label="昵称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="openid" label="OpenID" min-width="220" show-overflow-tooltip />
        <el-table-column prop="realName" label="真实姓名" width="110" align="center">
          <template #default="{ row }">
            {{ row.realName || "-" }}
          </template>
        </el-table-column>
        <el-table-column prop="studentNo" label="学号" width="130" align="center">
          <template #default="{ row }">
            {{ row.studentNo || "-" }}
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" min-width="140" align="center">
          <template #default="{ row }">
            {{ row.phone || "-" }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginTime" label="最后登录时间" min-width="180">
          <template #default="{ row }">
            {{ row.lastLoginTime || "-" }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="180" />
        <el-table-column label="操作" fixed="right" width="220" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">
              查看详情
            </el-button>

            <el-button
              :type="row.status === 1 ? 'danger' : 'success'"
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

    <el-drawer v-model="detailVisible" title="用户详情" size="620px">
      <div v-loading="detailLoading">
        <template v-if="detailData">
          <div class="user-profile">
            <el-avatar :src="detailData.avatar" :size="72">
              {{ detailData.nickname?.slice(0, 1) || "U" }}
            </el-avatar>
            <div class="user-profile__info">
              <div class="user-profile__name">{{ detailData.nickname || "-" }}</div>
              <div class="user-profile__sub">用户 ID：{{ detailData.id }}</div>
            </div>
          </div>

          <el-descriptions :column="2" border>
            <el-descriptions-item label="用户ID">
              {{ detailData.id }}
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="getStatusTagType(detailData.status)">
                {{ getStatusText(detailData.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="昵称">
              {{ detailData.nickname || "-" }}
            </el-descriptions-item>
            <el-descriptions-item label="OpenID">
              {{ detailData.openid || "-" }}
            </el-descriptions-item>
            <el-descriptions-item label="真实姓名">
              {{ detailData.realName || "-" }}
            </el-descriptions-item>
            <el-descriptions-item label="学号">
              {{ detailData.studentNo || "-" }}
            </el-descriptions-item>
            <el-descriptions-item label="手机号">
              {{ detailData.phone || "-" }}
            </el-descriptions-item>
            <el-descriptions-item label="邮箱">
              {{ detailData.email || "-" }}
            </el-descriptions-item>
            <el-descriptions-item label="性别">
              {{ getGenderText(detailData.gender) }}
            </el-descriptions-item>
            <el-descriptions-item label="最后登录时间">
              {{ detailData.lastLoginTime || "-" }}
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
            <el-button
              :type="detailData.status === 1 ? 'danger' : 'success'"
              :loading="actionLoadingId === detailData.id"
              @click="handleToggleStatus(detailData)"
            >
              {{ detailData.status === 1 ? "禁用用户" : "启用用户" }}
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

.user-profile {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;

  &__name {
    font-size: 20px;
    font-weight: 600;
    color: var(--el-text-color-primary);
  }

  &__sub {
    margin-top: 6px;
    font-size: 13px;
    color: var(--el-text-color-secondary);
  }
}

.drawer-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
}
</style>
