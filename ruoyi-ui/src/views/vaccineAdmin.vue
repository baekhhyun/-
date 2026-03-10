<!-- ==================== 文件路径：ruoyi-ui/src/views/dashboard/admin.vue ==================== -->

<template>
  <div class="admin-dashboard">
    <!-- 欢迎语 -->
    <div class="welcome-section">
      <h1 class="welcome-title">👋 下午好，{{ userName }}</h1>
      <p class="welcome-subtitle">
        今天是 {{ currentDate }}，有
        <span class="highlight">{{ pendingCount }}</span> 个预约待处理
      </p>
    </div>

    <!-- 统计卡片行 (4列) -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6" v-for="(stat, index) in statsCards" :key="index">
        <el-card
          class="stat-card"
          :style="{ background: stat.color }"
          shadow="hover"
          :body-style="{ padding: '20px' }"
        >
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-label">{{ stat.label }}</div>
              <div class="stat-value">{{ stat.value }}</div>
              <div class="stat-trend" v-if="stat.trend !== 0">
                <i
                  :class="stat.trend > 0 ? 'el-icon-top' : 'el-icon-bottom'"
                ></i>
                {{ Math.abs(stat.trend) }}% 较昨日
              </div>
            </div>
            <div class="stat-icon">
              <i :class="stat.icon"></i>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表行 - 第一行 (16+8) -->
    <el-row :gutter="20" class="charts-row">
      <!-- 预约趋势图 - 占16列 -->
      <el-col :span="16">
        <el-card class="chart-card" shadow="hover">
          <div slot="header" class="chart-header">
            <span
              ><i class="el-icon-data-line" style="color: #409eff"></i>
              预约趋势</span
            >
            <el-radio-group
              v-model="trendType"
              size="small"
              @change="handleTrendChange"
            >
              <el-radio-button label="week">14天</el-radio-button>
              <el-radio-button label="month">30天</el-radio-button>
            </el-radio-group>
          </div>
          <div id="trendChart" style="height: 350px; width: 100%"></div>
        </el-card>
      </el-col>

      <!-- 预约状态分布 - 占8列 -->
      <el-col :span="8">
        <el-card class="chart-card" shadow="hover">
          <div slot="header" class="chart-header">
            <span
              ><i class="el-icon-pie-chart" style="color: #67c23a"></i>
              预约状态分布</span
            >
          </div>
          <div id="pieChart" style="height: 350px; width: 100%"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 第二行 - 三栏对齐 (8+8+8) -->
    <el-row :gutter="20" class="charts-row">
      <!-- 热门疫苗预约榜 - 占8列 -->
      <el-col :span="8">
        <el-card class="chart-card" shadow="hover">
          <div slot="header" class="chart-header">
            <span
              ><i class="el-icon-medal" style="color: #e6a23c"></i>
              热门疫苗预约榜</span
            >
          </div>
          <div id="barChart" style="height: 350px; width: 100%"></div>
        </el-card>
      </el-col>

      <!-- 待处理预约列表 - 占8列 (和热门疫苗榜对齐) -->
      <el-col :span="8">
        <el-card
          class="pending-card"
          shadow="hover"
          :body-style="{ padding: '0' }"
        >
          <div slot="header" class="pending-header">
            <span
              ><i class="el-icon-warning" style="color: #e6a23c"></i> 待处理预约
              ({{ pendingCount }})</span
            >
            <el-button type="text" @click="goToPendingList">
              查看全部 <i class="el-icon-arrow-right"></i>
            </el-button>
          </div>

          <!-- 待处理列表 - 紧凑显示 -->
          <div class="pending-list" v-loading="loading">
            <div v-if="pendingList.length === 0" class="empty-state">
              <i class="el-icon-check"></i>
              <p>暂无待处理预约</p>
            </div>

            <div
              v-for="(item, index) in pendingList"
              :key="index"
              class="pending-item"
            >
              <div class="item-header">
                <span class="item-user">{{ item.userName || "未知用户" }}</span>
                <el-tag size="mini" type="warning" effect="light"
                  >待确认</el-tag
                >
              </div>
              <div class="item-info">
                <div class="info-line">
                  <i class="el-icon-medicine"></i>
                  <span>{{ item.vaccineName || "未知疫苗" }}</span>
                </div>
                <div class="info-line">
                  <i class="el-icon-date"></i>
                  <span
                    >{{ item.appointmentDate }} {{ formatTimeSlot(item) }}</span
                  >
                </div>
                <div class="info-line">
                  <i class="el-icon-phone"></i>
                  <span>{{ item.phone }}</span>
                </div>
              </div>
              <div class="item-actions">
                <el-button
                  type="primary"
                  size="mini"
                  @click="handleConfirm(item)"
                  :loading="item.loading"
                  style="flex: 1; margin-right: 5px"
                >
                  确认
                </el-button>
                <el-button
                  type="danger"
                  size="mini"
                  @click="handleCancel(item)"
                  :loading="item.loading"
                  style="flex: 1"
                >
                  取消
                </el-button>
              </div>
            </div>
            <div v-if="pendingList.length > 3" class="more-tip">
              <i class="el-icon-more"></i> 还有
              {{ pendingList.length - 3 }} 条待处理，<el-link
                type="primary"
                @click="goToPendingList"
                >查看全部</el-link
              >
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 时间段预约分布 - 占8列 -->
      <el-col :span="8">
        <el-card class="chart-card" shadow="hover">
          <div slot="header" class="chart-header">
            <span
              ><i class="el-icon-time" style="color: #f56c6c"></i>
              时间段预约分布</span
            >
          </div>
          <div id="timeSlotChart" style="height: 350px; width: 100%"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import * as echarts from "echarts";
import {
  getDashboardStats,
  getTrendData,
  getVaccineRanking,
  getPendingList,
} from "@/api/vaccine/dashboard";
import { updateAppointment } from "@/api/vaccine/appointment";

export default {
  name: "AdminDashboard",
  data() {
    return {
      refreshTimer: null,
      refreshInterval: 60000, // 30秒刷新一次
      isRefreshing: false,
      resizeTimer: null,
      observer: null,
      trendData: null,
      pieData: null,
      barData: null,
      timeSlotData: null,
      userName: "管理员",
      currentDate: "",
      pendingCount: 0,
      pendingList: [],
      loading: false,
      trendType: "week",
      statsCards: [
        {
          label: "总预约数",
          value: 0,
          icon: "el-icon-s-order",
          color: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)",
          trend: 0,
        },
        {
          label: "今日预约",
          value: 0,
          icon: "el-icon-date",
          color: "linear-gradient(135deg, #f093fb 0%, #f5576c 100%)",
          trend: 0,
        },
        {
          label: "已完成",
          value: 0,
          icon: "el-icon-success",
          color: "linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)",
          trend: 0,
        },
        {
          label: "疫苗种类",
          value: 0,
          icon: "el-icon-shopping-bag-1",
          color: "linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)",
          trend: 0,
        },
      ],
      trendChart: null,
      pieChart: null,
      barChart: null,
      timeSlotChart: null,
    };
  },
  created() {
    this.initData();
    this.getUserName();
    this.formatCurrentDate();
    this.startAutoRefresh();
  },
  mounted() {
    this.$nextTick(() => {
      this.initCharts();
    });
    window.addEventListener("resize", this.handleResize);
    this.observeContainer();
  },
  beforeDestroy() {
    window.removeEventListener("resize", this.handleResize);
    this.disposeCharts();
  },
  methods: {
    startAutoRefresh() {
      if (this.refreshTimer) {
        clearInterval(this.refreshTimer);
      }

      this.refreshTimer = setInterval(() => {
        this.refreshDashboard();
      }, this.refreshInterval);
    },

    // 停止自动刷新
    stopAutoRefresh() {
      if (this.refreshTimer) {
        clearInterval(this.refreshTimer);
        this.refreshTimer = null;
        console.log("自动刷新已停止");
      }
    },

    // 刷新仪表盘（带加载动画）
    refreshDashboard() {
      // 避免重复刷新
      if (this.isRefreshing) return;
      this.isRefreshing = true;
      // 并行请求所有数据
      Promise.all([
        this.getStats(),
        this.getTrend(),
        this.getRanking(),
        this.getPendingList(),
      ])
        .catch((error) => {
          console.error("刷新失败:", error);
        })
        .finally(() => {
          this.isRefreshing = false;
        });
    },
    // ========== 初始化数据 ==========
    initData() {
      this.getStats();
      this.getTrend();
      this.getRanking();
      this.getPendingList();
    },

    getUserName() {
      this.userName = this.$store.getters.name || "管理员";
    },

    formatCurrentDate() {
      const date = new Date();
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, "0");
      const day = String(date.getDate()).padStart(2, "0");
      const weekdays = [
        "星期日",
        "星期一",
        "星期二",
        "星期三",
        "星期四",
        "星期五",
        "星期六",
      ];
      const weekday = weekdays[date.getDay()];
      this.currentDate = `${year}年${month}月${day}日 ${weekday}`;
    },

    // ========== 获取数据 ==========
    getStats() {
      getDashboardStats()
        .then((response) => {
          if (response.code === 200) {
            const data = response.data || {};
            this.statsCards[0].value = data.totalAppointments || 0;
            this.statsCards[1].value = data.todayAppointments || 0;
            this.statsCards[2].value = data.completedCount || 0;
            this.statsCards[3].value = data.vaccineCount || 0;
            this.pendingCount = data.pendingCount || 0;

            // 更新饼图数据
            this.updatePieChart(
              data.statusData || [
                { name: "待确认", value: 0 },
                { name: "已确认", value: 0 },
                { name: "已完成", value: 0 },
                { name: "已取消", value: 0 },
              ]
            );
          }
        })
        .catch((error) => {
          console.error("获取统计数据失败", error);
        });
    },

    getTrend() {
      getTrendData({ type: this.trendType })
        .then((response) => {
          if (response.code === 200) {
            this.updateTrendChart(response.data || { dates: [], counts: [] });
          }
        })
        .catch((error) => {
          console.error("获取趋势数据失败", error);
        });
    },

    getRanking() {
      getVaccineRanking()
        .then((response) => {
          if (response.code === 200) {
            const data = response.data || {};
            this.updateBarChart(data.vaccineRank || []);
            this.updateTimeSlotChart(data.timeSlotData || []);
          }
        })
        .catch((error) => {
          console.error("获取排行榜数据失败", error);
        });
    },

    getPendingList() {
      this.loading = true;
      getPendingList({ pageNum: 1, pageSize: 5 })
        .then((response) => {
          this.pendingList = response.rows || [];
          this.loading = false;
        })
        .catch(() => {
          this.loading = false;
        });
    },

    // ========== 图表初始化 ==========
    initCharts() {
      const trendElement = document.getElementById("trendChart");
      if (trendElement) this.trendChart = echarts.init(trendElement);

      const pieElement = document.getElementById("pieChart");
      if (pieElement) this.pieChart = echarts.init(pieElement);

      const barElement = document.getElementById("barChart");
      if (barElement) this.barChart = echarts.init(barElement);

      const timeSlotElement = document.getElementById("timeSlotChart");
      if (timeSlotElement) this.timeSlotChart = echarts.init(timeSlotElement);
    },

    // ========== 更新图表 ==========
    updateTrendChart(data) {
      if (!this.trendChart) return;
      const option = {
        tooltip: { trigger: "axis" },
        grid: { left: "3%", right: "4%", bottom: "3%", containLabel: true },
        xAxis: {
          type: "category",
          data: data.dates || [],
          axisLabel: { color: "#666" },
        },
        yAxis: {
          type: "value",
          name: "预约数量",
          splitLine: { lineStyle: { type: "dashed", color: "#eee" } },
        },
        series: [
          {
            name: "预约数",
            type: "line",
            data: data.counts || [],
            smooth: true,
            symbol: "circle",
            symbolSize: 8,
            lineStyle: { color: "#409EFF", width: 3 },
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: "rgba(64,158,255,0.3)" },
                { offset: 1, color: "rgba(64,158,255,0.1)" },
              ]),
            },
          },
        ],
      };
      this.trendChart.setOption(option);
    },

    updatePieChart(data) {
      if (!this.pieChart) return;
      const option = {
        tooltip: { trigger: "item", formatter: "{b}: {c} ({d}%)" },
        legend: { orient: "vertical", left: "left", top: "center" },
        series: [
          {
            type: "pie",
            radius: ["50%", "70%"],
            data: data,
            color: ["#E6A23C", "#409EFF", "#67C23A", "#F56C6C"],
          },
        ],
      };
      this.pieChart.setOption(option);
    },

    updateBarChart(data) {
      if (!this.barChart) return;
      const option = {
        tooltip: { trigger: "axis" },
        grid: { left: "3%", right: "4%", bottom: "3%", containLabel: true },
        xAxis: {
          type: "category",
          data: data.map((item) => item.name) || [],
          axisLabel: { rotate: 20 },
        },
        yAxis: { type: "value", name: "预约次数" },
        series: [
          {
            name: "预约数",
            type: "bar",
            data: data.map((item) => item.count) || [],
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: "#409EFF" },
                { offset: 1, color: "#36CFC9" },
              ]),
            },
            barWidth: 30,
          },
        ],
      };
      this.barChart.setOption(option);
    },

    updateTimeSlotChart(data) {
      if (!this.timeSlotChart) return;
      const option = {
        tooltip: { trigger: "item" },
        legend: { orient: "vertical", right: "left", top: "center" },
        series: [
          {
            type: "pie",
            radius: ["40%", "70%"],
            data: data.length
              ? data
              : [
                  { name: "上午", value: 0 },
                  { name: "下午", value: 0 },
                  { name: "晚上", value: 0 },
                ],
            label: { show: true, formatter: "{b}: {d}%" },
            color: ["#409EFF", "#67C23A", "#E6A23C"],
          },
        ],
      };
      this.timeSlotChart.setOption(option);
    },

    // ========== 格式化函数 ==========
    formatTimeSlot(row) {
      if (!row || !row.timeSlot) return "未选择";
      const map = {
        1: "上午 09:00-11:00",
        2: "下午 14:00-16:00",
        3: "晚上 18:00-20:00",
      };
      return map[row.timeSlot] || row.timeSlot;
    },

    // ========== 操作处理 ==========
    handleConfirm(row) {
      this.$confirm("确认该预约吗？", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      }).then(() => {
        this.$set(row, "loading", true);
        updateAppointment({ id: row.id, status: "1" })
          .then(() => {
            this.$message.success("已确认");
            this.getPendingList();
            this.getStats();
          })
          .finally(() => {
            this.$set(row, "loading", false);
          });
      });
    },

    handleCancel(row) {
      this.$confirm("取消该预约吗？", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      }).then(() => {
        this.$set(row, "loading", true);
        updateAppointment({ id: row.id, status: "3" })
          .then(() => {
            this.$message.success("已取消");
            this.getPendingList();
            this.getStats();
          })
          .finally(() => {
            this.$set(row, "loading", false);
          });
      });
    },

    handleTrendChange() {
      this.getTrend();
    },

    goToPendingList() {
      this.$router.push({
        name: "VaccineAppointments",
      });
    },

    // ========== 窗口大小变化处理 ==========
    handleResize() {
      if (this.resizeTimer) {
        clearTimeout(this.resizeTimer);
      }

      this.resizeTimer = setTimeout(() => {
        // 强制重新计算图表大小
        this.trendChart?.resize({
          width: "auto",
          height: "auto",
        });
        this.pieChart?.resize({
          width: "auto",
          height: "auto",
        });
        this.barChart?.resize({
          width: "auto",
          height: "auto",
        });
        this.timeSlotChart?.resize({
          width: "auto",
          height: "auto",
        });

        console.log("图表已自适应调整");
      }, 100);
    },

    observeContainer() {
      if (window.ResizeObserver) {
        this.observer = new ResizeObserver(() => {
          window.requestAnimationFrame(() => {
            this.handleResize();
          });
        });

        // 监听所有图表容器
        const containers = [
          "trendChart",
          "pieChart",
          "barChart",
          "timeSlotChart",
        ];
        containers.forEach((id) => {
          const element = document.getElementById(id);
          if (element && element.parentElement) {
            this.observer.observe(element.parentElement);
          }
        });
      }
    },

    disposeCharts() {
      this.trendChart?.dispose();
      this.pieChart?.dispose();
      this.barChart?.dispose();
      this.timeSlotChart?.dispose();
    },
  },
};
</script>

<style scoped>
/* ========== 整体布局 ========== */
.admin-dashboard {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 84px);
}

/* ========== 欢迎语 ========== */
.welcome-section {
  margin-bottom: 30px;
}

.welcome-title {
  font-size: 28px;
  font-weight: 500;
  color: #2c3e50;
  margin: 0 0 10px;
}

.welcome-subtitle {
  font-size: 16px;
  color: #7f8c8d;
  margin: 0;
}

.highlight {
  color: #f56c6c;
  font-weight: bold;
  font-size: 18px;
}

/* ========== 统计卡片 ========== */
.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  border: none;
  border-radius: 12px;
  color: white;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.2) !important;
}

.stat-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 5px;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  margin-bottom: 5px;
}

.stat-trend {
  font-size: 12px;
  opacity: 0.9;
}

.stat-icon i {
  font-size: 48px;
  opacity: 0.8;
}

/* ========== 图表卡片 ========== */
.charts-row {
  margin-bottom: 20px;
}

.chart-card {
  border-radius: 12px;
}

.chart-card >>> .el-card__header {
  padding: 15px 20px !important;
  border-bottom: 1px solid #f0f0f0;
  height: 51px !important; /* 固定高度 */
  box-sizing: border-box !important;
  line-height: 21px !important; /* 与内边距配合 */
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
  width: 100%;
}

.chart-header span {
  font-size: 16px;
  font-weight: 500;
  color: #2c3e50;
  line-height: 1;
}

/* ========== 待处理列表卡片 ========== */
.pending-card {
  border-radius: 12px;
}

.pending-card >>> .el-card__header {
  padding: 15px 20px !important;
  border-bottom: 1px solid #f0f0f0;
  height: 51px !important; /* 固定高度，与图表卡片一致 */
  box-sizing: border-box !important;
  line-height: 21px !important;
  background-color: #f8fafc;
}

.pending-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
  width: 100%;
}

.pending-header span {
  font-size: 16px;
  font-weight: 500;
  color: #2c3e50;
  line-height: 1;
}

/* 卡片内容无内边距 */
.pending-card >>> .el-card__body {
  padding: 0 !important;
}

/* 待处理列表容器 */
.pending-list {
  height: 385px;
  overflow-y: auto; /* 超出滚动 */
  padding: 10px;
}

/* 单个待处理项 */
.pending-item {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 10px;
  transition: all 0.3s;
  background-color: #fff;
}

.pending-item:last-child {
  margin-bottom: 0;
}

.pending-item:hover {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  border-color: #409eff;
}

/* 项头部 */
.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.item-user {
  font-weight: 600;
  color: #2c3e50;
  font-size: 14px;
}

/* 信息行 */
.info-line {
  display: flex;
  align-items: center;
  margin-bottom: 5px;
  font-size: 13px;
  color: #606266;
}

.info-line i {
  width: 20px;
  color: #409eff;
  font-size: 14px;
}

/* 操作按钮区 */
.item-actions {
  display: flex;
  margin-top: 10px;
  padding-top: 8px;
  border-top: 1px dashed #e4e7ed;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 0;
  color: #909399;
}

.empty-state i {
  font-size: 48px;
  color: #67c23a;
  margin-bottom: 10px;
}

.empty-state p {
  margin: 0;
  font-size: 14px;
}

/* 滚动条美化 */
.pending-list::-webkit-scrollbar {
  width: 6px;
}

.pending-list::-webkit-scrollbar-thumb {
  background: #ddd;
  border-radius: 3px;
}

.pending-list::-webkit-scrollbar-track {
  background: #f5f7fa;
}
</style>
