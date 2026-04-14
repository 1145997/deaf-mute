import type { RouteRecordRaw } from "vue-router"
import { createRouter } from "vue-router"
import { routerConfig } from "@/router/config"
import { registerNavigationGuard } from "@/router/guard"
import { flatMultiLevelRoutes } from "./helper"

const Layouts = () => import("@/layouts/index.vue")

/**
 * @name 常驻路由
 * @description 除了 redirect/403/404/login 等隐藏页面，其他页面建议设置唯一的 Name 属性
 */
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
    path: "/lostfound",
    component: Layouts,
    redirect: "/lostfound/pending",
    name: "LostFound",
    meta: {
      title: "失物招领管理",
      elIcon: "Search"
    },
    children: [
      {
        path: "pending",
        component: () => import("@/pages/lostfound/pending.vue"),
        name: "LostFoundPending",
        meta: {
          title: "待审核信息"
        }
      },
      {
        path: "list",
        component: () => import("@/pages/lostfound/list.vue"),
        name: "LostFoundList",
        meta: {
          title: "信息管理"
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
  }

]

/**
 * @name 动态路由
 * @description 用来放置有权限 (Roles 属性) 的路由
 * @description 必须带有唯一的 Name 属性
 */
export const dynamicRoutes: RouteRecordRaw[] = []

/** 路由实例 */
export const router = createRouter({
  history: routerConfig.history,
  routes: routerConfig.thirdLevelRouteCache ? flatMultiLevelRoutes(constantRoutes) : constantRoutes
})

/** 重置路由 */
export function resetRouter() {
  try {
    // 注意：所有动态路由路由必须带有 Name 属性，否则可能会不能完全重置干净
    router.getRoutes().forEach((route) => {
      const { name, meta } = route
      if (name && meta.roles?.length) {
        router.hasRoute(name) && router.removeRoute(name)
      }
    })
  } catch {
    // 强制刷新浏览器也行，只是交互体验不是很好
    location.reload()
  }
}

// 注册路由导航守卫
registerNavigationGuard(router)
