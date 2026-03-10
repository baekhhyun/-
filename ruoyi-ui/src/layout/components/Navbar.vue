<template>
  <div class="navbar" :class="'nav' + navType">
    <hamburger
      id="hamburger-container"
      :is-active="sidebar.opened"
      class="hamburger-container"
      @toggleClick="toggleSideBar"
    />

    <breadcrumb
      v-if="navType == 1"
      id="breadcrumb-container"
      class="breadcrumb-container"
    />
    <top-nav
      v-if="navType == 2"
      id="topmenu-container"
      class="topmenu-container"
    />
    <template v-if="navType == 3">
      <logo v-show="showLogo" :collapse="false"></logo>
      <top-bar id="topbar-container" class="topbar-container" />
    </template>
    <div class="right-menu">
      <div
        v-if="isVaccineAdmin"
        class="custom-notification"
        @click="goToPendingList"
      >
        <i class="el-icon-bell"></i>
        <span v-if="pendingCount > 0" class="notification-badge">
          {{ pendingCount > 99 ? "99+" : pendingCount }}
        </span>
      </div>
      <template v-if="device !== 'mobile'">
        <search id="header-search" class="right-menu-item" />
      </template>

      <el-dropdown
        class="avatar-container right-menu-item hover-effect"
        trigger="hover"
      >
        <div class="avatar-wrapper">
          <img :src="avatar" class="user-avatar" />
          <span class="user-nickname"> {{ nickName }} </span>
        </div>
        <el-dropdown-menu slot="dropdown">
          <router-link to="/user/profile">
            <el-dropdown-item>个人中心</el-dropdown-item>
          </router-link>
          <el-dropdown-item @click.native="setLayout" v-if="setting">
            <span>布局设置</span>
          </el-dropdown-item>
          <el-dropdown-item divided @click.native="logout">
            <span>退出登录</span>
          </el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
    </div>
  </div>
</template>

<script>
import { mapGetters } from "vuex";
import Breadcrumb from "@/components/Breadcrumb";
import TopNav from "@/components/TopNav";
import TopBar from "./TopBar";
import Logo from "./Sidebar/Logo";
import Hamburger from "@/components/Hamburger";
import Screenfull from "@/components/Screenfull";
import SizeSelect from "@/components/SizeSelect";
import Search from "@/components/HeaderSearch";
import RuoYiGit from "@/components/RuoYi/Git";
import RuoYiDoc from "@/components/RuoYi/Doc";
import { getPendingCount } from "@/api/vaccine/appointment";

export default {
  emits: ["setLayout"],
  components: {
    Breadcrumb,
    Logo,
    TopNav,
    TopBar,
    Hamburger,
    Screenfull,
    SizeSelect,
    Search,
    RuoYiGit,
    RuoYiDoc,
  },
  data() {
    return {
      pendingCount: 0,
      timer: null,
    };
  },
  computed: {
    ...mapGetters(["sidebar", "avatar", "device", "nickName", "roles"]),
    isVaccineAdmin() {
      // 打印出来看看roles到底是什么
      console.log("当前用户roles:", this.roles);

      // roles可能是数组，也可能是字符串
      if (Array.isArray(this.roles)) {
        return this.roles.includes("vaccineAdmin");
      } else if (typeof this.roles === "string") {
        return this.roles === "vaccineAdmin";
      }
      return false;
    },
    setting: {
      get() {
        return this.$store.state.settings.showSettings;
      },
    },
    navType: {
      get() {
        return this.$store.state.settings.navType;
      },
    },
    showLogo: {
      get() {
        return this.$store.state.settings.sidebarLogo;
      },
    },
  },
  created() {
    // 如果是管理员，启动定时器

    if (this.isVaccineAdmin) {
      console.log("疫苗管理员已登录，启动小红点功能");
      this.loadPendingCount();
      // 每30秒刷新一次
      this.timer = setInterval(this.loadPendingCount, 30000);
    }
  },
  beforeDestroy() {
    // 组件销毁时清除定时器
    if (this.timer) {
      clearInterval(this.timer);
    }
  },
  methods: {
    loadPendingCount() {
      getPendingCount()
        .then((response) => {
          if (response.code === 200) {
            this.pendingCount = response.data || 0;
            console.log("待处理预约数:", this.pendingCount);
          }
        })
        .catch((error) => {
          console.error("获取待处理数量失败", error);
        });
    },

    // 点击小红点跳转到待确认列表
    goToPendingList() {
      this.$router.push({
        name: "VaccineAppointments",
        query: { status: "0" }, // 直接筛选待确认状态
      });
    },
    toggleSideBar() {
      this.$store.dispatch("app/toggleSideBar");
    },
    setLayout(event) {
      this.$emit("setLayout");
    },
    logout() {
      this.$confirm("确定注销并退出系统吗？", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(() => {
          this.$store.dispatch("LogOut").then(() => {
            location.href = "/index";
          });
        })
        .catch(() => {});
    },
  },
};
</script>

<style lang="scss" scoped>
.pending-badge {
  margin-right: 20px;
  cursor: pointer;
  display: flex;
  align-items: center; /* 垂直居中 */
  justify-content: center; /* 水平居中 */
  height: 25px; /* 与导航栏同高 */
  position: relative;
}

.pending-badge i {
  font-size: 20px;
  color: #606266;
  transition: color 0.3s;
  line-height: 1; /* 防止行高影响 */
}
.pending-badge .el-badge__content.is-fixed {
  /* 调整小红点位置 */
  right: 5px !important;
}

.pending-badge i:hover {
  color: #409eff;
}
/* 小红点动画（可选） */
.el-badge {
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
  100% {
    transform: scale(1);
  }
}
.navbar.nav3 {
  .hamburger-container {
    display: none !important;
  }
}

.navbar {
  height: 50px;
  overflow: hidden;
  position: relative;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  align-items: center;
  // padding: 0 8px;
  box-sizing: border-box;

  .hamburger-container {
    line-height: 46px;
    height: 100%;
    cursor: pointer;
    transition: background 0.3s;
    -webkit-tap-highlight-color: transparent;
    display: flex;
    align-items: center;
    flex-shrink: 0;
    margin-right: 8px;

    &:hover {
      background: rgba(0, 0, 0, 0.025);
    }
  }

  .breadcrumb-container {
    flex-shrink: 0;
  }

  .topmenu-container {
    position: absolute;
    left: 50px;
  }

  .topbar-container {
    flex: 1;
    min-width: 0;
    display: flex;
    align-items: center;
    overflow: hidden;
    margin-left: 8px;
  }

  .errLog-container {
    display: inline-block;
    vertical-align: top;
  }

  .right-menu {
    height: 100%;
    line-height: 50px;
    display: flex;
    align-items: center;
    margin-left: auto;

    &:focus {
      outline: none;
    }

    .right-menu-item {
      display: inline-block;
      padding: 0 8px;
      height: 100%;
      font-size: 18px;
      color: #5a5e66;
      vertical-align: text-bottom;

      &.hover-effect {
        cursor: pointer;
        transition: background 0.3s;

        &:hover {
          background: rgba(0, 0, 0, 0.025);
        }
      }
    }

    .avatar-container {
      margin-right: 0px;
      padding-right: 0px;

      .avatar-wrapper {
        margin-top: 10px;
        right: 8px;
        position: relative;

        .user-avatar {
          cursor: pointer;
          width: 30px;
          height: 30px;
          border-radius: 50%;
        }

        .user-nickname {
          position: relative;
          bottom: 10px;
          left: 2px;
          font-size: 14px;
          font-weight: bold;
        }

        .el-icon-caret-bottom {
          cursor: pointer;
          position: absolute;
          right: -20px;
          top: 25px;
          font-size: 12px;
        }
      }
    }
    .custom-notification {
      position: relative;
      height: 100%; /* 与导航栏同高 */
      display: flex;
      align-items: center; /* 垂直居中 */
      justify-content: center;
      margin-right: 5px;
      cursor: pointer;
      padding: 0 5px; /* 增加点击区域 */
    }

    .custom-notification i {
      font-size: 22px;
      color: #606266;
      transition: all 0.3s;
      line-height: 1; /* 防止行高影响 */
    }

    .custom-notification:hover i {
      color: #409eff;
      transform: scale(1.1);
    }

    /* ========== 小红点（完全自定义）========== */
    .notification-badge {
      position: absolute;
      top: 8px; /* 控制上下位置 */
      right: -2px; /* 控制左右位置 */
      min-width: 18px;
      height: 18px;
      padding: 0 4px;
      background: #f56c6c;
      /* background: linear-gradient(135deg, #f56c6c, #f78989);  /* 渐变效果 */
      border: 2px solid #fff; /* 白色边框 */
      border-radius: 18px;
      color: white;
      font-size: 10px;
      font-weight: bold;
      line-height: 14px; /* 文字垂直居中 */
      text-align: center;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
      animation: pulse 2s infinite; /* 脉冲动画 */
    }

    /* 脉冲动画 */
    @keyframes pulse {
      0% {
        transform: scale(1);
      }
      50% {
        transform: scale(1.1);
      }
      100% {
        transform: scale(1);
      }
    }

    /* 没有数字时的红点（可选） */
    .notification-dot {
      position: absolute;
      top: 10px;
      right: 2px;
      width: 8px;
      height: 8px;
      background: #f56c6c;
      border: 2px solid #fff;
      border-radius: 50%;
    }
  }
}
</style>
