<template>
  <div class="app-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2 class="page-title">我的预约</h2>
      <el-button type="primary" @click="goToVaccineList" size="small">
        <i class="el-icon-plus"></i> 继续预约
      </el-button>
    </div>

    <!--  待接种提醒卡片 -->
    <el-card
      class="pending-reminder-card"
      shadow="hover"
      v-if="pendingDoses.length > 0"
    >
      <div slot="header" class="reminder-header">
        <div class="header-left">
          <i class="el-icon-bell"></i>
          <span>待接种提醒</span>
          <el-badge :value="pendingDoses.length" class="badge" />
        </div>
        <el-button type="text" @click="goToVaccineList" class="header-btn">
          立即预约 <i class="el-icon-arrow-right"></i>
        </el-button>
      </div>

      <div class="reminder-list">
        <div
          v-for="(item, index) in pendingDoses"
          :key="index"
          class="reminder-item"
          :class="{
            'reminder-urgent': isUrgent(item),
            'reminder-normal': !isUrgent(item),
          }"
        >
          <div class="reminder-icon">
            <i :class="getReminderIcon(item)"></i>
          </div>
          <div class="reminder-content">
            <div class="reminder-title">
              <span class="vaccine-name">{{ item.vaccineName }}</span>
              <el-tag
                :type="item.status === 'pending' ? 'warning' : 'info'"
                size="small"
                effect="light"
              >
                {{ getDoseText(item) }}
              </el-tag>
            </div>
            <div class="reminder-info">
              <span class="info-item">
                <i class="el-icon-date"></i>
                <!--  根据是否有预约日期显示不同文案 -->
                <span v-if="item.appointmentDate">
                  预约日期：{{ formatDate(item.appointmentDate) }}
                </span>
                <span v-else-if="item.nextDoseDate">
                  建议接种日期：{{ formatDate(item.nextDoseDate) }}
                </span>
                <span v-else> 请尽快预约接种 </span>
              </span>
              <span class="info-item" v-if="item.status === 'pending'">
                <i class="el-icon-time"></i>
                <span>待预约</span>
              </span>
              <span
                class="info-item"
                v-else-if="item.status === '0' || item.status === '1'"
              >
                <i class="el-icon-date"></i>
                <span>已预约</span>
              </span>
            </div>
            <div class="reminder-progress" v-if="item.totalDoses">
              <el-progress
                :percentage="getProgressPercent(item)"
                :color="getProgressColor(item)"
                :stroke-width="8"
                :show-text="false"
              />
              <span class="progress-text">
                已完成 {{ item.completedDoses || 0 }} /
                {{ item.totalDoses }} 剂次
              </span>
            </div>
          </div>
          <div class="reminder-action">
            <el-button
              v-if="item.status === 'pending'"
              type="primary"
              size="small"
              @click="goToAppointment(item)"
              round
            >
              立即预约
            </el-button>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 无待接种提醒时的占位 -->
    <el-card class="pending-reminder-card empty-card" v-else shadow="hover">
      <div class="empty-reminder">
        <i class="el-icon-success"></i>
        <p>暂无待接种疫苗</p>
        <el-button type="text" @click="goToVaccineList">去预约疫苗</el-button>
      </div>
    </el-card>

    <!-- 标签页切换 -->
    <el-tabs v-model="activeTab" @tab-click="handleTabClick">
      <el-tab-pane label="全部预约" name="all"></el-tab-pane>
      <el-tab-pane label="待确认" name="0"></el-tab-pane>
      <el-tab-pane label="已确认" name="1"></el-tab-pane>
      <el-tab-pane label="已完成" name="2"></el-tab-pane>
      <el-tab-pane label="已取消" name="3"></el-tab-pane>
    </el-tabs>

    <!-- 预约列表 -->
    <el-table
      v-loading="loading"
      :data="appointmentList"
      class="appointment-table"
    >
      <el-table-column label="疫苗信息" width="260">
        <template slot-scope="scope">
          <div class="vaccine-cell">
            <span class="vaccine-name">{{ scope.row.vaccineName }}</span>
            <!--  疫苗类型标签 -->
            <el-tag
              v-if="scope.row.doseNumber == null"
              size="mini"
              type="warning"
              style="margin-left: 8px"
            >
              单剂次
            </el-tag>
            <el-tag v-else size="mini" type="success" style="margin-left: 8px">
              多剂次
            </el-tag>
            <!-- 剂次信息（多剂次时显示） -->
            <el-tag
              v-if="scope.row.doseNumber != null"
              size="mini"
              type="info"
              style="margin-left: 8px"
            >
              第{{ scope.row.doseNumber }}剂
            </el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="生产厂家" prop="manufacturer" width="150" />
      <el-table-column label="预约日期" prop="appointmentDate" width="120">
        <template slot-scope="scope">
          {{ formatDate(scope.row.appointmentDate) }}
        </template>
      </el-table-column>
      <el-table-column label="预约时段" prop="timeSlot" width="140">
        <template slot-scope="scope">
          {{ formatTimeSlot(scope.row.timeSlot) }}
        </template>
      </el-table-column>
      <el-table-column label="联系电话" prop="phone" width="120" />
      <el-table-column label="预约状态" prop="status" width="100">
        <template slot-scope="scope">
          <el-tag :type="getStatusTagType(scope.row.status)" size="small">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="接种状态" prop="isCompleted" width="100">
        <template slot-scope="scope">
          <el-tag
            v-if="scope.row.isCompleted === 1"
            type="success"
            size="small"
          >
            已完成接种
          </el-tag>
          <el-tag
            v-else-if="scope.row.status === '2'"
            type="success"
            size="small"
          >
            已完成
          </el-tag>
          <el-tag v-else-if="scope.row.status === '3'" type="info" size="small">
            已取消
          </el-tag>
          <el-tag v-else type="warning" size="small"> 待接种 </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="下一针日期" prop="nextDoseDate" width="120">
        <template slot-scope="scope">
          <span v-if="scope.row.nextDoseDate">{{
            formatDate(scope.row.nextDoseDate)
          }}</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="备注" prop="remark" show-overflow-tooltip />
      <el-table-column label="操作" align="center" width="180">
        <template slot-scope="scope">
          <!-- 查看详情 -->
          <el-button size="mini" type="text" @click="viewDetail(scope.row)">
            详情
          </el-button>
          <!-- 取消按钮：只有待确认或已确认状态可取消 -->
          <el-button
            v-if="scope.row.status === '0' || scope.row.status === '1'"
            size="mini"
            type="text"
            style="color: #f56c6c"
            @click="handleCancel(scope.row)"
          >
            取消
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
      style="margin-top: 8px"
    />

    <!-- 详情对话框 -->
    <el-dialog :title="detailTitle" :visible.sync="detailVisible" width="500px">
      <el-descriptions :column="1" border v-if="detailData">
        <el-descriptions-item label="疫苗名称">
          {{ detailData.vaccineName }}
          <el-tag
            v-if="detailData.doseNumber"
            size="mini"
            type="info"
            style="margin-left: 8px"
          >
            第{{ detailData.doseNumber }}剂
          </el-tag>
          <el-tag v-else size="mini" type="success" style="margin-left: 8px">
            单剂次
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="生产厂家">{{
          detailData.manufacturer || "-"
        }}</el-descriptions-item>
        <el-descriptions-item label="预约日期">{{
          formatDate(detailData.appointmentDate)
        }}</el-descriptions-item>
        <el-descriptions-item label="预约时段">{{
          formatTimeSlot(detailData.timeSlot)
        }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{
          detailData.phone
        }}</el-descriptions-item>
        <el-descriptions-item label="预约状态">
          <el-tag :type="getStatusTagType(detailData.status)" size="small">
            {{ getStatusText(detailData.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="接种状态">
          <el-tag
            v-if="detailData.isCompleted === 1"
            type="success"
            size="small"
            >已完成接种</el-tag
          >
          <el-tag
            v-else-if="detailData.status === '2'"
            type="success"
            size="small"
            >已完成</el-tag
          >
          <el-tag v-else-if="detailData.status === '3'" type="info" size="small"
            >已取消</el-tag
          >
          <el-tag v-else type="warning" size="small">待接种</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="下一针日期" v-if="detailData.nextDoseDate">
          {{ formatDate(detailData.nextDoseDate) }}
        </el-descriptions-item>
        <el-descriptions-item label="备注">{{
          detailData.remark || "无"
        }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{
          detailData.createTime
        }}</el-descriptions-item>
      </el-descriptions>
      <span slot="footer" class="dialog-footer">
        <el-button @click="detailVisible = false">关 闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  listMyAppointment,
  cancelAppointment,
  getMyPendingDoses,
} from "@/api/vaccine/appointment";
import { parseTime } from "@/utils/ruoyi";

export default {
  name: "MyAppointment",
  data() {
    return {
      loading: false,
      activeTab: "all",
      appointmentList: [],
      total: 0,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        status: undefined,
      },
      // 详情弹窗
      detailVisible: false,
      detailTitle: "",
      detailData: null,
      // 待接种提醒
      pendingDoses: [],
    };
  },
  created() {
    this.getList();
    this.getPendingDoses();
  },
  methods: {
    /** 获取我的预约列表 */
    getList() {
      this.loading = true;
      const params = { ...this.queryParams };
      if (this.activeTab === "all") {
        params.status = undefined;
      } else {
        params.status = this.activeTab;
      }

      listMyAppointment(params)
        .then((response) => {
          this.appointmentList = response.rows || [];
          this.total = response.total || 0;
          this.loading = false;
        })
        .catch(() => {
          this.loading = false;
        });
    },

    /** 获取待接种提醒 */
    getPendingDoses() {
      getMyPendingDoses()
        .then((response) => {
          this.pendingDoses = response.data || [];
        })
        .catch((error) => {
          console.error("获取待接种提醒失败:", error);
        });
    },

    /** 标签页切换 */
    handleTabClick() {
      this.queryParams.pageNum = 1;
      this.getList();
    },

    /** 查看详情 */
    viewDetail(row) {
      this.detailData = row;
      this.detailTitle = `${row.vaccineName} - 预约详情`;
      this.detailVisible = true;
    },

    /** 取消预约 */
    handleCancel(row) {
      this.$confirm(`确定要取消【${row.vaccineName}】的预约吗？`, "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      }).then(() => {
        cancelAppointment(row.id)
          .then(() => {
            this.$message.success("取消成功");
            this.getList();
            this.getPendingDoses(); // 刷新待接种提醒
          })
          .catch(() => {
            this.$message.error("取消失败");
          });
      });
    },

    /** 跳转到疫苗列表 */
    goToVaccineList() {
      this.$router.push("/vaccine/appointment/user");
    },

    /** 跳转到预约页面 */
    goToAppointment(item) {
      this.$router.push({
        path: "/vaccine/appointment/form",
        query: { vaccineId: item.vaccineId },
      });
    },

    /** 判断是否紧急（超过建议接种日期） */
    isUrgent(item) {
      if (!item.nextDoseDate) return false;
      const today = new Date();
      const nextDate = new Date(item.nextDoseDate);
      today.setHours(0, 0, 0, 0);
      nextDate.setHours(0, 0, 0, 0);
      return nextDate < today;
    },

    /** 获取提醒图标 */
    getReminderIcon(item) {
      if (item.status === "pending") return "el-icon-warning-outline";
      if (this.isUrgent(item)) return "el-icon-alarm-clock";
      return "el-icon-bell";
    },

    /** 获取剂次显示文本 */
    getDoseText(item) {
      if (item.doseNumber) {
        return `第${item.doseNumber}剂`;
      }
      return "待接种";
    },

    /** 获取进度百分比 */
    getProgressPercent(item) {
      if (!item.totalDoses || !item.completedDoses) return 0;
      return Math.round((item.completedDoses / item.totalDoses) * 100);
    },

    /** 获取进度条颜色 */
    getProgressColor(item) {
      const percent = this.getProgressPercent(item);
      if (percent >= 80) return "#67c23a";
      if (percent >= 50) return "#e6a23c";
      return "#409eff";
    },

    /** 格式化日期 */
    formatDate(dateStr) {
      if (!dateStr) return "";
      if (typeof dateStr === "string" && dateStr.includes("T")) {
        const date = new Date(dateStr);
        return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(
          2,
          "0"
        )}-${String(date.getDate()).padStart(2, "0")}`;
      }
      if (dateStr.length === 10) return dateStr;
      return dateStr;
    },

    /** 格式化时间段 */
    formatTimeSlot(slot) {
      const slotMap = {
        1: "上午 09:00-11:00",
        2: "下午 14:00-16:00",
        3: "晚上 18:00-20:00",
      };
      return slotMap[slot] || slot;
    },

    /** 获取状态文本 */
    getStatusText(status) {
      const statusMap = {
        0: "待确认",
        1: "已确认",
        2: "已完成",
        3: "已取消",
      };
      return statusMap[status] || "未知";
    },

    /** 获取状态标签类型 */
    getStatusTagType(status) {
      const typeMap = {
        0: "warning",
        1: "primary",
        2: "success",
        3: "info",
      };
      return typeMap[status] || "info";
    },
  },
};
</script>

<style scoped>
.app-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 84px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
}

/*  待接种提醒卡片样式 */
.pending-reminder-card {
  margin-bottom: 20px;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s;
}

.pending-reminder-card >>> .el-card__header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 15px 20px;
  border-bottom: none;
}

.reminder-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
  color: white;
}

.header-left i {
  font-size: 20px;
}

.header-left span {
  font-size: 16px;
  font-weight: 600;
}

.badge >>> .el-badge__content {
  background-color: #ff6b6b;
  color: white;
  border: none;
}

.header-btn {
  color: white !important;
  font-size: 13px;
}

.header-btn:hover {
  opacity: 0.8;
}

/* 提醒列表 */
.reminder-list {
  padding: 5px 0;
}

/* 单个提醒项 */
.reminder-item {
  display: flex;
  align-items: center;
  padding: 15px 20px;
  margin-bottom: 10px;
  border-radius: 10px;
  background-color: #f9fafc;
  transition: all 0.3s;
  cursor: pointer;
}

.reminder-item:hover {
  background-color: #f5f7fa;
  transform: translateX(5px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

/* 紧急提醒（超过建议日期） */
.reminder-urgent {
  background: linear-gradient(135deg, #fff5f5 0%, #fff 100%);
  border-left: 4px solid #f56c6c;
}

.reminder-normal {
  border-left: 4px solid #e6a23c;
}

/* 提醒图标 */
.reminder-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  flex-shrink: 0;
}

.reminder-urgent .reminder-icon {
  background-color: #fee;
  color: #f56c6c;
}

.reminder-normal .reminder-icon {
  background-color: #fef5e8;
  color: #e6a23c;
}

.reminder-icon i {
  font-size: 24px;
}

/* 提醒内容 */
.reminder-content {
  flex: 1;
}

.reminder-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
  flex-wrap: wrap;
}

.vaccine-name {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
}

/* 提醒信息 */
.reminder-info {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin-bottom: 10px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  color: #7f8c8d;
}

.info-item i {
  font-size: 14px;
  color: #409eff;
}

/* 进度条 */
.reminder-progress {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.reminder-progress .el-progress {
  flex: 1;
  max-width: 200px;
}

.progress-text {
  font-size: 12px;
  color: #7f8c8d;
}

/* 操作按钮 */
.reminder-action {
  margin-left: 15px;
  flex-shrink: 0;
}

.reminder-action .el-button {
  padding: 8px 18px;
  border-radius: 20px;
  font-weight: 500;
}

/* 空状态卡片 */
.empty-card {
  text-align: center;
  padding: 30px;
}

.empty-reminder i {
  font-size: 48px;
  color: #67c23a;
  margin-bottom: 15px;
}

.empty-reminder p {
  font-size: 14px;
  color: #7f8c8d;
  margin-bottom: 15px;
}

/* 预约表格样式 */
.appointment-table {
  margin-top: 20px;
  border-radius: 12px;
  overflow: hidden;
}

.vaccine-cell {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}
</style>
