<script lang="ts" setup>
import type { FormRules } from "element-plus"
import ThemeSwitch from "@@/components/ThemeSwitch/index.vue"
import { Lock, User } from "@element-plus/icons-vue"
import { useSettingsStore } from "@/pinia/stores/settings"
import { useUserStore } from "@/pinia/stores/user"
import { loginApi } from "./apis"
import { usePermissionStore } from "@/pinia/stores/permission"
import { routerConfig } from "@/router/config"


const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const settingsStore = useSettingsStore()
const permissionStore = usePermissionStore()

const loginFormRef = useTemplateRef("loginFormRef")
const loading = ref(false)

const loginFormData = reactive({
  username: "admin",
  password: "admin123"
})

const loginFormRules: FormRules = {
  username: [
    { required: true, message: "请输入用户名", trigger: "blur" }
  ],
  password: [
    { required: true, message: "请输入密码", trigger: "blur" },
    { min: 6, max: 20, message: "长度在 6 到 20 个字符", trigger: "blur" }
  ]
}

function handleLogin() {
  loginFormRef.value?.validate((valid) => {
    if (!valid) {
      ElMessage.error("表单校验不通过")
      return
    }

    loading.value = true
    loginApi(loginFormData)
      .then(async ({ data }) => {
        userStore.setToken(data.token)

        await userStore.getInfo()

        const roles = userStore.roles
        if (routerConfig.dynamic) {
          permissionStore.setRoutes(roles)
        } else {
          permissionStore.setAllRoutes()
        }

        permissionStore.addRoutes.forEach(route => {
          if (!router.hasRoute(route.name!)) {
            router.addRoute(route)
          }
        })

        await router.replace(route.query.redirect ? decodeURIComponent(route.query.redirect as string) : "/")
      })
      .catch(() => {
        loginFormData.password = ""
      })
      .finally(() => {
        loading.value = false
      })
  })
}
</script>

  <template>
    <div class="login-container">
      <div class="login-card">
        <div class="title">
          智能手语翻译工具后台管理系统
        </div>
        <div class="content">
          <el-form ref="loginFormRef" :model="loginFormData" :rules="loginFormRules" @keyup.enter="handleLogin">
            <el-form-item prop="username">
              <el-input
                v-model.trim="loginFormData.username"
                placeholder="用户名"
                type="text"
                tabindex="1"
                :prefix-icon="User"
                size="large"
              />
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                v-model.trim="loginFormData.password"
                placeholder="密码"
                type="password"
                tabindex="2"
                :prefix-icon="Lock"
                size="large"
                show-password
              />
            </el-form-item>

            <el-button :loading="loading" type="primary" size="large" @click.prevent="handleLogin">
              登 录
            </el-button>
          </el-form>
        </div>
      </div>
    </div>
  </template>

<style lang="scss" scoped>
.login-container {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  width: 100%;
  min-height: 100%;
  .theme-switch {
    position: fixed;
    top: 5%;
    right: 5%;
    cursor: pointer;
  }
  .login-card {
    width: 480px;
    max-width: 90%;
    border-radius: 20px;
    box-shadow: 0 0 10px #dcdfe6;
    background-color: var(--el-bg-color);
    overflow: hidden;
    .title {
      display: flex;
      justify-content: center;
      align-items: center;
      height: 150px;
      img {
        height: 100%;
      }
    }
    .content {
      padding: 20px 50px 50px 50px;
      .el-button {
        width: 100%;
        margin-top: 10px;
      }
    }
  }
}
</style>
