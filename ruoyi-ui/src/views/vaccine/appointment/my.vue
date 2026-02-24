// 在my.vue中调整显示逻辑
<template>
  <div>
    <el-table :data="appointmentList" v-loading="loading">
      <el-table-column label="疫苗信息" align="center" prop="vaccineName" />
      <el-table-column
        label="预约日期"
        align="center"
        prop="appointmentDate"
        width="120"
      >
        <template slot-scope="scope">
          {{ parseDate(scope.row.appointmentDate) }}
        </template>
      </el-table-column>
      <el-table-column
        label="时间段"
        align="center"
        prop="timeSlot"
        width="120"
      >
        <template slot-scope="scope">
          {{ formatTimeSlot(scope.row.timeSlot) }}
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template slot-scope="scope">
          <el-tag :type="formatStatus(scope.row.status).type">
            {{ formatStatus(scope.row.status).text }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="150">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="danger"
            @click="handleCancel(scope.row)"
            v-if="scope.row.status === '0' || scope.row.status === '1'"
          >
            取消
          </el-button>
          <el-button size="mini" type="info" disabled v-else>
            暂无操作
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
import {
  listMyAppointment,
  cancelAppointment,
} from "@/api/vaccine/appointment";

export default {
  data() {
    return {
      appointmentList: [],
      loading: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
      },
      total: 0,
    };
  },
  created() {
    this.getList();
  },
  methods: {
    getList() {
      this.loading = true;
      listMyAppointment(this.queryParams)
        .then((response) => {
          this.appointmentList = response.rows;
          this.total = response.total;
          this.loading = false;
        })
        .catch(() => {
          this.loading = false;
        });
    },

    // 取消预约
    handleCancel(row) {
      // 格式化日期
      const formatDate = (date) => {
        if (!date) return "";
        // 如果日期是 "2026-02-24" 格式
        if (typeof date === "string") {
          return date;
        }
        // 如果是Date对象
        return date.toISOString().split("T")[0];
      };

      // 格式化时间段
      const formatTimeSlot = (slot) => {
        const slotMap = {
          1: "上午",
          2: "下午",
          3: "晚上",
          morning: "上午",
          afternoon: "下午",
          evening: "晚上",
        };
        return slotMap[slot] || slot;
      };

      // 获取预约日期
      const appointmentDate = formatDate(row.appointmentDate);
      const timeSlot = formatTimeSlot(row.timeSlot);

      // ⭐️ 自定义提示信息
      const message = `确定要取消 ${appointmentDate}${timeSlot}的预约吗？`;

      this.$confirm(message, "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(() => {
          cancelAppointment(row.id).then((response) => {
            if (response.code === 200) {
              this.$message.success("取消成功");
              this.getList();
            } else {
              this.$message.error(response.msg);
            }
          });
        })
        .catch(() => {
          // 用户点击取消，什么都不做
          console.log("取消操作已取消");
        });
    },

    formatStatus(status) {
      const statusMap = {
        0: { text: "待确认", type: "warning" },
        1: { text: "已确认", type: "success" },
        2: { text: "已完成", type: "info" },
        3: { text: "已取消", type: "danger" },
      };
      return statusMap[status] || { text: "未知", type: "info" };
    },

    formatTimeSlot(slot) {
      const slotMap = {
        1: "上午",
        2: "下午",
        3: "晚上",
      };
      return slotMap[slot] || slot;
    },

    parseDate(dateStr) {
      if (!dateStr) return "";
      return dateStr.substring(0, 10); // 只取年月日
    },
  },
};
</script>
