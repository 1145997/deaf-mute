import { getAdminInfoApi, logoutApi } from "@/pages/login/apis"
import { setToken as _setToken, getToken, removeToken } from "@@/utils/local-storage"
import { pinia } from "@/pinia"
import { routerConfig } from "@/router/config"
import { useSettingsStore } from "./settings"
import { useTagsViewStore } from "./tags-view"
import { router, resetRouter } from "@/router"

export const useUserStore = defineStore("user", () => {
  const token = ref<string>(getToken() || "")
  const roles = ref<string[]>([])
  const username = ref<string>("")
  const nickname = ref<string>("")

  const tagsViewStore = useTagsViewStore()
  const settingsStore = useSettingsStore()

  const setToken = (value: string) => {
    _setToken(value)
    token.value = value
  }

  const getInfo = async () => {
    const { data } = await getAdminInfoApi()
    username.value = data.username
    nickname.value = data.nickname || data.username
    roles.value = data.role ? [data.role] : routerConfig.defaultRoles
  }

  const logout = async () => {
    try {
      await logoutApi()
    } catch {
      // 后端退出失败也不阻塞前端清理
    }
    removeToken()
    token.value = ""
    roles.value = []
    resetRouter()
    resetTagsView()
    await router.replace("/login")
  }

  const resetToken = () => {
    removeToken()
    token.value = ""
    roles.value = []
    username.value = ""
    nickname.value = ""
  }

  const resetTagsView = () => {
    if (!settingsStore.cacheTagsView) {
      tagsViewStore.delAllVisitedViews()
      tagsViewStore.delAllCachedViews()
    }
  }

  return { token, roles, username, nickname, setToken, getInfo, logout, resetToken }
})

export function useUserStoreOutside() {
  return useUserStore(pinia)
}
