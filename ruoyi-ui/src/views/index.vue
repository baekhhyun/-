<!-- ==================== 文件路径：ruoyi-ui/src/views/dashboard/index.vue ==================== -->

<template>
  <div>
    <!-- 根据角色显示不同首页 -->
    <admin-dashboard v-if="isAdmin" />
    <user-dashboard v-else />
  </div>
</template>

<script>
import { mapGetters } from "vuex";
import AdminDashboard from "./vaccineAdmin.vue";
import UserDashboard from "./vaccineUser.vue";

export default {
  name: "Dashboard",
  components: {
    AdminDashboard,
    UserDashboard,
  },
  computed: {
    ...mapGetters(["roles", "name"]),
    // 判断是否是管理员
    isAdmin() {
      console.log("当前用户角色:", this.roles);

      // 处理不同格式的roles
      if (Array.isArray(this.roles)) {
        return (
          this.roles.includes("admin") ||
          this.roles.includes("vaccineAdmin") ||
          this.roles.includes("管理员")
        );
      } else if (typeof this.roles === "string") {
        return (
          this.roles === "admin" ||
          this.roles === "vaccineAdmin" ||
          this.roles === "管理员"
        );
      }
      return false;
    },
  },
  created() {
    console.log("当前用户:", this.name);
    console.log("是否管理员:", this.isAdmin);
  },
};
</script>
