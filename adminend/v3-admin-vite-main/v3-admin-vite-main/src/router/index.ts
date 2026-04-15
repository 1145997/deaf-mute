import type { RouteRecordRaw } from "vue-router"
import { createRouter } from "vue-router"
import { routerConfig } from "@/router/config"
import { registerNavigationGuard } from "@/router/guard"
import { flatMultiLevelRoutes } from "./helper"

const Layouts = () => import("@/layouts/index.vue")

/** 常驻路由 */
export const constantRoutes: RouteRecordRaw[] = [
  {
    path: "/redirect",
    component: Layouts,
    meta: {
      hidden: true
    },
    children: [
      {
        path: ":path(.*)",
        component: () => import("@/pages/redirect/index.vue")
      }
    ]
  },
  {
    path: "/403",
    component: () => import("@/pages/error/403.vue"),
    meta: {
      hidden: true
    }
  },
  {
    path: "/404",
    component: () => import("@/pages/error/404.vue"),
    meta: {
      hidden: true
    },
    alias: "/:pathMatch(.*)*"
  },
  {
    path: "/login",
    component: () => import("@/pages/login/index.vue"),
    meta: {
      hidden: true
    }
  },
  {
    path: "/",
    component: Layouts,
    redirect: "/dashboard",
    children: [
      {
        path: "dashboard",
        component: () => import("@/pages/dashboard/index.vue"),
        name: "Dashboard",
        meta: {
          title: "首页",
          svgIcon: "dashboard",
          affix: true
        }
      }
    ]
  },
  {
    path: "/post",
    component: Layouts,
    redirect: "/post/pending",
    name: "Post",
    meta: {
      title: "帖子管理",
      elIcon: "Document"
    },
    children: [
      {
        path: "pending",
        component: () => import("@/pages/post/pending.vue"),
        name: "PostPending",
        meta: {
          title: "帖子审核"
        }
      },
      {
        path: "list",
        component: () => import("@/pages/post/list.vue"),
        name: "PostList",
        meta: {
          title: "帖子列表"
        }
      }
    ]
  },
  {
    path: "/category",
    component: Layouts,
    redirect: "/category/list",
    name: "Category",
    meta: {
      title: "分类管理",
      elIcon: "Collection"
    },
    children: [
      {
        path: "list",
        component: () => import("@/pages/category/list.vue"),
        name: "CategoryList",
        meta: {
          title: "分类列表"
        }
      }
    ]
  },
  {
    path: "/comment",
    component: Layouts,
    redirect: "/comment/list",
    name: "Comment",
    meta: {
      title: "评论管理",
      elIcon: "ChatDotRound"
    },
    children: [
      {
        path: "list",
        component: () => import("@/pages/comment/list.vue"),
        name: "CommentList",
        meta: {
          title: "评论列表"
        }
      }
    ]
  },
  {
    path: "/user",
    component: Layouts,
    redirect: "/user/list",
    name: "User",
    meta: {
      title: "用户管理",
      elIcon: "User"
    },
    children: [
      {
        path: "list",
        component: () => import("@/pages/user/list.vue"),
        name: "UserList",
        meta: {
          title: "用户列表"
        }
      }
    ]
  },
  {
    path: "/notice",
    component: Layouts,
    redirect: "/notice/list",
    name: "Notice",
    meta: {
      title: "公告管理",
      elIcon: "Bell"
    },
    children: [
      {
        path: "list",
        component: () => import("@/pages/notice/list.vue"),
        name: "NoticeList",
        meta: {
          title: "公告列表"
        }
      }
    ]
  },
  {
    path: "/recognition",
    component: Layouts,
    redirect: "/recognition/gesture-library",
    name: "Recognition",
    meta: {
      title: "识别配置",
      elIcon: "SetUp"
    },
    children: [
      {
        path: "gesture-library",
        component: () => import("@/pages/gesture-library/list.vue"),
        name: "GestureLibraryList",
        meta: {
          title: "基础手势库"
        }
      },
      {
        path: "phrase-template",
        component: () => import("@/pages/phrase-template/list.vue"),
        name: "PhraseTemplateList",
        meta: {
          title: "短语模板"
        }
      },
      {
        path: "recognition-config",
        component: () => import("@/pages/recognition-config/list.vue"),
        name: "RecognitionConfigList",
        meta: {
          title: "全局识别配置"
        }
      },
      {
        path: "gesture-flow",
        component: () => import("@/pages/gesture-flow/list.vue"),
        name: "GestureFlowList",
        meta: {
          title: "动作流管理"
        }
      }
    ]
  }
]

/** 动态路由 */
export const dynamicRoutes: RouteRecordRaw[] = []

/** 路由实例 */
export const router = createRouter({
  history: routerConfig.history,
  routes: routerConfig.thirdLevelRouteCache ? flatMultiLevelRoutes(constantRoutes) : constantRoutes
})

/** 重置路由 */
export function resetRouter() {
  try {
    router.getRoutes().forEach((route) => {
      const { name, meta } = route
      if (name && meta.roles?.length) {
        router.hasRoute(name) && router.removeRoute(name)
      }
    })
  } catch {
    location.reload()
  }
}

registerNavigationGuard(router)
